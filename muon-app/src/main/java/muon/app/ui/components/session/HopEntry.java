package muon.app.ui.components.session;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class HopEntry {
    private String id;
    private String host;
    private String user;
    private String password;
    private String keypath;
    private int port;

    public HopEntry(String id, String host, int port, String user, String password, String keypath) {
        super();
        this.id = id;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.keypath = keypath;
    }
    public HopEntry() {
    }

    @Override
    public String toString() {
        return host != null ? (user != null ? user + "@" + host : host) : "";
    }
}
