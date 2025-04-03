
package muon.app.ui.components.settings;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.util.FontUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * @author subhro
 *
 */
@Slf4j
public class FontItemRenderer extends JLabel implements ListCellRenderer<String> {

    
    public FontItemRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        log.debug("Creating font in renderer: {}", value);
        Font font = Objects.requireNonNull(FontUtils.loadTerminalFont(value)).deriveFont(Font.PLAIN, 14);
        setFont(font);
        setText(FontUtils.TERMINAL_FONTS.get(value));
        setBackground(isSelected ? App.getCONTEXT().getSkin().getAddressBarSelectionBackground() : App.getCONTEXT().getSkin().getSelectedTabColor());
        setForeground(isSelected ? App.getCONTEXT().getSkin().getDefaultSelectionForeground() : App.getCONTEXT().getSkin().getDefaultForeground());
        return this;
    }

}
