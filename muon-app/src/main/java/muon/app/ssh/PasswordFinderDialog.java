/**
 *
 */
package muon.app.ssh;

import muon.app.App;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

import javax.swing.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author subhro
 */
public class PasswordFinderDialog implements PasswordFinder {

    private boolean retry = true;
    private final CachedCredentialProvider cachedCredentialProvider;
    private final AtomicBoolean firstAttempt = new AtomicBoolean(true);

    public PasswordFinderDialog(CachedCredentialProvider cachedCredentialProvider) {
        this.cachedCredentialProvider = cachedCredentialProvider;
    }

    @Override
    public char[] reqPassword(Resource<?> resource) {
        // if pass phrase was already cached
        if (firstAttempt.get() && this.cachedCredentialProvider.getCachedPassPhrase() != null) {
            firstAttempt.set(false);
            return this.cachedCredentialProvider.getCachedPassPhrase();
        }
        firstAttempt.set(false);
        JPasswordField txtPass = new JPasswordField();
        JCheckBox chkUseCache = new JCheckBox(App.bundle.getString("remember_session"));

        int ret = JOptionPane.showOptionDialog(App.getAppWindow(),
                                               new Object[]{resource != null ? resource.toString() : "Private key passphrase:", txtPass, chkUseCache},
                                               "Passphrase", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (ret == JOptionPane.OK_OPTION) {
            char[] passPhrase = txtPass.getPassword();
            if (chkUseCache.isSelected()) {
                this.cachedCredentialProvider.setCachedPassPhrase(passPhrase);
            }
            return passPhrase;
        }
        retry = false;
        return new char[0];
    }

    @Override
    public boolean shouldRetry(Resource<?> resource) {
        return retry;
    }

}
