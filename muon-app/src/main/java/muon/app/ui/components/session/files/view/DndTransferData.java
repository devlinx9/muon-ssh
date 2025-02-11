package muon.app.ui.components.session.files.view;

import lombok.Getter;
import lombok.Setter;
import muon.app.common.FileInfo;

import java.io.Serializable;
import java.util.Arrays;

public class DndTransferData implements Serializable {

    private int sessionHashcode;
    @Setter
    @Getter
    private FileInfo[] files;
    @Setter
    @Getter
    private String currentDirectory;
    @Setter
    @Getter
    private int source;
    @Setter
    @Getter
    private TransferAction transferAction = TransferAction.DRAG_DROP;
    @Getter
    private final DndSourceType sourceType;
    public DndTransferData(int sessionHashcode, FileInfo[] files,
                           String currentDirectory, int source, DndSourceType sourceType) {
        this.sessionHashcode = sessionHashcode;
        this.files = files;
        this.currentDirectory = currentDirectory;
        this.source = source;
        this.sourceType = sourceType;
    }

    @Override
    public String toString() {
        return "DndTransferData{" + "sessionHashcode=" + sessionHashcode
                + ", files=" + Arrays.toString(files) + ", currentDirectory='"
                + currentDirectory + '\'' + '}';
    }

    public int getInfo() {
        return sessionHashcode;
    }

    public void setInfo(int info) {
        this.sessionHashcode = info;
    }

    public enum DndSourceType {
        SSH, SFTP, FTP, LOCAL
    }

    public enum TransferAction {
        DRAG_DROP, CUT, COPY
    }
}
