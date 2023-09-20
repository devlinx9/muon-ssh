/**
 *
 */
package muon.app.ui.components.session.terminal;

import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.emulator.ColorPaletteImpl;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import muon.app.App;
import muon.app.Settings;
import muon.app.ui.components.session.SessionInfo;
import util.FontUtils;

import javax.swing.*;
import java.awt.*;
import com.jediterm.core.Color;

/**
 * @author subhro
 *
 */
public class CustomizedSettingsProvider extends DefaultSettingsProvider {
    private final ColorPalette palette;
    
    private final SessionInfo info;
    
    /**
     *
     */
    public CustomizedSettingsProvider(SessionInfo info) {
        this.info = info;
        
        Color[] colors = new Color[16];
        int[] colorArr = App.getGlobalSettings().getPalleteColors();
        for (int i = 0; i < 16; i++) {
            colors[i] = new Color(colorArr[i]);
        }

        //palette = this.getTerminalColorPalette;
        palette = new ColorPalette() {

            public Color[] getIndexColors() {
                return colors;
            }

            @Override
            protected Color getBackgroundByColorIndex(int colorIndex) {
                return colors[colorIndex];
            }
            @Override
              public Color getForegroundByColorIndex(int colorIndex) {
                return colors[colorIndex];
              }
        };
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jediterm.terminal.ui.settings.DefaultSettingsProvider#
     * getTerminalColorPalette()
     */
    @Override
    public ColorPalette getTerminalColorPalette() {
        return palette;
    }

    /*
     * (non-Javadoc)
     *
     * @see com.jediterm.terminal.ui.settings.DefaultSettingsProvider#
     * useAntialiasing()
     */
    @Override
    public boolean useAntialiasing() {
        return true;
    }

    @Override
    public TextStyle getDefaultStyle() {
        return new TextStyle(getTerminalColor(App.getGlobalSettings().getDefaultColorFg()),
                getTerminalColor(App.getGlobalSettings().getDefaultColorBg()));
    }

    @Override
    public TextStyle getFoundPatternColor() {
        return new TextStyle(getTerminalColor(App.getGlobalSettings().getDefaultFoundFg()),
                getTerminalColor(App.getGlobalSettings().getDefaultFoundBg()));
    }

    @Override
    public TextStyle getSelectionColor() {
        return new TextStyle(getTerminalColor(App.getGlobalSettings().getDefaultSelectionFg()),
                getTerminalColor(App.getGlobalSettings().getDefaultSelectionBg()));
        //
    }

    @Override
    public TextStyle getHyperlinkColor() {
        return new TextStyle(getTerminalColor(App.getGlobalSettings().getDefaultHrefFg()),
                getTerminalColor(App.getGlobalSettings().getDefaultHrefBg()));

    }

    @Override
    public boolean emulateX11CopyPaste() {
        return App.getGlobalSettings().isPuttyLikeCopyPaste();
    }

    @Override
    public boolean enableMouseReporting() {
        return true;
    }

    @Override
    public boolean pasteOnMiddleMouseClick() {
        return App.getGlobalSettings().isPuttyLikeCopyPaste();
    }

    @Override
    public boolean copyOnSelect() {
        return App.getGlobalSettings().isPuttyLikeCopyPaste();
    }

    @Override
    public Font getTerminalFont() {
        System.out.println("Called terminal font: " + App.getGlobalSettings().getTerminalFontName());
        return FontUtils.loadTerminalFont(App.getGlobalSettings().getTerminalFontName()).deriveFont(Font.PLAIN,
                App.getGlobalSettings().getTerminalFontSize());
    }

    @Override
    public float getTerminalFontSize() {
        return App.getGlobalSettings().getTerminalFontSize();
    }

    @Override
    public boolean audibleBell() {
        return App.getGlobalSettings().isTerminalBell();
    }

    public final TerminalColor getTerminalColor(int rgb) {
        return TerminalColor.fromColor(new Color(rgb));
    }

    
    public KeyStroke[] getCopyKeyStrokes() {
        return new KeyStroke[]{getKeyStroke(Settings.COPY_KEY)};
    }

    
    public KeyStroke[] getPasteKeyStrokes() {
        return new KeyStroke[]{getKeyStroke(Settings.PASTE_KEY)};
    }

    
    public KeyStroke[] getClearBufferKeyStrokes() {
        return new KeyStroke[]{getKeyStroke(Settings.CLEAR_BUFFER)};
    }

    
    public KeyStroke[] getFindKeyStrokes() {
        return new KeyStroke[]{getKeyStroke(Settings.FIND_KEY)};
    }
    
    @Override
    public KeyStroke[] getTypeSudoPasswordKeyStrokes() {
        return new KeyStroke[]{getKeyStroke(Settings.TYPE_SUDO_PASSWORD)};
    }
    
    private KeyStroke getKeyStroke(String key) {
        return KeyStroke.getKeyStroke(
                App.getGlobalSettings().getKeyCodeMap().get(key),
                App.getGlobalSettings().getKeyModifierMap().get(key)
        );
    }
    
    @Override
    public String getSudoPassword() {
        return info.getSudoPassword();
    }
}
