
package muon.app.ui.components.session;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.DisabledPanel;
import muon.app.ui.components.session.files.transfer.TransferProgressPanel;
import muon.app.ui.components.session.terminal.LocalTerminalHolder;
import muon.app.util.LayoutUtilities;

import javax.swing.*;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author subhro
 */
@Slf4j
public class LocalSessionContentPanel extends JPanel implements PageHolder, ISessionContentPanel {
    public static final String PAGE_ID = "pageId";
    public final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    @Getter
    private final SessionInfo info;
    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private final JRootPane rootPane;
    private final DisabledPanel disabledPanel;
    private final TransferProgressPanel progressPanel = new TransferProgressPanel();
    private final TabbedPage[] pages;
    //    public final FileBrowser fileBrowser;
//    private final LogViewer logViewer;
    private final LocalTerminalHolder terminalHolder;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public LocalSessionContentPanel(SessionInfo info) {
        super(new BorderLayout());
        this.info = info;
        this.disabledPanel = new DisabledPanel();
        Box contentTabs = Box.createHorizontalBox();
        contentTabs.setBorder(new MatteBorder(0, 0, 1, 0, App.getCONTEXT().getSkin().getDefaultBorderColor()));

//        fileBrowser = new FileBrowser(info, this, null, this.hashCode());
//        logViewer = new LogViewer(this);
        terminalHolder = new LocalTerminalHolder();

        Page[] pageArr = getPages();

        this.cardLayout = new CardLayout();
        this.cardPanel = new JPanel(this.cardLayout);

        this.pages = new TabbedPage[pageArr.length];
        for (int i = 0; i < pageArr.length; i++) {
            TabbedPage tabbedPage = new TabbedPage(pageArr[i], this);
            this.pages[i] = tabbedPage;
            this.cardPanel.add(tabbedPage.getPage(), tabbedPage.getId());
            pageArr[i].putClientProperty(PAGE_ID, tabbedPage.getId());
        }

        LayoutUtilities.equalizeSize(this.pages);

        for (TabbedPage item : this.pages) {
            contentTabs.add(item);
        }

        contentTabs.add(Box.createHorizontalGlue());

        JPanel contentPane = new JPanel(new BorderLayout(), true);
        contentPane.add(contentTabs, BorderLayout.NORTH);
        contentPane.add(this.cardPanel);

        this.rootPane = new JRootPane();
        this.rootPane.setContentPane(contentPane);

        this.add(this.rootPane);

        showPage(this.pages[0].getId());
    }

    private Page[] getPages() {
//        DiskspaceAnalyzer diskspaceAnalyzer = new DiskspaceAnalyzer(this);
//        SearchPanel searchPanel = new SearchPanel(this);
//        ProcessViewer processViewer = new ProcessViewer(this);
//        UtilityPage utilityPage = new UtilityPage(this);

//        Page[] pageArr;
//        if (App.getGlobalSettings().isFirstFileBrowserView()) {
//            pageArr = new Page[]{fileBrowser, terminalHolder, logViewer, searchPanel, diskspaceAnalyzer,
//                                 processViewer, utilityPage};
//        } else {
//            pageArr = new Page[]{terminalHolder, fileBrowser, logViewer, searchPanel, diskspaceAnalyzer,
//                                 processViewer, utilityPage};
//        }

        Page[] pageArr = new Page[]{terminalHolder};
        return pageArr;
    }

    @Override
    public void showPage(String pageId) {
        TabbedPage selectedPage = null;
        for (TabbedPage item : this.pages) {
            if (pageId.equals(item.getId())) {
                selectedPage = item;
            }
            item.setSelected(false);
        }
        Objects.requireNonNull(selectedPage).setSelected(true);
        this.cardLayout.show(this.cardPanel, pageId);
        this.revalidate();
        this.repaint();
        selectedPage.getPage().onLoad();
    }

    public void disableUi() {
        SwingUtilities.invokeLater(() -> {
            this.disabledPanel.startAnimation(null);
            this.rootPane.setGlassPane(this.disabledPanel);
            this.disabledPanel.setVisible(true);
        });
    }

    public void disableUi(AtomicBoolean stopFlag) {
        SwingUtilities.invokeLater(() -> {
            this.disabledPanel.startAnimation(stopFlag);
            this.rootPane.setGlassPane(this.disabledPanel);
            log.debug("Showing disable panel");
            this.disabledPanel.setVisible(true);
        });
    }

    public void enableUi() {
        SwingUtilities.invokeLater(() -> {
            this.disabledPanel.stopAnimation();
            log.debug("Hiding disable panel");
            this.disabledPanel.setVisible(false);
        });
    }

    public int getActiveSessionId() {
        return this.hashCode();
    }

    /**
     * @return the closed
     */
    public boolean isSessionClosed() {
        return closed.get();
    }

    public void close() {
        this.closed.set(true);
        try {
            this.terminalHolder.close();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
