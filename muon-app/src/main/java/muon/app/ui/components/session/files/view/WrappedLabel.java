package muon.app.ui.components.session.files.view;

import javax.swing.*;
import java.awt.*;

public class WrappedLabel extends JComponent {

    private String text;
    private static final int ROWS = 3;

    public void setText(String text) {
        this.text = text;
        FontMetrics fm = getFontMetrics(getFont());
        Dimension d = new Dimension(10, fm.getHeight() * ROWS);
        setPreferredSize(d);
        revalidate();
        repaint(0);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setColor(getForeground());

        FontMetrics fm = g2.getFontMetrics();

        int y = fm.getAscent();
        int x = 0;

        int dotWidth = fm.stringWidth("...");
        int width = getWidth() - dotWidth;

        StringBuilder[] sb = new StringBuilder[ROWS];
        for (int i = 0; i < sb.length; i++) {
            sb[i] = new StringBuilder();
        }

        int lineWidth = 0;
        int c = 0;
        for (int i = 0; i < text.length(); i++) {
            int charWidth = fm.charWidth(text.charAt(i));
            if (lineWidth + charWidth > width) {
                c++;
                if (c > ROWS - 1) {
                    sb[c - 1].append("...");
                    break;
                }
                lineWidth = 0;
                sb[c].append(text.charAt(i));
            } else {
                lineWidth += charWidth;
                sb[c].append(text.charAt(i));
            }
        }

        for (StringBuilder s : sb) {
            String str = s.toString();
            int w = fm.stringWidth(str);
            int diffx = getWidth() / 2 - w / 2;
            g2.drawString(s.toString(), x + diffx, y);
            y += fm.getHeight();
        }
    }
}
