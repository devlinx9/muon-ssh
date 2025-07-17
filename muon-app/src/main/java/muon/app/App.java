package muon.app;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.common.AppContext;
import muon.app.common.settings.Settings;
import muon.app.common.settings.SettingsManager;
import muon.app.ssh.GraphicalHostKeyVerifier;
import muon.app.ui.AppWindow;
import muon.app.ui.components.session.ExternalEditorHandler;
import muon.app.ui.components.session.ISessionContentPanel;
import muon.app.ui.components.session.SessionExportImport;
import muon.app.ui.components.session.files.transfer.BackgroundFileTransfer;
import muon.app.ui.components.settings.SettingsPageName;
import muon.app.util.PlatformUtils;
import muon.app.util.enums.ConflictAction;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import static muon.app.util.Constants.DEFAULT_CONFIG_DIR;

@Slf4j
public final class App {

    private static final AppContext CONTEXT = new AppContext();

    @Getter
    private static ExternalEditorHandler externalEditorHandler;
    private static AppWindow mw;

    static {
        System.setProperty("java.net.useSystemProxies", "true");
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {

        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("networkaddress.cache.ttl", "1");
        Security.setProperty("networkaddress.cache.negative.ttl", "1");
        Security.setProperty("crypto.policy", "unlimited");

        log.info(System.getProperty("java.version"));

        boolean firstRun = false;

        // Checks if the parameter muonPath is set in the startup
        String muonPath = System.getProperty("muonPath");
        boolean isMuonPath = false;
        if (muonPath != null && !muonPath.isEmpty()) {
            log.info("Muon path: {}", muonPath);
            isMuonPath = true;
        } else {
            muonPath = DEFAULT_CONFIG_DIR;
        }

        File appDir = new File(muonPath);
        if (!appDir.exists()) {
            // Validate if the config directory can be created
            if (!appDir.mkdirs()) {
                log.error("The config directory for moun cannot be created: {}", muonPath);
                System.exit(1);
            }
            firstRun = true;
        }

        CONTEXT.setConfigDir(appDir);
        CONTEXT.setSettingsManager(new SettingsManager(appDir));
        CONTEXT.setSettings(CONTEXT.getSettingsManager().loadSettings());

        if (firstRun && !isMuonPath) {
            SessionExportImport.importOnFirstRun();
        }

        if (CONTEXT.getSettings().isManualScaling()) {
            System.setProperty("sun.java2d.uiScale.enabled", "true");
            System.setProperty("sun.java2d.uiScale", String.format("%.2f", CONTEXT.getSettings().getUiScaling()));
        }

        if (CONTEXT.getSettings().getEditors().isEmpty()) {
            log.info("Searching for known editors...");
            CONTEXT.getSettings().setEditors(PlatformUtils.getKnownEditors());
            CONTEXT.getSettingsManager().saveSettings();
            log.info("Searching for known editors...done");
        }

        setKnownHostFile();

        CONTEXT.setBundleLanguage();
        ConflictAction.update();

        UIManager.setLookAndFeel(CONTEXT.updateSkin().getLaf());

        validateMaxKeySize();

        mw = new AppWindow();
        externalEditorHandler = new ExternalEditorHandler(mw);
        SwingUtilities.invokeLater(() -> mw.setVisible(true));

        if (App.getGlobalSettings().isStartWithTerminal()) {
            mw.createLocalSessionPanel();
        } else {
            mw.createFirstSessionPanel();
        }
    }

    private static void setKnownHostFile() {
        try {
            File knownHostFile = new File(App.getCONTEXT().getConfigDir(), "known_hosts");
            CONTEXT.setHostKeyVerifier(new GraphicalHostKeyVerifier(knownHostFile));
        } catch (IOException e2) {
            log.error(e2.getMessage(), e2);
        }
    }

    private static void validateMaxKeySize() {
        try {
            int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
            log.info("maxKeySize: {}", maxKeySize);
            if (maxKeySize < Integer.MAX_VALUE) {
                JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("unlimited_cryptography"));
            }
        } catch (NoSuchAlgorithmException e1) {
            log.error(e1.getMessage(), e1);
        }
    }

    public static synchronized Settings getGlobalSettings() {
        return CONTEXT.getSettings();
    }

    public static ISessionContentPanel getSessionContainer(int activeSessionId) {
        return mw.getSessionListPanel().getSessionContainer(activeSessionId);
    }

    public static synchronized void addUpload(BackgroundFileTransfer transfer) {
        mw.addUpload(transfer);
    }

    public static synchronized AppContext getCONTEXT() {
        return CONTEXT;
    }

    public static synchronized void addDownload(BackgroundFileTransfer transfer) {
        mw.addDownload(transfer);
    }

    public static synchronized void removePendingTransfers(int sessionId) {
        mw.removePendingTransfers(sessionId);
    }

    public static synchronized void openSettings(SettingsPageName page) {
        mw.openSettings(page);
    }

    public static synchronized AppWindow getAppWindow() {
        return mw;
    }

}
