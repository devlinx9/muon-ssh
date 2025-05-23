
package muon.app.ssh;

import javax.net.SocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * @author subhro
 */
public class CustomSocketFactory extends SocketFactory {

    private final String proxyHost;
    private final String proxyUser;
    private final String proxyPass;
    private final int proxyPort;
    private final Proxy.Type proxyType;

    /**
     */
    public CustomSocketFactory(String proxyHost, int proxyPort,
                               String proxyUser, String proxyPass, Proxy.Type proxyType) {
        super();
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;
        this.proxyType = proxyType;
    }

    public static int getResponseCode(String statusLine) {
        String[] arr = statusLine.split(" ");
        if (arr.length < 2) {
            return 400;
        }
        return Integer.parseInt(arr[1]);
    }

    @Override
    public Socket createSocket(String host, int port)
            throws IOException {
        return this.createSocket(InetAddress.getByName(host), port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost,
                               int localPort) throws IOException {
        return this.createSocket(InetAddress.getByName(host), port, localHost,
                                 localPort);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.createSocket(host, port, null, 0);
    }

    @Override
    public Socket createSocket(InetAddress address, int port,
                               InetAddress localAddress, int localPort) throws IOException {
        Socket socket = this.createSocket();
        if (localAddress != null) {
            socket.bind(new InetSocketAddress(localAddress, localPort));
        }
        if (address != null) {
            socket.connect(new InetSocketAddress(address, port));
        }

        if (this.proxyType == Proxy.Type.HTTP && proxyUser != null) {
            connectToProxy(socket);
        }

        return socket;
    }

    @Override
    public Socket createSocket() throws IOException {
        Proxy proxy = Proxy.NO_PROXY;
        if (this.proxyType == Proxy.Type.SOCKS) {
            proxy = new Proxy(Proxy.Type.SOCKS,
                              new InetSocketAddress(proxyHost, proxyPort));
        } else if (this.proxyType == Proxy.Type.HTTP && (proxyUser == null || proxyUser.isEmpty())) {
            proxy = new Proxy(Proxy.Type.HTTP,
                              new InetSocketAddress(proxyHost, proxyPort));
        }


        return new Socket(proxy);
    }

    private void connectToProxy(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        OutputStream out = socket.getOutputStream();
        StringBuilder requestHeaders = new StringBuilder();
        requestHeaders.append("HTTP ").append(proxyHost).append(":").append(proxyPort).append(" HTTP/1.1\r\n").append("Host: ").append(proxyHost).append(":").append(proxyPort).append("\r\n");
        String proxyAuth = getBasicAuthStr();
        if (proxyAuth != null) {
            requestHeaders.append("Proxy-Authorization: basic ").append(proxyAuth).append("\r\n");
        }
        requestHeaders.append("\r\n");
        out.write(requestHeaders.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();

        String statusLine = readLine(in);

        int responseCode = getResponseCode(statusLine);
        if (responseCode < 200 || responseCode >= 300) {
            socket.close();
            throw new IOException("Invalid response code: " + responseCode);
        }

        while (true) {
            String line = readLine(in);
            if (line.isEmpty()) {
                break;
            }
        }
    }

    private String getBasicAuthStr() {
        if (proxyUser != null && !proxyUser.isEmpty()) {
            return (Base64.getEncoder().encodeToString(
                    (proxyUser + ":" + (proxyPass == null ? "" : proxyPass))
                            .getBytes(StandardCharsets.UTF_8)));
        }
        return null;
    }

    public final String readLine(InputStream in) throws IOException {
        StringBuilder buf = new StringBuilder();
        while (true) {
            int x = in.read();
            if (x == -1) {
                throw new IOException(
                        "Unexpected EOF while reading header line");
            }
            if (x == '\n') {
                return buf.toString();
            }
            if (x != '\r') {
                buf.append((char) x);
            }
        }
    }

}
