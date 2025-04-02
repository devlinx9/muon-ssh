package muon.app.ui.components.session.terminal;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.ProcessTtyConnector;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.JediTermWidget;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.pty4j.WinSize;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.ClosableTabContent;
import muon.app.ui.components.common.ClosableTabbedPanel.TabTitle;
import muon.app.util.PlatformUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

@Slf4j
public class LocalTerminalComponent extends JPanel implements ClosableTabContent {

    @Getter
    private final JediTermWidget term = new CustomJediterm(new CustomizedSettingsProvider());
    private String name;

    @Getter
    private final TabTitle tabTitle;

    public LocalTerminalComponent(String name) {
        setLayout(new BorderLayout());
        log.debug("Current terminal font: {}", App.getGlobalSettings().getTerminalFontName());
        this.name = name;
        this.tabTitle = new TabTitle();
        JPanel contentPane = new JPanel(new BorderLayout());
        JRootPane rootPane = new JRootPane();
        rootPane.setContentPane(contentPane);
        add(rootPane);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                log.debug("Requesting focus");
                term.requestFocusInWindow();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                log.info("Hiding focus");
            }
        });

        try {

            // Get terminal settings
            String terminalType = App.getGlobalSettings().getTerminalType(); // Example: "xterm-256color"
            int termWidth = App.getGlobalSettings().getTermWidth();         // Example: 80
            int termHeight = App.getGlobalSettings().getTermHeight();

            // Create PTY process
            String[] command2;

            if (PlatformUtils.IS_WINDOWS) {
                command2 = new String[]{"cmd.exe"};
            } else if (PlatformUtils.IS_MAC) {
                command2 = new String[]{"/bin/zsh", "--login"};
            } else {
                command2 = new String[]{"/bin/bash", "--login"};
            }
            HashMap<String, String> env = new HashMap<>(System.getenv());

            // Set ANSI terminal support and language environment variable
            env.put("TERM", terminalType);
            env.put("LANG", "en_US.UTF-8");

            PtyProcess process = new PtyProcessBuilder()
                    .setCommand(command2)
                    .setDirectory(System.getProperty("user.home"))
                    .setEnvironment(env)
                    .setInitialColumns(termWidth)  // Set terminal width
                    .setInitialRows(termHeight)    // Set terminal height
                    .start();

            // Create TtyConnector using the new API
            TtyConnector connector = new ProcessTtyConnector(process, StandardCharsets.UTF_8) {
                @Override
                public String getName() {
                    return "Local terminal";
                }

                // *** The important part! ***
                @Override
                public void resize(@NotNull TermSize termSize) {
                    // pty4j's "WinSize" wants (rows, cols).
                    // But JediTerm’s TermSize is (cols, rows).
                    int columns = termSize.getColumns();
                    int rows = termSize.getRows();

                    WinSize winSize = new WinSize(columns, rows);
                    process.setWinSize(winSize);
                }
            };

            term.setTtyConnector(connector);
            contentPane.add(term);
        } catch (IOException e) {
            log.error("Cannot start local terminal", e);
            JOptionPane.showMessageDialog(this, "Failed to start terminal: " + e.getMessage());
        }
    }

    @Override
    public String toString() {
        return "Terminal " + this.name;
    }

    @Override
    public boolean close() {
        log.info("Closing terminal...{}", name);
        this.term.close();
        return true;
    }

    public void sendCommand(String command) {
        ((CustomJediterm) this.term).sendCommand(command);
    }

    public void start() {
        term.start();
    }


}
