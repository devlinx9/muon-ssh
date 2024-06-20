package muon.app.ui.components.session.files.local;

import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.common.FileType;
import muon.app.common.local.LocalFileSystem;
import muon.app.ui.components.session.BookmarkManager;
import muon.app.ui.components.session.files.FileBrowser;
import muon.app.ui.components.session.files.view.FolderView;
import util.PathUtils;
import util.PlatformUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.stream.Collectors;

import static muon.app.App.bundle;

public class LocalMenuHandler {
    private final FileBrowser fileBrowser;
    private final LocalFileOperations fileOperations;
    private final LocalFileBrowserView fileBrowserView;
    private JMenuItem mOpenInNewTab, mRename, mDelete, mNewFile, mNewFolder, mCopy, mPaste, mCut, mAddToFav, mOpen,
            mOpenInFileExplorer;
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
        mOpen = new JMenuItem(bundle.getString("open"));
        mOpen.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });
        mOpenInNewTab = new JMenuItem(bundle.getString("open_new_tab"));
        mOpenInNewTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openNewTab();
            }
        });

        mOpenInFileExplorer = new JMenuItem(
                App.IS_WINDOWS ? "Open in Windows Explorer" : (App.IS_MAC ? "Open in Finder" : "Open in File Browser"));
        mOpenInFileExplorer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PlatformUtils.openFolderInExplorer(folderView.getSelectedFiles()[0].getPath(), null);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        });

        mRename = new JMenuItem(bundle.getString("rename"));
        mRename.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rename(folderView.getSelectedFiles()[0], fileBrowserView.getCurrentDirectory());
            }
        });

        mDelete = new JMenuItem(bundle.getString("delete"));
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

        mNewFile = new JMenuItem(bundle.getString("new_file"));
        mNewFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFile();
            }
        });

        mNewFolder = new JMenuItem(bundle.getString("new_folder"));
        mNewFolder.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newFolder(fileBrowserView.getCurrentDirectory());
            }
        });

        mCopy = new JMenuItem(bundle.getString("copy"));
        mCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        mPaste = new JMenuItem(bundle.getString("paste"));
        mPaste.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        mCut = new JMenuItem(bundle.getString("cut"));
        mCut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });

        mAddToFav = new JMenuItem(bundle.getString("bookmark"));
        mAddToFav.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addToFavourites();
            }
        });
    }

    public void createMenu(JPopupMenu popup, FileInfo[] selectedFiles) {
        createMenuContext(popup, selectedFiles);
    }

    private void createMenuContext(JPopupMenu popup, FileInfo[] files) {
        popup.removeAll();

        //create Common Menu Items
        popup.add(mDelete);
        popup.add(mNewFolder);
        popup.add(mNewFile);
        // check only if folder is selected
        popup.add(mAddToFav);

        int selectionCount = files.length;
        //create Menu Items For Single Selection
        if (selectionCount == 1) {
            if (files[0].getType() == FileType.File || files[0].getType() == FileType.FileLink) {
                popup.add(mOpen);
            }
            if (files[0].getType() == FileType.Directory || files[0].getType() == FileType.DirLink) {
                popup.add(mOpenInNewTab);
                popup.add(mOpenInFileExplorer);
            }
            popup.add(mRename);
        }
    }

    private void open() {
        FileInfo[] files = folderView.getSelectedFiles();
        if (files.length == 1) {
            FileInfo file = files[0];
            if (file.getType() == FileType.FileLink || file.getType() == FileType.File) {
            }
        }
    }

    private void openNewTab() {
        FileInfo[] files = folderView.getSelectedFiles();
        if (files.length == 1) {
            FileInfo file = files[0];
            if (file.getType() == FileType.Directory || file.getType() == FileType.DirLink) {
                fileBrowser.openLocalFileBrowserView(file.getPath(), this.fileBrowserView.getOrientation());
            }
        }
    }

    private void rename(FileInfo info, String baseFolder) {
        String text = JOptionPane.showInputDialog(bundle.getString("enter_new_name"), info.getName());
        if (text != null && text.length() > 0) {
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
     * @param baseFolder Used to refresh the folder view.
     */
    private void delete(FileInfo[] selectedFiles, String baseFolder) {
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            fileBrowser.disableUi();
            for (FileInfo f : selectedFiles) {
                try {
                    new LocalFileSystem().delete(f);
                } catch (Exception e) {
                    e.printStackTrace();
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
            String baseFolder = currentDirectory;
            if (fileOperations.newFolder(baseFolder)) {
                fileBrowserView.render(baseFolder);
            } else {
                fileBrowser.enableUi();
            }
        });
    }

    private void addToFavourites() {
        FileInfo[] arr = folderView.getSelectedFiles();

        if (arr.length > 0) {
            BookmarkManager.addEntry(null,
                    Arrays.asList(arr).stream()
                            .filter(a -> a.getType() == FileType.DirLink || a.getType() == FileType.Directory)
                            .map(a -> a.getPath()).collect(Collectors.toList()));
        } else if (arr.length == 0) {
            BookmarkManager.addEntry(null, fileBrowserView.getCurrentDirectory());
        }

        this.fileBrowserView.getOverflowMenuHandler().loadFavourites();

    }

    public JPopupMenu createAddressPopup() {
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem mOpenInNewTab = new JMenuItem(bundle.getString("open_new_tab"));
        JMenuItem mCopyPath = new JMenuItem(bundle.getString("copy_path"));
        JMenuItem mOpenInTerminal = new JMenuItem(bundle.getString("open_in_terminal"));
        JMenuItem mBookmark = new JMenuItem(bundle.getString("bookmark"));
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
