/**
 *
 */
package muon.app.ui.components.session.utilpage;

import muon.app.App;

import javax.swing.*;
import java.awt.*;

/**
 * @author subhro
 *
 */
public class UtilityPageButton extends JToggleButton {
    private final String text;
    private final String iconText;
    private final Font iconFont;

    /**
     *
     */
    public UtilityPageButton(String text, String iconText) {
        this.text = text;
        this.iconText = iconText;
        this.iconFont = App.skin.getIconFont().deriveFont(24.0f);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(
                super.isSelected() ? App.skin.getDefaultSelectionBackground()
                        : getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(
                super.isSelected() ? App.skin.getDefaultSelectionForeground()
                        : getForeground());
        FontMetrics fm1 = g2.getFontMetrics(iconFont);
        FontMetrics fm2 = g2.getFontMetrics(getFont());
        int y = getHeight() / 2 - (fm1.getHeight() + fm2.getHeight()) / 2;
        g2.setFont(iconFont);
        g2.drawString(iconText, getWidth() / 2 - fm1.stringWidth(iconText) / 2,
                y + fm1.getAscent());
        g2.setFont(getFont());
        g2.drawString(text, getWidth() / 2 - fm2.stringWidth(text) / 2,
                y + fm1.getHeight() + 5 + fm2.getAscent());
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fm1 = getFontMetrics(getFont());
        FontMetrics fm2 = getFontMetrics(iconFont);
        int w1 = fm1.stringWidth(text);
        int w2 = fm2.stringWidth(iconText);
        int h1 = fm1.getHeight();
        int h2 = fm2.getHeight();
        return new Dimension(Math.max(w1, w2) + 10, h1 + h2 + 30);
    }

    @Override
    public Dimension getMinimumSize() {
        return this.getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension d = this.getPreferredSize();
        return new Dimension(Short.MAX_VALUE, d.height);
    }

}
