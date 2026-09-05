
package muon.app.ui.components.session;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.ssh.SSHRemoteFileInputStream;
import muon.app.ssh.SSHRemoteFileOutputStream;
import muon.app.ssh.SshFileSystem;
import muon.app.ui.components.session.FileChangeWatcher.FileModificationInfo;
import muon.app.util.OptionPaneUtils;
import muon.app.util.PlatformUtils;
import muon.app.util.TimeUtils;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.UUID.randomUUID;
import static muon.app.ui.components.session.SessionContentPanel.PAGE_ID;
import static muon.app.util.ScalingUtil.getScaledEmptyBorder;


/**
 * @author subhro
 *
 */
@Slf4j
public class ExternalEditorHandler extends JDialog {
    private final JProgressBar progressBar;
    private final JLabel progressLabel;
    private final JFrame frame;
    private final AtomicBoolean stopFlag = new AtomicBoolean(false);
    private transient FileChangeWatcher fileWatcher;

    public ExternalEditorHandler(JFrame frame) {
        super(frame);
        setModal(true);
        this.frame = frame;
        setSize(400, 200);

        progressBar = new JProgressBar();
        progressLabel = new JLabel("Transferring...");
        progressLabel.setBorder(getScaledEmptyBorder(0, 0, 20, 0));
        progressLabel.setFont(App.getCONTEXT().getSkin().getDefaultFont(18.0f));
        JButton btnCanel = new JButton(App.getCONTEXT().getBundle().getString("cancel"));
        Box bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue());
        bottomBox.add(btnCanel);

        progressLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        progressBar.setAlignmentX(Component.LEFT_ALIGNMENT);
        bottomBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        Box box = Box.createVerticalBox();
        box.add(progressLabel);
        box.add(progressBar);
        box.add(Box.createVerticalGlue());
        box.add(bottomBox);

        box.setBorder(getScaledEmptyBorder(10, 10, 10, 10));

        this.add(box);
        this.fileWatcher = new FileChangeWatcher(files -> {
            List<String> messages = new ArrayList<>();
            messages.add("Some file(s) have been modified, upload changes to server?\n");
            messages.add("Changed file(s):");
            messages.addAll(files.stream().map(Object::toString).collect(Collectors.toList()));
            if (OptionPaneUtils.showOptionDialog(this.frame, messages.toArray(new String[0]),
                                                 "File changed") == JOptionPane.OK_OPTION) {
                this.fileWatcher.stopWatching();
                App.getCONTEXT().getExecutor().submit(() -> {
                    try {
                        log.info("In app executor");
                        this.saveRemoteFiles(files);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        }, 1000);

        this.fileWatcher.startWatching();

    }

    private void saveRemoteFiles(List<FileModificationInfo> files) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            setVisible(true);
        });
        this.fileWatcher.stopWatching();
        long totalSize = 0L;
        for (FileModificationInfo info : files) {
            totalSize += info.localFile.length();
        }
        log.info("Total size: {}", totalSize);
        long totalBytes = 0L;
        for (FileModificationInfo info : files) {
            log.info("Total size: {} opcying: {}", totalSize, info);
            totalBytes += saveRemoteFile(info, totalSize, totalBytes);
        }
        fileWatcher.resumeWatching();
        log.info("Transfer complete");
        SwingUtilities.invokeLater(() -> setVisible(false));
    }

