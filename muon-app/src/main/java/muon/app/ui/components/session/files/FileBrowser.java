package muon.app.ui.components.session.files;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileSystem;
import muon.app.ssh.RemoteSessionInstance;
import muon.app.ssh.SshFileSystem;
import muon.app.ui.components.common.ClosableTabbedPanel;
import muon.app.ui.components.common.SkinnedSplitPane;
import muon.app.ui.components.session.Page;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.files.local.LocalFileBrowserView;
import muon.app.ui.components.session.files.ssh.SshFileBrowserView;
import muon.app.ui.components.session.files.transfer.FileTransfer;
import muon.app.ui.components.session.files.transfer.FileTransferProgress;
import muon.app.ui.components.session.files.view.DndTransferData;
import muon.app.util.FontAwesomeContants;
import muon.app.util.enums.ConflictAction;
import muon.app.util.enums.PanelOrientation;
import muon.app.util.enums.TransferMode;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;



@Slf4j
public class FileBrowser extends Page {
    private final JSplitPane horizontalSplitter;
    @Getter
    private final ClosableTabbedPanel leftTabs;
    @Getter
    private final ClosableTabbedPanel rightTabs;

    @Getter
    private final SessionContentPanel holder;
    @Getter
    private final SessionInfo info;
    private final Map<String, List<FileInfo>> sshDirCache = new HashMap<>();

    @Getter
    private final int activeSessionId;
    private final AtomicBoolean init = new AtomicBoolean(false);
    private final JPopupMenu popup;
    private final List<AbstractFileBrowserView> viewList = new ArrayList<>();
    private FileTransfer ongoingFileTransfer;
    private boolean leftPopup = false;
    private JLabel lblStat1;
    private Box statusBox;

    public FileBrowser(SessionInfo info, SessionContentPanel holder, JRootPane rootPane, int activeSessionId) {
        this.activeSessionId = activeSessionId;
        this.info = info;
        this.holder = holder;

        JMenuItem localMenuItem = new JMenuItem("Local file browser");
        JMenuItem remoteMenuItem = new JMenuItem("Remote file browser");

        popup = new JPopupMenu();
        popup.add(remoteMenuItem);
        popup.add(localMenuItem);
        popup.pack();

        localMenuItem.addActionListener(e -> {
            if (leftPopup) {
                openLocalFileBrowserView(null, PanelOrientation.LEFT);
            } else {
                openLocalFileBrowserView(null, PanelOrientation.RIGHT);
            }
        });

        remoteMenuItem.addActionListener(e -> {
            if (leftPopup) {
                openSshFileBrowserView(null, PanelOrientation.LEFT);
            } else {
                openSshFileBrowserView(null, PanelOrientation.RIGHT);
            }
        });

        this.leftTabs = new ClosableTabbedPanel(c -> {
            popup.setInvoker(c);
            leftPopup = true;
            popup.show(c, 0, c.getHeight());
        });

        this.rightTabs = new ClosableTabbedPanel(c -> {
            popup.setInvoker(c);
            leftPopup = false;
            popup.show(c, 0, c.getHeight());
        });

        horizontalSplitter = new SkinnedSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        horizontalSplitter.setResizeWeight(0.5);
        horizontalSplitter.setLeftComponent(this.leftTabs);
        horizontalSplitter.setRightComponent(this.rightTabs);
        horizontalSplitter.setDividerSize(5);

        if (App.getGlobalSettings().isDualPaneMode()) {
            switchToDualPaneMode();
        } else {
            switchToSinglePanelMode();
        }

    }


    private void switchToDualPaneMode() {
        horizontalSplitter.setRightComponent(this.rightTabs);
        horizontalSplitter.setLeftComponent(this.leftTabs);
        this.add(horizontalSplitter);
        this.revalidate();
        this.repaint();
    }

    private void switchToSinglePanelMode() {
        this.remove(horizontalSplitter);
        this.add(this.leftTabs);
        this.revalidate();
        this.repaint();
    }

    public void disableUi() {
        holder.disableUi();
    }

    public void disableUi(AtomicBoolean stopFlag) {
        holder.disableUi(stopFlag);
    }

    public void enableUi() {
        holder.enableUi();
    }

    public void openSshFileBrowserView(String path, PanelOrientation orientation) {
        SshFileBrowserView tab = new SshFileBrowserView(this, path, orientation);
        if (orientation == PanelOrientation.LEFT) {
            this.leftTabs.addTab(tab.getTabTitle(), tab);
        } else {
            this.rightTabs.addTab(tab.getTabTitle(), tab);
        }
    }

    public void openLocalFileBrowserView(String path, PanelOrientation orientation) {

        LocalFileBrowserView tab = new LocalFileBrowserView(this, path, orientation);
        if (orientation == PanelOrientation.LEFT) {
            this.leftTabs.addTab(tab.getTabTitle(), tab);
        } else {
            this.rightTabs.addTab(tab.getTabTitle(), tab);
        }
    }

    public SshFileSystem getSSHFileSystem() {
        return this.getSessionInstance().getSshFs();
    }

    public RemoteSessionInstance getSessionInstance() {
        return this.holder.getRemoteSessionInstance();
    }

    public boolean isCloseRequested() {
        return this.holder.isSessionClosed();
    }

    public Map<String, List<FileInfo>> getSSHDirectoryCache() {
        return this.sshDirCache;
    }

