package muon.app;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import util.Constants;
import util.Language;
import util.PlatformUtils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.apache.commons.io.FileUtils.copyDirectory;
import static util.Constants.*;

/**
 * Hello world!
 */
public class App {

    private static final Logger LOG = Logger.getLogger(App.class);

    public static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    public static final SnippetManager SNIPPET_MANAGER = new SnippetManager();
    public static final boolean IS_MAC = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH)
            .startsWith("mac");
    public static final boolean IS_WINDOWS = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH)
            .contains("windows");
    public static final String APP_INSTANCE_ID = UUID.randomUUID().toString();
    public static GraphicalHostKeyVerifier hostKeyVerifier;
    public static ResourceBundle bundle;
    public static AppSkin skin;
    private static Settings settings;
    private static InputBlocker inputBlocker;
    private static ExternalEditorHandler externalEditorHandler;
    private static AppWindow mw;
    private static Map<String, List<String>> pinnedLogs = new HashMap<>();

    static {
        System.setProperty("java.net.useSystemProxies", "true");
    }

    public static void main(String[] args) throws UnsupportedLookAndFeelException {
        LOG.setLevel(Level.INFO);
        LOG.debug("Java version : ".concat(System.getProperty("java.version")));

        if (Boolean.parseBoolean(System.getProperty("debugMuon"))) {
            LOG.setLevel(Level.DEBUG);
        }

        Security.addProvider(new BouncyCastleProvider());
        Security.setProperty("networkaddress.cache.ttl", "1");
        Security.setProperty("networkaddress.cache.negative.ttl", "1");
        Security.setProperty("crypto.policy", "unlimited");

        boolean importOnFirstRun = validateCustomMuonPath() || !validateConfigPath();

        setBundleLanguage();
        loadSettings();

        if (importOnFirstRun) {
            SessionExportImport.importOnFirstRun();
        
        /*File appDir = new File(CONFIG_DIR);
       if (!appDir.exists()) {
            //Validate if the config directory can be created
            if(!appDir.mkdirs()){
                System.err.println("The config directory for muon cannot be created: "+ CONFIG_DIR);
                System.exit(1);
            }
            firstRun = true;
            */
        }

        if (settings.isManualScaling()) {
            System.setProperty("sun.java2d.uiScale.enabled", "true");
            System.setProperty("sun.java2d.uiScale", String.format("%.2f", settings.getUiScaling()));
        }

        if (settings.getEditors().isEmpty()) {
            LOG.info("Searching for known editors...");
            settings.setEditors(PlatformUtils.getKnownEditors());
            saveSettings();
            LOG.info("Searching for known editors...done");
        }

        setBundleLanguage();
        Constants.TransferMode.update();
        Constants.ConflictAction.update();

        skin = settings.isUseGlobalDarkTheme() ? new AppSkinDark() : new AppSkinLight();

        UIManager.setLookAndFeel(skin.getLaf());

        validateMaxKeySize();

        // JediTerm seems to take a long time to load, this might make UI more
        // responsive
        App.EXECUTOR.submit(() -> {
            try {
                Class.forName("com.jediterm.terminal.ui.JediTermWidget");
            } catch (ClassNotFoundException e) {
                LOG.error(e.getMessage(), e);
            }
        });

        mw = new AppWindow();
        inputBlocker = new GraphicalInputBlocker(mw);
        externalEditorHandler = new ExternalEditorHandler(mw);
        SwingUtilities.invokeLater(() -> mw.setVisible(true));

        try {
            File knownHostFile = new File(configDir, "known_hosts");
            hostKeyVerifier = new GraphicalHostKeyVerifier(knownHostFile);
        } catch (Exception e2) {
            LOG.error(e2.getMessage(), e2);
        }

        mw.createFirstSessionPanel();
    }

    private static boolean validateConfigPath() {
        File appDir = new File(configDir);
        File oldAppDir = new File(oldConfigDir);
        if (!appDir.exists()) {
            //Validate if the config directory can be created
            if (!appDir.mkdirs()) {
                LOG.error("The config directory for moun cannot be created: " + configDir);
                System.exit(1);
            }

            if (!oldAppDir.exists()) {
                return true;
            }

            try {
                copyDirectory(oldAppDir, appDir);
            } catch (IOException e) {
                LOG.error("The copy to the new directory failed: " + oldConfigDir, e);
                System.exit(1);
            }
        }
        return false;
    }

    private static void validateMaxKeySize() {
        try {
            int maxKeySize = javax.crypto.Cipher.getMaxAllowedKeyLength("AES");
            LOG.info("maxKeySize: " + maxKeySize);
            if (maxKeySize < Integer.MAX_VALUE) {
                JOptionPane.showMessageDialog(null, App.bundle.getString("unlimited cryptography"));
            }
        } catch (NoSuchAlgorithmException e1) {
            LOG.error(e1.getMessage(), e1);
        }
    }

    private static boolean validateCustomMuonPath() {
        //Checks if the parameter muonPath is set in the startup
        String muonPath = System.getProperty("muonPath");
        boolean isMuonPath = false;
        if (muonPath != null && !muonPath.isEmpty()) {
            LOG.info("Muon path: " + muonPath);
            configDir = muonPath;
            //Validate if the config directory can be created
            if (!Paths.get(muonPath).toFile().exists()) {
                LOG.error("The config directory for moun doesn't exists: " + configDir);
                System.exit(1);
            }
            isMuonPath = true;
        }
        return isMuonPath;
    }

    public static synchronized void loadSettings() {
        File file = new File(configDir, CONFIG_DB_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (file.exists()) {
            try {
                settings = objectMapper.readValue(file, new TypeReference<>() {
                });
                return;
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        settings = new Settings();
    }

    public static synchronized Settings loadSettings2() {
        File file = new File(configDir, CONFIG_DB_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (file.exists()) {
            try {
                settings = objectMapper.readValue(file, new TypeReference<>() {
                });
                return settings;
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        settings = new Settings();
        return settings;
    }

    public static synchronized void saveSettings() {
        File file = new File(configDir, CONFIG_DB_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, settings);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public static synchronized Settings getGlobalSettings() {
        return settings;
    }

    /**
     * @return the inputBlocker
     */
    public static InputBlocker getInputBlocker() {
        return inputBlocker;
    }

    /**
     * @return the externalEditorHandler
     */
    public static ExternalEditorHandler getExternalEditorHandler() {
        return externalEditorHandler;
    }

    public static SessionContentPanel getSessionContainer(int activeSessionId) {
        return mw.getSessionListPanel().getSessionContainer(activeSessionId);
    }

    /**
     * @return the pinnedLogs
     */
    public static Map<String, List<String>> getPinnedLogs() {
        return pinnedLogs;
    }

    public static synchronized void loadPinnedLogs() {
        File file = new File(configDir, PINNED_LOGS);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (file.exists()) {
            try {
                pinnedLogs = objectMapper.readValue(file, new TypeReference<>() {
                });
                return;
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        pinnedLogs = new HashMap<>();
    }

    public static synchronized void savePinnedLogs() {
        File file = new File(configDir, PINNED_LOGS);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, pinnedLogs);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
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

    }
}
