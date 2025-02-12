/**
 *
 */
package muon.app.ui.components.session;

import muon.app.ui.AppWindow;

import javax.swing.*;

/**
 * @author subhro
 *
 */
public class SessionConnectDialog extends JDialog implements GUIBlocker {
    /**
     *
     */
    private final AppWindow appWindow;

    public SessionConnectDialog(AppWindow appWindow) {
        super(appWindow);
        this.setSize(400, 300);
        this.setModal(true);
        this.appWindow = appWindow;
    }

    @Override
    public void blockInterface() {
        setLocationRelativeTo(appWindow);
        setVisible(true);
    }

    @Override
    public void unBlockInterface() {
        setVisible(false);
    }
}
