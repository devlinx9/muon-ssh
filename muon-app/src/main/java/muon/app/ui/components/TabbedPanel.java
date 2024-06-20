package muon.app.ui.components;

import muon.app.App;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TabbedPanel extends JPanel {
    private final Color unselectedBg = App.skin.getSelectedTabColor();
    private final Color selectedBg = App.skin.getDefaultBackground();
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final Box tabHolder;
    private final Border selectedTabBorder = new CompoundBorder(
            new MatteBorder(2, 0, 0, 0,
                    App.skin.getDefaultSelectionBackground()),
            new EmptyBorder(10, 15, 10, 15));
    private final Border unselectedTabBorder = new CompoundBorder(
            new MatteBorder(2, 0, 0, 0, App.skin.getSelectedTabColor()),
            new EmptyBorder(10, 15, 10, 15));

    public TabbedPanel() {
        super(new BorderLayout(), true);
        setOpaque(true);
        tabHolder = Box.createHorizontalBox();
        tabHolder.setBackground(unselectedBg);
        tabHolder.setOpaque(true);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        JPanel tabTop = new JPanel(new BorderLayout());
        tabTop.setOpaque(true);
        tabTop.setBackground(unselectedBg);
        tabTop.add(tabHolder);
        add(tabTop, BorderLayout.NORTH);
        add(cardPanel);
    }

    public void addTab(String tabTitle, Component body) {
        int index = tabHolder.getComponentCount();
        cardPanel.add(body, body.hashCode() + "");

        JLabel tabLabel = new JLabel(tabTitle);
        tabLabel.setOpaque(true);
        tabLabel.setBorder(unselectedTabBorder);
        tabLabel.setName(body.hashCode() + "");
        tabLabel.putClientProperty("component", body);
        tabHolder.add(tabLabel);

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                for (int i = 0; i < tabHolder.getComponentCount(); i++) {
                    JComponent c = (JComponent) tabHolder.getComponent(i);
                    if (c == tabLabel) {
                        setSelectedIndex(i);
                        break;
                    }
                }
            }
        };

        tabLabel.addMouseListener(mouseAdapter);
        setSelectedIndex(index);
    }

    public int getSelectedIndex() {
        for (int i = 0; i < tabHolder.getComponentCount(); i++) {
            JComponent c = (JComponent) tabHolder.getComponent(i);
            if (c.getClientProperty("selected") == Boolean.TRUE) {
                return i;
            }
        }
        return -1;
    }

    public void setSelectedIndex(int n) {
        JComponent c = (JComponent) tabHolder.getComponent(n);
        String id = c.getName();
        cardLayout.show(cardPanel, id);
        for (int i = 0; i < tabHolder.getComponentCount(); i++) {
            JComponent cc = (JComponent) tabHolder.getComponent(i);
            cc.putClientProperty("selected", Boolean.FALSE);
            unselectTabTitle(cc);
        }

        c.putClientProperty("selected", Boolean.TRUE);
        selectTabTitle(c);
    }

    private void selectTabTitle(JComponent c) {
        c.setBorder(selectedTabBorder);
        c.setBackground(selectedBg);
        c.revalidate();
        c.repaint();
    }

    private void unselectTabTitle(JComponent c) {
        c.setBorder(unselectedTabBorder);
        c.setBackground(unselectedBg);
        c.revalidate();
        c.repaint();
    }

    @Deprecated
    @Override
    public Component add(Component comp) {
        // TODO Auto-generated method stub
        return super.add(comp);
    }
}
