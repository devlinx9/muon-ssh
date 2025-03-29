package muon.app.ui.components.session.files.ssh;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileSystem;
import muon.app.common.local.LocalFileSystem;
import muon.app.ssh.OperationCancelledException;
import muon.app.ssh.RemoteSessionInstance;
import muon.app.ssh.SshFileSystem;
import muon.app.ui.components.session.files.AbstractFileBrowserView;
import muon.app.ui.components.session.files.FileBrowser;
import muon.app.ui.components.session.files.view.AddressBar;
import muon.app.ui.components.session.files.view.DndTransferData;
import muon.app.ui.components.session.files.view.DndTransferHandler;
import muon.app.util.PathUtils;
import muon.app.util.enums.DndSourceType;
import muon.app.util.enums.PanelOrientation;
import muon.app.util.enums.TransferAction;
import muon.app.util.enums.TransferMode;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;



@Slf4j
public class SshFileBrowserView extends AbstractFileBrowserView {
    private final SshMenuHandler menuHandler;
    private final JPopupMenu addressPopup;
    private final DndTransferHandler transferHandler;

    public SshFileBrowserView(FileBrowser fileBrowser, String initialPath, PanelOrientation orientation) {
        super(orientation, fileBrowser);
        this.menuHandler = new SshMenuHandler(fileBrowser, this);
        this.menuHandler.initMenuHandler(this.folderView);
        this.transferHandler = new DndTransferHandler(this.folderView, this.fileBrowser.getInfo(), this,
                                                      DndSourceType.SSH, this.fileBrowser);
        this.folderView.setTransferHandler(transferHandler);
        this.folderView.setFolderViewTransferHandler(transferHandler);
        this.addressPopup = menuHandler.createAddressPopup();
        if (initialPath == null) {
            this.path = this.fileBrowser.getInfo().getRemoteFolder();
            if (this.path != null && this.path.trim().isEmpty()) {
                this.path = null;
            }
            log.info("Path: {}", path);
        } else {
            this.path = initialPath;
        }

        this.render(path, App.getGlobalSettings().isDirectoryCache());
    }

    public void createAddressBar() {
        addressBar = new AddressBar('/', e -> {
            String selectedPath = e.getActionCommand();
            addressPopup.setName(selectedPath);
            MouseEvent me = (MouseEvent) e.getSource();
            addressPopup.show(me.getComponent(), me.getX(), me.getY());
            log.info("clicked");
        });
        if (App.getGlobalSettings().isShowPathBar()) {
            addressBar.switchToPathBar();
        } else {
            addressBar.switchToText();
        }
    }

    @Override
    public String toString() {
        return this.fileBrowser.getInfo().getName()
               + (this.path == null || this.path.isEmpty() ? "" : " [" + this.path + "]");
    }

    private String trimPath(String path) {
        if (path.equals("/")) {
            return path;
        }
        if (path.endsWith("/")) {
            String trim = path.substring(0, path.length() - 1);
            log.info("Trimmed path: {}", trim);
            return trim;
        }
        return path;
    }

    private void renderDirectory(final String path, final boolean fromCache) throws Exception {
        List<FileInfo> list = null;
        if (fromCache) {
            list = this.fileBrowser.getSSHDirectoryCache().get(trimPath(path));
        }
        if (list == null) {
            list = this.fileBrowser.getSSHFileSystem().list(path);
            if (list != null) {
                this.fileBrowser.getSSHDirectoryCache().put(trimPath(path), list);
            }
        }
        if (list != null) {
            final List<FileInfo> list2 = list;
            log.info("New file list: {}", list2);
            SwingUtilities.invokeLater(() -> {
                addressBar.setText(path);
                folderView.setItems(list2);
                tabTitle.getCallback().accept(PathUtils.getFileName(path));
                int tc = list2.size();
                String text = String.format("Total %d remote file(s)", tc);
                fileBrowser.updateRemoteStatus(text);
            });
        }
    }

