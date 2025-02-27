package muon.app.ui.components.session;

import javax.swing.*;
import javax.swing.tree.TreeModel;
import java.awt.*;
import java.awt.dnd.Autoscroll;

//http://www.java2s.com/Code/Java/Swing-JFC/DnDdraganddropJTreecode.htm
public class AutoScrollingJTree extends JTree implements Autoscroll {
    private static final int MARGIN = 12;

    public AutoScrollingJTree() {
        super();
    }

    public AutoScrollingJTree(TreeModel model) {
        super(model);
    }

    public void autoscroll(Point p) {
        int realRow = getRowForLocation(p.x, p.y);
        Rectangle outer = getBounds();
        if (p.y + outer.y <= MARGIN) {
            if (realRow < 1) {
                realRow = 0;
            } else {
                realRow = realRow - 1;
            }
        } else {
            realRow = (realRow < getRowCount() - 1 ? realRow + 1 : realRow);
        }
        scrollRowToVisible(realRow);
    }

    public Insets getAutoscrollInsets() {
        Rectangle outer = getBounds();
        Rectangle inner = getParent().getBounds();
        return new Insets(inner.y - outer.y + MARGIN, inner.x - outer.x + MARGIN,
                outer.height - inner.height - inner.y + outer.y + MARGIN,
                outer.width - inner.width - inner.x + outer.x + MARGIN);
    }

}
