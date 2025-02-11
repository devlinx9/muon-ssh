/**
 *
 */
package muon.app.ssh;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;

import javax.swing.*;

/**
 * @author subhro
 *
 */
@Slf4j
public class GraphicalInputBlocker extends JDialog implements InputBlocker {
    private final JFrame window;

    //Todo devlinx9 fix this.
    private final JLabel connectingLabel = new JLabel(App.bundle.getString("connecting"));

    /**
     *
     */
    public GraphicalInputBlocker(JFrame window) {
        super(window);
        this.window = window;
        setModal(true);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(200, 100);
    }

    @Override
    public void blockInput() {
        SwingUtilities.invokeLater(() -> {
            log.debug("Making visible...");
            this.setLocationRelativeTo(window);
            this.setUndecorated(true);
            connectingLabel.setHorizontalAlignment(SwingConstants.CENTER);
            this.add(connectingLabel);
            this.setVisible(true);
        });
    }

    @Override
    public void unblockInput() {
        SwingUtilities.invokeLater(() -> this.setVisible(false));
    }

}
