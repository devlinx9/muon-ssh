package muon.app.ui.components.session.files.local;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.local.LocalFileSystem;
import muon.app.ui.components.session.BookmarkManager;
import muon.app.ui.components.session.files.FileBrowser;
import muon.app.ui.components.session.files.view.FolderView;
import muon.app.util.PathUtils;
import muon.app.util.PlatformUtils;
import muon.app.util.enums.FileType;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.Collectors;


import static muon.app.util.PlatformUtils.IS_MAC;
import static muon.app.util.PlatformUtils.IS_WINDOWS;


@Slf4j
public class LocalMenuHandler {
    private final FileBrowser fileBrowser;
    private final LocalFileOperations fileOperations;
    private final LocalFileBrowserView fileBrowserView;
    private JMenuItem mOpenInNewTab;
    private JMenuItem mRename;
    private JMenuItem mDelete;
    private JMenuItem mNewFile;
    private JMenuItem mNewFolder;
    private JMenuItem mAddToFav;
    private JMenuItem mOpen;
    private JMenuItem mOpenInFileExplorer;
    private FolderView folderView;

    public LocalMenuHandler(FileBrowser fileBrowser, LocalFileBrowserView fileBrowserView) {
        this.fileBrowser = fileBrowser;
        this.fileOperations = new LocalFileOperations();
        this.fileBrowserView = fileBrowserView;
    }

    public void initMenuHandler(FolderView folderView) {
        this.folderView = folderView;
        InputMap map = folderView.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap act = folderView.getActionMap();
        this.initMenuItems(map, act);
    }

    /**
     * Add shortcut for menu item
     */
    private static void addShortcut(JMenuItem menuItem, KeyStroke keyStroke, InputMap inputMap,
                                    ActionMap actionMap, String actionKey, Action action) {
        menuItem.addActionListener(action);
        inputMap.put(keyStroke, actionKey);
        actionMap.put(actionKey, action);
        menuItem.setAccelerator(keyStroke);
    }

