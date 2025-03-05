/**
 *
 */
package muon.app.ui.components.common;

import muon.app.App;

import javax.swing.*;

/**
 * @author subhro
 *
 */
public class SkinnedSplitPane extends JSplitPane {
    /**
     *
     */
    public SkinnedSplitPane() {
        applySkin();
    }

    public SkinnedSplitPane(int orientation) {
        super(orientation);
        applySkin();
    }

    public void applySkin() {
        this.putClientProperty("Nimbus.Overrides", App.getContext().getSkin().getSplitPaneSkin());
    }

}
