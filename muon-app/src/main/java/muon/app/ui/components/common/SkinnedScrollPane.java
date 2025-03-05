/**
 *
 */
package muon.app.ui.components.common;

import javax.swing.*;
import java.awt.*;

/**
 * @author subhro
 *
 */
public class SkinnedScrollPane extends JScrollPane {

    /**
     *
     */
    public SkinnedScrollPane() {
    }

    public SkinnedScrollPane(Component c) {
        super(c);
        this.setBackground(c.getBackground());
        this.getViewport().setBackground(c.getBackground());

        if (horizontalScrollBar != null) {
            horizontalScrollBar.putClientProperty("ScrollBar.background",
                    c.getBackground());
        }

        if (verticalScrollBar != null) {
            verticalScrollBar.putClientProperty("ScrollBar.background",
                    c.getBackground());
        }

        JLabel lbl = new JLabel();
        lbl.setOpaque(true);
        lbl.setBackground(c.getBackground());

        setCorner(ScrollPaneConstants.LOWER_RIGHT_CORNER, lbl);

    }

}