    private void initMenuItems(InputMap map, ActionMap act) {
        mOpen = new JMenuItem(App.getContext().getBundle().getString("open"));
        mOpen.addActionListener(e -> open());
        mOpenInNewTab = new JMenuItem(App.getContext().getBundle().getString("open_new_tab"));
        mOpenInNewTab.addActionListener(e -> openNewTab());

        if (IS_WINDOWS) {
            mOpenInFileExplorer = new JMenuItem(
                    "Open in Windows Explorer");
        } else {
            mOpenInFileExplorer = new JMenuItem(
                    IS_MAC ? "Open in Finder" : "Open in File Browser");
        }
        mOpenInFileExplorer.addActionListener(e -> {
            try {
                PlatformUtils.openFolderInFileBrowser(folderView.getSelectedFiles()[0].getPath());
            } catch (FileNotFoundException e1) {
                log.error(e1.getMessage(), e1);
            }
        });

        mRename = new JMenuItem(App.getContext().getBundle().getString("rename"));
        mRename.addActionListener(e -> rename(folderView.getSelectedFiles()[0], fileBrowserView.getCurrentDirectory()));

        mDelete = new JMenuItem(App.getContext().getBundle().getString("delete"));
        AbstractAction aDelete = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                delete(folderView.getSelectedFiles(), fileBrowserView.getCurrentDirectory());
            }
        };

        // create delete file shortcut
        mDelete.addActionListener(aDelete);
        KeyStroke ksDelete = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0);
        map.put(ksDelete, "ksDelete");
        act.put("ksDelete", aDelete);
        mDelete.setAccelerator(ksDelete);

        mNewFile = new JMenuItem(App.getContext().getBundle().getString("new_file"));
        mNewFile.addActionListener(e -> newFile());

        mNewFolder = new JMenuItem(App.getContext().getBundle().getString("new_folder"));
        mNewFolder.addActionListener(e -> newFolder(fileBrowserView.getCurrentDirectory()));

        JMenuItem mCopy = new JMenuItem(App.getContext().getBundle().getString("copy"));
        mCopy.addActionListener(e -> {
        });

        JMenuItem mPaste = new JMenuItem(App.getContext().getBundle().getString("paste"));
        mPaste.addActionListener(e -> {
        });

        JMenuItem mCut = new JMenuItem(App.getContext().getBundle().getString("cut"));
        mCut.addActionListener(e -> {
        });

        mAddToFav = new JMenuItem(App.getContext().getBundle().getString("bookmark"));
        mAddToFav.addActionListener(e -> addToFavourites());
    }

    public void createMenu(JPopupMenu popup, FileInfo[] selectedFiles) {
        createMenuContext(popup, selectedFiles);
    }

    private void createMenuContext(JPopupMenu popup, FileInfo[] files) {
        popup.removeAll();

        //create Common Menu Items
        popup.add(mNewFolder);
        popup.add(mNewFile);
        // check only if folder is selected
        popup.add(mAddToFav);

        int selectionCount = files.length;
        //create Menu Items For Single Selection
        if (selectionCount == 1) {
            if (files[0].getType() == FileType.FILE || files[0].getType() == FileType.FILE_LINK) {
                popup.add(mOpen);
            }
            if (files[0].getType() == FileType.DIRECTORY || files[0].getType() == FileType.DIR_LINK) {
                popup.add(mOpenInNewTab);
                popup.add(mOpenInFileExplorer);
            }
            popup.add(mRename);
        }
        popup.add(mDelete);
    }

    private void open() {
        FileInfo[] files = folderView.getSelectedFiles();
        if (files.length == 1) {
            FileInfo file = files[0];
            if (file.getType() == FileType.FILE_LINK || file.getType() == FileType.FILE) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        PlatformUtils.openWithDefaultApp(new File(file.getPath()), false);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                });
            }
        }
    }

    private void openNewTab() {
        FileInfo[] files = folderView.getSelectedFiles();
        if (files.length == 1) {
            FileInfo file = files[0];
            if (file.getType() == FileType.DIRECTORY || file.getType() == FileType.DIR_LINK) {
                fileBrowser.openLocalFileBrowserView(file.getPath(), this.fileBrowserView.getOrientation());
            }
        }
    }

    private void rename(FileInfo info, String baseFolder) {
        String text = JOptionPane.showInputDialog(App.getContext().getBundle().getString("enter_new_name"), info.getName());
        if (text != null && !text.isEmpty()) {
            renameAsync(info.getPath(), PathUtils.combineUnix(PathUtils.getParent(info.getPath()), text), baseFolder);
        }
    }

    private void renameAsync(String oldName, String newName, String baseFolder) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            if (fileOperations.rename(oldName, newName)) {
                fileBrowserView.render(baseFolder);
            } else {
                fileBrowser.enableUi();
            }
        });
    }

    /**
     * Delete files and refresh folder view.
     *
     * @param selectedFiles Files need to be deleted.
     * @param baseFolder    Used to refresh the folder view.
     */
    private void delete(FileInfo[] selectedFiles, String baseFolder) {
        boolean delete = true;
        if (App.getGlobalSettings().isConfirmBeforeDelete()) {
            delete = JOptionPane.showConfirmDialog(App.getAppWindow(), App.getContext().getBundle().getString("delete_selected_files")) == JOptionPane.YES_OPTION;
        }
        if (!delete) {
            return;
        }
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            for (FileInfo f : selectedFiles) {
                try {
                    new LocalFileSystem().delete(f);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            fileBrowserView.render(baseFolder);
            fileBrowser.enableUi();
        });
    }

    private void newFile() {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            String baseFolder = fileBrowserView.getCurrentDirectory();
            if (fileOperations.newFile(baseFolder)) {
                fileBrowserView.render(baseFolder);
            } else {
                fileBrowser.enableUi();
            }
        });
    }

    private void newFolder(String currentDirectory) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            if (fileOperations.newFolder(currentDirectory)) {
                fileBrowserView.render(currentDirectory);
            } else {
                fileBrowser.enableUi();
            }
        });
    }

    private void addToFavourites() {
        FileInfo[] arr = folderView.getSelectedFiles();

        if (arr.length > 0) {
            BookmarkManager.addEntry(null,
                                     Arrays.stream(arr)
                                             .filter(a -> a.getType() == FileType.DIR_LINK || a.getType() == FileType.DIRECTORY)
                                             .map(FileInfo::getPath).collect(Collectors.toList()));
        } else {
            BookmarkManager.addEntry(null, fileBrowserView.getCurrentDirectory());
        }

        this.fileBrowserView.getOverflowMenuHandler().loadFavourites();

    }

    public JPopupMenu createAddressPopup() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mOpenInNewTab = new JMenuItem(App.getContext().getBundle().getString("open_new_tab"));
        JMenuItem mCopyPath = new JMenuItem(App.getContext().getBundle().getString("copy_path"));
        JMenuItem mOpenInTerminal = new JMenuItem(App.getContext().getBundle().getString("open_in_terminal"));
        JMenuItem mBookmark = new JMenuItem(App.getContext().getBundle().getString("bookmark"));
        popupMenu.add(mOpenInNewTab);
        popupMenu.add(mCopyPath);
        popupMenu.add(mOpenInTerminal);
        popupMenu.add(mBookmark);

        mOpenInNewTab.addActionListener(e -> {
            String path = popupMenu.getName();
        });

        mOpenInTerminal.addActionListener(e -> {

        });

        mCopyPath.addActionListener(e -> {
            String path = popupMenu.getName();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(path), null);
        });

        mBookmark.addActionListener(e -> {
            String path = popupMenu.getName();
            BookmarkManager.addEntry(null, path);
        });
        return popupMenu;
    }
}