    /**
     *
     */
    public long saveRemoteFile(FileModificationInfo info, long total, long totalBytes) {
        log.info("Init transfer...1");
        ISessionContentPanel scp = App.getSessionContainer(info.activeSessionId);
        if (scp == null) {
            log.info("No session found");
            return info.remoteFile.getSize();
        }

        log.info("Init transfer...2");
        try (OutputStream out = scp.getRemoteSessionInstance().getSshFs().outputTransferChannel()
                .getOutputStream(info.remoteFile.getPath()); InputStream in = new FileInputStream(info.localFile)) {
            int cap = 8192;
            if (out instanceof SSHRemoteFileOutputStream) {
                cap = ((SSHRemoteFileOutputStream) out).getBufferCapacity();
            }
            byte[] b = new byte[cap];
            log.info("Init transfer...");
            while (!this.stopFlag.get()) {
                int x = in.read(b);
                if (x == -1) {
                    break;
                }
                totalBytes += x;
                out.write(b, 0, x);
                final int progress = (int) ((totalBytes * 100) / total);
                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return info.remoteFile.getSize();
    }

    /**
     * Downloads a remote file using SFTP in a temporary directory and if download
     * completes successfully, adds it for monitoring.
     *
     * @param openWith            should show windows open with dialog
     * @param app                 should open with specified app
     * @param sessionContentPanel session content panel of the current connection
     */
    public void openRemoteFile(FileInfo remoteFile, SshFileSystem remoteFs, int activeSessionId, boolean openWith,
                               String app, SessionContentPanel sessionContentPanel) throws IOException {
        this.fileWatcher.stopWatching();

        FileModificationInfo watchedFile = isAlreadyOpenFile(remoteFile);

        if (watchedFile == null) {
            Path tempFolderPath = Files.createTempDirectory(randomUUID().toString());
            Path localFilePath = tempFolderPath.resolve(remoteFile.getName());
            this.stopFlag.set(false);
            this.progressLabel.setText(remoteFile.getName());

            watchedFile = new FileChangeWatcher.FileModificationInfo();
            watchedFile.remoteFile = remoteFile;
            watchedFile.activeSessionId = activeSessionId;
            watchedFile.localFile = localFilePath.toFile();
        }


        FileModificationInfo finalWatchedFile = watchedFile;
        App.getCONTEXT().getExecutor().submit(() -> {
            boolean isFileOpen;
            if (!finalWatchedFile.alreadyWatched) {
                isFileOpen = getFile(remoteFs, fileWatcher, finalWatchedFile);
            } else {
                isFileOpen = true;
            }
            SwingUtilities.invokeLater(() -> {
                fileWatcher.resumeWatching();
                if (isFileOpen) {
                    openEditor(remoteFile, openWith, app, sessionContentPanel, finalWatchedFile);
                }
                setVisible(false);
            });
        });


        setLocationRelativeTo(frame);
    }

    private static void openEditor(FileInfo remoteFile, boolean openWith, String app, SessionContentPanel sessionContentPanel, FileModificationInfo watchedFile) {
        try {
            if (app == null) {
                PlatformUtils.openWithDefaultApp(watchedFile.localFile.toPath().toFile(), openWith);
            } else if (sessionContentPanel != null) {
                sessionContentPanel.getTextEditorHolder().getEditor().openRemoteFile(remoteFile, watchedFile.localFile.getAbsolutePath());
                sessionContentPanel.showPage(sessionContentPanel.getTextEditorHolder().getClientProperty(PAGE_ID) + "");
            } else {
                PlatformUtils.openWithApp(watchedFile.localFile.toPath().toFile(), app);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            JOptionPane.showMessageDialog(App.getAppWindow(), "There was an error opening the file: " + e.getMessage());
        }
    }

    private FileModificationInfo isAlreadyOpenFile(FileInfo remoteFile) {
        for (var watchedFile : this.fileWatcher.getFilesToWatch()) {
            if (watchedFile.remoteFile.equals(remoteFile)) {
                watchedFile.alreadyWatched = true;
                return watchedFile;
            }
        }
        return null;
    }

    private boolean getFile(SshFileSystem remoteFs, FileChangeWatcher fileWatcher, FileModificationInfo watchedFile) {
        boolean isFileOpen = false;
        try (InputStream in = remoteFs.inputTransferChannel().getInputStream(watchedFile.remoteFile.getPath());
             OutputStream out = new FileOutputStream(watchedFile.localFile)) {
            int cap = 8192;
            if (in instanceof SSHRemoteFileInputStream) {
                cap = ((SSHRemoteFileInputStream) in).getBufferCapacity();
            }
            byte[] b = new byte[cap];
            long totalBytes = 0L;
            while (!this.stopFlag.get()) {
                int x = in.read(b);
                if (x == -1) {
                    break;
                }
                totalBytes += x;
                out.write(b, 0, x);
                if (watchedFile.remoteFile.getSize() != 0) {
                    final int progress = (int) ((totalBytes * 100) / watchedFile.remoteFile.getSize());
                    SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                }
            }
            if (watchedFile.remoteFile.getSize() != 0) {
                watchedFile.localFile.setLastModified(TimeUtils.toEpochMilli(watchedFile.remoteFile.getLastModified()));
                watchedFile.lastModified = TimeUtils.toEpochMilli(watchedFile.remoteFile.getLastModified());
            }
            fileWatcher.addForMonitoring(watchedFile);
            isFileOpen = true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(App.getAppWindow(), e.getMessage(), App.getCONTEXT().getBundle().getString("error"), JOptionPane.ERROR_MESSAGE);
            log.error(e.getMessage(), e);
            isFileOpen = false;
        }
        return isFileOpen;
    }
}
