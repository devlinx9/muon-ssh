/**
 *
 */
package muon.app.ssh;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import net.schmizz.sshj.common.KeyType;
import net.schmizz.sshj.common.SecurityUtils;
import net.schmizz.sshj.transport.verification.OpenSSHKnownHosts;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.security.PublicKey;

/**
 * @author subhro
 *
 */
@Slf4j
public class GraphicalHostKeyVerifier extends OpenSSHKnownHosts {
    /**
     *
     */
    public GraphicalHostKeyVerifier(File knownHostFile) throws IOException {
        super(knownHostFile);
    }

    @Override
    protected boolean hostKeyUnverifiableAction(String hostname, PublicKey key) {
        final KeyType type = KeyType.fromKey(key);

        int resp = JOptionPane.showConfirmDialog(null,
                String.format(
                        App.getContext().getBundle().getString("unverifiable_action"),
                        hostname, type, SecurityUtils.getFingerprint(key)));

        if (resp == JOptionPane.YES_OPTION) {
            try {
                this.entries.add(new HostEntry(null, hostname, KeyType.fromKey(key), key));
                write();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return true;
        }
        return false;
    }

    @Override
    protected boolean hostKeyChangedAction(String hostname, PublicKey key) {
        final KeyType type = KeyType.fromKey(key);
        final String fp = SecurityUtils.getFingerprint(key);
        final String path = getFile().getAbsolutePath();
        String msg = String.format(App.getContext().getBundle().getString("host_key_change_warning"), type, fp, path);
        return JOptionPane.showConfirmDialog(null, msg) == JOptionPane.YES_OPTION;
    }

    @Override
    public boolean verify(String hostname, int port, PublicKey key) {
        try {
            if (!super.verify(hostname, port, key)) {
                return this.hostKeyUnverifiableAction(hostname, key);
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return this.hostKeyUnverifiableAction(hostname, key);
        }
    }
}
