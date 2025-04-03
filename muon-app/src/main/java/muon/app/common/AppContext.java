package muon.app.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import muon.app.common.settings.Settings;
import muon.app.common.settings.SettingsManager;
import muon.app.ssh.GraphicalHostKeyVerifier;
import muon.app.ui.laf.AppSkin;
import muon.app.ui.laf.AppSkinDark;
import muon.app.ui.laf.AppSkinLight;
import muon.app.updater.VersionEntry;
import muon.app.util.enums.Language;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static muon.app.util.Constants.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Slf4j
public class AppContext {

    private VersionEntry version;
    private File configDir;
    private Settings settings;
    private SettingsManager settingsManager;
    private ResourceBundle bundle;
    private AppSkin skin;
    private ExecutorService executor;
    private SnippetManager snippetManager;
    private GraphicalHostKeyVerifier hostKeyVerifier;


    public AppContext() {
        setBundleLanguage();
        this.configDir = new File(DEFAULT_CONFIG_DIR);
        this.version = new VersionEntry("v" + APPLICATION_VERSION);
        this.settingsManager = new SettingsManager(configDir);
        this.snippetManager = new SnippetManager();
        var pExecutor = Executors.newSingleThreadExecutor();
        this.executor = pExecutor;

        pExecutor.submit(() -> {
            try {
                Class.forName("com.jediterm.terminal.ui.JediTermWidget");
            } catch (ClassNotFoundException e) {
                log.error("JediTermWidget not found: {}", e.getMessage(), e);
            }
        });

    }

    //Set the bundle language
    public synchronized void setBundleLanguage() {
        Language language = Language.ENGLISH;
        if (settings != null && settings.getLanguage() != null) {
            language = settings.getLanguage();
        }

        Locale locale = new Locale.Builder().setLanguage(language.getLangAbbr()).build();
        bundle = ResourceBundle.getBundle(PATH_MESSAGES_FILE, locale);
        Locale.setDefault(locale);
    }

    public AppSkin updateSkin() {
        skin = settings.isUseGlobalDarkTheme() ? new AppSkinDark() : new AppSkinLight();
        return skin;
    }

}
