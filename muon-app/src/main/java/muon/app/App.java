package muon.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.common.Settings;
import muon.app.common.SnippetManager;
import muon.app.ssh.GraphicalHostKeyVerifier;
import muon.app.ssh.GraphicalInputBlocker;
import muon.app.ssh.InputBlocker;
import muon.app.ui.AppWindow;
import muon.app.ui.components.session.ExternalEditorHandler;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionExportImport;
import muon.app.ui.components.session.files.transfer.BackgroundFileTransfer;
import muon.app.ui.components.settings.SettingsPageName;
import muon.app.ui.laf.AppSkin;
import muon.app.ui.laf.AppSkinDark;
import muon.app.ui.laf.AppSkinLight;
import muon.app.updater.VersionEntry;
import muon.app.util.Constants;
import muon.app.util.PlatformUtils;
import muon.app.util.enums.ConflictAction;
import muon.app.util.enums.Language;
import muon.app.util.enums.TransferMode;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static muon.app.util.Constants.APPLICATION_VERSION;
import static muon.app.util.Constants.UPDATE_URL;

@Slf4j
public class App {
    public static final VersionEntry VERSION = new VersionEntry("v" + APPLICATION_VERSION);

    public static String CONFIG_DIR = System.getProperty("user.home") + File.separatorChar + "muon-ssh";
    private static final String PATH_MESSAGES_FILE = "i18n/messages";
    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    public static final SnippetManager SNIPPET_MANAGER = new SnippetManager();

    public static GraphicalHostKeyVerifier HOST_KEY_VERIFIER;
    public static ResourceBundle bundle;
    public static AppSkin SKIN;
    private static Settings settings;

    @Getter
    private static InputBlocker inputBlocker;

    @Getter
    private static ExternalEditorHandler externalEditorHandler;
    private static AppWindow mw;

    @Getter
    private static Map<String, List<String>> pinnedLogs = new HashMap<>();

    static {
        System.setProperty("java.net.useSystemProxies", "true");
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {

        setBundleLanguage();

        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("networkaddress.cache.ttl", "1");
        Security.setProperty("networkaddress.cache.negative.ttl", "1");
        Security.setProperty("crypto.policy", "unlimited");

        log.info(System.getProperty("java.version"));

        boolean firstRun = false;

        //Checks if the parameter muonPath is set in the startup
        String muonPath = System.getProperty("muonPath");
        boolean isMuonPath = false;
        if (muonPath != null && !muonPath.isEmpty()) {
            log.info("Muon path: {}", muonPath);
            CONFIG_DIR = muonPath;
            isMuonPath = true;
        }

        File appDir = new File(CONFIG_DIR);
        if (!appDir.exists()) {
            //Validate if the config directory can be created
            if (!appDir.mkdirs()) {
                log.error("The config directory for moun cannot be created: {}", CONFIG_DIR);
                System.exit(1);
            }
            firstRun = true;
        }

        loadSettings();
        if (firstRun && !isMuonPath) {
            SessionExportImport.importOnFirstRun();
        }

        if (settings.isManualScaling()) {
            System.setProperty("sun.java2d.uiScale.enabled", "true");
            System.setProperty("sun.java2d.uiScale", String.format("%.2f", settings.getUiScaling()));
        }

        if (settings.getEditors().isEmpty()) {
            log.info("Searching for known editors...");
            settings.setEditors(PlatformUtils.getKnownEditors());
            saveSettings();
            log.info("Searching for known editors...done");
        }

        setBundleLanguage();
        TransferMode.update();
        ConflictAction.update();


        SKIN = settings.isUseGlobalDarkTheme() ? new AppSkinDark() : new AppSkinLight();

        UIManager.setLookAndFeel(SKIN.getLaf());

        try {
            int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
            log.info("maxKeySize: {}", maxKeySize);
            if (maxKeySize < Integer.MAX_VALUE) {
                JOptionPane.showMessageDialog(null, App.bundle.getString("unlimited_cryptography"));
            }
        } catch (NoSuchAlgorithmException e1) {
            log.error(e1.getMessage(), e1);
        }

        // JediTerm seems to take a long time to load, this might make UI more
        // responsive
        App.EXECUTOR.submit(() -> {
            try {
                Class.forName("com.jediterm.terminal.ui.JediTermWidget");
            } catch (ClassNotFoundException e) {
                log.error(e.getMessage(), e);
            }
        });

        mw = new AppWindow();
        inputBlocker = new GraphicalInputBlocker(mw);
        externalEditorHandler = new ExternalEditorHandler(mw);
        SwingUtilities.invokeLater(() -> mw.setVisible(true));

        try {
            File knownHostFile = new File(App.CONFIG_DIR, "known_hosts");
            HOST_KEY_VERIFIER = new GraphicalHostKeyVerifier(knownHostFile);
        } catch (Exception e2) {
            log.error(e2.getMessage(), e2);
        }

        mw.createFirstSessionPanel();
    }

    public static synchronized Settings loadSettings() {
        File file = new File(CONFIG_DIR, Constants.CONFIG_DB_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (file.exists()) {
            try {
                settings = objectMapper.readValue(file, Settings.class);
                return settings;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        settings = new Settings();
        return settings;
    }

    public static synchronized void saveSettings() {
        File file = new File(CONFIG_DIR, Constants.CONFIG_DB_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, settings);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static synchronized Settings getGlobalSettings() {
        return settings;
    }

    public static SessionContentPanel getSessionContainer(int activeSessionId) {
        return mw.getSessionListPanel().getSessionContainer(activeSessionId);
    }

    public static synchronized void loadPinnedLogs() {
        File file = new File(CONFIG_DIR, Constants.PINNED_LOGS);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (file.exists()) {
            try {
                pinnedLogs = objectMapper.readValue(file, new TypeReference<>() {
                });
                return;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        pinnedLogs = new HashMap<>();
    }

    public static synchronized void savePinnedLogs() {
        File file = new File(CONFIG_DIR, Constants.PINNED_LOGS);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, pinnedLogs);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static synchronized void addUpload(BackgroundFileTransfer transfer) {
        mw.addUpload(transfer);
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

    //Set the bundle language
    private static void setBundleLanguage() {
        Language language = Language.ENGLISH;
        if (settings != null && settings.getLanguage() != null) {
            language = settings.getLanguage();
        }

        Locale locale = new Locale.Builder().setLanguage(language.getLangAbbr()).build();
        bundle = ResourceBundle.getBundle(PATH_MESSAGES_FILE, locale);
        Locale.setDefault(locale);

    }
}
