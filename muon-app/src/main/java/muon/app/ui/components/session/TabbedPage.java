/**
 *
 */
package muon.app.ui.components.session;

import lombok.Getter;
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
 */
public class TabbedPage extends JPanel {

    @Getter
    private final Page page;
    private final JLabel lblIcon;
    private final JLabel lblText;
    private final Border selectedBorder = new CompoundBorder(
            new MatteBorder(0, 0, 2, 0,
                            App.getContext().getSkin().getDefaultSelectionBackground()),
            new EmptyBorder(10, 0, 10, 0));
    private final Border normalBorder = new CompoundBorder(
            new MatteBorder(0, 0, 2, 0, App.getContext().getSkin().getDefaultBackground()),
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

        lblIcon.setForeground(App.getContext().getSkin().getInfoTextForeground());
        lblText.setForeground(App.getContext().getSkin().getInfoTextForeground());

        int prefW = lblText.getPreferredSize().width + 20;

        lblIcon.setHorizontalAlignment(JLabel.CENTER);
        lblText.setHorizontalAlignment(JLabel.CENTER);

        lblIcon.setFont(App.getContext().getSkin().getIconFont().deriveFont(24.0f));
        lblText.setFont(App.getContext().getSkin().getDefaultFont().deriveFont(12.0f));

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
        this.lblIcon.setForeground(selected ? App.getContext().getSkin().getDefaultForeground()
                                            : App.getContext().getSkin().getInfoTextForeground());
        this.lblText.setForeground(selected ? App.getContext().getSkin().getDefaultForeground()
                                            : App.getContext().getSkin().getInfoTextForeground());
        this.revalidate();
        this.repaint();
    }

    /**
     *
     */
    public String getText() {
        return lblText.getText();
    }

    public String getId() {
        return this.hashCode() + "";
    }
}
