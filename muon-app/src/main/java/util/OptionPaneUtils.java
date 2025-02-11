/**
 *
 */
package util;

import lombok.extern.slf4j.Slf4j;
import muon.app.ui.components.SkinnedTextField;

import javax.swing.*;
import java.awt.*;

/**
 * @author subhro
 *
 */
@Slf4j
public class OptionPaneUtils {
    public static synchronized int showOptionDialog(Component owner, Object[] components, String title) {
        return JOptionPane.showOptionDialog(owner, components, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, null, null);
    }

    public static synchronized String showInputDialog(Component owner, String text, String title) {
        return showInputDialog(owner, text, null, title);
    }

    public static synchronized String showInputDialog(Component owner, String text, String initialText, String title) {
        JTextField txt = new SkinnedTextField(30);
        if (initialText != null) {
            txt.setText(initialText);
        }

        if (JOptionPane.showOptionDialog(owner, new Object[]{text, txt}, title, JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION && !txt.getText().isEmpty()) {
            return txt.getText();
        }
        return null;
    }
}
