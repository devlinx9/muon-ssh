package muon.app.ui.components.session.utilpage.portview;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SocketEntry {
    private String app;
    private int pid;
    private int port;
    private String host;

    public SocketEntry() {
    }

    public SocketEntry(String app, int pid, int port, String host) {
        this.app = app;
        this.pid = pid;
        this.port = port;
        this.host = host;
    }

}
