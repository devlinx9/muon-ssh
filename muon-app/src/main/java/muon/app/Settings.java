package muon.app;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import muon.app.ui.components.settings.DarkTerminalTheme;
import muon.app.ui.components.settings.EditorEntry;
import util.CollectionHelper;
import util.enums.ConflictAction;
import util.enums.Language;
import util.enums.TransferMode;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Settings {
    public static final String COPY_KEY = "Copy";
    public static final String PASTE_KEY = "Paste";
    public static final String CLEAR_BUFFER = "Clear buffer";
    public static final String FIND_KEY = "Find";
    private boolean usingMasterPassword = false;
    private TransferMode fileTransferMode = TransferMode.NORMAL;
    private ConflictAction conflictAction = ConflictAction.AUTORENAME;
    private boolean confirmBeforeDelete = true;
    private boolean startMaximized = true;
    private boolean confirmBeforeMoveOrCopy = false;
    private boolean showHiddenFilesByDefault = false;
    private boolean firstFileBrowserView = false;
    private boolean firstLocalViewInFileBrowser = false;
    private boolean transferTemporaryDirectory = false;
    private boolean openInSecondScreen = false;
    private boolean useSudo = false;
    private boolean promptForSudo = false;
    private boolean directoryCache = true;
    private boolean showPathBar = true;
    private boolean useDarkThemeForTerminal = false;
    private boolean showMessagePrompt = false;
    private boolean useGlobalDarkTheme = true;
    private int connectionTimeout = 60;
    private boolean connectionKeepAlive = false;
    private int logViewerFont = 14;
    private boolean logViewerUseWordWrap = true;
    private int logViewerLinesPerPage = 50;
    private int sysloadRefreshInterval = 3;
    private boolean puttyLikeCopyPaste = false;
    private String terminalType = "ansi";
    private boolean confirmBeforeTerminalClosing = true;
    private int termWidth = 80;
    private int termHeight = 24;
    private boolean terminalBell = false;
    private String terminalFontName = "NotoMono-Regular";
    private int terminalFontSize = 14;
    private Language language = Language.ENGLISH;
    private String terminalTheme = "Dark";
    private String terminalPalette = "xterm";
    private int[] palleteColors = {0x000000, 0xcd0000, 0x00cd00, 0xcdcd00, 0x1e90ff, 0xcd00cd, 0x00cdcd, 0xe5e5e5,
                                   0x4c4c4c, 0xff0000, 0x00ff00, 0xffff00, 0x4682b4, 0xff00ff, 0x00ffff, 0xffffff};
    private int backgroundTransferQueueSize = 2;
    private int defaultColorFg = DarkTerminalTheme.DEF_FG;
    private int defaultColorBg = DarkTerminalTheme.DEF_BG;
    private int defaultSelectionFg = DarkTerminalTheme.SEL_FG;
    private int defaultSelectionBg = DarkTerminalTheme.SEL_BG;
    private int defaultFoundFg = DarkTerminalTheme.FIND_FG;
    private int defaultFoundBg = DarkTerminalTheme.FIND_BG;
    private int defaultHrefFg = DarkTerminalTheme.HREF_FG;
    private int defaultHrefBg = DarkTerminalTheme.HREF_BG;
    private Map<String, Integer> keyCodeMap = new CollectionHelper.OrderedDict<String, Integer>()
            .putItem(COPY_KEY, KeyEvent.VK_C).putItem(PASTE_KEY, KeyEvent.VK_V)
            .putItem(CLEAR_BUFFER, App.IS_MAC ? KeyEvent.VK_K : KeyEvent.VK_L).putItem(FIND_KEY, KeyEvent.VK_F);

    private Map<String, Integer> keyModifierMap = new CollectionHelper.Dict<String, Integer>()
            .putItem(COPY_KEY,
                     App.IS_MAC ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
            .putItem(PASTE_KEY,
                     App.IS_MAC ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)
            .putItem(CLEAR_BUFFER, App.IS_MAC ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK)
            .putItem(FIND_KEY, App.IS_MAC ? InputEvent.META_DOWN_MASK : InputEvent.CTRL_DOWN_MASK);

    private boolean dualPaneMode = true;
    private boolean listViewEnabled = false;

    private int defaultOpenAction = 0
            // 0 Open with default application
            // 1 Open with default editor
            // 2 Open with internal editor
            ;
    private int numberOfSimultaneousConnection = 3;

    private double uiScaling = 1.0;
    private boolean manualScaling = false;

    private List<EditorEntry> editors = new ArrayList<>();

    private String defaultPanel = "FILES";

    @JsonSetter("fileTransferMode")
    public void setOldFileTransferMode(String s) {
        if (s == null) {
            fileTransferMode = TransferMode.NORMAL;
        } else if (s.equalsIgnoreCase("prompt")) {
            fileTransferMode = TransferMode.NORMAL;
        } else {
            fileTransferMode = TransferMode.BACKGROUND;
        }
    }

    @JsonSetter("conflictAction")
    public void setOldConflictAction(String s) {

        switch (s.toLowerCase()) {
            case "overwrite":
                conflictAction = ConflictAction.OVERWRITE;
                break;
            case "autorename":
                conflictAction = ConflictAction.AUTORENAME;
                break;
            case "prompt":
                conflictAction = ConflictAction.PROMPT;
                break;
            default:
                conflictAction = ConflictAction.SKIP;
                break;
        }
    }

}
