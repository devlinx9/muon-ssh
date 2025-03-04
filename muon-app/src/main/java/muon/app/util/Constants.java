package muon.app.util;

import java.util.UUID;

import static muon.app.App.VERSION;

public class Constants {
    public static final String BASE_URL = "https://github.com/devlinx9";
    public static final String HELP_URL = "https://github.com/devlinx9/muon-ssh/wiki";
    public static final String UPDATE_URL = "https://devlinx9.github.io/muon-ssh";
    public static final String API_UPDATE_URL = "https://api.github.com/repos/devlinx9/muon-ssh/releases/latest";
    public static final String REPOSITORY_URL = BASE_URL + "/muon-ssh";
    public static final String APPLICATION_VERSION = "2.4.0";
    public static final String APPLICATION_NAME = "Muon SSH";
    public static final String SESSION_DB_FILE = "session-store.json";
    public static final String CONFIG_DB_FILE = "settings.json";
    public static final String SNIPPETS_FILE = "snippets.json";
    public static final String PINNED_LOGS = "pinned-logs.json";
    public static final String TRANSFER_HOSTS = "transfer-hosts.json";
    public static final String BOOKMARKS_FILE = "bookmarks.json";

    public static final String UPDATE_URL2 = UPDATE_URL + "/check-update.html?v="
                                             + VERSION.getNumericValue();

    public static final String APP_INSTANCE_ID = UUID.randomUUID().toString();



}
