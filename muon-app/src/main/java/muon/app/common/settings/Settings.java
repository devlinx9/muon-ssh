package muon.app.common.settings;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;
import muon.app.ui.components.settings.DarkTerminalTheme;
import muon.app.ui.components.settings.EditorEntry;
import muon.app.util.enums.ConflictAction;
import muon.app.util.enums.Language;
import muon.app.util.enums.TransferMode;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
import static muon.app.util.PlatformUtils.IS_MAC;

@Getter
@Setter
public class Settings {
    public static final String COPY_KEY = "Copy";
    public static final String PASTE_KEY = "Paste";
    public static final String CLEAR_BUFFER = "Clear buffer";
    public static final String FIND_KEY = "Find";
    private boolean usingMasterPassword = false;
    private TransferMode fileTransferMode = TransferMode.BACKGROUND;
    private ConflictAction conflictAction = ConflictAction.AUTORENAME;
    private boolean confirmBeforeDelete = true;
    private boolean enabledK8sContextPlugin = false;
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
    private String terminalType = "xterm-256color";
    private boolean confirmBeforeTerminalClosing = true;
    private int termWidth = 80;
    private int termHeight = 24;
    private boolean terminalBell = false;
    private String terminalFontName = "FiraCode-Regular";
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
    private Map<String, Integer> keyCodeMap = new LinkedHashMap<>(
            Map.ofEntries(
                    entry(COPY_KEY, KeyEvent.VK_C),
                    entry(PASTE_KEY, KeyEvent.VK_V),
                    entry(CLEAR_BUFFER, IS_MAC ? KeyEvent.VK_K : KeyEvent.VK_L),
                    entry(FIND_KEY, KeyEvent.VK_F))
    );

    private Map<String, Integer> keyModifierMap = Map.ofEntries(
            entry(COPY_KEY, IS_MAC
                            ? InputEvent.META_DOWN_MASK
                            : (InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)),
            entry(PASTE_KEY, IS_MAC
                             ? InputEvent.META_DOWN_MASK
                             : (InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK)),
            entry(CLEAR_BUFFER, IS_MAC
                                ? InputEvent.META_DOWN_MASK
                                : InputEvent.CTRL_DOWN_MASK),
            entry(FIND_KEY, IS_MAC
                            ? InputEvent.META_DOWN_MASK
                            : InputEvent.CTRL_DOWN_MASK));

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
        fileTransferMode = TransferMode.BACKGROUND;
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
