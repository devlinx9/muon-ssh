/**
 *
 */
package muon.app.ui.components;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static muon.app.App.bundle;

/**
 * @author subhro
 *
 */
@Slf4j
public class SkinnedTextArea extends JTextArea {
    /**
     *
     */
    public SkinnedTextArea() {
        installPopUp();
    }

    private void installPopUp() {
        this.putClientProperty("flat.popup", createPopup());
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.info("Right click on text field");
                if (e.getButton() == MouseEvent.BUTTON3 || e.isPopupTrigger()) {

                    JPopupMenu pop = (JPopupMenu) SkinnedTextArea.this
                            .getClientProperty("flat.popup");
                    if (pop != null) {
                        pop.show(SkinnedTextArea.this, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private JPopupMenu createPopup() {
        JPopupMenu popup = new JPopupMenu();
        JMenuItem mCut = new JMenuItem(bundle.getString("cut"));
        JMenuItem mCopy = new JMenuItem(bundle.getString("copy"));
        JMenuItem mPaste = new JMenuItem(bundle.getString("paste"));
        JMenuItem mSelect = new JMenuItem(bundle.getString("select_all"));

        popup.add(mCut);
        popup.add(mCopy);
        popup.add(mPaste);
        popup.add(mSelect);

        mCut.addActionListener(e -> cut());

        mCopy.addActionListener(e -> copy());

        mPaste.addActionListener(e -> paste());

        mSelect.addActionListener(e -> selectAll());

        return popup;
    }
}
