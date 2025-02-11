package muon.app.ui.components.session.files.transfer;

import lombok.Getter;
import lombok.Setter;
import muon.app.ssh.RemoteSessionInstance;
import muon.app.ui.components.session.SessionContentPanel;

@Setter
@Getter
public class BackgroundFileTransfer {
    private FileTransfer fileTransfer;
    private RemoteSessionInstance instance;
    private SessionContentPanel session;

    public BackgroundFileTransfer(FileTransfer fileTransfer, RemoteSessionInstance instance,
                                  SessionContentPanel session) {
        super();
        this.fileTransfer = fileTransfer;
        this.instance = instance;
        this.session = session;
    }

}
