
package muon.app.ui.components.session.utilpage;

import muon.app.ui.components.session.SessionContentPanel;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author subhro
 *
 */
public abstract class UtilPageItemView extends JPanel {
    protected AtomicBoolean init = new AtomicBoolean(false);
    protected SessionContentPanel holder;

    
    protected UtilPageItemView(SessionContentPanel holder) {
        super(new BorderLayout());
        this.holder = holder;
        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                onComponentHide();
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
            }

            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (!init.get()) {
                    init.set(true);
                    createUI();
                    revalidate();
                    repaint(0);
                }
                onComponentVisible();
            }
        });
    }

    protected abstract void createUI();

    protected abstract void onComponentVisible();

    protected abstract void onComponentHide();
}
