/**
 *
 */
package muon.app.ssh;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.ui.components.session.SessionInfo;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * @author subhro
 */
@Slf4j
public class RemoteSessionInstance {
    private final SshClient2 ssh;
    @Getter
    private final SshFileSystem sshFs;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public RemoteSessionInstance(SessionInfo info, InputBlocker inputBlocker,
                                 CachedCredentialProvider cachedCredentialProvider) {
        this.ssh = new SshClient2(info, inputBlocker, cachedCredentialProvider);
        this.sshFs = new SshFileSystem(this.ssh);
    }

    public int exec(String command, Function<Command, Integer> callback, boolean pty) throws Exception {
        synchronized (this.ssh) {
            if (this.closed.get()) {
                throw new OperationCancelledException();
            }
            try {
                if (!ssh.isConnected()) {
                    ssh.connect();
                }
                try (Session session = ssh.openSession()) {
                    session.setAutoExpand(true);
                    if (pty) {
                        session.allocatePTY("vt100", 80, 24, 0, 0, Collections.emptyMap());
                    }
                    try (final Command cmd = session.exec(command)) {
                        return callback.apply(cmd);
                    }
                }

            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return 1;
        }
    }

    public int exec(String command, AtomicBoolean stopFlag) throws Exception {
        return exec(command, stopFlag, null, null);
    }

    public int exec(String command, AtomicBoolean stopFlag, StringBuilder output) throws Exception {
        return exec(command, stopFlag, output, null);
    }

    public int exec(String command, AtomicBoolean stopFlag, StringBuilder output, StringBuilder error)
            throws Exception {
        ByteArrayOutputStream bout = output == null ? null : new ByteArrayOutputStream();
        ByteArrayOutputStream berr = error == null ? null : new ByteArrayOutputStream();
        int ret = execBin(command, stopFlag, bout, berr);
        if (output != null) {
            output.append(bout.toString(StandardCharsets.UTF_8));
        }
        if (error != null) {
            error.append(berr.toString(StandardCharsets.UTF_8));
        }
        return ret;
    }

    public int execBin(String command, AtomicBoolean stopFlag, OutputStream bout, OutputStream berr) throws Exception {
        synchronized (this.ssh) {
            if (this.closed.get()) {
                throw new OperationCancelledException();
            }
            log.debug(Thread.currentThread().getName());
            log.debug(command);
            if (stopFlag.get()) {
                return -1;
            }
            try {
                if (!ssh.isConnected()) {
                    ssh.connect();
                }
                try (Session session = ssh.openSession()) {
                    session.setAutoExpand(true);
                    try (final Command cmd = session.exec(command)) {
                        log.debug("Command and Session started");

                        InputStream in = cmd.getInputStream();
                        InputStream err = cmd.getErrorStream();

                        byte[] b = new byte[8192];

                        do {
                            if (stopFlag.get()) {
                                log.debug("stopflag");
                                break;
                            }

                            if (in.available() > 0) {
                                int m = in.available();
                                while (m > 0) {
                                    int x = in.read(b, 0, Math.min(m, b.length));
                                    if (x == -1) {
                                        break;
                                    }
                                    m -= x;
                                    if (bout != null) {
                                        bout.write(b, 0, x);
                                    }

                                }
                            }

                            if (err.available() > 0) {
                                int m = err.available();
                                while (m > 0) {
                                    int x = err.read(b, 0, Math.min(m, b.length));
                                    if (x == -1) {
                                        break;
                                    }
                                    m -= x;
                                    if (berr != null) {
                                        berr.write(b, 0, x);
                                    }

                                }
                            }

                        }
                        while (cmd.isOpen());

                        log.debug("{} {} {}", cmd.isOpen(), cmd.isEOF(), cmd.getExitStatus());

                        log.debug("Command and Session closed");

                        return cmd.getExitStatus();
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return 1;
        }
    }

    /**
     *
     */
    public void close() {
        try {
            this.closed.set(true);
            try {
                this.sshFs.close();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            this.ssh.close();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public boolean isSessionClosed() {
        return closed.get();
    }

}
