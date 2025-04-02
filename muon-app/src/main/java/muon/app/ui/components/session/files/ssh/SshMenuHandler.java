package muon.app.ui.components.session.files.ssh;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.local.LocalFileSystem;
import muon.app.ui.components.session.BookmarkManager;
import muon.app.ui.components.session.files.FileBrowser;
import muon.app.ui.components.session.files.local.LocalFileBrowserView;
import muon.app.ui.components.session.files.remote2remote.LocalPipeTransfer;
import muon.app.ui.components.session.files.remote2remote.Remote2RemoteTransferDialog;
import muon.app.ui.components.session.files.view.DndTransferData;
import muon.app.ui.components.session.files.view.DndTransferHandler;
import muon.app.ui.components.session.files.view.FolderView;
import muon.app.ui.components.settings.EditorEntry;
import muon.app.ui.components.settings.SettingsPageName;
import muon.app.util.OptionPaneUtils;
import muon.app.util.PathUtils;
import muon.app.util.enums.DndSourceType;
import muon.app.util.enums.FileType;
import muon.app.util.enums.TransferAction;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static muon.app.App.getAppWindow;
import static muon.app.util.PlatformUtils.IS_WINDOWS;

@Slf4j
public class SshMenuHandler {
    private final FileBrowser fileBrowser;
    private final SshFileOperations fileOperations;
    private final SshFileBrowserView fileBrowserView;
    private final ArchiveOperation archiveOperation;
    private JMenuItem mOpenInTab;
    private JMenuItem mOpen;
    private JMenuItem mRename;
    private JMenuItem mDelete;
    private JMenuItem mNewFile;
    private JMenuItem mNewFolder;
    private JMenuItem mCopy;
    private JMenuItem mPaste;
    private JMenuItem mCut;
    private JMenuItem mAddToFav;
    private JMenuItem mChangePerm;
    private JMenuItem mUpload;
    private JMenuItem mEditorConfig;
    private JMenuItem mOpenWithLogView;
    private JMenuItem mDownload;
    private JMenuItem mCreateLink;
    private JMenuItem mCopyPath;
    private JMenuItem mOpenFolderInTerminal;
    private JMenuItem mOpenTerminalHere;
    private JMenuItem mRunScriptInTerminal;
    private JMenuItem mRunScriptInBackground;
    private JMenuItem mExtractHere;
    private JMenuItem mExtractTo;
    private JMenuItem mCreateArchive;
    private JMenuItem mOpenWithMenu;
    private JMenu mEditWith;
    private JMenu mSendTo;
    private FolderView folderView;

    public SshMenuHandler(FileBrowser fileBrowser, SshFileBrowserView fileBrowserView) {
        this.fileBrowser = fileBrowser;
        this.fileOperations = new SshFileOperations();
        this.fileBrowserView = fileBrowserView;
        this.archiveOperation = new ArchiveOperation();
    }

    public void initMenuHandler(FolderView folderView) {
        this.folderView = folderView;
        InputMap map = folderView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap act = folderView.getActionMap();
        this.initMenuItems(map, act);
    }

