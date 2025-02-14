package muon.app.ui.components.session.files.transfer;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileSystem;
import muon.app.common.InputTransferChannel;
import muon.app.common.OutputTransferChannel;
import muon.app.ssh.RemoteSessionInstance;
import muon.app.ssh.SSHRemoteFileInputStream;
import muon.app.ssh.SSHRemoteFileOutputStream;
import muon.app.ssh.SshFileSystem;
import util.PathUtils;
import util.SudoUtils;
import util.enums.ConflictAction;
import util.enums.FileType;

import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class FileTransfer implements Runnable, AutoCloseable {
    // -> skip
    private static final int BUF_SIZE = Short.MAX_VALUE;
    @Getter
    private final FileSystem sourceFs;
    @Getter
    private final FileSystem targetFs;
    @Getter
    private final FileInfo[] files;
    @Getter
    private final String targetFolder;
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);
    private final RemoteSessionInstance instance;
    private long totalSize;
    @Setter
    private FileTransferProgress callback;
    private long processedBytes;
    private int processedFilesCount;
    private long totalFiles;
    private ConflictAction conflictAction; // 0 -> overwrite, 1 -> auto rename, 2

    public FileTransfer(FileSystem sourceFs, FileSystem targetFs, FileInfo[] files, String targetFolder,
                        FileTransferProgress callback, ConflictAction defaultConflictAction, RemoteSessionInstance instance) {
        this.sourceFs = sourceFs;
        this.targetFs = targetFs;
        this.files = files;
        this.targetFolder = targetFolder;
        this.callback = callback;
        this.conflictAction = defaultConflictAction;
        this.instance = instance;
        if (defaultConflictAction == ConflictAction.CANCEL) {
            throw new IllegalArgumentException("defaultConflictAction can not be ConflictAction.Cancel");
        }
    }

    private void transfer(String targetFolder, RemoteSessionInstance instance1) throws Exception {
        log.info("Copying to {}", targetFolder);
        List<FileInfoHolder> fileList = new ArrayList<>();
        List<FileInfo> list = targetFs.list(targetFolder);
        List<FileInfo> dupList = new ArrayList<>();

        if (this.conflictAction == ConflictAction.PROMPT) {
            this.conflictAction = checkForConflict(dupList);
            if (!dupList.isEmpty() && this.conflictAction == ConflictAction.CANCEL) {
                log.info("Operation cancelled by user");
                return;
            }
        }

        totalSize = 0;
        for (FileInfo file : files) {
            if (stopFlag.get()) {
                return;
            }

            String proposedName = null;
            if (isDuplicate(list, file.getName())) {
                if (this.conflictAction == ConflictAction.AUTORENAME) {
                    proposedName = generateNewName(list, file.getName());
                    log.info("new name: {}", proposedName);
                } else if (this.conflictAction == ConflictAction.SKIP) {
                    continue;
                }
            }

            if (file.getType() == FileType.DIRECTORY || file.getType() == FileType.DIR_LINK) {
                fileList.addAll(createFileList(file, targetFolder, proposedName));
            } else {
                fileList.add(new FileInfoHolder(file, targetFolder, proposedName));
                totalSize += file.getSize();
            }
        }
        totalFiles = fileList.size();

        callback.init(totalSize, totalFiles, this);
        InputTransferChannel inc = sourceFs.inputTransferChannel();
        OutputTransferChannel outc = targetFs.outputTransferChannel();
        for (FileInfoHolder file : fileList) {
            log.info("Copying: {}", file.info.getPath());
            if (stopFlag.get()) {
                log.info("Operation cancelled by user");
                return;
            }
            copyFile(file.info, file.targetPath, file.proposedName, inc, outc);
            log.info("Copying done: {}", file.info.getPath());
            processedFilesCount++;
        }

    }

    public void run() {
        try {
            try {
                transfer(this.targetFolder, instance);
                callback.done(this);
            } catch (AccessDeniedException e) {
                if (targetFs instanceof SshFileSystem) {
                    String tmpDir = "/tmp/" + UUID.randomUUID();
                    if (App.getGlobalSettings().isTransferTemporaryDirectory()) {
                        targetFs.mkdir(tmpDir);
                        transfer(tmpDir, instance);
                        callback.done(this);
                        JTextArea tmpFilePath = new JTextArea(5, 20);
                        tmpFilePath.setText("Files copied in " + tmpDir + " due to permission issues");
                        tmpFilePath.setEnabled(true);
                        JOptionPane.showMessageDialog(null, tmpFilePath, App.bundle.getString("copied_temp_directory"), JOptionPane.WARNING_MESSAGE);
                    }

                    if (!App.getGlobalSettings().isPromptForSudo() ||
                        JOptionPane.showConfirmDialog(null,
                                                      App.bundle.getString("permission_denied_file"),
                                                      App.bundle.getString("insufficient_permisions"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                        // Because transferTemporaryDirectory already create and transfer files, here can skip these steps
                        if (!App.getGlobalSettings().isTransferTemporaryDirectory()) {
                            targetFs.mkdir(tmpDir);
                            transfer(tmpDir, instance);
                        }

                        String command = "sh -c  \"cd '" + tmpDir + "'; cp -r * '" + this.targetFolder + "'\"";

                        log.info("Invoke sudo: {}", command);
                        int ret = SudoUtils.runSudo(command, instance);
                        if (ret == 0) {
                            callback.done(this);
                            return;
                        }
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            if (stopFlag.get()) {
                log.info("Operation cancelled by user");
                callback.done(this);
                return;
            }
            callback.error("Error", this);
        }
    }

    private List<FileInfoHolder> createFileList(FileInfo folder, String target, String proposedName) throws Exception {
        if (stopFlag.get()) {
            throw new Exception("Interrupted");
        }
        String folderTarget = PathUtils.combineUnix(target, proposedName == null ? folder.getName() : proposedName);
        targetFs.mkdir(folderTarget);
        List<FileInfoHolder> fileInfoHolders = new ArrayList<>();
        List<FileInfo> list = sourceFs.list(folder.getPath());
        for (FileInfo file : list) {
            if (stopFlag.get()) {
                throw new Exception("Interrupted");
            }
            if (file.getType() == FileType.DIRECTORY) {
                fileInfoHolders.addAll(createFileList(file, folderTarget, null));
            } else if (file.getType() == FileType.FILE) {
                fileInfoHolders.add(new FileInfoHolder(file, folderTarget, null));
                totalSize += file.getSize();
            }
        }
        log.info("File list created");
        return fileInfoHolders;
    }

    private synchronized void copyFile(FileInfo file, String targetDirectory, String proposedName,
                                       InputTransferChannel inc, OutputTransferChannel outc) throws Exception {

        String outPath = PathUtils.combine(targetDirectory, proposedName == null ? file.getName() : proposedName,
                                           outc.getSeparator());
        String inPath = file.getPath();
        log.info("Copying -- {} to {}", inPath, outPath);
        try (InputStream in = inc.getInputStream(inPath); OutputStream out = outc.getOutputStream(outPath)) {
            long len = inc.getSize(inPath);
            log.info("Initiate write");

            int bufferCapacity = BUF_SIZE;
            if (in instanceof SSHRemoteFileInputStream && out instanceof SSHRemoteFileOutputStream) {
                bufferCapacity = Math.min(((SSHRemoteFileInputStream) in).getBufferCapacity(),
                                          ((SSHRemoteFileOutputStream) out).getBufferCapacity());
            } else if (in instanceof SSHRemoteFileInputStream) {
                bufferCapacity = ((SSHRemoteFileInputStream) in).getBufferCapacity();
            } else if (out instanceof SSHRemoteFileOutputStream) {
                bufferCapacity = ((SSHRemoteFileOutputStream) out).getBufferCapacity();
            }

            byte[] buf = new byte[bufferCapacity];

            while (len > 0 && !stopFlag.get()) {
                int x = in.read(buf);
                if (x == -1) {
                    throw new IOException("Unexpected EOF");
                }
                out.write(buf, 0, x);
                len -= x;
                processedBytes += x;
                callback.progress(processedBytes, totalSize, processedFilesCount, totalFiles, this);
            }
            log.debug("Copy done before stream closing");
            out.flush();
        }
        log.debug("Copy done");
    }

    public void stop() {
        stopFlag.set(true);
    }

    @Override
    public void close() {
        stopFlag.set(true);
    }

    private ConflictAction checkForConflict(List<FileInfo> dupList) throws Exception {
        List<FileInfo> fileList = targetFs.list(targetFolder);
        for (FileInfo file : files) {
            for (FileInfo file1 : fileList) {
                if (file.getName().equals(file1.getName())) {
                    dupList.add(file);
                }
            }
        }

        ConflictAction action = ConflictAction.CANCEL;
        if (!dupList.isEmpty()) {

            DefaultComboBoxModel<ConflictAction> conflictOptionsCmb = new DefaultComboBoxModel<>(ConflictAction.values());
            conflictOptionsCmb.removeAllElements();
            for (ConflictAction conflictActionCmb : ConflictAction.values()) {
                if (conflictActionCmb.getKey() < 3) {
                    conflictOptionsCmb.addElement(conflictActionCmb);
                }
            }
            JComboBox<ConflictAction> cmbs = new JComboBox<>(conflictOptionsCmb);

            if (JOptionPane.showOptionDialog(null,
                                             new Object[]{App.bundle.getString("some_file_exists_action_required"), cmbs},
                                             App.bundle.getString("action_required"), JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                                             null) == JOptionPane.YES_OPTION) {
                action = (ConflictAction) cmbs.getSelectedItem();
            }
        }

        return action;
    }

    private boolean isDuplicate(List<FileInfo> list, String name) {
        for (FileInfo s : list) {
            log.debug("Checking for duplicate: {} --- {}", s.getName(), name);
            if (s.getName().equalsIgnoreCase(name)) {
                return true;
            }
        }
        log.info("Not duplicate: {}", name);
        return false;
    }

    public String generateNewName(List<FileInfo> list, String name) {
        while (isDuplicate(list, name)) {
            name = "Copy-of-" + name;
        }
        return name;
    }

    public String getSourceName() {
        return this.sourceFs.getName();
    }

    public String getTargetName() {
        return this.targetFs.getName();
    }

    static class FileInfoHolder {
        FileInfo info;
        String targetPath;
        String proposedName;

        public FileInfoHolder(FileInfo info, String targetPath, String proposedName) {
            this.info = info;
            this.targetPath = targetPath;
            this.proposedName = proposedName;
        }
    }

}
