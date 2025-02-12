/**
 *
 */
package muon.app.ui.components.session.files.remote2remote;

import lombok.Getter;
import lombok.Setter;

/**
 * @author subhro
 *
 */
@Setter
@Getter
public class RemoteServerEntry {
    private String id;
    private String host;
    private String user;
    private String path;
    private int port;

    public RemoteServerEntry(String host, int port, String user, String path) {
        super();
        this.host = host;
        this.port = port;
        this.user = user;
        this.path = path;
    }
    public RemoteServerEntry() {
    }

}
