/**
 *
 */
package muon.app.ui.components;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

/**
 * @author subhro
 *
 */
@Slf4j
public class TextGutter extends JComponent {
    private final JTextArea textArea;

    @Getter
    private int digitCount;

    @Getter
    private long lineStart;

    /**
     *
     */
    public TextGutter(JTextArea textArea, int digitCount) {
        this.textArea = textArea;
        this.digitCount = digitCount;
        this.lineStart = 1;
    }

    public TextGutter(JTextArea textArea) {
        this(textArea, 3);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = textArea.getPreferredSize();
        FontMetrics fm = getFontMetrics(getFont());
        int w = fm.charWidth('w');
        return new Dimension(digitCount * w + 20, d.height);
    }

    @Override
    protected void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(getForeground());

        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();

        try {
            for (int i = 0; i < textArea.getLineCount(); i++) {
                String lineNum = (lineStart + i) + "";
                int startIndex = textArea.getLineStartOffset(i);
                double y = textArea.modelToView2D(startIndex).getY();
                int x = getWidth() / 2 - fm.stringWidth(lineNum) / 2;
                g.drawString(lineNum, x, (int) (y + asc));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param digitCount the digitCount to set
     */
    public void setDigitCount(int digitCount) {
        this.digitCount = digitCount;
        revalidate();
        repaint(0);
    }

    /**
     * @param lineStart the lineStart to set
     */
    public void setLineStart(long lineStart) {
        this.lineStart = lineStart;
        revalidate();
        repaint(0);
    }
}
