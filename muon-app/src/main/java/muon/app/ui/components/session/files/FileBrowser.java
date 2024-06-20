package muon.app.ui.components.session.files;

import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileSystem;
import muon.app.ssh.RemoteSessionInstance;
import muon.app.ssh.SshFileSystem;
import muon.app.ui.components.ClosableTabbedPanel;
import muon.app.ui.components.SkinnedSplitPane;
import muon.app.ui.components.session.Page;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.files.local.LocalFileBrowserView;
import muon.app.ui.components.session.files.ssh.SshFileBrowserView;
import muon.app.ui.components.session.files.transfer.FileTransfer;
import muon.app.ui.components.session.files.transfer.FileTransferProgress;
import muon.app.ui.components.session.files.view.DndTransferData;
import util.Constants;
import util.FontAwesomeContants;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static muon.app.App.bundle;

public class FileBrowser extends Page {
    private final JSplitPane horizontalSplitter;
    private final ClosableTabbedPanel leftTabs;
    private final ClosableTabbedPanel rightTabs;
    private final SessionContentPanel holder;
    private final SessionInfo info;
    private final Map<String, List<FileInfo>> sshDirCache = new HashMap<>();
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
                openLocalFileBrowserView(null, AbstractFileBrowserView.PanelOrientation.LEFT);
            } else {
                openLocalFileBrowserView(null, AbstractFileBrowserView.PanelOrientation.RIGHT);
            }
        });

        remoteMenuItem.addActionListener(e -> {
            if (leftPopup) {
                openSshFileBrowserView(null, AbstractFileBrowserView.PanelOrientation.LEFT);
            } else {
                openSshFileBrowserView(null, AbstractFileBrowserView.PanelOrientation.RIGHT);
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

    public void openSshFileBrowserView(String path, AbstractFileBrowserView.PanelOrientation orientation) {
        SshFileBrowserView tab = new SshFileBrowserView(this, path, orientation);
        if (orientation == AbstractFileBrowserView.PanelOrientation.LEFT) {
            this.leftTabs.addTab(tab.getTabTitle(), tab);
        } else {
            this.rightTabs.addTab(tab.getTabTitle(), tab);
        }
    }

    public void openLocalFileBrowserView(String path, AbstractFileBrowserView.PanelOrientation orientation) {

        LocalFileBrowserView tab = new LocalFileBrowserView(this, path, orientation);
        if (orientation == AbstractFileBrowserView.PanelOrientation.LEFT) {
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

    public SessionInfo getInfo() {
        return info;
    }

    public boolean isCloseRequested() {
        return this.holder.isSessionClosed();
    }

    public Map<String, List<FileInfo>> getSSHDirectoryCache() {
        return this.sshDirCache;
    }

    public void newFileTransfer(FileSystem sourceFs, FileSystem targetFs, FileInfo[] files, String targetFolder,
                                int dragsource, Constants.ConflictAction defaultConflictAction, RemoteSessionInstance instance) {
        System.out.println("Initiating new file transfer...");
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
                                JOptionPane.showMessageDialog(null, App.bundle.getString("operation_failed"));
                            }
                        });
                    }

                    @Override
                    public void done(FileTransfer fileTransfer) {
                        System.out.println("Done");
                        SwingUtilities.invokeLater(() -> {
                            holder.endFileTransfer();
                            reloadView();
                        });
                    }
                }, defaultConflictAction, instance);
        holder.startFileTransferModal(e -> this.ongoingFileTransfer.close());
        holder.executor.submit(this.ongoingFileTransfer);
    }

    private void reloadView() {
        Component c = leftTabs.getSelectedContent();
        System.out.println("c1 " + c);
        if (c instanceof AbstractFileBrowserView) {
            ((AbstractFileBrowserView) c).reload();
        }
        c = rightTabs.getSelectedContent();
        System.out.println("c2 " + c);
        if (c instanceof AbstractFileBrowserView) {
            ((AbstractFileBrowserView) c).reload();
        }
    }

    /**
     * @return the activeSessionId
     */
    public int getActiveSessionId() {
        return activeSessionId;
    }

    @Override
    public void onLoad() {
        if (init.get()) {
            return;
        }
        init.set(true);
        SshFileBrowserView left = new SshFileBrowserView(this, null, AbstractFileBrowserView.PanelOrientation.LEFT);
        this.leftTabs.addTab(left.getTabTitle(), left);

        LocalFileBrowserView right = new LocalFileBrowserView(this, System.getProperty("user.home"),
                AbstractFileBrowserView.PanelOrientation.RIGHT);
        this.rightTabs.addTab(right.getTabTitle(), right);
    }

    @Override
    public String getIcon() {
        return FontAwesomeContants.FA_FOLDER;
    }

    @Override
    public String getText() {
        return bundle.getString("file_browser");
    }

    /**
     * @return the holder
     */
    public SessionContentPanel getHolder() {
        return holder;
    }

    public void openPath(String path) {
        openSshFileBrowserView(path, AbstractFileBrowserView.PanelOrientation.LEFT);
    }

    public boolean isSessionClosed() {
        return this.holder.isSessionClosed();
    }

    public boolean selectTransferModeAndConflictAction(ResponseHolder holder) throws Exception {
        holder.transferMode = App.getGlobalSettings().getFileTransferMode();
        holder.conflictAction = App.getGlobalSettings().getConflictAction();
        return true;
    }

    public boolean handleLocalDrop(DndTransferData transferData, SessionInfo info, FileSystem currentFileSystem,
                                   String currentPath) {
        // TODO i18n
        if (App.getGlobalSettings().isConfirmBeforeMoveOrCopy()
                && JOptionPane.showConfirmDialog(null, "Move/copy files?") != JOptionPane.YES_OPTION) {
            return false;
        }

        try {

            ResponseHolder holder = new ResponseHolder();

            if (!selectTransferModeAndConflictAction(holder)) {
                return false;
            }

            System.out.println("Dropped: " + transferData);
            int sessionHashCode = transferData.getInfo();
            if (sessionHashCode == 0) {
                System.out.println("Session hash code: " + sessionHashCode);
                return true;
            }

            if (info != null && info.hashCode() == sessionHashCode) {
                if (holder.transferMode == Constants.TransferMode.BACKGROUND) {
                    this.getHolder().downloadInBackground(transferData.getFiles(), currentPath, holder.conflictAction);
                    return true;
                }
                FileSystem sourceFs = this.getSSHFileSystem();
                if (sourceFs == null) {
                    return false;
                }
                FileSystem targetFs = currentFileSystem;
                this.newFileTransfer(sourceFs, targetFs, transferData.getFiles(), currentPath, this.hashCode(),
                        holder.conflictAction, this.getSessionInstance());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
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
        public Constants.TransferMode transferMode;
        public Constants.ConflictAction conflictAction;
    }
}
