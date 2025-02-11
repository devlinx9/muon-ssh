/**
 *
 */
package muon.app.ssh;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.sftp.RemoteFile;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author subhro
 *
 */
@Slf4j
public class SSHRemoteFileOutputStream extends OutputStream {
    @Setter
    @Getter
    private int bufferCapacity;
    private final RemoteFile remoteFile;
    private final OutputStream remoteFileOutputStream;
    /**
     * @param remoteFile
     */
    public SSHRemoteFileOutputStream(RemoteFile remoteFile, int remoteMaxPacketSize) {
        this.remoteFile = remoteFile;
        this.bufferCapacity = remoteMaxPacketSize - this.remoteFile.getOutgoingPacketOverhead();
        this.remoteFileOutputStream = this.remoteFile.new RemoteFileOutputStream(0, 16);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.remoteFileOutputStream.write(b, off, len);
    }

    @Override
    public void write(int b) throws IOException {
        this.remoteFileOutputStream.write(b);
    }

    @Override
    public void close() throws IOException {
        log.info(this.getClass().getName() + " closing");
        try {
            this.remoteFile.close();
        } catch (Exception e) {
        }
        try {
            this.remoteFileOutputStream.close();
        } catch (Exception e) {
        }
    }

    @Override
    public void flush() throws IOException {
        log.info(this.getClass().getName() + " flushing");
        this.remoteFileOutputStream.flush();
    }

}
