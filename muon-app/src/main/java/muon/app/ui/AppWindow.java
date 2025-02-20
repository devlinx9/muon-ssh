package muon.app.ui;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.session.dialog.NewSessionDlg;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.SessionListPanel;
import muon.app.ui.components.session.files.transfer.BackgroundFileTransfer;
import muon.app.ui.components.session.files.transfer.BackgroundTransferPanel;
import muon.app.ui.components.settings.SettingsDialog;
import muon.app.ui.components.settings.SettingsPageName;
import muon.app.updater.UpdateChecker;
import util.FontAwesomeContants;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static muon.app.App.bundle;
import static util.Constants.*;

/**
 * @author subhro
 */
@Slf4j
public class AppWindow extends JFrame {
    private final CardLayout sessionCard = new CardLayout();
    private final JPanel cardPanel = new JPanel(sessionCard, true);
    private final BackgroundTransferPanel uploadPanel;
    private final BackgroundTransferPanel downloadPanel;
    private final Component bottomPanel;

    @Getter
    private SessionListPanel sessionListPanel;

    private JLabel lblUploadCount;
    private JLabel lblDownloadCount;
    private JPopupMenu popup;
    private JLabel lblUpdate;
    private JLabel lblUpdateText;

    public AppWindow() {
        super(APPLICATION_NAME);
        setWindowProperties();

        this.cardPanel.setDoubleBuffered(true);

        this.add(createSessionPanel(), BorderLayout.WEST);
        this.add(this.cardPanel);

        this.bottomPanel = createBottomPanel();
        this.add(this.bottomPanel, BorderLayout.SOUTH);

        this.uploadPanel = new BackgroundTransferPanel(count -> SwingUtilities.invokeLater(() -> lblUploadCount.setText(count + "")));
        this.downloadPanel = new BackgroundTransferPanel(count -> SwingUtilities.invokeLater(() -> lblDownloadCount.setText(count + "")));

        checkForUpdates();
    }

    private void setWindowProperties() {
        try {
            this.setIconImage(ImageIO.read(Objects.requireNonNull(AppWindow.class.getResource("/muon.png"))));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setWindowsSizeAndPosition();
    }

    private void checkForUpdates() {
        new Thread(() -> {
            if (UpdateChecker.isNewUpdateAvailable()) {
                lblUpdate.setText(FontAwesomeContants.FA_DOWNLOAD);
                lblUpdate.setVisible(true);
                lblUpdateText.setText("Update available");
                lblUpdateText.setVisible(true);

            }
        }).start();
    }

    private void setWindowsSizeAndPosition() {
        Insets inset = Toolkit.getDefaultToolkit().getScreenInsets(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());

        Dimension screenD = Toolkit.getDefaultToolkit().getScreenSize();

        int screenWidth = screenD.width - inset.left - inset.right;
        int screenHeight = screenD.height - inset.top - inset.bottom;

        if (screenWidth < 1024 || screenHeight < 650 || App.getGlobalSettings().isStartMaximized()) {
            setSize(screenWidth, screenHeight);
        } else {
            int width = (screenWidth * 80) / 100;
            int height = (screenHeight * 80) / 100;
            setSize(width, height);
        }

        this.setLocationRelativeTo(null);
    }

    public void createFirstSessionPanel() {
        SessionInfo info = new NewSessionDlg(this).newSession();
        if (info != null) {
            sessionListPanel.createSession(info);
        }
    }

    private JPanel createSessionPanel() {
        JButton btnNew = new JButton(FontAwesomeContants.FA_TELEVISION);
        btnNew.setFont(App.SKIN.getIconFont().deriveFont(14.0f));
        btnNew.addActionListener(e -> this.createFirstSessionPanel());
        btnNew.setToolTipText(bundle.getString("new_connection"));

        JButton btnToggle = new JButton(FontAwesomeContants.FA_ANGLE_DOUBLE_LEFT);
        btnToggle.setFont(App.SKIN.getIconFont().deriveFont(14.0f));

        JPanel topBox = new JPanel();
        topBox.setLayout(new BoxLayout(topBox, BoxLayout.X_AXIS));
        topBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        topBox.add(btnToggle);
        topBox.add(Box.createRigidArea(new Dimension(10, 10)));
        topBox.add(btnNew);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new MatteBorder(0, 0, 0, 1, App.SKIN.getDefaultBorderColor()));

        sessionListPanel = new SessionListPanel(this);
        panel.add(topBox, BorderLayout.NORTH);
        panel.add(sessionListPanel, BorderLayout.CENTER);

        btnToggle.addActionListener(e -> {
            boolean isVisible = btnNew.isVisible();
            sessionListPanel.setVisible(!isVisible);
            btnNew.setVisible(!isVisible);
            topBox.setBorder(null);

            if (!isVisible) {
                topBox.setBorder(new EmptyBorder(10, 10, 10, 10));

            }

            btnToggle.setText(isVisible ? FontAwesomeContants.FA_ANGLE_DOUBLE_RIGHT : FontAwesomeContants.FA_ANGLE_DOUBLE_LEFT);

            topBox.revalidate();
            topBox.repaint();

        });


        return panel;
    }


    /**
     *
     */
    public void showSession(SessionContentPanel sessionContentPanel) {
        cardPanel.add(sessionContentPanel, sessionContentPanel.hashCode() + "");
        sessionCard.show(cardPanel, sessionContentPanel.hashCode() + "");
        revalidate();
        repaint();
    }

    /**
     *
     */
    public void removeSession(SessionContentPanel sessionContentPanel) {
        cardPanel.remove(sessionContentPanel);
        revalidate();
        repaint();
    }

