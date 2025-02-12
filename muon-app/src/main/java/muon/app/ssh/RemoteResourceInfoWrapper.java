/**
 *
 */
package muon.app.ssh;

import lombok.Getter;
import lombok.Setter;
import net.schmizz.sshj.sftp.RemoteResourceInfo;

/**
 * @author subhro
 *
 */
@Setter
@Getter
public class RemoteResourceInfoWrapper {

    private RemoteResourceInfo info;

    private String longPath;

    /**
     */
    public RemoteResourceInfoWrapper(RemoteResourceInfo info, String longPath) {
        super();
        this.info = info;
        this.longPath = longPath;
    }

}