    public void newFileTransfer(FileSystem sourceFs, FileSystem targetFs, FileInfo[] files, String targetFolder,
                                int dragsource, ConflictAction defaultConflictAction, RemoteSessionInstance instance) {
        log.info("Initiating new file transfer...");
        this.ongoingFileTransfer = new FileTransfer(sourceFs, targetFs, files, targetFolder,
                                                    new FileTransferProgress() {

                                                        @Override
                                                        public void progress(long processedBytes, long totalBytes, long processedCount, long totalCount,
                                                                             FileTransfer fileTransfer) {
                                                            SwingUtilities.invokeLater(() -> {
                                                                if (totalBytes == 0) {
                                                                    holder.setTransferProgress(0);
                                                                } else {
                                                                    holder.setTransferProgress((int) ((processedBytes * 100) / totalBytes));
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void init(long totalSize, long files, FileTransfer fileTransfer) {
                                                        }

                                                        @Override
                                                        public void error(String cause, FileTransfer fileTransfer) {
                                                            SwingUtilities.invokeLater(() -> {
                                                                holder.endFileTransfer();
                                                                if (!holder.isSessionClosed()) {
                                                                    JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("operation_failed"));
                                                                }
                                                            });
                                                        }

                                                        @Override
                                                        public void done(FileTransfer fileTransfer) {
                                                            log.info("Done");
                                                            SwingUtilities.invokeLater(() -> {
                                                                holder.endFileTransfer();
                                                                reloadView();
                                                            });
                                                        }
                                                    }, defaultConflictAction, instance);
        holder.startFileTransferModal(e -> this.ongoingFileTransfer.close());
        holder.EXECUTOR.submit(this.ongoingFileTransfer);
    }

    public void reloadView() {
        Component c = leftTabs.getSelectedContent();
        log.info("c1 {}", c);
        if (c instanceof AbstractFileBrowserView) {
            ((AbstractFileBrowserView) c).reload();
        }
        c = rightTabs.getSelectedContent();
        log.info("c2 {}", c);
        if (c instanceof AbstractFileBrowserView) {
            ((AbstractFileBrowserView) c).reload();
        }
    }

    @Override
    public void onLoad() {
        if (init.get()) {
            return;
        }
        init.set(true);
        LocalFileBrowserView localFileBrowserView = new LocalFileBrowserView(this, System.getProperty("user.home"),
                                                                             PanelOrientation.LEFT);

        SshFileBrowserView sshFileBrowserView = new SshFileBrowserView(this, null, PanelOrientation.RIGHT);

        AbstractFileBrowserView left = sshFileBrowserView;
        AbstractFileBrowserView right = localFileBrowserView;

        if (App.getGlobalSettings().isFirstLocalViewInFileBrowser()) {
            left = localFileBrowserView;
            right = sshFileBrowserView;
        }

        this.leftTabs.addTab(left.getTabTitle(), left);
        this.rightTabs.addTab(right.getTabTitle(), right);
    }

    @Override
    public String getIcon() {
        return FontAwesomeContants.FA_FOLDER;
    }

    @Override
    public String getText() {
        return App.getCONTEXT().getBundle().getString("file_browser");
    }

    public void openPath(String path) {
        openSshFileBrowserView(path, PanelOrientation.LEFT);
    }

    public boolean isSessionClosed() {
        return this.holder.isSessionClosed();
    }

    public boolean selectTransferModeAndConflictAction(ResponseHolder holder) {
        holder.transferMode = App.getGlobalSettings().getFileTransferMode();
        holder.conflictAction = App.getGlobalSettings().getConflictAction();
        return true;
    }

    public boolean handleLocalDrop(DndTransferData transferData, SessionInfo info, FileSystem currentFileSystem,
                                   String currentPath) {
        if (App.getGlobalSettings().isConfirmBeforeMoveOrCopy()
            && JOptionPane.showConfirmDialog(null, App.getCONTEXT().getBundle().getString("move_copy_files")) != JOptionPane.YES_OPTION) {
            return false;
        }

        try {

            ResponseHolder holder = new ResponseHolder();

            if (!selectTransferModeAndConflictAction(holder)) {
                return false;
            }

            log.info("Dropped: {}", transferData);
            int sessionHashCode = transferData.getInfo();
            if (sessionHashCode == 0) {
                log.info("Session hash code: {}", sessionHashCode);
                return true;
            }

            if (info != null && info.hashCode() == sessionHashCode) {
                if (holder.transferMode == TransferMode.BACKGROUND) {
                    this.getHolder().downloadInBackground(transferData.getFiles(), currentPath, holder.conflictAction);
                    return true;
                }
                FileSystem sourceFs = this.getSSHFileSystem();
                if (sourceFs == null) {
                    return false;
                }
                this.newFileTransfer(sourceFs, currentFileSystem, transferData.getFiles(), currentPath, this.hashCode(),
                                     holder.conflictAction, this.getSessionInstance());
            }
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public void refreshViewMode() {
        for (AbstractFileBrowserView view : this.viewList) {
            view.refreshViewMode();
        }
        this.revalidate();
        this.repaint(0);
    }

    public void registerForViewNotification(AbstractFileBrowserView view) {
        this.viewList.add(view);
    }

    public void unRegisterForViewNotification(AbstractFileBrowserView view) {
        this.viewList.remove(view);
    }

    public void updateRemoteStatus(String text) {
    }

    public static class ResponseHolder {
        public TransferMode transferMode;
        public ConflictAction conflictAction;
    }
}