    private Component createBottomPanel() {
        popup = new JPopupMenu();
        popup.setBorder(new LineBorder(App.SKIN.getDefaultBorderColor(), 1));
        popup.setPreferredSize(new Dimension(400, 500));

        Box b1 = Box.createHorizontalBox();
        b1.setOpaque(true);
        b1.setBackground(App.SKIN.getTableBackgroundColor());
        b1.setBorder(new CompoundBorder(new MatteBorder(1, 0, 0, 0, App.SKIN.getDefaultBorderColor()),
                                        new EmptyBorder(5, 5, 5, 5)));
        b1.add(createSpacer(10, 10));
        b1.add(createBrandLabel());
        b1.add(createSpacer(10, 10));

        b1.add(createRepositoryLabel());
        b1.add(Box.createHorizontalGlue());

        JLabel lblUpload = createUploadLabel();
        b1.add(lblUpload);
        b1.add(createSpacer(5, 10));
        createUploadLabelCount(lblUpload);

        b1.add(lblUploadCount);
        b1.add(createSpacer(10, 10));

        JLabel lblDownload = createDownloadLabel();
        b1.add(lblDownload);
        b1.add(createSpacer(5, 10));
        createDownloadCountLabel(lblDownload);
        b1.add(lblDownloadCount);

        b1.add(createSpacer(10, 10));
        createUpdateLabel();
        b1.add(lblUpdate);

        b1.add(createSpacer(5, 5));
        createUpdateTextLabel();
        b1.add(lblUpdateText);

        b1.add(createSpacer(10, 10));
        b1.add(createSettingLabel());

        b1.add(createSpacer(10, 10));
        b1.add(createHelpLabel());

        return b1;
    }

    private JLabel createHelpLabel() {
        JLabel lblHelp = createIconLabel(FontAwesomeContants.FA_QUESTION_CIRCLE);
        lblHelp.addMouseListener(createMouseListener(HELP_URL));
        return lblHelp;
    }

    private JLabel createSettingLabel() {
        JLabel lblSetting;
        lblSetting = createIconLabel(FontAwesomeContants.FA_COG);
        lblSetting.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblSetting.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openSettings(null);
            }
        });
        return lblSetting;
    }

    private void createUpdateTextLabel() {
        lblUpdateText = new JLabel(bundle.getString("chk_update"));
        lblUpdateText.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUpdateText.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUpdateURL();
            }
        });

        lblUpdateText.setVisible(false);
    }

    private void createUpdateLabel() {
        lblUpdate = createIconLabel(FontAwesomeContants.FA_REFRESH);
        lblUpdate.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUpdate.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openUpdateURL();
            }
        });
        lblUpdate.setVisible(false);
    }

    private void createDownloadCountLabel(JLabel lblDownload) {
        lblDownloadCount = new JLabel("0");
        lblDownloadCount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblDownloadCount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPopup(downloadPanel, lblDownload);
            }
        });
    }

    private JLabel createUploadLabel() {
        JLabel lblUpload = createIconLabel(FontAwesomeContants.FA_CLOUD_UPLOAD);
        lblUpload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPopup(uploadPanel, lblUpload);
            }
        });
        return lblUpload;
    }

    private JLabel createDownloadLabel() {
        JLabel lblDownload = createIconLabel(FontAwesomeContants.FA_CLOUD_DOWNLOAD);
        lblDownload.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPopup(downloadPanel, lblDownload);
            }
        });
        return lblDownload;
    }

    private void createUploadLabelCount(JLabel lblUpload) {
        lblUploadCount = new JLabel("0");
        lblUploadCount.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblUploadCount.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showPopup(uploadPanel, lblUpload);
            }
        });
    }

    private Component createSpacer(int width, int height) {
        return Box.createRigidArea(new Dimension(width, height));
    }

    private JLabel createBrandLabel() {
        JLabel lblBrand = new JLabel("Version: " + APPLICATION_VERSION);
        lblBrand.addMouseListener(createMouseListener(REPOSITORY_URL));
        lblBrand.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBrand.setVerticalAlignment(SwingConstants.CENTER);
        return lblBrand;
    }

    private JLabel createRepositoryLabel() {
        JLabel lblUrl = new JLabel(REPOSITORY_URL);
        lblUrl.addMouseListener(createMouseListener(REPOSITORY_URL));
        lblUrl.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return lblUrl;
    }

    private JLabel createIconLabel(String icon) {
        JLabel label = new JLabel(icon);
        label.setFont(App.SKIN.getIconFont().deriveFont((float) 16.0));
        label.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return label;
    }

    private MouseListener createMouseListener(String url) {
        return new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (IOException | URISyntaxException ex) {
                        log.error(ex.getMessage(), ex);
                    }
                }
            }
        };
    }


    protected void openUpdateURL() {
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(new URI(App.UPDATE_URL2));
            } catch (IOException | URISyntaxException ex) {
                log.error(ex.getMessage(), ex);
            }
        }
    }

    public void addUpload(BackgroundFileTransfer transfer) {
        this.uploadPanel.addNewBackgroundTransfer(transfer);
    }

    public void addDownload(BackgroundFileTransfer transfer) {
        this.downloadPanel.addNewBackgroundTransfer(transfer);
    }

    private void showPopup(Component panel, Component invoker) {
        popup.removeAll();
        popup.add(panel);
        popup.setInvoker(bottomPanel);

        popup.show(bottomPanel, bottomPanel.getWidth() - popup.getPreferredSize().width,
                   -popup.getPreferredSize().height);
    }

    public void removePendingTransfers(int sessionId) {
        this.uploadPanel.removePendingTransfers(sessionId);
        this.downloadPanel.removePendingTransfers(sessionId);
    }

    public void openSettings(SettingsPageName page) {
        SettingsDialog settingsDialog = new SettingsDialog(this);
        settingsDialog.showDialog(this, page);
    }
}
