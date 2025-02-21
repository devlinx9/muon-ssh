package muon.app.ui.components.session.files.view;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.local.LocalFileSystem;
import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.files.AbstractFileBrowserView;
import muon.app.ui.components.session.files.FileBrowser;
import muon.app.util.Win32DragHandler;
import muon.app.util.enums.DndSourceType;
import muon.app.util.enums.FileType;

import javax.swing.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class DndTransferHandler extends TransferHandler implements Transferable {
    public static final DataFlavor DATA_FLAVOR_DATA_FILE = new DataFlavor(DndTransferData.class, "data-file");
    public static final DataFlavor DATA_FLAVOR_FILE_LIST = DataFlavor.javaFileListFlavor;
    private final FolderView folderView;
    private final SessionInfo info;
    private final AbstractFileBrowserView fileBrowserView;
    private DndTransferData transferData;
    private final DndSourceType sourceType;
    private Win32DragHandler win32DragHandler;
    private File tempDir;
    private final FileBrowser fileBrowser;

    public DndTransferHandler(FolderView folderView, SessionInfo info, AbstractFileBrowserView fileBrowserView,
                              DndSourceType sourceType, FileBrowser fileBrowser) {
        this.folderView = folderView;
        this.fileBrowser = fileBrowser;
        this.info = info;
        this.fileBrowserView = fileBrowserView;
        this.sourceType = sourceType;
    }

    @Override
    public void exportAsDrag(JComponent comp, InputEvent e, int action) {
        if (info != null) {
            if (App.IS_WINDOWS) {
                try {
                    this.tempDir = Files.createTempDirectory(App.APP_INSTANCE_ID).toFile();
                    log.info("New monitor");
                    this.win32DragHandler = new Win32DragHandler();
                    this.win32DragHandler.listenForDrop(tempDir.getName(), file -> {
                        log.error("Dropped on {}", file.getParent());
                        this.fileBrowser.handleLocalDrop(transferData, info, new LocalFileSystem(), file.getParent());
                    });
                } catch (IOException e1) {
                    log.error(e1.getMessage(), e1);
                }
            }
        }

        DndTransferData data = new DndTransferData(info == null ? 0 : info.hashCode(), folderView.getSelectedFiles(),
                this.fileBrowserView.getCurrentDirectory(), this.fileBrowserView.hashCode(), sourceType);
        log.info("Exporting drag {} hashcode: {}", data, data.hashCode());
        this.transferData = data;
        super.exportAsDrag(comp, e, action);
    }

    @Override
    public boolean canImport(TransferSupport support) {

        log.debug("Data flavors: {}", support.getDataFlavors().length);
        boolean isDataFile = false, isJavaFileList = false;
        for (DataFlavor f : support.getDataFlavors()) {
            log.debug("Data flavor: {}", f);
            if (f.isFlavorJavaFileListType()) {
                isJavaFileList = this.info != null;
            }
            if (DATA_FLAVOR_DATA_FILE.equals(f)) {
                isDataFile = true;
            }
        }

        try {
            log.debug("Dropped java file list: {}", isJavaFileList);
            if (isDataFile) {
                if (support.isDataFlavorSupported(DATA_FLAVOR_DATA_FILE)) {
                    return (support.getTransferable()
                            .getTransferData(DATA_FLAVOR_DATA_FILE) instanceof DndTransferData);
                }
            } else if (isJavaFileList) {
                return true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.error("drop not supported");
        return false;

    }

    @Override
    public int getSourceActions(JComponent c) {
        return TransferHandler.COPY;
    }

    protected void exportDone(JComponent c, Transferable data, int action) {
        log.info("Export complete: {} {}", action, Arrays.asList(data.getTransferDataFlavors()));
        if (this.win32DragHandler != null) {
            this.win32DragHandler.dispose();
        }
    }

    /**
     * When importing always DATA_FLAVOR_DATA_FILE will be preferred over file list
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean importData(TransferSupport info) {
        if (!info.isDrop()) {
            return false;
        }

        boolean isDataFile = false, isJavaFileList = false;
        for (DataFlavor f : info.getDataFlavors()) {
            log.debug("Data flavor: {}", f);
            if (f.isFlavorJavaFileListType()) {
                isJavaFileList = this.info != null;
            }
            if (DATA_FLAVOR_DATA_FILE.equals(f)) {
                isDataFile = true;
            }
        }

        Transferable t = info.getTransferable();

        if (isDataFile) {
            try {
                DndTransferData transferData = (DndTransferData) t.getTransferData(DATA_FLAVOR_DATA_FILE);
                return this.fileBrowserView.handleDrop(transferData);
            } catch (UnsupportedFlavorException | IOException e) {
                log.error(e.getMessage(), e);
            }
        } else if (isJavaFileList) {
            try {
                List<File> fileList = ((List<File>) t.getTransferData(DataFlavor.javaFileListFlavor));
                FileInfo[] infoArr = new FileInfo[fileList.size()];
                int c = 0;
                for (File file : fileList) {

                    if (file.getName().startsWith(App.APP_INSTANCE_ID)) {
                        log.info("Internal fake folder dropped");
                        return false;
                    }

                    Path p = file.toPath();
                    BasicFileAttributes attrs = null;
                    try {
                        attrs = Files.readAttributes(p, BasicFileAttributes.class);
                    } catch (IOException e) {
                        log.error(e.getMessage(), e);
                    }
                    FileInfo finfo = new FileInfo(file.getName(), file.getAbsolutePath(), file.length(),
                                                  file.isDirectory() ? FileType.DIRECTORY : FileType.FILE, file.lastModified(), -1,
                                                  LocalFileSystem.PROTO_LOCAL_FILE, "",
                            attrs != null ? attrs.creationTime().toMillis() : file.lastModified(), "",
                                                  file.isHidden());
                    infoArr[c++] = finfo;
                }

                DndTransferData data = new DndTransferData(0, infoArr, this.fileBrowserView.getCurrentDirectory(),
                        this.fileBrowserView.hashCode(), DndSourceType.LOCAL);
                log.info("Exporting drag {} hashcode: {}", data, data.hashCode());
                return this.fileBrowserView.handleDrop(data);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        return false;
    }

    @Override
    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[]{DATA_FLAVOR_DATA_FILE, DATA_FLAVOR_FILE_LIST};
    }

    @Override
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        if (this.info != null) {
            return DATA_FLAVOR_DATA_FILE.equals(flavor) || DATA_FLAVOR_FILE_LIST.equals(flavor);
        } else {
            return DATA_FLAVOR_DATA_FILE.equals(flavor);
        }
    }

    @Override
    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
        if (DATA_FLAVOR_DATA_FILE.equals(flavor)) {
            return this.transferData;
        }

        if (DATA_FLAVOR_FILE_LIST.equals(flavor)) {
            if (App.IS_WINDOWS && tempDir != null) {
                return List.of(tempDir);
            }
        }
        return null;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        return this;
    }
}
