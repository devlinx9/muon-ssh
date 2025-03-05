package muon.app.ssh;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.ui.components.session.HopEntry;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionInfo;
import muon.app.util.OptionPaneUtils;
import muon.app.util.SshUtil;
import muon.app.util.enums.JumpType;
import net.schmizz.keepalive.KeepAliveProvider;
import net.schmizz.sshj.DefaultConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.DirectConnection;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.connection.channel.direct.Parameters;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.forwarded.RemotePortForwarder;
import net.schmizz.sshj.connection.channel.forwarded.SocketForwardingConnectListener;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.Transport;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.method.AuthKeyboardInteractive;
import net.schmizz.sshj.userauth.method.AuthNone;

import javax.swing.*;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static muon.app.util.PlatformUtils.IS_LINUX;
import static muon.app.util.PlatformUtils.IS_MAC;

/**
 * @author subhro
 */
@Slf4j
public class SshClient2 implements Closeable {
    private static final int CONNECTION_TIMEOUT = App.getGlobalSettings().getConnectionTimeout() * 1000;
    public static final String LOCALHOST = "127.0.0.1";
    private final AtomicBoolean closed = new AtomicBoolean(false);

    @Getter
    private final SessionInfo info;
    private final PasswordFinderDialog passwordFinder;
    private final CachedCredentialProvider cachedCredentialProvider;
    private SSHClient sshj;
    private SshClient2 previousHop;
    private ServerSocket ss;
    private final SessionContentPanel sessionContentPanel;


    public SshClient2(SessionInfo info,
                      CachedCredentialProvider cachedCredentialProvider,
                      SessionContentPanel sessionContentPanel) {
        this.info = info;
        this.cachedCredentialProvider = cachedCredentialProvider;
        passwordFinder = new PasswordFinderDialog(cachedCredentialProvider);
        this.sessionContentPanel = sessionContentPanel;

    }

    private void setupProxyAndSocketFactory() {
        String proxyHost = info.getProxyHost();
        int proxyType = info.getProxyType();
        String proxyUser = info.getProxyUser();
        String proxyPass = info.getProxyPassword();
        int proxyPort = info.getProxyPort();

        Proxy.Type proxyType1 = Proxy.Type.DIRECT;

        if (proxyType == 1) {
            proxyType1 = Proxy.Type.HTTP;
        } else if (proxyType > 1) {
            proxyType1 = Proxy.Type.SOCKS;
        }

        sshj.setSocketFactory(new CustomSocketFactory(proxyHost, proxyPort, proxyUser, proxyPass, proxyType1));
    }

    private void getAuthMethods(AtomicBoolean authenticated, List<String> allowedMethods)
            throws OperationCancelledException {
        log.info("Trying to get allowed authentication methods...");
        try {
            String user = promptUser();
            if (user == null || user.isEmpty()) {
                throw new OperationCancelledException();
            }
            sshj.auth(user, new AuthNone());
            authenticated.set(true); // Surprise! no authentication!!!
        } catch (OperationCancelledException e) {
            throw e;
        } catch (Exception e) {
            allowedMethods.addAll(sshj.getUserAuth().getAllowedMethods());
            log.info("List of allowed authentications: {}", allowedMethods);
        }
    }

    private void authPublicKey() throws Exception {
        KeyProvider provider = null;
        if (info.getPrivateKeyFile() != null && !info.getPrivateKeyFile().isEmpty()) {
            File keyFile = new File(info.getPrivateKeyFile());
            if (keyFile.exists()) {
                provider = sshj.loadKeys(info.getPrivateKeyFile(), passwordFinder);
                log.info("Key provider: {}", provider);
                log.info("Key type: {}", provider.getType());
            }
        }

        if (closed.get()) {
            disconnect();
            throw new OperationCancelledException();
        }

        if (provider == null) {
            throw new Exception("No suitable key providers");
        }

        sshj.authPublickey(promptUser(), provider);
    }

    private void authPassoword() throws Exception {
        String user = getUser();
        char[] password = getPassword();
        if (user == null || user.isEmpty()) {
            password = null;
        }
        // keep on trying with password
        while (!closed.get()) {
            if (password == null || password.length < 1) {
                JTextField txtUser = new SkinnedTextField(30);
                JPasswordField txtPassword = new JPasswordField(30);
                JCheckBox chkUseCache = new JCheckBox(App.getContext().getBundle().getString("remember_session"));
                txtUser.setText(user);
                int ret = OptionPaneUtils.showOptionDialog(App.getAppWindow(),
                                                           new Object[]{"User", txtUser, "Password", txtPassword, chkUseCache}, App.getContext().getBundle().getString("authentication"));

                if (ret != JOptionPane.OK_OPTION) {
                    throw new OperationCancelledException();
                }

                user = txtUser.getText();
                password = txtPassword.getPassword();
                if (chkUseCache.isSelected()) {
                    cachedCredentialProvider.setCachedUser(user);
                    cachedCredentialProvider.cachePassword(new String(password));
                }
            }
            try {
                sshj.authPassword(user, password); // provide
                // password
                // updater
                // PasswordUpdateProvider
                // net.schmizz.sshj.userauth.password.PasswordUpdateProvider
                return;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                password = null;
            }
        }
    }

