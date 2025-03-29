
package muon.app.util;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.ShellAPI;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.win32.StdCallLibrary;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.settings.EditorEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static java.util.Map.entry;

/**
 * @author subhro
 */
@Slf4j
public class PlatformUtils {
    public static final String OS_NAME = "os.name";
    public static final boolean IS_MAC = System.getProperty(OS_NAME, "").toLowerCase(Locale.ENGLISH)
            .startsWith("mac");
    public static final boolean IS_WINDOWS = System.getProperty(OS_NAME, "").toLowerCase(Locale.ENGLISH)
            .contains("windows");
    public static final boolean IS_LINUX = System.getProperty(OS_NAME, "").toLowerCase(Locale.ENGLISH)
            .contains("linux");
    public static final String VISUAL_STUDIO_CODE = "Visual Studio Code";

    private static final Map<String, List<String>> KNOWN_LINUX_EDITORS = Map.ofEntries(
            entry(VISUAL_STUDIO_CODE,
                  List.of("/usr/bin/code", "/usr/local/bin/code", "/snap/bin/code", "/flatpak/bin/code")),
            entry("Atom",
                  List.of("/usr/bin/atom", "/usr/local/bin/atom", "/snap/bin/atom")),
            entry("Sublime Text",
                  List.of("/usr/bin/subl", "/usr/local/bin/subl", "/snap/bin/subl")),
            entry("Gedit",
                  List.of("/usr/bin/gedit")),
            entry("Kate",
                  List.of("/usr/bin/kate")));

    // Immutable Map of Editors to Potential Paths
    private static final Map<String, List<String>> KNOWN_MAC_EDITORS = Map.ofEntries(
            entry(VISUAL_STUDIO_CODE, List.of(
                    "/Applications/Visual Studio Code.app/Contents/Resources/app/bin/code",
                    "/usr/local/bin/code",
                    "/usr/bin/code")),
            entry("Sublime Text", List.of(
                    "/Applications/Sublime Text.app/Contents/SharedSupport/bin/subl",
                    "/usr/local/bin/subl",
                    "/usr/bin/subl")),
            entry("Atom", List.of(
                    "/Applications/Atom.app/Contents/MacOS/Atom",
                    "/usr/local/bin/atom",
                    "/usr/bin/atom")),
            entry("IntelliJ IDEA", List.of(
                    "/Applications/IntelliJ IDEA.app/Contents/MacOS/idea",
                    "/usr/local/bin/idea")));

    public static void openWithDefaultApp(File file, boolean openWith) throws IOException {
        if (IS_MAC) {
            openMac(file);
        } else if (IS_LINUX) {
            openLinux(file);
        } else if (IS_WINDOWS) {
            openWin(file, openWith);
        } else {
            throw new IOException("Unsupported OS: '" + System.getProperty(OS_NAME, "") + "'");
        }
    }

    public static void openWithApp(File f, String app) throws Exception {
        new ProcessBuilder(app, f.getAbsolutePath()).start();
    }

