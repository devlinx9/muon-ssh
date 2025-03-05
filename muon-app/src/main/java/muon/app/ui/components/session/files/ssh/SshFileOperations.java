package muon.app.ui.components.session.files.ssh;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileSystem;
import muon.app.ssh.RemoteSessionInstance;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.util.PathUtils;
import muon.app.util.SudoUtils;
import muon.app.util.enums.FileType;

import javax.swing.*;
import java.io.FileNotFoundException;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class SshFileOperations {

    public static final String ACCESS_DENIED = "access_denied";
    public static final String USE_SUDO = "use_sudo";
    public static final String OPERATION_FAILED = "operation_failed";
    public static final String INVOKE_SUDO = "Invoke sudo: {}";

    public SshFileOperations() {
    }

    public static void delete(List<FileInfo> files,
                              RemoteSessionInstance instance) throws Exception {

        StringBuilder sb = new StringBuilder("rm -rf ");

        for (FileInfo file : files) {
            sb.append("\"").append(file.getPath()).append("\" ");
        }

        log.info("Delete command1: {}", sb);

        if (instance.exec(sb.toString(), new AtomicBoolean(false)) != 0) {
            throw new FileNotFoundException("Operation failed");
        }
    }

    public boolean runScriptInBackground(RemoteSessionInstance instance,
                                         String command, AtomicBoolean stopFlag) throws Exception {
        log.info("Invoke command: {}", command);
        StringBuilder output = new StringBuilder();
        boolean ret = instance.exec(command, stopFlag, output,
                new StringBuilder()) == 0;
        log.info("output: {}", output);
        return ret;
    }

    public boolean moveTo(RemoteSessionInstance instance, List<FileInfo> files,
                          String targetFolder, FileSystem fs, String password) throws Exception {
        List<FileInfo> fileList = fs.list(targetFolder);
        List<FileInfo> dupList = new ArrayList<>();
        for (FileInfo file : files) {
            for (FileInfo file1 : fileList) {
                if (file.getName().equals(file1.getName())) {
                    dupList.add(file);
                }
            }
        }

        int action = -1;
        if (!dupList.isEmpty()) {
            JComboBox<String> cmbs = new JComboBox<>(
                    new String[]{"Auto rename", "Overwrite"});
            if (JOptionPane.showOptionDialog(null, new Object[]{
                            App.getContext().getBundle().getString("file_exists_action"),
                            cmbs}, App.getContext().getBundle().getString("action_required"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null,
                    null) == JOptionPane.YES_OPTION) {
                action = cmbs.getSelectedIndex();
            } else {
                return false;
            }
        }

        StringBuilder command = new StringBuilder();
        for (FileInfo fileInfo : files) {
            if (fileInfo.getType() == FileType.DIR_LINK
                    || fileInfo.getType() == FileType.DIRECTORY) {
                command.append("mv ");
            } else {
                command.append("mv -T ");
            }
            combinePaths(targetFolder, fileList, dupList, action, command, fileInfo);
        }

        log.info("Move: {}", command);
        if (instance.exec(command.toString(), new AtomicBoolean(false)) != 0) {
            if (!App.getGlobalSettings().isUseSudo()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(ACCESS_DENIED));
                return false;
            }

            if (!App.getGlobalSettings().isPromptForSudo()
                    || JOptionPane.showConfirmDialog(null,
                    App.getContext().getBundle().getString("access_denied_rename_sudo"), App.getContext().getBundle().getString(USE_SUDO),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                if (!instance.isSessionClosed()) {
                    JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
                }
                return false;
            }

            int ret = SudoUtils.runSudo(command.toString(), instance, password);
            if (ret != -1) {
                return ret == 0;
            }

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
        } else {
            return true;
        }
        return false;
    }

    private void combinePaths(String targetFolder, List<FileInfo> fileList, List<FileInfo> dupList, int action, StringBuilder command, FileInfo fileInfo) {
        command.append("\"").append(fileInfo.getPath()).append("\" ");
        if (dupList.contains(fileInfo) && action == 0) {
            command.append("\"").append(PathUtils.combineUnix(targetFolder,
                                                              getUniqueName(fileList, fileInfo.getName()))).append("\"; ");
        } else {
            command.append("\"").append(PathUtils.combineUnix(targetFolder,
                                                              fileInfo.getName())).append("\"; ");
        }
    }

    public boolean copyTo(RemoteSessionInstance instance, List<FileInfo> files,
                          String targetFolder, FileSystem fs, String password) throws Exception {
        List<FileInfo> fileList = fs.list(targetFolder);
        List<FileInfo> dupList = new ArrayList<>();
        for (FileInfo file : files) {
            for (FileInfo file1 : fileList) {
                if (file.getName().equals(file1.getName())) {
                    dupList.add(file);
                }
            }
        }

        int action = -1;
        if (!dupList.isEmpty()) {
            JComboBox<String> cmbs = new JComboBox<>(
                    new String[]{"Auto rename", "Overwrite"});
            if (JOptionPane.showOptionDialog(null, new Object[]{
                            App.getContext().getBundle().getString("some_file_exists_action_required"),
                            cmbs}, App.getContext().getBundle().getString("action_required"), JOptionPane.YES_NO_OPTION,
                    JOptionPane.PLAIN_MESSAGE, null, null,
                    null) == JOptionPane.YES_OPTION) {
                action = cmbs.getSelectedIndex();
            } else {
                return false;
            }
        }

        StringBuilder command = new StringBuilder();
        for (FileInfo fileInfo : files) {
            if (fileInfo.getType() == FileType.DIR_LINK
                    || fileInfo.getType() == FileType.DIRECTORY) {
                command.append("cp -rf ");
            } else {
                command.append("cp -Tf ");
            }
            combinePaths(targetFolder, fileList, dupList, action, command, fileInfo);
        }

        log.info("Copy: {}", command);
        if (instance.exec(command.toString(), new AtomicBoolean(false)) != 0) {
            if (!App.getGlobalSettings().isUseSudo()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(ACCESS_DENIED));
                return false;
            }
            if (!App.getGlobalSettings().isPromptForSudo()
                    || JOptionPane.showConfirmDialog(null,
                    App.getContext().getBundle().getString("access_denied_copy_sudo"), App.getContext().getBundle().getString(USE_SUDO),
                    JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                if (!instance.isSessionClosed()) {
                    JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
                }
                return false;
            }

            int ret = SudoUtils.runSudo(command.toString(), instance, password);
            if (ret != -1) {
                return ret == 0;
            }

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
        } else {
            return true;
        }
        return false;
    }

    private String getUniqueName(List<FileInfo> list, String name) {
        StringBuilder nameBuilder = new StringBuilder(name);
        while (true) {
            boolean found = false;
            for (FileInfo f : list) {
                if (nameBuilder.toString().equals(f.getName())) {
                    nameBuilder.insert(0, "Copy of ");
                    found = true;
                    break;
                }
            }
            if (!found)
                break;
        }
        name = nameBuilder.toString();
        return name;
    }

    public boolean rename(String oldName, String newName, FileSystem fs,
                          RemoteSessionInstance instance, String password) {
        try {
            fs.rename(oldName, newName);
            return true;
        } catch (AccessDeniedException e) {
            log.error(e.getMessage(), e);

            if (!App.getGlobalSettings().isUseSudo()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(ACCESS_DENIED));
                return false;
            }

            if (!App.getGlobalSettings().isPromptForSudo()
                    || JOptionPane.showConfirmDialog(null,
                    App.getContext().getBundle().getString("access_denied_rename_sudo"), App.getContext().getBundle().getString(USE_SUDO),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                return renameWithPrivilege(oldName, newName, instance, password);
            }

            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
            return false;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
            return false;
        }
    }

    private boolean renameWithPrivilege(String oldName, String newName,
                                        RemoteSessionInstance instance, String password) {
        StringBuilder command = new StringBuilder();
        command.append("mv \"").append(oldName).append("\" \"").append(newName).append("\"");
        log.info(INVOKE_SUDO, command);
        int ret = SudoUtils.runSudo(command.toString(), instance, password);
        if (ret == -1) {
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
        }
        return ret == 0;
    }

    public boolean delete(FileInfo[] targetList, FileSystem fs,
                          RemoteSessionInstance instance, String password) {
        try {
            try {
                delete(Arrays.asList(targetList), instance);
                return true;
            } catch (FileNotFoundException e) {
                log.info("delete: file not found");
                log.error(e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                for (FileInfo s : targetList) {
                    fs.delete(s);
                }
                return true;
            }
        } catch (FileNotFoundException | AccessDeniedException e) {
            log.error(e.getMessage(), e);
            if (!App.getGlobalSettings().isUseSudo()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(ACCESS_DENIED));
                return false;
            }
            if (!App.getGlobalSettings().isPromptForSudo()
                    || JOptionPane.showConfirmDialog(null,
                    "Access denied, delete using sudo?", App.getContext().getBundle().getString(USE_SUDO),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                return deletePrivilege(targetList, instance, password);
            }
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
            return false;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString("error_delete_file"));
            }

            return false;
        }
    }

    private boolean deletePrivilege(FileInfo[] targetList,
                                    RemoteSessionInstance instance, String password) {
        StringBuilder sb = new StringBuilder("rm -rf ");
        for (FileInfo file : targetList) {
            sb.append("\"").append(file.getPath()).append("\" ");
        }

        log.info(INVOKE_SUDO, sb);
        int ret = SudoUtils.runSudo(sb.toString(), instance, password);
        if (ret == -1) {
            JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
        }
        return ret == 0;
    }

    public boolean newFile(FileInfo[] files, FileSystem fs, String folder,
                           RemoteSessionInstance instance, String password) {
        String text = JOptionPane.showInputDialog("New file");
        if (text == null || text.isEmpty()) {
            return false;
        }
        boolean alreadyExists = false;
        for (FileInfo f : files) {
            if (f.getName().equals(text)) {
                alreadyExists = true;
                break;
            }
        }
        if (alreadyExists) {
            JOptionPane.showMessageDialog(null,App.getContext().getBundle().getString("file_exists"));
            return false;
        }
        try {
            fs.createFile(PathUtils.combineUnix(folder, text));
            return true;
        } catch (AccessDeniedException e1) {
            log.error(e1.getMessage(), e1);
            if (!App.getGlobalSettings().isUseSudo()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(ACCESS_DENIED));
                return false;
            }
            if (!App.getGlobalSettings().isPromptForSudo()
                    || JOptionPane.showConfirmDialog(null,
                    "Access denied, new file using sudo?", App.getContext().getBundle().getString(USE_SUDO),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (!touchWithPrivilege(folder, text, instance, password)) {
                    if (!instance.isSessionClosed()) {
                        JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
                    }
                    return false;
                }
                return true;
            }
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }

            return false;
        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
        }
        return false;
    }

    private boolean touchWithPrivilege(String path, String newFile,
                                       RemoteSessionInstance instance, String password) {
        String file = PathUtils.combineUnix(path, newFile);
        StringBuilder command = new StringBuilder();
        command.append("touch \"").append(file).append("\"");
        log.info(INVOKE_SUDO, command);
        int ret = SudoUtils.runSudo(command.toString(), instance, password);
        if (ret == -1) {
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
        }
        return ret == 0;
    }

    public boolean newFolder(FileInfo[] files, String folder, FileSystem fs,
                             RemoteSessionInstance instance, String password) {
        String text = JOptionPane.showInputDialog("New folder name");
        if (text == null || text.isEmpty()) {
            return false;
        }
        boolean alreadyExists = false;
        for (FileInfo f : files) {
            if (f.getName().equals(text)) {
                alreadyExists = true;
                break;
            }
        }
        if (alreadyExists) {
            JOptionPane.showMessageDialog(null,
                    "File with same name already exists");
            return false;
        }
        try {
            fs.mkdir(PathUtils.combineUnix(folder, text));
            return true;
        } catch (AccessDeniedException e1) {
            log.error(e1.getMessage(), e1);
            if (!App.getGlobalSettings().isUseSudo()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(ACCESS_DENIED));
                return false;
            }
            if (!App.getGlobalSettings().isPromptForSudo()
                    || JOptionPane.showConfirmDialog(null,
                    "Access denied, try using sudo?", App.getContext().getBundle().getString(USE_SUDO),
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (!mkdirWithPrivilege(folder, text, instance, password)) {
                    if (!instance.isSessionClosed()) {
                        JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
                    }
                    return false;
                }
                return true;
            }
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
            return false;

        } catch (Exception e1) {
            log.error(e1.getMessage(), e1);
            if (!instance.isSessionClosed()) {
                JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
            }
        }
        return false;
    }

    private boolean mkdirWithPrivilege(String path, String newFolder,
                                       RemoteSessionInstance instance, String password) {
        String file = PathUtils.combineUnix(path, newFolder);
        StringBuilder command = new StringBuilder();
        command.append("mkdir \"").append(file).append("\"");
        log.info(INVOKE_SUDO, command);
        int ret = SudoUtils.runSudo(command.toString(), instance, password);
        if (ret == -1 && !instance.isSessionClosed()) {
            JOptionPane.showMessageDialog(null, App.getContext().getBundle().getString(OPERATION_FAILED));
        }
        return ret == 0;
    }

    public boolean createLink(FileInfo[] files, FileSystem fs,
                              RemoteSessionInstance instance) {
        JTextField txtLinkName = new SkinnedTextField(30);
        JTextField txtFileName = new SkinnedTextField(30);
        JCheckBox chkHardLink = new JCheckBox("Hardlink");

        if (files.length > 0) {
            FileInfo info = files[0];
            txtLinkName.setText(
                    PathUtils.combineUnix(PathUtils.getParent(info.getPath()),
                            "Link to " + info.getName()));
            txtFileName.setText(info.getPath());
        }

        if (JOptionPane.showOptionDialog(null,
                new Object[]{"Create link", "Link path", txtLinkName,
                        "File name", txtFileName, chkHardLink},
                "Create link", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, null,
                null) == JOptionPane.OK_OPTION) {
            if (!txtLinkName.getText().isEmpty()
                    && !txtFileName.getText().isEmpty()) {
                return createLinkAsync(txtFileName.getText(),
                        txtLinkName.getText(), chkHardLink.isSelected(), fs);
            }
        }
        return false;
    }

    private boolean createLinkAsync(String src, String dst, boolean hardLink,
                                    FileSystem fs) {
        try {
            fs.createLink(src, dst, hardLink);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

}
