package muon.app.ui.components.session.files.local;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileSystem;
import muon.app.common.local.LocalFileSystem;
import muon.app.ui.components.session.files.AbstractFileBrowserView;
import muon.app.ui.components.session.files.FileBrowser;
import muon.app.ui.components.session.files.view.AddressBar;
import muon.app.ui.components.session.files.view.DndTransferData;
import muon.app.ui.components.session.files.view.DndTransferHandler;
import util.PathUtils;
import util.enums.DndSourceType;
import util.enums.PanelOrientation;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class LocalFileBrowserView extends AbstractFileBrowserView {
    private final LocalMenuHandler menuHandler;
    private final JPopupMenu addressPopup;
    private LocalFileSystem fs;

    public LocalFileBrowserView(FileBrowser fileBrowser, String initialPath, PanelOrientation orientation) {
        super(orientation, fileBrowser);
        this.menuHandler = new LocalMenuHandler(fileBrowser, this);
        this.menuHandler.initMenuHandler(this.folderView);
        DndTransferHandler transferHandler = new DndTransferHandler(this.folderView, null, this, DndSourceType.LOCAL,
                                                                    this.fileBrowser);
        this.folderView.setTransferHandler(transferHandler);
        this.folderView.setFolderViewTransferHandler(transferHandler);
        this.addressPopup = menuHandler.createAddressPopup();

        if (this.fileBrowser.getInfo().getLocalFolder() != null && this.fileBrowser.getInfo().getLocalFolder().trim().length() > 1) {
            this.path = fileBrowser.getInfo().getLocalFolder();
        } else if (initialPath != null) {
            this.path = initialPath;
        }

        log.info("Path: {}", path);
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            try {
                this.fs = new LocalFileSystem();
                //Validate if local path exists, if not set the home path
                if (this.path == null || Files.notExists(Paths.get(this.path)) || !Files.isDirectory(Paths.get(this.path))) {
                    log.error("The file path doesn't exists {}", this.path);
                    log.info("Setting to {}", fs.getHome());

                    path = fs.getHome();
                }
                List<FileInfo> list = fs.list(path);
                SwingUtilities.invokeLater(() -> {
                    addressBar.setText(path);
                    folderView.setItems(list);
                    tabTitle.getCallback().accept(PathUtils.getFileName(path).concat(" (local)"));
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public void createAddressBar() {
        addressBar = new AddressBar(File.separatorChar, e -> {
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
        return "Local files [" + this.path + "]";
    }

    public String getHostText() {
        return "Local files";
    }

    public String getPathText() {
        return (this.path == null || this.path.isEmpty() ? "" : this.path);
    }

    @Override
    public void render(String path, boolean useCache) {
        this.render(path);
    }

    @Override
    public void render(String path) {
        this.path = path;
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (this.path == null) {
                    this.path = fs.getHome();
                }
                List<FileInfo> list = fs.list(this.path);
                SwingUtilities.invokeLater(() -> {
                    addressBar.setText(this.path);
                    folderView.setItems(list);
                    int tc = list.size();
                    String text = String.format("Total %d remote file(s)", tc);
                    fileBrowser.updateRemoteStatus(text);
                    tabTitle.getCallback().accept(PathUtils.getFileName(this.path).concat(" (local)"));
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            fileBrowser.enableUi();
        });
    }

    @Override
    public void openApp(FileInfo file) {
    }

    @Override
    public boolean createMenu(JPopupMenu popup, FileInfo[] files) {
        menuHandler.createMenu(popup, files);
        return true;
    }

    protected void up() {
        String s = new File(path).getParent();
        if (s != null) {
            addBack(path);
            render(s);
        }
    }

    protected void home() {
        addBack(path);
        render(null);
    }

    @Override
    public void install(JComponent c) {

    }

    public boolean handleDrop(DndTransferData transferData) {
        log.info("### {} {}", transferData.getSource(), this.hashCode());
        if (transferData.getSource() == this.hashCode()) {
            return false;
        }
        return this.fileBrowser.handleLocalDrop(transferData, fileBrowser.getInfo(), this.fs, this.path);
    }

    public FileSystem getFileSystem() {
        return new LocalFileSystem();
    }
}