    private void initMenuItems(InputMap map, ActionMap act) {
        KeyStroke ksOpenInTab = KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK);
        mOpenInTab = new JMenuItem(App.getCONTEXT().getBundle().getString("open_in_tab"));
        mOpenInTab.setAccelerator(ksOpenInTab);
        AbstractAction aOpenInTab = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewTab();
            }
        };
        mOpenInTab.addActionListener(aOpenInTab);
        map.put(ksOpenInTab, "ksOpenInTab");
        act.put("ksOpenInTab", aOpenInTab);

        AbstractAction aOpen = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("Open app");
                FileInfo fileInfo = folderView.getSelectedFiles()[0];
                try {
                    App.getExternalEditorHandler().openRemoteFile(fileInfo, fileBrowser.getSSHFileSystem(),
                                                                  fileBrowser.getActiveSessionId(), false, null);
                } catch (IOException e1) {

                    log.error(e1.getMessage(), e1);
                }
            }
        };
        KeyStroke ksOpen = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
        mOpen = new JMenuItem(App.getCONTEXT().getBundle().getString("open"));
        mOpen.addActionListener(aOpen);
        map.put(ksOpen, "mOpen");
        act.put("mOpen", aOpen);
        mOpen.setAccelerator(ksOpen);


        if (IS_WINDOWS) {
            mOpenWithMenu = new JMenuItem(App.getCONTEXT().getBundle().getString("open_with"));
            mOpenWithMenu.addActionListener(e -> {
                FileInfo fileInfo = folderView.getSelectedFiles()[0];
                try {
                    log.info("Called open with");
                    App.getExternalEditorHandler().openRemoteFile(fileInfo, fileBrowser.getSSHFileSystem(),
                                                                  fileBrowser.getActiveSessionId(), true, null);
                } catch (IOException e1) {

                    log.error(e1.getMessage(), e1);
                }
            });
        }

        mEditorConfig = new JMenuItem(App.getCONTEXT().getBundle().getString("configure_editor"));
        mEditorConfig.addActionListener(e -> openEditorConfig());

        mOpenWithLogView = new JMenuItem(App.getCONTEXT().getBundle().getString("open_log_viewer"));
        mOpenWithLogView.addActionListener(e -> openLogViewer());

        mEditWith = new JMenu(App.getCONTEXT().getBundle().getString("edit_with"));

        mSendTo = new JMenu(App.getCONTEXT().getBundle().getString("send_another_server"));

        JMenuItem mSendViaSSH = new JMenuItem(App.getCONTEXT().getBundle().getString("send_over_ftp"));
        mSendViaSSH.addActionListener(e -> this.sendFilesViaSSH());
        JMenuItem mSendViaLocal = new JMenuItem(App.getCONTEXT().getBundle().getString("send_this_computer"));
        mSendViaLocal.addActionListener(e -> this.sendFilesViaLocal());

        mSendTo.add(mSendViaSSH);
        mSendTo.add(mSendViaLocal);

        mRunScriptInTerminal = new JMenuItem(App.getCONTEXT().getBundle().getString("run_in_terminal"));
        mRunScriptInTerminal.addActionListener(e -> {

        });

        mOpenFolderInTerminal = new JMenuItem(App.getCONTEXT().getBundle().getString("open_folder_terminal"));
        mOpenFolderInTerminal.addActionListener(e -> openFolderInTerminal(folderView.getSelectedFiles()[0].getPath()));

        mOpenTerminalHere = new JMenuItem(App.getCONTEXT().getBundle().getString("open_terminal_here"));
        mOpenTerminalHere.addActionListener(e -> openFolderInTerminal(fileBrowserView.getCurrentDirectory()));

        mRunScriptInTerminal = new JMenuItem(App.getCONTEXT().getBundle().getString("run_file_in_terminal"));
        mRunScriptInTerminal.addActionListener(e -> openRunInTerminal(fileBrowserView.getCurrentDirectory(), folderView.getSelectedFiles()[0].getPath()));

        mRunScriptInBackground = new JMenuItem(App.getCONTEXT().getBundle().getString("run_file_in_background"));
        mRunScriptInBackground.addActionListener(e -> openRunInBackground(fileBrowserView.getCurrentDirectory(), folderView.getSelectedFiles()[0].getPath()));

        KeyStroke ksRename = KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0);
        AbstractAction aRename = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rename(folderView.getSelectedFiles()[0], fileBrowserView.getCurrentDirectory());
            }
        };
        mRename = new JMenuItem(App.getCONTEXT().getBundle().getString("rename"));
        mRename.addActionListener(aRename);
        map.put(ksRename, "mRename");
        act.put("mRename", aRename);
        mRename.setAccelerator(ksRename);

        KeyStroke ksDelete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        AbstractAction aDelete = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete(folderView.getSelectedFiles(), fileBrowserView.getCurrentDirectory());
            }
        };
        mDelete = new JMenuItem(App.getCONTEXT().getBundle().getString("delete"));
        mDelete.addActionListener(aDelete);
        map.put(ksDelete, "ksDelete");
        act.put("ksDelete", aDelete);
        mDelete.setAccelerator(ksDelete);

        KeyStroke ksNewFile = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        AbstractAction aNewFile = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFile(fileBrowserView.getCurrentDirectory(), folderView.getFiles());
            }
        };
        mNewFile = new JMenuItem(App.getCONTEXT().getBundle().getString("new_file"));
        mNewFile.addActionListener(aNewFile);
        map.put(ksNewFile, "ksNewFile");
        act.put("ksNewFile", aNewFile);
        mNewFile.setAccelerator(ksNewFile);

        KeyStroke ksNewFolder = KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK | InputEvent.ALT_DOWN_MASK);
        AbstractAction aNewFolder = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFolder(fileBrowserView.getCurrentDirectory(), folderView.getFiles());
            }
        };
        mNewFolder = new JMenuItem(App.getCONTEXT().getBundle().getString("new_folder"));
        mNewFolder.addActionListener(aNewFolder);
        mNewFolder.setAccelerator(ksNewFolder);
        map.put(ksNewFolder, "ksNewFolder");
        act.put("ksNewFolder", aNewFolder);

        KeyStroke ksCopy = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK);
        AbstractAction aCopy = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard(false);
            }
        };
        mCopy = new JMenuItem(App.getCONTEXT().getBundle().getString("copy"));
        mCopy.addActionListener(aCopy);
        map.put(ksCopy, "ksCopy");
        act.put("ksCopy", aCopy);
        mCopy.setAccelerator(ksCopy);

        KeyStroke ksCopyPath = KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        AbstractAction aCopyPath = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyPathToClipboard();
            }
        };
        mCopyPath = new JMenuItem(App.getCONTEXT().getBundle().getString("copy_path"));
        mCopyPath.addActionListener(aCopyPath);
        map.put(ksCopyPath, "ksCopyPath");
        act.put("ksCopyPath", aCopyPath);
        mCopyPath.setAccelerator(ksCopyPath);

        KeyStroke ksPaste = KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK);
        AbstractAction aPaste = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handlePaste();
            }
        };
        mPaste = new JMenuItem(App.getCONTEXT().getBundle().getString("paste"));
        mPaste.addActionListener(aPaste);
        map.put(ksPaste, "ksPaste");
        act.put("ksPaste", aPaste);
        mPaste.setAccelerator(ksPaste);

        KeyStroke ksCut = KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK);
        AbstractAction aCut = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyToClipboard(true);
            }
        };
        mCut = new JMenuItem(App.getCONTEXT().getBundle().getString("cut"));
        mCut.addActionListener(aCut);
        map.put(ksCut, "ksCut");
        act.put("ksCut", aCut);
        mCut.setAccelerator(ksCut);

        KeyStroke ksAddToFav = KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        AbstractAction aAddToFav = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToFavourites();
            }
        };
        mAddToFav = new JMenuItem(App.getCONTEXT().getBundle().getString("bookmark"));
        mAddToFav.addActionListener(aAddToFav);
        map.put(ksAddToFav, "ksAddToFav");
        act.put("ksAddToFav", aAddToFav);
        mAddToFav.setAccelerator(ksAddToFav);

        KeyStroke ksChangePerm = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, InputEvent.ALT_DOWN_MASK);
        AbstractAction aChangePerm = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changePermission(folderView.getSelectedFiles(), fileBrowserView.getCurrentDirectory());
            }
        };
        mChangePerm = new JMenuItem(App.getCONTEXT().getBundle().getString("properties"));
        mChangePerm.addActionListener(aChangePerm);
        map.put(ksChangePerm, "ksChangePerm");
        act.put("ksChangePerm", aChangePerm);
        mChangePerm.setAccelerator(ksChangePerm);

        KeyStroke ksCreateLink = KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK);
        AbstractAction aCreateLink = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createLink(fileBrowserView.getCurrentDirectory(), folderView.getSelectedFiles());
            }
        };
        mCreateLink = new JMenuItem(App.getCONTEXT().getBundle().getString("create_link"));
        mCreateLink.addActionListener(aCreateLink);
        map.put(ksCreateLink, "ksCreateLink");
        act.put("ksCreateLink", aCreateLink);
        mCreateLink.setAccelerator(ksCreateLink);

        mExtractHere = new JMenuItem(App.getCONTEXT().getBundle().getString("extract_here"));
        mExtractHere.addActionListener(e -> extractArchive(folderView.getSelectedFiles()[0].getPath(), fileBrowserView.getCurrentDirectory(),
                                                           fileBrowserView.getCurrentDirectory()));

        mExtractTo = new JMenuItem(App.getCONTEXT().getBundle().getString("extract_to"));
        mExtractTo.addActionListener(e -> {
            String text = OptionPaneUtils.showInputDialog(null, App.getCONTEXT().getBundle().getString("select_target"),
                                                          fileBrowserView.getCurrentDirectory());
            if (text == null || text.isEmpty()) {
                return;
            }
            extractArchive(folderView.getSelectedFiles()[0].getPath(), text, fileBrowserView.getCurrentDirectory());
        });

        mCreateArchive = new JMenuItem(App.getCONTEXT().getBundle().getString("create_archive"));
        mCreateArchive.addActionListener(e -> {
            List<String> files = new ArrayList<>();
            for (FileInfo fileInfo : folderView.getSelectedFiles()) {
                files.add(fileInfo.getName());
            }
            createArchive(files, fileBrowserView.getCurrentDirectory(), fileBrowserView.getCurrentDirectory());
        });

        mDownload = new JMenuItem(App.getCONTEXT().getBundle().getString("download_files"));
        mDownload.addActionListener(e -> downloadFiles(folderView.getSelectedFiles(), fileBrowserView.getCurrentDirectory()));

        mUpload = new JMenuItem(App.getCONTEXT().getBundle().getString("upload_here"));
        mUpload.addActionListener(e -> {
            try {
                uploadFiles();
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }
        });
    }

    private void changePermission(FileInfo[] selectedFiles, String currentDirectory) {
        log.info("Showing property of: {}", selectedFiles.length);
        PropertiesDialog propertiesDialog = new PropertiesDialog(fileBrowser,
                                                                 SwingUtilities.windowForComponent(fileBrowserView), selectedFiles.length > 1);
        if (selectedFiles.length > 1) {
            propertiesDialog.setMultipleDetails(selectedFiles);
        } else if (selectedFiles.length == 1) {
            propertiesDialog.setDetails(selectedFiles[0]);
        } else {
            return;
        }
        propertiesDialog.setVisible(true);
    }

    private void copyToClipboard(boolean cut) {
        FileInfo[] selectedFiles = folderView.getSelectedFiles();
        DndTransferData transferData = new DndTransferData(fileBrowser.getInfo().hashCode(), selectedFiles,
                                                           fileBrowserView.getCurrentDirectory(), fileBrowserView.hashCode(), DndSourceType.SSH);
        transferData.setTransferAction(cut ? TransferAction.CUT : TransferAction.COPY);

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DndTransferHandler.DATA_FLAVOR_DATA_FILE};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return flavor.equals(DndTransferHandler.DATA_FLAVOR_DATA_FILE);
            }

            @Override
            public @NotNull Object getTransferData(DataFlavor flavor) {
                return transferData;
            }
        }, (a, b) -> {
        });
    }

    private void copyPathToClipboard() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (FileInfo f : folderView.getSelectedFiles()) {
            if (!first) {
                sb.append("\n");
            }
            sb.append(f.getPath());
            if (first) {
                first = false;
            }
        }
        if (sb.length() > 0) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(sb.toString()), null);
        }
    }

    private void openLogViewer() {
        fileBrowser.getHolder().openLog(folderView.getSelectedFiles()[0]);
    }

    public boolean createMenu(JPopupMenu popup, FileInfo[] files) {
        popup.removeAll();
        int selectionCount = files.length;
        int count = 0;
        count += createBuiltInItems1(selectionCount, popup, files);
        count += createBuiltInItems2(selectionCount, popup, files);
        return count > 0;
    }

    private int createBuiltInItems1(int selectionCount, JPopupMenu popup, FileInfo[] selectedFiles) {
        int count = 0;
        if (selectionCount == 1) {
            if (selectedFiles[0].getType() == FileType.DIRECTORY || selectedFiles[0].getType() == FileType.DIR_LINK) {
                popup.add(mOpenInTab);
                count++;
                popup.add(mOpenFolderInTerminal);
                count++;
            }

            if ((selectedFiles[0].getType() == FileType.FILE || selectedFiles[0].getType() == FileType.FILE_LINK)) {
                popup.add(mOpen);
                count++;

                if (IS_WINDOWS) {
                    popup.add(mOpenWithMenu);
                    count++;
                }

                loadEditors();
                popup.add(mEditWith);
                count++;

                popup.add(mRunScriptInTerminal);
                count++;

                popup.add(mRunScriptInBackground);
                count++;

                popup.add(mOpenWithLogView);
                count++;
            }
        }

        if (selectionCount > 0) {
            popup.add(mCut);
            popup.add(mCopy);
            popup.add(mCopyPath);
            popup.add(mDownload);
            count += 3;
        }

        if (hasSupportedContentOnClipboard()) {
            popup.add(mPaste);
        }

        if (selectionCount == 1) {
            popup.add(mRename);
            count++;
        }

        return count;
    }

    private int createBuiltInItems2(int selectionCount, JPopupMenu popup, FileInfo[] selectedFiles) {
        int count = 0;
        if (selectionCount > 0) {
            popup.add(mDelete);
            popup.add(mCreateArchive);
            popup.add(mSendTo);
            count += 3;
        }

        if (selectionCount == 1) {
            FileInfo fileInfo = selectedFiles[0];
            if ((selectedFiles[0].getType() == FileType.FILE || selectedFiles[0].getType() == FileType.FILE_LINK)
                && this.archiveOperation.isSupportedArchive(fileInfo.getName())) {
                popup.add(mExtractHere);
                popup.add(mExtractTo);
            }
            count += 2;
        }

        if (selectionCount < 1) {
            popup.add(mNewFolder);
            popup.add(mNewFile);
            popup.add(mOpenTerminalHere);
            count += 2;
        }

        if (selectionCount < 1 || (selectionCount == 1
                                   && (selectedFiles[0].getType() == FileType.FILE || selectedFiles[0].getType() == FileType.FILE_LINK))) {
            popup.add(mUpload);
            count += 1;
        }

        // check only if folder is selected
        boolean allFolder = true;
        for (FileInfo f : selectedFiles) {
            if (f.getType() != FileType.DIRECTORY && f.getType() != FileType.DIR_LINK) {
                allFolder = false;
                break;
            }
        }

        popup.add(mAddToFav);
        count++;

        if (selectionCount <= 1) {
            popup.add(mCreateLink);
            count++;
        }

        if (selectionCount >= 1) {
            popup.add(mChangePerm);
            count++;
        }
        return count;
    }

    public void openNewTab() {
        FileInfo[] files = folderView.getSelectedFiles();
        if (files.length == 1) {
            FileInfo file = files[0];
            if (file.getType() == FileType.DIRECTORY || file.getType() == FileType.DIR_LINK) {
                fileBrowser.openSshFileBrowserView(file.getPath(), this.fileBrowserView.getOrientation());
            }
        }
    }

    private void rename(FileInfo info, String baseFolder) {
        String text = OptionPaneUtils.showInputDialog(App.getAppWindow(), App.getCONTEXT().getBundle().getString("enter_new_name"), info.getName(), info.getName());
        if (text != null && !text.isEmpty() && !text.equals(info.getName())) {
            renameAsync(info.getPath(), PathUtils.combineUnix(PathUtils.getParent(info.getPath()), text), baseFolder);
        }
    }

    private void renameAsync(String oldName, String newName, String baseFolder) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (fileOperations.rename(oldName, newName, fileBrowserView.getFileSystem(),
                                          fileBrowserView.getSshClient(), fileBrowser.getInfo().getPassword())) {
                    fileBrowserView.render(baseFolder);
                } else {
                    fileBrowser.enableUi();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                fileBrowser.enableUi();
            }
        });
    }

    private void delete(FileInfo[] targetList, String baseFolder) {
        boolean delete = true;
        if (App.getGlobalSettings().isConfirmBeforeDelete()) {
            delete = JOptionPane.showConfirmDialog(getAppWindow(), App.getCONTEXT().getBundle().getString("delete_selected_files")) == JOptionPane.YES_OPTION;
        }
        if (!delete) {
            return;
        }
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (fileOperations.delete(targetList, fileBrowserView.getFileSystem(),
                                          fileBrowserView.getSshClient(), fileBrowser.getInfo().getPassword())) {
                    fileBrowserView.render(baseFolder);
                } else {
                    fileBrowser.enableUi();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                fileBrowser.enableUi();
            }

        });
    }

    public void newFile(String baseFolder, FileInfo[] files) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (fileOperations.newFile(files, fileBrowserView.getFileSystem(), baseFolder,
                                           fileBrowserView.getSshClient(), fileBrowser.getInfo().getPassword())) {
                    fileBrowserView.render(baseFolder);
                } else {
                    fileBrowser.enableUi();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                fileBrowser.enableUi();
            }

        });
    }

    public void newFolder(String baseFolder, FileInfo[] files) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (fileOperations.newFolder(files, baseFolder, fileBrowserView.getFileSystem(),
                                             fileBrowserView.getSshClient(), fileBrowser.getInfo().getPassword())) {
                    fileBrowserView.render(baseFolder);
                } else {
                    fileBrowser.enableUi();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                fileBrowser.enableUi();
            }

        });
    }

    public void createLink(String baseFolder, FileInfo[] files) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (fileOperations.createLink(files, fileBrowserView.getFileSystem(), fileBrowserView.getSshClient())) {
                    fileBrowserView.render(baseFolder);
                } else {
                    fileBrowser.enableUi();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                fileBrowser.enableUi();
            }
        });
    }

    private void handlePaste() {
        if (Toolkit.getDefaultToolkit().getSystemClipboard().isDataFlavorAvailable(DndTransferHandler.DATA_FLAVOR_DATA_FILE)) {
            try {
                DndTransferData transferData = (DndTransferData) Toolkit.getDefaultToolkit().getSystemClipboard()
                        .getData(DndTransferHandler.DATA_FLAVOR_DATA_FILE);
                if (transferData != null) {
                    fileBrowserView.handleDrop(transferData);
                }
            } catch (UnsupportedFlavorException | IOException e1) {
                log.error(e1.getMessage(), e1);
            }
        } else {
            DataFlavor[] flavors = Toolkit.getDefaultToolkit().getSystemClipboard().getAvailableDataFlavors();
            for (DataFlavor flavor : flavors) {
                if (flavor.isFlavorJavaFileListType()) {

                }
            }
        }
    }

    private boolean hasSupportedContentOnClipboard() {
        boolean ret = (Toolkit.getDefaultToolkit().getSystemClipboard()
                               .isDataFlavorAvailable(DndTransferHandler.DATA_FLAVOR_DATA_FILE)
                       || Toolkit.getDefaultToolkit().getSystemClipboard()
                               .isDataFlavorAvailable(DataFlavor.javaFileListFlavor));
        if (!ret) {
            log.debug("Nothing on clipboard");
        }
        return ret;
    }

    public void copy(List<FileInfo> files, String targetFolder) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (fileOperations.copyTo(fileBrowserView.getSshClient(), files, targetFolder,
                                          fileBrowserView.getFileSystem(), fileBrowser.getInfo().getPassword())) {
                    fileBrowserView.render(targetFolder);
                } else {
                    fileBrowser.enableUi();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public void move(List<FileInfo> files, String targetFolder) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (fileOperations.moveTo(fileBrowserView.getSshClient(), files, targetFolder,
                                          fileBrowserView.getFileSystem(), fileBrowser.getInfo().getPassword())) {
                    fileBrowserView.render(targetFolder);
                } else {
                    fileBrowser.enableUi();
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    private void addToFavourites() {
        FileInfo[] arr = folderView.getSelectedFiles();

        if (arr.length > 0) {
            BookmarkManager.addEntry(fileBrowser.getInfo().getId(),
                                     Arrays.stream(arr)
                                             .filter(a -> a.getType() == FileType.DIR_LINK || a.getType() == FileType.DIRECTORY)
                                             .map(FileInfo::getPath).collect(Collectors.toList()));
        } else {
            BookmarkManager.addEntry(fileBrowser.getInfo().getId(), fileBrowserView.getCurrentDirectory());
        }

        this.fileBrowserView.getOverflowMenuHandler().loadFavourites();
    }

    public JPopupMenu createAddressPopup() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mOpenInNewTab = new JMenuItem(App.getCONTEXT().getBundle().getString("open_new_tab"));
        JMenuItem mCopyPathAddressPopup = new JMenuItem(App.getCONTEXT().getBundle().getString("copy_path"));
        JMenuItem mOpenInTerminal = new JMenuItem(App.getCONTEXT().getBundle().getString("open_in_terminal"));
        JMenuItem mBookmark = new JMenuItem(App.getCONTEXT().getBundle().getString("bookmark"));
        popupMenu.add(mOpenInNewTab);
        popupMenu.add(mCopyPathAddressPopup);
        popupMenu.add(mOpenInTerminal);
        popupMenu.add(mBookmark);

        mOpenInNewTab.addActionListener(e -> {
            String path = popupMenu.getName();
            fileBrowser.openSshFileBrowserView(path, this.fileBrowserView.getOrientation());
        });

        mOpenInTerminal.addActionListener(e -> {
            String path = popupMenu.getName();
            this.openFolderInTerminal(path);
        });

        mCopyPathAddressPopup.addActionListener(e -> {
            String path = popupMenu.getName();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(path), null);
        });

        mBookmark.addActionListener(e -> addToFavourites());
        return popupMenu;
    }

    private void openFolderInTerminal(String folder) {
        fileBrowser.getHolder().openTerminal("cd \"" + folder + "\"");
    }

    private void openRunInTerminal(String folder, String file) {
        fileBrowser.getHolder().openTerminal("cd \"" + folder + "\"; \"" + file + "\"");
    }

    private void openRunInBackground(String folder, String file) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            try {
                if (fileOperations.runScriptInBackground(fileBrowserView.getSshClient(),
                                                         "cd \"" + folder + "\"; nohup \"" + file + "\" &", new AtomicBoolean())) {
                }
                fileBrowser.enableUi();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    private void extractArchive(String archive, String folder, String currentFolder) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            AtomicBoolean stopFlag = new AtomicBoolean(false);
            fileBrowser.disableUi(stopFlag);
            try {
                if (!archiveOperation.extractArchive(fileBrowserView.getSshClient(), archive, folder, stopFlag)
                    && !fileBrowser.isSessionClosed()) {
                    JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("operation_failed"));
                }

                fileBrowserView.render(currentFolder);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    private void createArchive(List<String> files, String folder, String currentFolder) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            AtomicBoolean stopFlag = new AtomicBoolean(false);
            fileBrowser.disableUi(stopFlag);
            try {
                if (!archiveOperation.createArchive(fileBrowserView.getSshClient(), files, folder, stopFlag) && !fileBrowser.isSessionClosed()) {
                    JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("operation_failed"));
                }
                fileBrowserView.render(currentFolder);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    private void downloadFiles(FileInfo[] files, String currentDirectory) {
        String currentPath;
        if (fileBrowser.getLeftTabs().getSelectedContent() instanceof LocalFileBrowserView) {
            var localFileBrowserView = ((LocalFileBrowserView) fileBrowser.getLeftTabs().getSelectedContent());
            currentPath = localFileBrowserView.getPathText();
        } else if (fileBrowser.getRightTabs().getSelectedContent() instanceof LocalFileBrowserView) {
            var localFileBrowserView = ((LocalFileBrowserView) fileBrowser.getRightTabs().getSelectedContent());
            currentPath = localFileBrowserView.getPathText();
        } else {
            throw new IllegalStateException("Can't Download Files");
        }

        try {
            fileBrowser.getHolder().downloadInBackground(files, currentPath, App.getGlobalSettings().getConflictAction());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void uploadFiles() throws IOException {
        JFileChooser jfc = new JFileChooser();
        jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        jfc.setMultiSelectionEnabled(true);
        if (jfc.showOpenDialog(SwingUtilities.getWindowAncestor(fileBrowser)) == JFileChooser.APPROVE_OPTION) {
            log.info("After file selection");
            File[] files = jfc.getSelectedFiles();
            if (files.length > 0) {
                List<FileInfo> list = new ArrayList<>();

                try (LocalFileSystem localFileSystem = new LocalFileSystem()) {
                    for (File file : files) {
                        FileInfo fileInfo = localFileSystem.getInfo(file.getAbsolutePath());
                        list.add(fileInfo);
                    }
                }
                DndTransferData uploadData = new DndTransferData(0, list.toArray(new FileInfo[0]), files[0].getParent(),
                                                                 0, DndSourceType.LOCAL);
                fileBrowserView.handleDrop(uploadData);
            }
        }
    }

    private void openWithEditor(String path) {
        FileInfo fileInfo = folderView.getSelectedFiles()[0];
        try {
            App.getExternalEditorHandler().openRemoteFile(fileInfo, fileBrowser.getSSHFileSystem(),
                                                          fileBrowser.getActiveSessionId(), false, path);
        } catch (IOException e1) {
            log.error(e1.getMessage(), e1);
        }
    }

    public void openEditorConfig() {
        App.openSettings(SettingsPageName.EDITOR);
    }

    private void sendFilesViaLocal() {
        LocalPipeTransfer pipTransfer = new LocalPipeTransfer();
        pipTransfer.transferFiles(fileBrowser, fileBrowserView.getCurrentDirectory(), folderView.getSelectedFiles());
    }

    private void sendFilesViaSSH() {
        Remote2RemoteTransferDialog r2rt = new Remote2RemoteTransferDialog(App.getAppWindow(),
                                                                           this.fileBrowser.getHolder(), folderView.getSelectedFiles(), fileBrowserView.getCurrentDirectory());
        r2rt.setLocationRelativeTo(App.getAppWindow());
        r2rt.setVisible(true);
    }

    private void loadEditors() {
        mEditWith.removeAll();
        for (EditorEntry ent : App.getGlobalSettings().getEditors()) {
            JMenuItem mEditorItem = new JMenuItem(ent.getName());
            mEditorItem.addActionListener(e -> openWithEditor(ent.getPath()));
            mEditWith.add(mEditorItem);
        }
        mEditWith.add(mEditorConfig);
    }

}
