package muon.app.ui.components.session.terminal;

import com.jediterm.terminal.ui.JediTermWidget;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.ClosableTabContent;
import muon.app.ui.components.common.ClosableTabbedPanel.TabTitle;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.terminal.ssh.DisposableTtyConnector;
import muon.app.ui.components.session.terminal.ssh.SshTtyConnector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@Slf4j
public class TerminalComponent extends JPanel implements ClosableTabContent {
    private final JPanel contentPane;

    @Getter
    private final JediTermWidget term = new CustomJediterm(new CustomizedSettingsProvider());
    private DisposableTtyConnector tty;
    private String name;
    private final Box reconnectionBox;

    @Getter
    private final TabTitle tabTitle;

    public TerminalComponent(SessionInfo info, String name, String command, SessionContentPanel sessionContentPanel) {
        setLayout(new BorderLayout());
        log.debug("Current terminal font: {}", App.getGlobalSettings().getTerminalFontName());
        this.name = name;
        this.tabTitle = new TabTitle();
        contentPane = new JPanel(new BorderLayout());
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

        tty = new SshTtyConnector(info, command, sessionContentPanel);

        reconnectionBox = Box.createHorizontalBox();
        reconnectionBox.setOpaque(true);
        reconnectionBox.setBackground(Color.RED);
        reconnectionBox.add(new JLabel("Session not connected"));
        JButton btnReconnect = new JButton("Reconnect");
        btnReconnect.addActionListener(e -> {
            contentPane.remove(reconnectionBox);
            contentPane.revalidate();
            contentPane.repaint();
            tty = new SshTtyConnector(info, command, sessionContentPanel);
            term.setTtyConnector(tty);
            term.getTerminal().setCursorVisible(true);
            term.start();
        });
        reconnectionBox.add(Box.createHorizontalGlue());
        reconnectionBox.add(btnReconnect);
        reconnectionBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        term.addListener(e -> {
            log.info("Disconnected");
            SwingUtilities.invokeLater(() -> {
                contentPane.add(reconnectionBox, BorderLayout.NORTH);
                contentPane.revalidate();
                contentPane.repaint();
            });
        });
        term.setTtyConnector(tty);
        contentPane.add(term);

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
