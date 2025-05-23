package muon.app.ui.components.session.files.ssh;

import lombok.extern.slf4j.Slf4j;
import muon.app.ssh.RemoteSessionInstance;
import muon.app.util.OptionPaneUtils;
import muon.app.util.PathUtils;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ArchiveOperation {
    private final Map<String, String> extractCommands;
    private final Map<String, String> compressCommands;

    public ArchiveOperation() {
        extractCommands = new LinkedHashMap<>();
        extractCommands.put(".tar", "cat \"%s\"|tar -C \"%s\" -xvf -");
        extractCommands.put(".tar.gz",
                            "gunzip -c <\"%s\"|tar -C \"%s\" -xvf -");
        extractCommands.put(".tgz", "gunzip -c <\"%s\"|tar -C \"%s\" -xvf -");
        extractCommands.put(".tar.bz2",
                            "bzip2 -d -c <\"%s\"|tar -C \"%s\" -xvf -");
        extractCommands.put(".tbz2",
                            "bzip2 -d -c <\"%s\"|tar -C \"%s\" -xvf -");
        extractCommands.put(".tbz", "bzip2 -d -c <\"%s\"|tar -C \"%s\" -xvf -");
        extractCommands.put(".tar.xz", "xz -d -c <\"%s\"|tar -C \"%s\" -xvf -");
        extractCommands.put(".txz", "xz -d -c <\"%s\"|tar -C \"%s\" -xvf -");
        extractCommands.put(".zip", "unzip -o \"%s\" -d \"%s\" ");
        extractCommands.put(".gz", "gunzip -c < \"%s\" > \"%s\" ");
        extractCommands.put(".xz", "xz -d -c < \"%s\" > \"%s\" ");
        extractCommands.put(".bz2", "bzip2 -d -c  < \"%s\" > \"%s\" ");

        compressCommands = new LinkedHashMap<>();
        compressCommands.put("tar", "tar cvf - %s|cat>\"%s\"");
        compressCommands.put("tar.gz", "tar cvf - %s|gzip>\"%s\"");
        compressCommands.put("tar.bz2", "tar cvf - %s|bzip2 -z>\"%s\"");
        compressCommands.put("tar.xz", "tar cvf - %s|xz -z>\"%s\"");
        compressCommands.put("zip", "zip -r - %s|cat>\"%s\"");
    }

    public boolean isSupportedArchive(String fileName) {
        for (String key : extractCommands.keySet()) {
            if (fileName.endsWith(key)) {
                return true;
            }
        }
        return false;
    }

    public String getExtractCommand(String fileName) {
        for (String key : extractCommands.keySet()) {
            if (fileName.endsWith(key)) {
                return extractCommands.get(key);
            }
        }
        return null;
    }

    private boolean isSingleArchive(String archivePath) {
        archivePath = archivePath.toLowerCase(Locale.ENGLISH);
        for (String key : extractCommands.keySet()) {
            if (archivePath.endsWith(key) && (key.equals(".xz")
                                              || key.equals(".gz") || key.equals(".bz2")) && !(archivePath.endsWith(".tar.xz")
                                                                                               || archivePath.endsWith(".tar.gz")
                                                                                               || archivePath.endsWith(".tar.bz2"))) {
                return true;
            }

        }
        return false;
    }

    private String getArchiveFileName(String archivePath) {
        String path = archivePath.toLowerCase(Locale.ENGLISH);
        if (path.endsWith(".gz") || path.endsWith(".xz")) {
            return archivePath.substring(0, archivePath.length() - 3);
        } else {
            return archivePath.substring(0, archivePath.length() - 4);
        }
    }

    public boolean extractArchive(RemoteSessionInstance client,
                                  String archivePath, String targetFolder, AtomicBoolean stopFlag)
            throws Exception {
        String command = getExtractCommand(archivePath);
        if (command == null) {
            log.info("Unsupported file: {}", archivePath);
            return false;
        }
        command = String
                .format(command, archivePath,
                        isSingleArchive(archivePath)
                        ? PathUtils.combineUnix(targetFolder,
                                                getArchiveFileName(PathUtils
                                                                           .getFileName(archivePath)))
                        : targetFolder);
        log.info("Invoke command: {}", command);
        StringBuilder output = new StringBuilder();
        boolean ret = client.exec(command, stopFlag, output) == 0;
        log.info("output: {}", output);
        return ret;
    }

    public boolean createArchive(RemoteSessionInstance client,
                                 List<String> files, String targetFolder, AtomicBoolean stopFlag) throws Exception {
        String text = files.size() > 1 ? PathUtils.getFileName(targetFolder)
                                       : files.get(0);
        JTextField txtFileName = new JTextField(text);
        JTextField txtTargetFolder = new JTextField(targetFolder);
        JComboBox<String> comboBox = new JComboBox<>(
                compressCommands.keySet().toArray(new String[0]));
        if (OptionPaneUtils.showOptionDialog(null,
                                             new Object[]{"Archive name", txtFileName, "Target folder",
                                                          txtTargetFolder, "Archive type", comboBox},
                                             "Create archive") == JOptionPane.OK_OPTION) {

            StringBuilder sb = new StringBuilder();
            for (String s : files) {
                sb.append(" \"").append(s).append("\"");
            }

            String ext = comboBox.getSelectedItem() + "";

            String compressCmd = String.format(compressCommands.get(ext),
                                               sb,
                                               PathUtils.combineUnix(txtTargetFolder.getText(),
                                                                     txtFileName.getText() + "." + ext));
            String cd = String.format("cd \"%s\";", txtTargetFolder.getText());
            log.info("{}{}", cd, compressCmd);
            StringBuilder output = new StringBuilder();
            boolean ret = client.exec(cd + compressCmd, stopFlag, output) == 0;
            log.info("output: {}", output);
            return ret;
        }
        return true;
    }
}