    @Override
    public void render(String path, boolean useCache) {
        log.debug("Rendering: {} caching: {}", path, useCache);
        this.path = path;
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            this.fileBrowser.disableUi();
            try {
                while (!fileBrowser.isCloseRequested()) {
                    log.info("Listing files now ...");
                    try {
                        if (path == null) {
                            SshFileSystem sshfs = this.fileBrowser.getSSHFileSystem();
                            this.path = sshfs.getHome();
                        }
                        renderDirectory(this.path, useCache);
                        break;
                    } catch (OperationCancelledException e) {
                        log.error(e.getMessage(), e);

                        break;
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                        if (this.fileBrowser.isSessionClosed()) {
                            return;
                        }
                        log.info("Exception caught in sftp file browser: {}", e.getMessage());

                        this.fileBrowser.getHolder().reconnect();

                        log.error(e.getMessage(), e);
                        if (JOptionPane.showConfirmDialog(null,
                                                          "Unable to connect to server " + this.fileBrowser.getInfo().getName() + " at "
                                                          + this.fileBrowser.getInfo().getHost()
                                                          + (e.getMessage() != null ? "\n\nReason: " + e.getMessage() : "\n")
                                                          + "\n\nDo you want to retry?") == JOptionPane.YES_OPTION) {
                            continue;
                        }
                        break;
                    }
                }
            } finally {
                this.fileBrowser.enableUi();
            }
        });
    }

    @Override
    public void render(String path) {
        this.render(path, false);
    }

    @Override
    public void openApp(FileInfo file) {

        FileInfo fileInfo = folderView.getSelectedFiles()[0];
        try {
            App.getExternalEditorHandler().openRemoteFile(fileInfo, fileBrowser.getSSHFileSystem(),
                                                          fileBrowser.getActiveSessionId(), false, null);
        } catch (IOException e1) {
            log.error(e1.getMessage(), e1);
        }

    }

    protected void up() {
        if (path != null) {
            String parent = PathUtils.getParent(path);
            addBack(path);
            render(parent, App.getGlobalSettings().isDirectoryCache());
        }
    }

    protected void home() {
        addBack(path);
        render(null, App.getGlobalSettings().isDirectoryCache());
    }

    @Override
    public void install(JComponent c) {

    }

    @Override
    public boolean createMenu(JPopupMenu popup, FileInfo[] files) {
        if (this.path == null) {
            return false;
        }
        return menuHandler.createMenu(popup, files);
    }

    public boolean handleDrop(DndTransferData transferData) {
        if (App.getGlobalSettings().isConfirmBeforeMoveOrCopy()
            && JOptionPane.showConfirmDialog(null, App.getCONTEXT().getBundle().getString("move_copy_files")) != JOptionPane.YES_OPTION) {
            return false;
        }
        try {
            int sessionHashCode = transferData.getInfo();
            log.info("Session hash code: {}", sessionHashCode);
            FileSystem sourceFs = null;
            if (sessionHashCode == 0 && transferData.getSourceType() == DndSourceType.LOCAL) {
                log.info("Source fs is local");
                sourceFs = new LocalFileSystem();
            } else if (transferData.getSourceType() == DndSourceType.SSH
                       && sessionHashCode == this.fileBrowser.getInfo().hashCode()) {
                log.info("Source fs is remote");
                sourceFs = this.fileBrowser.getSSHFileSystem();
            }

            if (sourceFs instanceof LocalFileSystem) {
                log.debug("Dropped: {}", transferData);
                FileBrowser.ResponseHolder holder = new FileBrowser.ResponseHolder();
                if (!this.fileBrowser.selectTransferModeAndConflictAction(holder)) {
                    return false;
                }
                if (holder.transferMode == TransferMode.BACKGROUND) {
                    this.fileBrowser.getHolder().uploadInBackground(transferData.getFiles(), this.path,
                                                                    holder.conflictAction);
                    return true;
                }
                FileSystem targetFs = this.fileBrowser.getSSHFileSystem();
                this.fileBrowser.newFileTransfer(sourceFs, targetFs, transferData.getFiles(), this.path,
                                                 this.hashCode(), holder.conflictAction, null);
            } else if (sourceFs instanceof SshFileSystem && (sourceFs == this.fileBrowser.getSSHFileSystem())) {
                log.info("SshFs is of same instance: {}", sourceFs == this.fileBrowser.getSSHFileSystem());
                if (transferData.getFiles().length > 0) {
                    FileInfo fileInfo = transferData.getFiles()[0];
                    String parent = PathUtils.getParent(fileInfo.getPath());
                    log.info("Parent: {} == {}", parent, this.getCurrentDirectory());
                    if (!Objects.requireNonNull(parent).endsWith("/")) {
                        parent += "/";
                    }
                    String pwd = this.getCurrentDirectory();
                    if (!pwd.endsWith("/")) {
                        pwd += "/";
                    }
                    if (parent.equals(pwd)) {
                        JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("same_directory"));
                        return false;
                    }
                }

                if (transferData.getTransferAction() == TransferAction.COPY) {
                    menuHandler.copy(Arrays.asList(transferData.getFiles()), getCurrentDirectory());
                } else {
                    menuHandler.move(Arrays.asList(transferData.getFiles()), getCurrentDirectory());
                }
            } else if (sourceFs instanceof SshFileSystem
                       && (transferData.getSourceType() == DndSourceType.SFTP)) {
            }
            log.info("12345: {} {}", sourceFs instanceof SshFileSystem, transferData.getSourceType());
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }
    }

    public FileSystem getFileSystem() {
        return this.fileBrowser.getSSHFileSystem();
    }

    public RemoteSessionInstance getSshClient() {
        return this.fileBrowser.getSessionInstance();
    }

    @Override
    public TransferHandler getTransferHandler() {
        return transferHandler;
    }

    public String getHostText() {
        return this.fileBrowser.getInfo().getName();
    }

    public String getPathText() {
        return (this.path == null || this.path.isEmpty() ? "" : this.path);
    }

}
