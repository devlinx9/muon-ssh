/**
 *
 */
package muon.app.ui.components.session.logviewer;

import muon.app.common.FileInfo;
import muon.app.ui.components.ClosableTabbedPanel;
import muon.app.ui.components.ClosableTabbedPanel.TabTitle;
import muon.app.ui.components.SkinnedTextField;
import muon.app.ui.components.session.Page;
import muon.app.ui.components.session.SessionContentPanel;
import util.FontAwesomeContants;
import util.PathUtils;

import javax.swing.*;
import java.util.LinkedHashSet;
import java.util.Set;

import static muon.app.App.bundle;

/**
 * @author subhro
 *
 */
public class LogViewer extends Page {
    private final ClosableTabbedPanel tabs;
    private final StartPage startPage;
    private final JPanel content;
    private final SessionContentPanel sessionContent;
    private final Set<String> openLogs = new LinkedHashSet<>();

    /**
     *
     */
    public LogViewer(SessionContentPanel sessionContent) {
        this.sessionContent = sessionContent;
        startPage = new StartPage(this::openLog, sessionContent.getInfo().getId());
        content = new JPanel();
        tabs = new ClosableTabbedPanel(e -> {
            String path = promptLogPath();
            if (path != null) {
                openLog(path);
            }
        });

        TabTitle tabTitle = new TabTitle();
        tabs.addTab(tabTitle, startPage);
        this.add(tabs);
        tabTitle.getCallback().accept("Pinned logs");
    }

    @Override
    public void onLoad() {

    }

    @Override
    public String getIcon() {
        return FontAwesomeContants.FA_STICKY_NOTE;
    }

    @Override
    public String getText() {
        return bundle.getString("server_logs");
    }

    public void openLog(FileInfo remotePath) {
        openLog(remotePath.getPath());
    }

    public void openLog(String remotePath) {
        if (openLogs.contains(remotePath)) {
            int index = 0;
            for (String logPath : openLogs) {
                if (logPath.equals(remotePath)) {
                    tabs.setSelectedIndex(index + 1);
                    return;
                }
                index++;
            }
        }
        LogContent logContent = new LogContent(sessionContent, remotePath,
                startPage, e -> openLogs.remove(remotePath));
        TabTitle title = new TabTitle();
        tabs.addTab(title, logContent);
        title.getCallback().accept(PathUtils.getFileName(remotePath));
        openLogs.add(remotePath);
    }

    private String promptLogPath() {
        JTextField txt = new SkinnedTextField(30);
        if (JOptionPane.showOptionDialog(this,
                new Object[]{"Please provide full path of the log file",
                        txt},
                "Input", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null, null,
                null) == JOptionPane.OK_OPTION && txt.getText().length() > 0) {
            return txt.getText();
        }
        return null;
    }

}
