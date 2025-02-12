package muon.app.ui.components.session;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.dnd.Autoscroll;

//http://www.java2s.com/Code/Java/Swing-JFC/DnDdraganddropJTreecode.htm
public class AutoScrollingJTree extends JTree implements Autoscroll {
    private final int margin = 12;

    public AutoScrollingJTree() {
        super();
    }

    public AutoScrollingJTree(TreeModel model) {
        super(model);
    }

    public void autoscroll(Point p) {
        int realRow = getRowForLocation(p.x, p.y);
        Rectangle outer = getBounds();
        realRow = (p.y + outer.y <= margin ? realRow < 1 ? 0 : realRow - 1
                : realRow < getRowCount() - 1 ? realRow + 1 : realRow);
        scrollRowToVisible(realRow);
    }

    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();
        return new Insets(inner.y - outer.y + margin, inner.x - outer.x + margin,
                outer.height - inner.height - inner.y + outer.y + margin,
                outer.width - inner.width - inner.x + outer.x + margin);
    }

}
