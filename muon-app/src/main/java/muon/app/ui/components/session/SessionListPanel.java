
package muon.app.ui.components.session;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.AppWindow;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.util.FontAwesomeContants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static muon.app.util.Constants.SMALL_TEXT_SIZE;

/**
 * @author subhro
 */
@Slf4j
public class SessionListPanel extends JPanel {
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
    private final DefaultListModel<SessionContentPanel> sessionListModel;
    private final JList<SessionContentPanel> sessionList;
    private final AppWindow window;
    @Getter
    private SessionContentPanel selected;


    public SessionListPanel(AppWindow window) {
        super(new BorderLayout());
        this.window = window;
        sessionListModel = new DefaultListModel<>();
        sessionList = new JList<>(sessionListModel);
        sessionList.setCursor(DEFAULT_CURSOR);

        SessionListRenderer r = new SessionListRenderer();
        sessionList.setCellRenderer(r);

        JScrollPane scrollPane = new SkinnedScrollPane(sessionList);
        this.add(scrollPane);
        setMouseListener();

        setMouseMotionListener();

        setAddListSelectionListener();
    }

    private void setAddListSelectionListener() {
        sessionList.addListSelectionListener(e -> {
            log.debug("called for index: {} {} {}{}", sessionList.getSelectedIndex(), e.getFirstIndex(), e.getLastIndex(), e.getValueIsAdjusting());
            if (!e.getValueIsAdjusting()) {
                int index = sessionList.getSelectedIndex();
                if (index != -1) {
                    this.selectSession(index);
                }
            }
        });
    }

    private void setMouseMotionListener() {
        sessionList.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int index = sessionList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Rectangle r = sessionList.getCellBounds(index, index);
                    if (r != null && r.contains(e.getPoint())) {
                        int x = e.getPoint().x;
                        int y = e.getPoint().y;

                        if (x > r.x + r.width - 30 && x < r.x + r.width && y > r.y + 10 && y < r.y + r.height - 10) {
                            sessionList.setCursor(HAND_CURSOR);
                            return;
                        }
                    }
                }
                sessionList.setCursor(DEFAULT_CURSOR);
            }
        });
    }

    private void setMouseListener() {
        sessionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedIndex = sessionList.getSelectedIndex();
                int index = sessionList.locationToIndex(e.getPoint());
                if (index != -1 && selectedIndex == index) {
                    Rectangle r = sessionList.getCellBounds(index, index);
                    if (r != null && r.contains(e.getPoint())) {
                        int x = e.getPoint().x;
                        int y = e.getPoint().y;

                        if (x > r.x + r.width - 30 && x < r.x + r.width && y > r.y + 10 && y < r.y + r.height - 10) {
                            log.info("Clicked on: {}", index);
                            removeSession(index);
                        }
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                sessionList.setCursor(DEFAULT_CURSOR);
            }
        });
    }

    public void createSession(SessionInfo info) {
        SessionContentPanel panel = new SessionContentPanel(info);
        sessionListModel.insertElementAt(panel, 0);
        sessionList.setSelectedIndex(0);
    }

    public void selectSession(int index) {
        selected = sessionListModel.get(index);
        window.showSession(selected);
        window.revalidate();
        window.repaint();
    }

    public void removeSession(int index) {
        if (JOptionPane.showConfirmDialog(window, App.getCONTEXT().getBundle().getString("disconnect_session")) == JOptionPane.YES_OPTION) {
            SessionContentPanel sessionContentPanel = sessionListModel.get(index);
            sessionContentPanel.close();
            window.removeSession(sessionContentPanel);
            window.revalidate();
            window.repaint();
            sessionListModel.remove(index);
            if (sessionListModel.isEmpty()) {
                return;
            }
            if (index == sessionListModel.size()) {
                sessionList.setSelectedIndex(index - 1);
            } else {
                sessionList.setSelectedIndex(index);
            }
        }
    }

    public SessionContentPanel getSessionContainer(int activeSessionId) {
        for (int i = 0; i < sessionListModel.size(); i++) {
            SessionContentPanel scp = sessionListModel.get(i);
            if (scp.getActiveSessionId() == activeSessionId) {
                return scp;
            }
        }
        return null;
    }

    public static final class SessionListRenderer implements ListCellRenderer<SessionContentPanel> {

        private final JPanel panel;
        private final JLabel lblIcon;
        private final JLabel lblText;
        private final JLabel lblHost;
        private final JLabel lblClose;

        /**
         *
         */
        public SessionListRenderer() {
            lblIcon = new JLabel();
            lblText = new JLabel();
            lblHost = new JLabel();
            lblClose = new JLabel();

            lblIcon.setFont(App.getCONTEXT().getSkin().getIconFont().deriveFont(24.0f));
            lblText.setFont(App.getCONTEXT().getSkin().getDefaultFont().deriveFont(SMALL_TEXT_SIZE));
            lblHost.setFont(App.getCONTEXT().getSkin().getDefaultFont().deriveFont(12.0f));
            lblClose.setFont(App.getCONTEXT().getSkin().getIconFont().deriveFont(SMALL_TEXT_SIZE));

            lblText.setText("Sample server");
            lblHost.setText("server host");
            lblIcon.setText(FontAwesomeContants.FA_CUBE);
            lblClose.setText(FontAwesomeContants.FA_EJECT);

            JPanel textHolder = new JPanel(new BorderLayout(5, 0));
            textHolder.setOpaque(false);
            textHolder.add(lblText);
            textHolder.add(lblHost, BorderLayout.SOUTH);

            panel = new JPanel(new BorderLayout(5, 5));
            panel.add(lblIcon, BorderLayout.WEST);
            panel.add(lblClose, BorderLayout.EAST);
            panel.add(textHolder);

            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            panel.setBackground(App.getCONTEXT().getSkin().getDefaultBackground());
            panel.setOpaque(true);

            Dimension d = panel.getPreferredSize();
            panel.setPreferredSize(d);
            panel.setMaximumSize(d);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends SessionContentPanel> list,
                                                      SessionContentPanel value, int index, boolean isSelected, boolean cellHasFocus) {

            SessionInfo info = value.getInfo();

            lblText.setText(info.getName());
            lblHost.setText(info.getHost());
            lblIcon.setText(FontAwesomeContants.FA_CUBE);
            lblClose.setText(FontAwesomeContants.FA_EJECT);

            lblText.setName("lblText");
            lblHost.setName("lblHost");
            lblIcon.setName("lblIcon");
            lblClose.setName("lblClose");

            boolean isPanelVisible = list.isVisible();
            lblText.setVisible(isPanelVisible);
            lblHost.setVisible(isPanelVisible);

            panel.setBackground(App.getCONTEXT().getSkin().getDefaultBackground());
            lblText.setForeground(App.getCONTEXT().getSkin().getDefaultForeground());
            lblHost.setForeground(App.getCONTEXT().getSkin().getInfoTextForeground());
            lblIcon.setForeground(App.getCONTEXT().getSkin().getDefaultForeground());

            if (isSelected) {
                panel.setBackground(App.getCONTEXT().getSkin().getDefaultSelectionBackground());
                lblText.setForeground(App.getCONTEXT().getSkin().getDefaultSelectionForeground());
                lblHost.setForeground(App.getCONTEXT().getSkin().getDefaultSelectionForeground());
                lblIcon.setForeground(App.getCONTEXT().getSkin().getDefaultSelectionForeground());
            }

            return panel;
        }

    }
}
