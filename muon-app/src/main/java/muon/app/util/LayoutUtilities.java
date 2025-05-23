
package muon.app.util;

import java.awt.*;

/**
 * @author subhro
 *
 */
public final class LayoutUtilities {

    protected LayoutUtilities() {

    }
    public static void equalizeSize(Component... components) {
        int maxWidth = 0;
        int maxHeight = 0;
        for (Component item : components) {
            Dimension dim = item.getPreferredSize();
            if (maxWidth <= dim.width) {
                maxWidth = dim.width;
            }
            if (maxHeight <= dim.height) {
                maxHeight = dim.height;
            }
        }

        Dimension dimMax = new Dimension(maxWidth, maxHeight);
        for (Component item : components) {
            item.setPreferredSize(dimMax);
            item.setMinimumSize(dimMax);
            item.setMaximumSize(dimMax);
        }
    }
}
