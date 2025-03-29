
package muon.app.ssh;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.util.OptionPaneUtils;
import net.schmizz.sshj.userauth.method.ChallengeResponseProvider;
import net.schmizz.sshj.userauth.password.Resource;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * @author subhro
 */
@Slf4j
public class InteractiveResponseProvider implements ChallengeResponseProvider {

    private boolean retry = true;

    @Override
    public List<String> getSubmethods() {
        return Collections.emptyList();
    }

    @Override
    public void init(Resource resource, String name, String instruction) {
        log.info("ChallengeResponseProvider init - resource: {} name: {} instruction: {}", resource, name, instruction);
        if ((name != null && !name.isEmpty())
            || (instruction != null && !instruction.isEmpty())) {
            JOptionPane.showMessageDialog(null, name + "\n" + instruction);
        }
    }

    @Override
    public char[] getResponse(String prompt, boolean echo) {
        log.info("prompt: {} echo: {}", prompt, echo);

        if (echo) {
            String str = OptionPaneUtils.showInputDialog(null, prompt, App.getCONTEXT().getBundle().getString("input"));
            if (str != null) {
                return str.toCharArray();
            }
        } else {
            JPasswordField passwordField = new JPasswordField(30);
            int ret = OptionPaneUtils.showOptionDialog(null,
                                                       new Object[]{prompt, passwordField}, App.getCONTEXT().getBundle().getString("Input"));
            if (ret == JOptionPane.OK_OPTION) {
                return passwordField.getPassword();
            }
        }
        retry = false;
        return new char[0];
    }

    @Override
    public boolean shouldRetry() {
        return retry;
    }

}
