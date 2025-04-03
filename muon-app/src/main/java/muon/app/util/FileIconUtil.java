package muon.app.util;

import muon.app.common.FileInfo;
import muon.app.util.enums.FileType;

import java.util.Locale;
import java.util.Set;

public class FileIconUtil {
    protected FileIconUtil() {

    }

    private static final Set<String> ARCHIVE_EXTS = Set.of(
            "zip", "tar", "tgz", "gz", "bz2", "tbz2", "tbz", "txz", "xz", "7z", "rar");
    private static final Set<String> AUDIO_EXTS = Set.of(
            "mp3", "aac", "mp2", "wav", "flac", "mpa", "m4a");
    private static final Set<String> CODE_EXTS = Set.of(
            "c", "js", "cpp", "java", "cs", "py", "pl", "rb", "sql", "go",
            "css", "scss", "html", "htm", "ts", "json", "xml");
    private static final Set<String> EXEC_EXTS = Set.of(
            "sh", "jar", "bat", "psi", "zsh", "ksh");
    private static final Set<String> EXCEL_EXTS = Set.of("xls", "xlsx");
    private static final Set<String> IMAGE_EXTS = Set.of("jpg", "jpeg", "png", "ico", "gif", "svg");
    private static final Set<String> VIDEO_EXTS = Set.of("mp4", "mkv", "m4v", "avi");
    private static final Set<String> PPT_EXTS = Set.of("ppt", "pptx");
    private static final Set<String> WORD_EXTS = Set.of("doc", "docx");
    private static final Set<String> TEXT_EXTS = Set.of("txt", "out", "log", "csv");
    private static final Set<String> CONFIG_EXTS = Set.of("conf", "yml", "cfg");
    private static final Set<String> WIN_EXTS = Set.of("exe", "msi");
    private static final Set<String> KEY_EXTS = Set.of("key", "pfx", "jks");
    private static final Set<String> MAC_EXTS = Set.of("app", "dmg");
    private static final Set<String> LINUX_EXTS = Set.of("deb", "rpm", "flatpak", "appimage");
    private static final Set<String> PUB_CERT_EXTS = Set.of("pub", "pem", "crt");


    public static String getIconForType(FileInfo ent) {

        // Directory checks
        if (ent.getType() == FileType.DIRECTORY || ent.getType() == FileType.DIR_LINK) {
            return FontAwesomeContants.FA_FOLDER;
        }

        String name = ent.getName().toLowerCase(Locale.ENGLISH);
        String extension = getFileExtension(name);

        if (extension.isEmpty()) {
            // No extension => default file icon
            return FontAwesomeContants.FA_FILE;
        }

        if (ARCHIVE_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_ARCHIVE_O;
        } else if (AUDIO_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_AUDIO_O;
        } else if (CODE_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_CODE_O;
        } else if (EXCEL_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_EXCEL_O;
        } else if (IMAGE_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_IMAGE_O;
        } else if (VIDEO_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_VIDEO_O;
        } else if (extension.equals("pdf")) {
            return FontAwesomeContants.FA_FILE_PDF_O;
        } else if (PPT_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_POWERPOINT_O;
        } else if (WORD_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_WORD_O;
        } else if (TEXT_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_FILE_TEXT;
        } else if (CONFIG_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_WRENCH;
        } else if (EXEC_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_TERMINAL;
        } else if (WIN_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_WINDOWS;
        } else if (KEY_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_KEY;
        } else if (MAC_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_APPLE;
        } else if (LINUX_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_LINUX;
        } else if (PUB_CERT_EXTS.contains(extension)) {
            return FontAwesomeContants.FA_VCARD;
        }

        // Default for unrecognized extensions
        return FontAwesomeContants.FA_FILE;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex + 1);
    }

}
