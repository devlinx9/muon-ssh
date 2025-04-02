package muon.app.ui.components.common;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.util.FontAwesomeContants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.Serializable;
import java.util.function.Consumer;

import static muon.app.util.Constants.MEDIUM_TEXT_SIZE;

@Slf4j
public class ClosableTabbedPanel extends JPanel {
    private final Color unselectedBg = App.getCONTEXT().getSkin().getSelectedTabColor();
    private final Color selectedBg = App.getCONTEXT().getSkin().getDefaultBackground();
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final JPanel tabHolder;

    @Getter
    private final JPanel buttonsBox;

    /**
     * Create a tabbed pane with closable tabs
     *
     * @param newTabCallback Called whenever new tab button is clicked
     */
    public ClosableTabbedPanel(final Consumer<JButton> newTabCallback) {
        super(new BorderLayout(0, 0), true);
        setOpaque(true);
        tabHolder = new JPanel(new GridLayout(1, 0, 0, 0));
        tabHolder.setOpaque(true);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setOpaque(false);

        JPanel tabTop = new JPanel(new BorderLayout(3, 3));
        tabTop.setOpaque(true);
        tabTop.add(tabHolder);

        JButton btn = new JButton();
        btn.setToolTipText("New tab");
        btn.setFont(App.getCONTEXT().getSkin().getIconFont().deriveFont(MEDIUM_TEXT_SIZE));
        btn.setText(FontAwesomeContants.FA_PLUS_SQUARE);
        btn.putClientProperty("Nimbus.Overrides",
                              App.getCONTEXT().getSkin().createTabButtonSkin());
        btn.setForeground(App.getCONTEXT().getSkin().getInfoTextForeground());
        btn.addActionListener(e -> {
            log.debug("Callback called");
            newTabCallback.accept(btn);
        });
        buttonsBox = new JPanel(new GridLayout(1, 0));
        buttonsBox.setOpaque(true);
        buttonsBox.setBackground(App.getCONTEXT().getSkin().getDefaultBackground());
        buttonsBox.setBorder(new EmptyBorder(0, 0, 0, 0));
        buttonsBox.add(btn);
        tabTop.add(buttonsBox, BorderLayout.EAST);

        add(tabTop, BorderLayout.NORTH);
        add(cardPanel);
    }

    public void addTab(TabTitle tabTitle, Component body) {
        int index = tabHolder.getComponentCount();
        cardPanel.add(body, body.hashCode() + "");

        TabTitleComponent titleComponent = new TabTitleComponent(
                body instanceof ClosableTabContent);
        tabTitle.setCallback(titleComponent.titleLabel::setText);
        titleComponent.setName(body.hashCode() + "");
        titleComponent.component = body;

        tabHolder.add(titleComponent);

        MouseAdapter mouseAdapter = getMouseAdapter(titleComponent);

        MouseAdapter mouseAdapter2 = getMouseAdapter2(body, titleComponent);

        titleComponent.addMouseListener(mouseAdapter);
        titleComponent.titleLabel.addMouseListener(mouseAdapter);
        if (titleComponent.tabCloseButton != null) {
            titleComponent.tabCloseButton.addMouseListener(mouseAdapter2);
        }

        setSelectedIndex(index);
    }

