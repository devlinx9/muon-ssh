package muon.app.ui.components.common;

import muon.app.App;

import javax.swing.*;
import java.awt.*;

public class FontAwesomeIcon implements Icon {
    private final int width;
    private final int height;
    private final String faIcon;

    public FontAwesomeIcon(String pFaIcon, int width, int height) {
        this.faIcon = pFaIcon;
        this.width = width;
        this.height = height;
    }

    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setFont(App.getCONTEXT().getSkin().getIconFont().deriveFont((float) height));
        int ypos = g2.getFontMetrics().getAscent();
        g2.drawString(faIcon, x, ypos);
    }

    @Override
    public int getIconWidth() {
        return width;
    }

    @Override
    public int getIconHeight() {
        return height;
    }

}
