/**
 *
 */
package muon.app.ui.components.settings;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import muon.app.App;

import java.awt.*;
import com.jediterm.core.Color;

/**
 * @author subhro
 *
 */
public class CustomTerminalTheme implements TerminalTheme {

    public static final TerminalColor getTerminalColor(int rgb) {
        return TerminalColor.fromColor(new Color(rgb));
    }

    @Override
    public String getName() {
        return "Custom";
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public TextStyle getDefaultStyle() {
        return new TextStyle(
                getTerminalColor(App.getGlobalSettings().getDefaultColorFg()),
                getTerminalColor(App.getGlobalSettings().getDefaultColorBg()));
    }

    @Override
    public TextStyle getSelectionColor() {
        return new TextStyle(
                getTerminalColor(
                        App.getGlobalSettings().getDefaultSelectionFg()),
                getTerminalColor(
                        App.getGlobalSettings().getDefaultSelectionBg()));
    }

    @Override
    public TextStyle getFoundPatternColor() {
        return new TextStyle(
                getTerminalColor(App.getGlobalSettings().getDefaultFoundFg()),
                getTerminalColor(App.getGlobalSettings().getDefaultFoundFg()));
    }

    @Override
    public TextStyle getHyperlinkColor() {
        return new TextStyle(
                getTerminalColor(App.getGlobalSettings().getDefaultHrefFg()),
                getTerminalColor(App.getGlobalSettings().getDefaultHrefBg()));
    }
}
