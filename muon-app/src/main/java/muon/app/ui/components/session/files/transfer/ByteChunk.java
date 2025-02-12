package muon.app.ui.components.session.files.transfer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ByteChunk {
    private byte[] buf;
    private long len;

    public ByteChunk(byte[] buf, long len) {
        this.buf = buf;
        this.len = len;
    }

}