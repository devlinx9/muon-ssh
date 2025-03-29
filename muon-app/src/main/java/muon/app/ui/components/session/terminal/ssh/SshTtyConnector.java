package muon.app.ui.components.session.terminal.ssh;

import com.jediterm.terminal.Questioner;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ssh.SSHHandler;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionInfo;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.connection.channel.direct.SessionChannel;
import net.schmizz.sshj.transport.TransportException;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SshTtyConnector implements DisposableTtyConnector {
    private InputStreamReader myInputStreamReader;
    private OutputStream myOutputStream = null;
    private SessionChannel shell;
    private Session channel;
    private final AtomicBoolean isInitiated = new AtomicBoolean(false);
    private final AtomicBoolean isCancelled = new AtomicBoolean(false);
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);
    private Dimension myPendingTermSize;
    private Dimension myPendingPixelSize;
    private SSHHandler wr;
    private final String initialCommand;
    private final SessionInfo info;
    private final SessionContentPanel sessionContentPanel;

    public SshTtyConnector(SessionInfo info, String initialCommand, SessionContentPanel sessionContentPanel) {
        this.initialCommand = initialCommand;
        this.info = info;
        this.sessionContentPanel = sessionContentPanel;
    }

    @Override
    public boolean init(Questioner q) {
        try {
            this.wr = new SSHHandler(this.info, sessionContentPanel, sessionContentPanel);
            this.wr.connect();
            this.channel = wr.openSession();
            this.channel.setAutoExpand(true);

            this.channel.allocatePTY(App.getGlobalSettings().getTerminalType(), App.getGlobalSettings().getTermWidth(),
                                     App.getGlobalSettings().getTermHeight(), 0, 0, Collections.emptyMap());

            setEnvVar();


            this.shell = (SessionChannel) this.channel.startShell();

            InputStream myInputStream = shell.getInputStream();
            myOutputStream = shell.getOutputStream();
            myInputStreamReader = new InputStreamReader(myInputStream, StandardCharsets.UTF_8);

            resizeImmediately();
            log.debug("Initiated");

            if (initialCommand != null) {
                myOutputStream.write((initialCommand + "\n").getBytes(StandardCharsets.UTF_8));
                myOutputStream.flush();
            }

            isInitiated.set(true);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            isInitiated.set(false);
            isCancelled.set(true);
            return false;
        }
    }

    private void setEnvVar() {
        try {
            this.channel.setEnvVar("LANG", "en_US.UTF-8");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("Cannot set environment variable Lang: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            stopFlag.set(true);
            log.info("Terminal wrapper disconnecting");
            wr.disconnect();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    public void resize(Dimension termSize, Dimension pixelSize) {
        myPendingTermSize = termSize;
        myPendingPixelSize = pixelSize;
        if (channel != null) {
            resizeImmediately();
        }

    }

    @Override
    public String getName() {
        return "Remote";
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        return myInputStreamReader.read(buf, offset, length);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        myOutputStream.write(bytes);
        myOutputStream.flush();
    }

    @Override
    public boolean isConnected() {
        return channel != null && channel.isOpen() && isInitiated.get();
    }

    @Override
    public void write(String string) throws IOException {
        write(string.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public int waitFor() throws InterruptedException {
        log.info("Start waiting...");
        while (!isInitiated.get() || isRunning()) {
            log.info("waiting");
            Thread.sleep(100); // TODO: remove busy wait
        }
        log.info("waiting exit");
        try {
            shell.join();
        } catch (ConnectionException e) {

            log.error(e.getMessage(), e);
        }
        return shell.getExitStatus();
    }

    @Override
    public boolean isRunning() {
        return shell != null && shell.isOpen();
    }

    @Override
    public boolean isBusy() {
        return channel.isOpen();
    }

    @Override
    public boolean isCancelled() {
        return isCancelled.get();
    }

    @Override
    public void stop() {
        stopFlag.set(true);
        close();
    }

    @Override
    public int getExitStatus() {
        if (shell != null) {
            Integer exit = shell.getExitStatus();
            return exit == null ? -1 : exit;
        }
        return -2;
    }

    private void resizeImmediately() {
        if (myPendingTermSize != null && myPendingPixelSize != null) {
            setPtySize(shell, myPendingTermSize.width, myPendingTermSize.height, myPendingPixelSize.width,
                       myPendingPixelSize.height);
            myPendingTermSize = null;
            myPendingPixelSize = null;
        }
    }

    private void setPtySize(Shell shell, int col, int row, int wp, int hp) {
        log.debug("Exec pty resized:- col: {} row: {} wp: {} hp: {}", col, row, wp, hp);
        if (shell != null) {
            try {
                shell.changeWindowDimensions(col, row, wp, hp);
            } catch (TransportException e) {

                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean isInitialized() {
        return isInitiated.get();
    }

}