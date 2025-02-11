/**
 *
 */
package util;

import lombok.extern.slf4j.Slf4j;
import muon.app.ui.laf.AppSkin;

import java.awt.*;
import java.io.InputStream;
import java.util.Map;

/**
 * @author subhro
 *
 */
@Slf4j
public class FontUtils {
    public static final Map<String, String> TERMINAL_FONTS = new CollectionHelper.OrderedDict<String, String>()
            .putItem("DejaVuSansMono", "DejaVu Sans Mono").putItem("FiraCode-Regular", "Fira Code Regular")
            .putItem("Inconsolata-Regular", "Inconsolata Regular").putItem("NotoMono-Regular", "Noto Mono");

    public static Font loadFont(String path) {
        try (InputStream is = AppSkin.class.getResourceAsStream(path)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            log.info("Font loaded: " + font.getFontName() + " of family: " + font.getFamily());
            return font.deriveFont(Font.PLAIN, 12.0f);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static Font loadTerminalFont(String name) {
        log.debug("Loading font: " + name);
        try (InputStream is = AppSkin.class.getResourceAsStream(String.format("/fonts/terminal/%s.ttf", name))) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            log.debug("Font loaded: " + font.getFontName() + " of family: " + font.getFamily());
            return font.deriveFont(Font.PLAIN, 12.0f);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
