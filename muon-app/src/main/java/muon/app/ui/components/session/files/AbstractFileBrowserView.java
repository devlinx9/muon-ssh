package muon.app.ui.components.session.files;

import muon.app.App;
import muon.app.common.FileSystem;
import muon.app.ui.components.ClosableTabContent;
import muon.app.ui.components.ClosableTabbedPanel.TabTitle;
import muon.app.ui.components.session.files.view.*;
import util.LayoutUtilities;
import util.PathUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

public abstract class AbstractFileBrowserView extends JPanel implements FolderViewEventListener, ClosableTabContent {
    private final NavigationHistory history;
    private final JButton btnBack;
    private final JButton btnNext;
    private final OverflowMenuHandler overflowMenuHandler;
    protected AddressBar addressBar;
    protected FolderView folderView;
    protected String path;
    protected PanelOrientation orientation;
    protected TabTitle tabTitle;

    protected FileBrowser fileBrowser;

    public AbstractFileBrowserView(PanelOrientation orientation, FileBrowser fileBrowser) {
        super(new BorderLayout());
        this.fileBrowser = fileBrowser;
        this.orientation = orientation;
        this.tabTitle = new TabTitle();

        UIDefaults toolbarButtonSkin = App.skin.createToolbarSkin();

        overflowMenuHandler = new OverflowMenuHandler(this, fileBrowser);
        history = new NavigationHistory();
        JPanel toolBar = new JPanel(new BorderLayout());
        createAddressBar();
        addressBar.addActionListener(e -> {
            String text = e.getActionCommand();
            System.out.println("Address changed: " + text + " old: " + this.path);
            if (PathUtils.isSamePath(this.path, text)) {
                System.out.println("Same text");
                return;
            }
            if (text != null && text.length() > 0) {
                addBack(this.path);
                render(text, App.getGlobalSettings().isDirectoryCache());
            }
        });
        Box smallToolbar = Box.createHorizontalBox();

        AbstractAction upAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addBack(path);
                up();
            }
        };
        AbstractAction reloadAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reload();
            }
        };

        btnBack = new JButton();
        btnBack.putClientProperty("Nimbus.Overrides", toolbarButtonSkin);
        btnBack.setFont(App.skin.getIconFont());
        btnBack.setText("\uf060");
        btnBack.addActionListener(e -> {
            String item = history.prevElement();
            addNext(this.path);
            render(item, App.getGlobalSettings().isDirectoryCache());
        });

        btnNext = new JButton();
        btnNext.putClientProperty("Nimbus.Overrides", toolbarButtonSkin);
        btnNext.setFont(App.skin.getIconFont());
        btnNext.setText("\uf061");
        btnNext.addActionListener(e -> {
            String item = history.nextElement();
            addBack(this.path);
            render(item, App.getGlobalSettings().isDirectoryCache());
        });

        JButton btnHome = new JButton();
        btnHome.putClientProperty("Nimbus.Overrides", toolbarButtonSkin);
        btnHome.setFont(App.skin.getIconFont());
        btnHome.setText("\uf015");
        btnHome.addActionListener(e -> {
            addBack(this.path);
            home();
        });

        JButton btnUp = new JButton();
        btnUp.putClientProperty("Nimbus.Overrides", toolbarButtonSkin);
        btnUp.addActionListener(upAction);
        btnUp.setFont(App.skin.getIconFont());
        btnUp.setText("\uf062");

        smallToolbar.add(Box.createHorizontalStrut(5));

        JButton btnReload = new JButton();
        btnReload.putClientProperty("Nimbus.Overrides", toolbarButtonSkin);
        btnReload.addActionListener(reloadAction);
        btnReload.setFont(App.skin.getIconFont());
        btnReload.setText("\uf021");


        JButton btnMore = new JButton();
        btnMore.putClientProperty("Nimbus.Overrides", toolbarButtonSkin);
        btnMore.setFont(App.skin.getIconFont());
        btnMore.setText("\uf142");
        btnMore.addActionListener(e -> {
            JPopupMenu popupMenu = overflowMenuHandler.getOverflowMenu();
            overflowMenuHandler.loadFavourites();
            popupMenu.pack();
            Dimension d = popupMenu.getPreferredSize();
            int x = btnMore.getWidth() - d.width;
            int y = btnMore.getHeight();
            popupMenu.show(btnMore, x, y);
        });

        LayoutUtilities.equalizeSize(btnMore, btnReload, btnUp, btnHome, btnNext, btnBack);

        smallToolbar.add(btnBack);
        smallToolbar.add(btnNext);
        smallToolbar.add(btnHome);
        smallToolbar.add(btnUp);

        Box b2 = Box.createHorizontalBox();
        b2.add(btnReload);
        b2.setBorder(new EmptyBorder(3, 0, 3, 0));
        b2.add(btnReload);
        b2.add(btnMore);

        toolBar.add(smallToolbar, BorderLayout.WEST);
        toolBar.add(addressBar);
        toolBar.add(b2, BorderLayout.EAST);
        toolBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        add(toolBar, BorderLayout.NORTH);

        folderView = new FolderView(this, text -> this.fileBrowser.updateRemoteStatus(text));

        this.overflowMenuHandler.setFolderView(folderView);

        add(folderView);

        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "up");
        this.getActionMap().put("up", upAction);

        updateNavButtons();

        this.fileBrowser.registerForViewNotification(this);

    }

    protected abstract void createAddressBar();

    public abstract String getHostText();

    public abstract String getPathText();

    @Override
    public abstract String toString();

    public boolean close() {
        System.out.println("Unregistering for view mode notification");
        this.fileBrowser.unRegisterForViewNotification(this);
        return true;
    }

    public String getCurrentDirectory() {
        return this.path;
    }

    public abstract boolean handleDrop(DndTransferData transferData);

    protected abstract void up();

    protected abstract void home();

    @Override
    public void reload() {
        this.render(this.path, false);
    }

    public PanelOrientation getOrientation() {
        return orientation;
    }

    @Override
    public void addBack(String path) {
        history.addBack(path);
        updateNavButtons();
    }

    private void addNext(String path) {
        history.addForward(this.path);
        updateNavButtons();
    }

    private void updateNavButtons() {
        btnBack.setEnabled(history.hasPrevElement());
        btnNext.setEnabled(history.hasNextElement());
    }

    public OverflowMenuHandler getOverflowMenuHandler() {
        return this.overflowMenuHandler;
    }

    public abstract FileSystem getFileSystem() throws Exception;

    /**
     * @return the tabTitle
     */
    public TabTitle getTabTitle() {
        return tabTitle;
    }

    public void refreshViewMode() {
        this.folderView.refreshViewMode();
        this.revalidate();
        this.repaint();
    }

    public FileBrowser getFileBrowser() {
        return this.fileBrowser;
    }

    public enum PanelOrientation {
        LEFT, RIGHT
    }

}