    public static void openWin(File f, boolean openWith) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException();
        }

        if (openWith) {
            try {
                log.info("Opening with rulldll");
                ProcessBuilder builder = new ProcessBuilder();
                builder.command(Arrays.asList("rundll32", "shell32.dll,OpenAs_RunDLL", f.getAbsolutePath()));
                builder.start();
                return;
            } catch (IOException e1) {
                log.error(e1.getMessage(), e1);
            }

        }

        try {
            Shell32 instance = Native.load("shell32", Shell32.class);
            WinDef.HWND h = null;
            WString file = new WString(f.getAbsolutePath());
            instance.shellExecuteW(h, new WString("open"), file, null, null, 1);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                ProcessBuilder builder = new ProcessBuilder();
                builder.command(Arrays.asList("rundll32", "url.dll,FileProtocolHandler", f.getAbsolutePath()));
                builder.start();
            } catch (IOException e1) {
                log.error(e1.getMessage(), e1);
            }
        }

    }

    public static void openLinux(final File f) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException();
        }
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("xdg-open", f.getAbsolutePath());
            pb.start();
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
    }

    public static void openMac(final File f) throws FileNotFoundException {
        if (!f.exists()) {
            throw new FileNotFoundException();
        }
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("open", f.getAbsolutePath());
            if (pb.start().waitFor() != 0) {
                throw new FileNotFoundException();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * @param folder folder to open in explorer
     */
    public static void openFolderInFileBrowser(String folder) throws FileNotFoundException {
        try {
            ProcessBuilder builder = new ProcessBuilder();
            if (PlatformUtils.IS_WINDOWS) {
                // Windows
                builder.command(Arrays.asList("explorer", "/select,", folder));
            } else {
                // Linux or Mac
                builder.command(Arrays.asList("xdg-open", folder));
            }
            builder.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static List<EditorEntry> getKnownEditors() {
        List<EditorEntry> list = new ArrayList<>();
        if (IS_WINDOWS) {
            setWindowsEditors(list);
        } else if (IS_MAC) {
            populateEditors(list, KNOWN_MAC_EDITORS);
        } else {
            populateEditors(list, KNOWN_LINUX_EDITORS);
        }
        return list;
    }

    private static void setWindowsEditors(List<EditorEntry> list) {
        String vscode = detectVSCode(false);
        if (vscode != null) {
            EditorEntry ent = new EditorEntry(VISUAL_STUDIO_CODE, vscode);
            list.add(ent);
        } else {
            vscode = detectVSCode(true);
            if (vscode != null) {
                EditorEntry ent = new EditorEntry(VISUAL_STUDIO_CODE, vscode);
                list.add(ent);
            }
        }


        String npp = RegUtil.regGetStr(WinReg.HKEY_LOCAL_MACHINE,
                                       "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Notepad++", "DisplayIcon");
        if (npp != null) {
            EditorEntry ent = new EditorEntry("Notepad++", npp);
            list.add(ent);
        }

        String atom = RegUtil.regGetStr(WinReg.HKEY_CURRENT_USER,
                                        "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\atom", "InstallLocation");
        if (atom != null) {
            EditorEntry ent = new EditorEntry("Atom", atom + "\\atom.exe");
            list.add(ent);
            return;
        }

        atom = RegUtil.regGetStr(WinReg.HKEY_LOCAL_MACHINE,
                                 "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\atom", "InstallLocation");
        if (atom != null) {
            EditorEntry ent = new EditorEntry("Atom", atom + "\\atom.exe");
            list.add(ent);
        }
    }


    private static void populateEditors(List<EditorEntry> list, Map<String, List<String>> knownLinuxEditors) {
        for (Map.Entry<String, List<String>> knownEditor : knownLinuxEditors.entrySet()) {
            for (String path : knownEditor.getValue()) {
                File file = new File(path);
                if (file.exists()) {
                    list.add(new EditorEntry(knownEditor.getKey(), file.getAbsolutePath()));
                    break;
                }
            }
        }
    }

    private static String detectVSCode(boolean hklm) {
        try {
            HKEY hkey = hklm ? WinReg.HKEY_LOCAL_MACHINE : WinReg.HKEY_CURRENT_USER;
            String[] keys = Advapi32Util.registryGetKeys(hkey, "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall");
            for (String key : keys) {
                Map<String, Object> values = Advapi32Util.registryGetValues(hkey,
                                                                            "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\" + key);
                if (values.containsKey("DisplayName")) {
                    String text = (values.get("DisplayName") + "").toLowerCase(Locale.ENGLISH);
                    if (text.contains("visual studio code")) {
                        return values.get("DisplayIcon") + "";
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public interface Shell32 extends ShellAPI, StdCallLibrary {
        WinDef.HINSTANCE shellExecuteW(WinDef.HWND hwnd, WString lpOperation, WString lpFile, WString lpParameters,
                                       WString lpDirectory, int nShowCmd);
    }

    public static String getStringForOpenInFileBrowser() {
        if (IS_WINDOWS) {
            return App.getCONTEXT().getBundle().getString("open_in_file_browser_win");
        } else {
            return IS_MAC ? App.getCONTEXT().getBundle().getString("open_in_file_browser_mac") : App.getCONTEXT().getBundle().getString("open_in_file_browser_nix");
        }
    }

}
