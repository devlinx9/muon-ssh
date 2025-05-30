
package muon.app.ui.components.common;

import lombok.Getter;
import lombok.Setter;
import muon.app.App;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author subhro
 *
 */
public class KeyShortcutComponent extends JComponent {

    private static final String WAITING_STRING = "Please press the key combination";

    @Setter
    @Getter
    private int keyCode = Integer.MIN_VALUE;

    @Setter
    @Getter
    private int modifier;
    private boolean waitingForKeys = false;

    public KeyShortcutComponent() {
        setFocusable(true);
        setBorder(new EmptyBorder(10, 10, 10, 10));
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (!waitingForKeys)
                    return;
                keyCode = e.getExtendedKeyCode();
                modifier = e.getModifiersEx();
                waitingForKeys = false;
                revalidate();
                repaint(0);

            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                waitingForKeys = !waitingForKeys;
                requestFocusInWindow();
                revalidate();
                repaint(0);
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.setColor(App.getCONTEXT().getSkin().getSelectedTabColor());

        Insets inset = getInsets();

        g2.fillRoundRect(inset.left, inset.top,
                getWidth() - inset.left - inset.right,
                getHeight() - inset.top - inset.bottom, 5, 5);

        String text = getText();
        g2.setColor(getForeground());
        g2.setFont(getFont());
        int stringWidth = g2.getFontMetrics().stringWidth(text);
        int stringHeight = g2.getFontMetrics().getHeight();
        int x = getWidth() / 2 - stringWidth / 2;
        int y = getHeight() / 2 - stringHeight / 2;
        g2.drawString(text, x, y + g2.getFontMetrics().getAscent());
        g2.dispose();
    }

    private String getText() {
        if (!waitingForKeys) {
            if (keyCode == Integer.MIN_VALUE) {
                return "Not configured";
            }
            String txtModifier = KeyEvent.getModifiersExText(modifier);
            String txtKeyText = KeyEvent.getKeyText(keyCode);
            return txtModifier == null || txtModifier.isEmpty() ? txtKeyText
                    : txtModifier + "+" + txtKeyText;
        } else {
            return WAITING_STRING;
        }
    }

    @Override
    public Dimension getPreferredSize() {
        FontMetrics fmt = getFontMetrics(getFont());
        int width = fmt.stringWidth(getText());
        int height = fmt.getHeight();
        Insets insets = getInsets();
        return new Dimension(width + insets.left + insets.right + 6,
                height + insets.top + insets.bottom + 6);
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public Dimension getMaximumSize() {
        return getPreferredSize();
    }

}
