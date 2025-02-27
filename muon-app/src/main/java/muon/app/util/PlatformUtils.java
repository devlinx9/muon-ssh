/**
 *
 */
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
import muon.app.ui.components.settings.EditorEntry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

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
            try {
                String vscode = detectVSCode(false);
                if (vscode != null) {
                    EditorEntry ent = new EditorEntry(VISUAL_STUDIO_CODE, vscode);
                    list.add(ent);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                String vscode = detectVSCode(true);
                if (vscode != null) {
                    EditorEntry ent = new EditorEntry(VISUAL_STUDIO_CODE, vscode);
                    list.add(ent);
                }
            }

            try {
                String npp = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                                                                 "SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\Notepad++", "DisplayIcon");
                EditorEntry ent = new EditorEntry("Notepad++", npp);
                list.add(ent);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            try {
                String atom = Advapi32Util.registryGetStringValue(WinReg.HKEY_CURRENT_USER,
                                                                  "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\atom", "InstallLocation");
                EditorEntry ent = new EditorEntry("Atom", atom + "\\atom.exe");
                list.add(ent);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                try {
                    String atom = Advapi32Util.registryGetStringValue(WinReg.HKEY_LOCAL_MACHINE,
                                                                      "Software\\Microsoft\\Windows\\CurrentVersion\\Uninstall\\atom", "InstallLocation");
                    EditorEntry ent = new EditorEntry("Atom", atom + "\\atom.exe");
                    list.add(ent);
                } catch (Exception e1) {
                    log.error(e1.getMessage(), e1);
                }
            }

        } else if (IS_MAC) {
            Map<String, String> knownEditorMap = new CollectionHelper.Dict<String, String>().putItem(
                    VISUAL_STUDIO_CODE, "/Applications/Visual Studio Code.app/Contents/Resources/app/bin/code");
            for (String key : knownEditorMap.keySet()) {
                File file = new File(knownEditorMap.get(key));
                if (file.exists()) {
                    EditorEntry ent = new EditorEntry(key, file.getAbsolutePath());
                    list.add(ent);
                }
            }

        } else {
            Map<String, String> knownEditorMap = new CollectionHelper.Dict<String, String>()
                    .putItem(VISUAL_STUDIO_CODE, "/usr/bin/code").putItem("Atom", "/usr/bin/atom")
                    .putItem("Sublime Text", "/usr/bin/subl").putItem("Gedit", "/usr/bin/gedit")
                    .putItem("Kate", "/usr/bin/kate");
            for (String key : knownEditorMap.keySet()) {
                File file = new File(knownEditorMap.get(key));
                if (file.exists()) {
                    EditorEntry ent = new EditorEntry(key, file.getAbsolutePath());
                    list.add(ent);
                }
            }
        }
        return list;
    }

    private static String detectVSCode(boolean hklm) {
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
        return null;
    }

    public interface Shell32 extends ShellAPI, StdCallLibrary {
        WinDef.HINSTANCE shellExecuteW(WinDef.HWND hwnd, WString lpOperation, WString lpFile, WString lpParameters,
                                       WString lpDirectory, int nShowCmd);
    }

}
