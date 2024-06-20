/**
 *
 */
package muon.app.ui.components.session;

import muon.app.App;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author subhro
 *
 */
public class TabbedPage extends JPanel {
    /**
     *
     */
    private final Page page;
    private final JLabel lblIcon;
    private final JLabel lblText;
    private final Border selectedBorder = new CompoundBorder(
            new MatteBorder(0, 0, 2, 0,
                    App.skin.getDefaultSelectionBackground()),
            new EmptyBorder(10, 0, 10, 0));
    private final Border normalBorder = new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, App.skin.getDefaultBackground()),
            new EmptyBorder(10, 0, 10, 0));

    public TabbedPage(Page page, PageHolder holder) {
        super(new BorderLayout(5, 5));
        this.page = page;
        setBorder(normalBorder);

        lblIcon = new JLabel(page.getIcon());
        lblText = new JLabel(page.getText());

        lblIcon.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                holder.showPage(TabbedPage.this.hashCode() + "");
            }
        });
        lblText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                holder.showPage(TabbedPage.this.hashCode() + "");
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                holder.showPage(TabbedPage.this.hashCode() + "");
            }
        });

        lblIcon.setForeground(App.skin.getInfoTextForeground());
        lblText.setForeground(App.skin.getInfoTextForeground());

        int prefW = lblText.getPreferredSize().width + 20;

        lblIcon.setHorizontalAlignment(JLabel.CENTER);
        lblText.setHorizontalAlignment(JLabel.CENTER);

        lblIcon.setFont(App.skin.getIconFont().deriveFont(24.0f));
        lblText.setFont(App.skin.getDefaultFont().deriveFont(12.0f));

        this.add(lblIcon);
        this.add(lblText, BorderLayout.SOUTH);

        this.setPreferredSize(
                new Dimension(prefW, this.getPreferredSize().height));
        this.setMaximumSize(
                new Dimension(prefW, this.getPreferredSize().height));
        this.setMinimumSize(
                new Dimension(prefW, this.getPreferredSize().height));
    }

    public void setSelected(boolean selected) {
        this.setBorder(selected ? selectedBorder : normalBorder);
        this.lblIcon.setForeground(selected ? App.skin.getDefaultForeground()
                : App.skin.getInfoTextForeground());
        this.lblText.setForeground(selected ? App.skin.getDefaultForeground()
                : App.skin.getInfoTextForeground());
        this.revalidate();
        this.repaint();
    }

    /**
     *
     */
    public String getText() {
        return lblText.getText();
    }

    /**
     * @return the page
     */
    public Page getPage() {
        return page;
    }

    public String getId() {
        return this.hashCode() + "";
    }
}
