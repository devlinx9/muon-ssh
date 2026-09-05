
package muon.app.ui.components.session;

import lombok.Getter;
import muon.app.App;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static muon.app.util.ScalingUtil.*;

/**
 * @author subhro
 */
public class TabbedPage extends JPanel {

    @Getter
    private final Page page;
    private final JLabel lblIcon;
    private final JLabel lblText;
    private final Border selectedBorder = new CompoundBorder(
            getScaledMatteBorder(0, 0, 2, 0,
                                 App.getCONTEXT().getSkin().getDefaultSelectionBackground()),
            getScaledEmptyBorder(10, 0, 10, 0));
    private final Border normalBorder = new CompoundBorder(
            getScaledMatteBorder(0, 0, 2, 0, App.getCONTEXT().getSkin().getDefaultBackground()),
            getScaledEmptyBorder(10, 0, 10, 0));

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

        lblIcon.setForeground(App.getCONTEXT().getSkin().getInfoTextForeground());
        lblText.setForeground(App.getCONTEXT().getSkin().getInfoTextForeground());

        int prefW = lblText.getPreferredSize().width + 20;

        lblIcon.setHorizontalAlignment(JLabel.CENTER);
        lblText.setHorizontalAlignment(JLabel.CENTER);


        lblIcon.setFont(App.getCONTEXT().getSkin().getIconFont(24.0f));
        lblText.setFont(App.getCONTEXT().getSkin().getDefaultFont(12.0f));

        this.add(lblIcon);
        if (App.getCONTEXT().getSettings().isUseCompactView()) {
            lblIcon.setFont(App.getCONTEXT().getSkin().getIconFont(16.0f));
        } else {
            this.add(lblText, BorderLayout.SOUTH);
        }
        this.setPreferredSize(
                scale(new Dimension(prefW, this.getPreferredSize().height)));
        this.setMaximumSize(
                scale(new Dimension(prefW, this.getPreferredSize().height)));
        this.setMinimumSize(
                scale(new Dimension(prefW, this.getPreferredSize().height)));
    }

    public void setSelected(boolean selected) {
        this.setBorder(selected ? selectedBorder : normalBorder);
        this.lblIcon.setForeground(selected ? App.getCONTEXT().getSkin().getDefaultForeground()
                                            : App.getCONTEXT().getSkin().getInfoTextForeground());
        this.lblText.setForeground(selected ? App.getCONTEXT().getSkin().getDefaultForeground()
                                            : App.getCONTEXT().getSkin().getInfoTextForeground());
        this.revalidate();
        this.repaint();
    }


    public String getText() {
        return lblText.getText();
    }

    public String getId() {
        return this.hashCode() + "";
    }
}