    private MouseAdapter getMouseAdapter2(Component body, TabTitleComponent titleComponent) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (body instanceof ClosableTabContent) {
                    ClosableTabContent closableTabContent = (ClosableTabContent) body;
                    if (!App.getGlobalSettings().isConfirmBeforeTerminalClosing()
                        || JOptionPane.showConfirmDialog(App.getAppWindow(), App.getCONTEXT()
                            .getBundle()
                            .getString("disconnect_session")) == JOptionPane.YES_OPTION) {
                        if (closableTabContent.close()) {
                            log.debug("Closing...");
                            for (int i = 0; i < tabHolder.getComponentCount(); i++) {
                                JComponent c = (JComponent) tabHolder.getComponent(i);
                                if (c == titleComponent) {
                                    removeTabAt(i, titleComponent);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };
    }

    private MouseAdapter getMouseAdapter(TabTitleComponent titleComponent) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setMouseEventForPrincipalButton(e, titleComponent);
                setMouseEventForSecondaryButton(e, titleComponent);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (titleComponent.tabCloseButton != null) {
                    titleComponent.tabCloseButton.setHovering(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (titleComponent.tabCloseButton != null) {
                    titleComponent.tabCloseButton.setHovering(false);
                }
            }
        };
    }

    private void setMouseEventForPrincipalButton(MouseEvent e, TabTitleComponent titleComponent) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            for (int i = 0; i < tabHolder.getComponentCount(); i++) {
                JComponent c = (JComponent) tabHolder.getComponent(i);
                if (c == titleComponent) {
                    setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void setMouseEventForSecondaryButton(MouseEvent e, TabTitleComponent titleComponent) {
        // Check for the secondary (context) mouse button
        if (e.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu contextMenu = new JPopupMenu();
            JMenuItem changeNameItem = new JMenuItem("Change Name");

            changeNameItem.addActionListener(event -> {
                String newName = JOptionPane.showInputDialog(App.getAppWindow(), "Enter new name:");
                if (newName != null && !newName.trim().isEmpty()) {
                    titleComponent.titleLabel.setText(newName);
                    titleComponent.revalidate();
                    titleComponent.repaint();
                    log.debug("Changing the tab name to {}: ", newName);
                }
            });

            contextMenu.add(changeNameItem);
            contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    public int getSelectedIndex() {
        for (int i = 0; i < tabHolder.getComponentCount(); i++) {
            if (tabHolder.getComponent(i) instanceof TabTitleComponent) {
                TabTitleComponent c = (TabTitleComponent) tabHolder
                        .getComponent(i);
                if (c.selected) {
                    return i;
                }
            }
        }
        return -1;
    }

    public void setSelectedIndex(int n) {
        JComponent c = (JComponent) tabHolder.getComponent(n);
        if (c instanceof TabTitleComponent) {
            String id = c.getName();
            cardLayout.show(cardPanel, id);
            for (int i = 0; i < tabHolder.getComponentCount(); i++) {
                JComponent cc = (JComponent) tabHolder.getComponent(i);
                if (cc instanceof TabTitleComponent) {
                    ((TabTitleComponent) cc).selected = false;
                    unselectTabTitle((TabTitleComponent) cc);
                }
            }
            JComponent cc = (JComponent) tabHolder.getComponent(n);
            if (cc instanceof TabTitleComponent) {
                ((TabTitleComponent) cc).selected = true;
                selectTabTitle((TabTitleComponent) cc);
            }
        }
    }

    private void selectTabTitle(TabTitleComponent c) {
        c.setBackground(selectedBg);
        if (c.tabCloseButton != null) {
            c.tabCloseButton.setSelected(true);
        }
        c.revalidate();
        c.repaint();
    }

    private void unselectTabTitle(TabTitleComponent c) {
        c.setBackground(unselectedBg);
        if (c.tabCloseButton != null) {
            c.tabCloseButton.setSelected(false);
        }
        c.revalidate();
        c.repaint();
    }

    private void removeTabAt(int index, TabTitleComponent title) {
        tabHolder.remove(title);
        cardPanel.remove(title.component);
        if (index > 0) {
            setSelectedIndex(index - 1);
        } else if (cardPanel.getComponentCount() > index) {
            setSelectedIndex(index);
        }
        tabHolder.revalidate();
        tabHolder.repaint();
    }

    public Component getSelectedContent() {
        for (int i = 0; i < tabHolder.getComponentCount(); i++) {
            if (tabHolder.getComponent(i) instanceof TabTitleComponent) {
                TabTitleComponent c = (TabTitleComponent) tabHolder
                        .getComponent(i);
                if (c.selected) {
                    return c.component;
                }
            }
        }
        return null;
    }

    public Component[] getTabContents() {
        return cardPanel.getComponents();
    }


    @Setter
    @Getter
    public static class TabTitle implements Serializable {
        private Consumer<String> callback;
    }

    private class TabTitleComponent extends JPanel {
        JLabel titleLabel;
        TabCloseButton tabCloseButton;
        boolean selected;
        Component component;


        public TabTitleComponent(boolean closable) {
            super(new BorderLayout());
            setBorder(
                    new CompoundBorder(new MatteBorder(0, 0, 0, 1, selectedBg),
                                       new EmptyBorder(5, 10, 5, 5)));
            setBackground(unselectedBg);
            setOpaque(true);
            titleLabel = new JLabel();
            titleLabel.setHorizontalAlignment(JLabel.CENTER);
            this.add(titleLabel);

            if (closable) {
                tabCloseButton = new TabCloseButton();
                tabCloseButton.setForeground(App.getCONTEXT().getSkin().getInfoTextForeground());
                this.add(tabCloseButton, BorderLayout.EAST);
            }
        }
    }
}
