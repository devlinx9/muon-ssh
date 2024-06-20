/**
 *
 */
package muon.app.ui.components.settings;

import muon.app.App;
import util.FontUtils;

import javax.swing.*;
import java.awt.*;

/**
 * @author subhro
 *
 */
public class FontItemRenderer extends JLabel implements ListCellRenderer<String> {

    /**
     *
     */
    public FontItemRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        System.out.println("Creating font in renderer: " + value);
        Font font = FontUtils.loadTerminalFont(value).deriveFont(Font.PLAIN, 14);
        setFont(font);
        setText(FontUtils.TERMINAL_FONTS.get(value));
        setBackground(isSelected ? App.skin.getAddressBarSelectionBackground() : App.skin.getSelectedTabColor());
        setForeground(isSelected ? App.skin.getDefaultSelectionForeground() : App.skin.getDefaultForeground());
        return this;
    }

}
