package muon.app.ui.components.session.terminal;

import com.jediterm.terminal.RequestOrigin;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.TerminalPanelListener;
import com.jediterm.terminal.ui.TerminalSession;
import muon.app.App;
import muon.app.ui.components.ClosableTabContent;
import muon.app.ui.components.ClosableTabbedPanel.TabTitle;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.SessionInfo;
import muon.app.ui.components.session.terminal.ssh.DisposableTtyConnector;
import muon.app.ui.components.session.terminal.ssh.SshTtyConnector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class TerminalComponent extends JPanel implements ClosableTabContent {
    private final JPanel contentPane;
    private final JediTermWidget term;
    private DisposableTtyConnector tty;
    private String name;
    private final Box reconnectionBox;
    private final TabTitle tabTitle;

    public TerminalComponent(SessionInfo info, String name, String command, SessionContentPanel sessionContentPanel) {
        setLayout(new BorderLayout());
        System.out.println("Current terminal font: " + App.getGlobalSettings().getTerminalFontName());
        this.name = name;
        this.tabTitle = new TabTitle();
        contentPane = new JPanel(new BorderLayout());
        JRootPane rootPane = new JRootPane();
        rootPane.setContentPane(contentPane);
        add(rootPane);
        
        term = new CustomJediterm(new CustomizedSettingsProvider(info));

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                System.out.println("Requesting focus");
                term.requestFocusInWindow();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                System.out.println("Hiding focus");
            }
        });

        tty = new SshTtyConnector(info, command, sessionContentPanel);

        reconnectionBox = Box.createHorizontalBox();
        reconnectionBox.setOpaque(true);
        reconnectionBox.setBackground(Color.RED);
        // TODO i18n
        reconnectionBox.add(new JLabel("Session not connected"));
        JButton btnReconnect = new JButton("Reconnect");
        btnReconnect.addActionListener(e -> {
            contentPane.remove(reconnectionBox);
            contentPane.revalidate();
            contentPane.repaint();
            tty = new SshTtyConnector(info, command, sessionContentPanel);
            term.setTtyConnector(tty);
            term.start();
        });
        reconnectionBox.add(Box.createHorizontalGlue());
        reconnectionBox.add(btnReconnect);
        reconnectionBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        term.addListener((e) -> {
            System.out.println("Disconnected");
            SwingUtilities.invokeLater(() -> {
                contentPane.add(reconnectionBox, BorderLayout.NORTH);
                contentPane.revalidate();
                contentPane.repaint();
            });
        });
        term.setTtyConnector(tty);
        term.setTerminalPanelListener(new TerminalPanelListener() {

            @Override
            public void onTitleChanged(String title) {
                System.out.println("new title: " + title);
                TerminalComponent.this.name = title;
                SwingUtilities.invokeLater(() -> tabTitle.getCallback().accept(title));
            }

            @Override
            public void onSessionChanged(TerminalSession currentSession) {
                System.out.println("currentSession: " + currentSession);
            }

            @Override
            public void onPanelResize(Dimension pixelDimension, RequestOrigin origin) {
            }
        });
        contentPane.add(term);

    }

    @Override
    public String toString() {
        return "Terminal " + this.name;
    }

    @Override
    public boolean close() {
        System.out.println("Closing terminal..." + name);
        this.term.close();
        return true;
    }

    public void sendCommand(String command) {
        this.term.getTerminalStarter().sendString(command);
    }

    /**
     * @return the term
     */
    public JediTermWidget getTerm() {
        return term;
    }

    public void start() {
        term.start();
    }

    /**
     * @return the tabTitle
     */
    public TabTitle getTabTitle() {
        return tabTitle;
    }
}
