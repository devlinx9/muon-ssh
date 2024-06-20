package util;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import muon.app.updater.VersionEntry;

import java.io.File;

import static muon.app.App.bundle;

public class Constants {
    public static final String BASE_URL = "https://github.com/achiikun/muon-ssh";//"https://github.com/devlinx9";
    public static final String HELP_URL = "https://github.com/subhra74/snowflake/wiki"; //TODO change wiki pages
    public static final String UPDATE_URL = "https://devlinx9.github.io/muon-ssh";
    public static final String API_UPDATE_URL = "https://api.github.com/repos/devlinx9/muon-ssh/releases/latest";
    public static final String REPOSITORY_URL = BASE_URL + "/muon-ssh";
    public static final String APPLICATION_VERSION = "2.2.0";
    public static final String APPLICATION_NAME = "Muon SSH";

    public static final VersionEntry VERSION = new VersionEntry("v" + APPLICATION_VERSION);
    public static final String UPDATE_URL2 = UPDATE_URL + "/check-update.html?v="
            + VERSION.getNumericValue();
    public static String oldConfigDir = System.getProperty("user.home") + File.separatorChar + "muon-ssh";
    public static String configDir = System.getProperty("user.home") + File.separatorChar + ".muon-ssh";
    public static final String SESSION_DB_FILE = "session-store.json";
    public static final String CONFIG_DB_FILE = "settings.json";
    public static final String SNIPPETS_FILE = "snippets.json";
    public static final String PINNED_LOGS = "pinned-logs.json";
    public static final String TRANSFER_HOSTS = "transfer-hosts.json";
    public static final String BOOKMARKS_FILE = "bookmarks.json";
    public static final String PATH_MESSAGES_FILE= "i18n/messages";

    public enum ConflictAction {


        OVERWRITE(0, bundle.getString("overwrite")), AUTORENAME(1, bundle.getString("autorename")), SKIP(2, bundle.getString("skip")), PROMPT(3, bundle.getString("prompt")), CANCEL(4, bundle.getString("cancel"));
        private final int key;
        private String value;

        ConflictAction(int pKey, String pValue) {
            this.key = pKey;
            this.value = pValue;
        }

        public static void update() {
            OVERWRITE.setValue(bundle.getString("overwrite"));
            AUTORENAME.setValue(bundle.getString("autorename"));
            SKIP.setValue(bundle.getString("skip"));
            PROMPT.setValue(bundle.getString("prompt"));
            CANCEL.setValue(bundle.getString("cancel"));
        }

        public int getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String pValue) {
            this.value = pValue;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public enum TransferMode {

        @JsonEnumDefaultValue NORMAL(0, bundle.getString("transfer_normally")), BACKGROUND(1, bundle.getString("transfer_background"));

        private final int key;
        private String value;

        TransferMode(int pKey, String pValue) {
            this.key = pKey;
            this.value = pValue;
        }

        public static void update() {
            NORMAL.setValue(bundle.getString("transfer_normally"));
            BACKGROUND.setValue(bundle.getString("transfer_background"));
        }

        public int getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String pValue) {
            this.value = pValue;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