    public void connect() throws IOException, OperationCancelledException {
        Deque<HopEntry> hopStack = new ArrayDeque<>(this.info.getJumpHosts());
        this.connect(hopStack);
    }

    private void connect(Deque<HopEntry> hopStack) throws IOException, OperationCancelledException {
        this.sessionContentPanel.disableUi();
        try {
            initializeSSHClient();
            if (hopStack.isEmpty()) {
                this.setupProxyAndSocketFactory();
                this.sshj.addHostKeyVerifier(App.getContext().getHostKeyVerifier());
                sshj.connect(info.getHost(), info.getPort());
            } else {
                createTunnel(hopStack);
            }

            sshj.getConnection().getKeepAlive().setKeepAliveInterval(5);
            setX11Forwarding();

            if (closed.get()) {
                disconnect();
                throw new OperationCancelledException();
            }

            // Connection established, now find out supported authentication
            // methods
            AtomicBoolean authenticated = new AtomicBoolean(false);
            List<String> allowedMethods = new ArrayList<>();

            this.getAuthMethods(authenticated, allowedMethods);

            if (authenticated.get()) {
                return;
            }

            if (closed.get()) {
                disconnect();
                throw new OperationCancelledException();
            }

            // loop over servers preferred authentication methods in the same
            // order sent by server
            for (String authMethod : allowedMethods) {
                if (closed.get()) {
                    disconnect();
                    throw new OperationCancelledException();
                }

                log.info("Trying auth method: {}", authMethod);

                switch (authMethod) {
                    case "publickey":
                        try {
                            this.authPublicKey();
                            authenticated.set(true);
                        } catch (OperationCancelledException e) {
                            disconnect();
                            throw e;
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        break;

                    case "keyboard-interactive":
                        try {
                            sshj.auth(promptUser(), new AuthKeyboardInteractive(new InteractiveResponseProvider()));
                            authenticated.set(true);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        break;

                    case "password":
                        try {
                            this.authPassoword();
                            authenticated.set(true);
                        } catch (OperationCancelledException e) {
                            disconnect();
                            throw e;
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                        break;
                }

                if (authenticated.get()) {
                    return;
                }
            }

            throw new IOException("Authentication failed");

        } catch (Exception e) {
            if (this.sshj != null) {
                this.sshj.close();
            }
            throw e;
        } finally {
            this.sessionContentPanel.enableUi();
        }
    }

    private void setX11Forwarding() throws IOException {
        if (info.isUseX11Forwarding()) {
            if (IS_LINUX) {
                sshj.registerX11Forwarder(new MuonSocketForwardingConnectListener(
                        SshUtil.socketAddress("/tmp/.X11-unix/X0")
                ));
            } else if (IS_MAC) {
                sshj.registerX11Forwarder(new MuonSocketForwardingConnectListener(
                        SshUtil.socketAddress("/private/tmp/com.apple.launchd.ezzemjFmFP/org.xquartz:0")
                ));
            } else {
                sshj.registerX11Forwarder(new SocketForwardingConnectListener(
                        new InetSocketAddress("localhost", 6000)
                ));
            }
        }
    }

    private void initializeSSHClient() {
        DefaultConfig defaultConfig = new DefaultConfig();
        if (App.getGlobalSettings().isShowMessagePrompt()) {
            log.info("enabled KeepAliveProvider");
            defaultConfig.setKeepAliveProvider(KeepAliveProvider.KEEP_ALIVE);
        }
        sshj = new SSHClient(defaultConfig);
        sshj.setConnectTimeout(CONNECTION_TIMEOUT);
        sshj.setTimeout(CONNECTION_TIMEOUT);
    }

    private void createTunnel(Deque<HopEntry> hopStack) throws IOException {
        try {
            log.info("Tunneling through...");
            tunnelThrough(hopStack);
            log.info("adding host key verifier");
            this.sshj.addHostKeyVerifier(App.getContext().getHostKeyVerifier());
            log.info("Host key verifier added");
            if (this.info.getJumpType() == JumpType.TCP_FORWARDING) {
                log.info("tcp forwarding...");
                this.connectViaTcpForwarding();
            } else {
                log.info("port forwarding...");
                this.connectViaPortForwarding();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            disconnect();
            throw new IOException(e);
        }
    }

    private boolean isPasswordSet() {
        return this.info.getPassword() != null && !this.info.getPassword().isEmpty();
    }

    private String getUser() {
        String user = cachedCredentialProvider.getCachedUser();
        if (user == null || user.isEmpty()) {
            user = this.info.getUser();
        }
        return user;
    }

    private char[] getPassword() {
        char[] password = cachedCredentialProvider.getCachedPassword() == null ? null : cachedCredentialProvider.getCachedPassword().toCharArray();
        if (password == null && isPasswordSet()) {
            password = this.info.getPassword().toCharArray();
        }
        return password;
    }

    private String promptUser() {
        String user = getUser();
        if (user == null || user.isEmpty()) {
            JTextField txtUser = new SkinnedTextField(30);
            JCheckBox chkCacheUser = new JCheckBox(App.getContext().getBundle().getString("remember_username"));
            int ret = OptionPaneUtils.showOptionDialog(null, new Object[]{App.getContext().getBundle().getString("username"), txtUser, chkCacheUser}, App.getContext().getBundle().getString("user"));
            if (ret == JOptionPane.OK_OPTION) {
                user = txtUser.getText();
                if (chkCacheUser.isSelected()) {
                    cachedCredentialProvider.setCachedUser(user);
                }
            }
        }
        return user;
    }

    public Session openSession() throws Exception {
        if (closed.get()) {
            disconnect();
            throw new IOException("Closed by user");
        }
        Session session = sshj.startSession();

        if (info.isUseX11Forwarding()) {
            // Request X11 forwarding
            session.reqX11Forwarding("MIT-MAGIC-COOKIE-1", generateSecureRandomCookie(), 0);
        }

        if (closed.get()) {
            disconnect();
            throw new IOException("Closed by user");
        }
        return session;
    }

    public boolean isConnected() {
        return sshj != null && sshj.isConnected();
    }

    @Override
    public void close() throws IOException {
        try {
            log.info("Wrapper closing for: {}", info);
            disconnect();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public void disconnect() {
        if (closed.get()) {
            log.info("Already closed: {}", info);
            return;
        }
        closed.set(true);
        try {
            if (sshj != null) {
                sshj.disconnect();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            if (previousHop != null) {
                previousHop.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        try {
            if (this.ss != null) {
                this.ss.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return info.getName();
    }

    public SessionInfo getSource() {
        return info;
    }

    public SSHClient getSession() {
        return sshj;
    }

    public SFTPClient createSftpClient() throws IOException {
        return sshj.newSFTPClient();
    }

    // recursively
    private void tunnelThrough(Deque<HopEntry> hopStack) throws Exception {
        HopEntry ent = hopStack.poll();
        SessionInfo hopInfo = new SessionInfo();
        hopInfo.setHost(Objects.requireNonNull(ent).getHost());
        hopInfo.setPort(ent.getPort());
        hopInfo.setUser(ent.getUser());
        hopInfo.setPassword(ent.getPassword());
        hopInfo.setPrivateKeyFile(ent.getKeypath());
        previousHop = new SshClient2(hopInfo, cachedCredentialProvider, sessionContentPanel);
        previousHop.connect(hopStack);
    }

    private DirectConnection newDirectConnection(String host, int port) throws Exception {
        return sshj.newDirectConnection(host, port);
    }

    private void connectViaTcpForwarding() throws Exception {
        this.sshj.connectVia(this.previousHop.newDirectConnection(info.getHost(), info.getPort()), info.getHost(),
                             info.getPort());
    }

    private void connectViaPortForwarding() throws Exception {
        ss = new ServerSocket();
        ss.setReuseAddress(true);
        ss.bind(new InetSocketAddress(LOCALHOST, 0));
        int port = ss.getLocalPort();
        new Thread(() -> {
            try {
                this.previousHop
                        .newLocalPortForwarder(
                                new Parameters(LOCALHOST, port, this.info.getHost(), this.info.getPort()), ss)
                        .listen();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }).start();
        while (!ss.isBound()) {
            Thread.sleep(100);
        }
        this.sshj.connect(LOCALHOST, port);
    }

    public LocalPortForwarder newLocalPortForwarder(Parameters parameters, ServerSocket serverSocket) {
        return this.sshj.newLocalPortForwarder(parameters, serverSocket);
    }


    public RemotePortForwarder getRemotePortForwarder() {
        this.sshj.getConnection().getKeepAlive().setKeepAliveInterval(30);
        return this.sshj.getRemotePortForwarder();
    }

    public Transport getTransport() {
        return this.sshj.getTransport();
    }

    private String generateSecureRandomCookie() {
        SecureRandom random = new SecureRandom();
        StringBuilder cookie = new StringBuilder(32);
        for (int i = 0; i < 32; i++) {
            int n = random.nextInt(16);
            if (n < 10) {
                cookie.append(n);
            } else {
                cookie.append((char) ('a' + (n - 10)));
            }
        }
        return cookie.toString();
    }

}
