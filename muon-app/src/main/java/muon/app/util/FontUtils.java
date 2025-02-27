/**
 *
 */
package muon.app.util;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.laf.AppSkin;
import muon.app.util.enums.Language;

import java.awt.*;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

/**
 * @author subhro
 *
 */
@Slf4j
public class FontUtils {
    public static final Map<String, String> TERMINAL_FONTS = new CollectionHelper.OrderedDict<String, String>()
            .putItem("DejaVuSansMono", "DejaVu Sans Mono").putItem("FiraCode-Regular", "Fira Code Regular")
            .putItem("Inconsolata-Regular", "Inconsolata Regular").putItem("NotoMono-Regular", "Noto Mono");

    public static Font loadFonts() {
        String fontPath = "/fonts/Helvetica.ttf";
        if (App.getGlobalSettings().getLanguage().equals(Language.CHINESE)) {
            fontPath = "/fonts/WenQuanYi-Micro-Hei-Regular.ttf";
        }

        try (InputStream is = AppSkin.class
                .getResourceAsStream(fontPath)) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            return font.deriveFont(Font.PLAIN, 12.0f);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static Font loadFontAwesomeFonts() {
        try (InputStream is = AppSkin.class.getResourceAsStream("/fonts/fontawesome-webfont.ttf")) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            return font.deriveFont(Font.PLAIN, 14f);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static Font loadTerminalFont(String name) {
        log.debug("Loading font: {}", name);
        try (InputStream is = AppSkin.class.getResourceAsStream(String.format("/fonts/terminal/%s.ttf", name))) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(is));
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(font);
            log.debug("Font loaded: {} of family: {}", font.getFontName(), font.getFamily());
            return font.deriveFont(Font.PLAIN, 12.0f);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
