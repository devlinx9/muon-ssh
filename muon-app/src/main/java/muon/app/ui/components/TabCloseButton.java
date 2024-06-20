/**
 *
 */
package muon.app.ui.components;

import muon.app.App;
import util.FontAwesomeContants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author subhro
 *
 */
public class TabCloseButton extends JComponent {
    /**
     *
     */
    private boolean hovering;
    private boolean selected;
    private final Font font;

    public TabCloseButton() {
        setPreferredSize(new Dimension(20, 20));
        setMinimumSize(new Dimension(20, 20));
        setMaximumSize(new Dimension(20, 20));
        font = App.skin.getIconFont().deriveFont(14.0f);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                hovering = true;
                repaint(0);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hovering = false;
                repaint(0);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        boolean drawButton = selected || hovering;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        if (drawButton) {
            g2.setColor(getForeground());
            int size = Math.min(getHeight(), Math.min(getWidth(), 16));
            int x = (getWidth() - size) / 2;
            int y = (getHeight() - size) / 2;

            g2.setFont(font);
            int acc = g2.getFontMetrics().getAscent();
            int w = g2.getFontMetrics()
                    .stringWidth(FontAwesomeContants.FA_WINDOW_CLOSE);
            g2.drawString(FontAwesomeContants.FA_WINDOW_CLOSE, x, y + acc);
        }
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
        this.repaint(0);
    }

    /**
     * @param hovering the hovering to set
     */
    public void setHovering(boolean hovering) {
        this.hovering = hovering;
        this.repaint(0);
    }

}
