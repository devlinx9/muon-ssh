package muon.app.ui.components.session.files;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileSystem;
import muon.app.ui.components.ClosableTabContent;
import muon.app.ui.components.ClosableTabbedPanel.TabTitle;
import muon.app.ui.components.session.files.view.*;
import muon.app.util.LayoutUtilities;
import muon.app.util.PathUtils;
import muon.app.util.enums.PanelOrientation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

@Slf4j
public abstract class AbstractFileBrowserView extends JPanel implements FolderViewEventListener, ClosableTabContent {
    public static final String NIMBUS_OVERRIDES = "Nimbus.Overrides";
    private final NavigationHistory history;
    private final JButton btnBack;
    private final JButton btnNext;
    @Getter
    private final OverflowMenuHandler overflowMenuHandler;
    protected AddressBar addressBar;
    protected FolderView folderView;
    protected String path;
    @Getter
    protected PanelOrientation orientation;

    @Getter
    protected TabTitle tabTitle;

    @Getter
    protected FileBrowser fileBrowser;

    public AbstractFileBrowserView(PanelOrientation orientation, FileBrowser fileBrowser) {
        super(new BorderLayout());
        this.fileBrowser = fileBrowser;
        this.orientation = orientation;
        this.tabTitle = new TabTitle();

        UIDefaults toolbarButtonSkin = App.SKIN.createToolbarSkin();

        overflowMenuHandler = new OverflowMenuHandler(this, fileBrowser);
        history = new NavigationHistory();
        JPanel toolBar = new JPanel(new BorderLayout());
        createAddressBar();
        addressBar.addActionListener(e -> {
            String text = e.getActionCommand();
            log.info("Address changed: {} old: {}", text, this.path);
            if (PathUtils.isSamePath(this.path, text)) {
                log.info("Same text");
                return;
            }
            if (text != null && !text.isEmpty()) {
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
        btnBack.putClientProperty(NIMBUS_OVERRIDES, toolbarButtonSkin);
        btnBack.setFont(App.SKIN.getIconFont());
        btnBack.setText("\uf060");
        btnBack.addActionListener(e -> {
            String item = history.prevElement();
            addNext(this.path);
            render(item, App.getGlobalSettings().isDirectoryCache());
        });

        btnNext = new JButton();
        btnNext.putClientProperty(NIMBUS_OVERRIDES, toolbarButtonSkin);
        btnNext.setFont(App.SKIN.getIconFont());
        btnNext.setText("\uf061");
        btnNext.addActionListener(e -> {
            String item = history.nextElement();
            addBack(this.path);
            render(item, App.getGlobalSettings().isDirectoryCache());
        });

        JButton btnHome = new JButton();
        btnHome.putClientProperty(NIMBUS_OVERRIDES, toolbarButtonSkin);
        btnHome.setFont(App.SKIN.getIconFont());
        btnHome.setText("\uf015");
        btnHome.addActionListener(e -> {
            addBack(this.path);
            home();
        });

        JButton btnUp = new JButton();
        btnUp.putClientProperty(NIMBUS_OVERRIDES, toolbarButtonSkin);
        btnUp.addActionListener(upAction);
        btnUp.setFont(App.SKIN.getIconFont());
        btnUp.setText("\uf062");

        smallToolbar.add(Box.createHorizontalStrut(5));

        JButton btnReload = new JButton();
        btnReload.putClientProperty(NIMBUS_OVERRIDES, toolbarButtonSkin);
        btnReload.addActionListener(reloadAction);
        btnReload.setFont(App.SKIN.getIconFont());
        btnReload.setText("\uf021");


        JButton btnMore = new JButton();
        btnMore.putClientProperty(NIMBUS_OVERRIDES, toolbarButtonSkin);
        btnMore.setFont(App.SKIN.getIconFont());
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

    public abstract String toString();

    public boolean close() {
        log.debug("Unregistering for view mode notification");
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

    public abstract FileSystem getFileSystem();

    public void refreshViewMode() {
        this.folderView.refreshViewMode();
        this.revalidate();
        this.repaint();
    }



}
