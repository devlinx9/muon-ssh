package com.jediterm.terminal.ui;

import com.jediterm.terminal.RequestOrigin;
import com.jediterm.terminal.TabbedTerminalWidget;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.debug.BufferPanel;
import com.jediterm.terminal.model.SelectionUtil;
import com.jediterm.terminal.ui.settings.DefaultTabbedSettingsProvider;
import com.jediterm.terminal.ui.settings.TabbedSettingsProvider;
import com.jediterm.terminal.util.Pair;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


@Slf4j
public abstract class AbstractTerminalFrame {

    private JFrame myBufferFrame;

    private final TerminalWidget myTerminal;

    private final AbstractAction myOpenAction = new AbstractAction("New Session") {
        public void actionPerformed(final ActionEvent e) {
            openSession(myTerminal);
        }
    };

    private final AbstractAction myShowBuffersAction = new AbstractAction("Show buffers") {
        public void actionPerformed(final ActionEvent e) {
            if (myBufferFrame == null) {
                showBuffers();
            }
        }
    };

    private final AbstractAction myDumpDimension = new AbstractAction("Dump terminal dimension") {
        public void actionPerformed(final ActionEvent e) {
            log.info("{}x{}", myTerminal.getTerminalDisplay().getColumnCount(), myTerminal.getTerminalDisplay().getRowCount());
        }
    };

    private final AbstractAction myDumpSelection = new AbstractAction("Dump selection") {
        public void actionPerformed(final ActionEvent e) {
            Pair<Point, Point> points = myTerminal.getTerminalDisplay()
                    .getSelection().pointsForRun(myTerminal.getTerminalDisplay().getColumnCount());
            log.info("{} : '{}'", myTerminal.getTerminalDisplay().getSelection(), SelectionUtil.getSelectionText(points.first, points.second, myTerminal.getCurrentSession().getTerminalTextBuffer()));
        }
    };

    private final AbstractAction myDumpCursorPosition = new AbstractAction("Dump cursor position") {
        public void actionPerformed(final ActionEvent e) {
            log.info("{}x{}", myTerminal.getCurrentSession().getTerminal().getCursorX(), myTerminal.getCurrentSession().getTerminal().getCursorY());
        }
    };

    private final AbstractAction myCursor0x0 = new AbstractAction("1x1") {
        public void actionPerformed(final ActionEvent e) {
            myTerminal.getCurrentSession().getTerminal().cursorPosition(1, 1);
        }
    };

    private final AbstractAction myCursor10x10 = new AbstractAction("10x10") {
        public void actionPerformed(final ActionEvent e) {
            myTerminal.getCurrentSession().getTerminal().cursorPosition(10, 10);
        }
    };

    private final AbstractAction myCursor80x24 = new AbstractAction("80x24") {
        public void actionPerformed(final ActionEvent e) {
            myTerminal.getCurrentSession().getTerminal().cursorPosition(80, 24);
        }
    };

    private JMenuBar getJMenuBar() {
        final JMenuBar mb = new JMenuBar();
        final JMenu m = new JMenu("File");

        m.add(myOpenAction);
        mb.add(m);
        final JMenu dm = new JMenu("Debug");

        JMenu logLevel = new JMenu("Set log level ...");
//        Level[] levels = new Level[]{Level.ALL, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR, Level.FATAL, Level.OFF};
//        for (final Level l : levels) {
//            logLevel.add(new AbstractAction(l.toString()) {
//                @Override
//                public void actionPerformed(ActionEvent e) {
////                    Logger.setLevel(l);
//                }
//            });
//        }
        dm.add(logLevel);
        dm.addSeparator();

        dm.add(myShowBuffersAction);
        dm.addSeparator();
        dm.add(myDumpDimension);
        dm.add(myDumpSelection);
        dm.add(myDumpCursorPosition);

        JMenu cursorPosition = new JMenu("Set cursor position ...");
        cursorPosition.add(myCursor0x0);
        cursorPosition.add(myCursor10x10);
        cursorPosition.add(myCursor80x24);
        dm.add(cursorPosition);
        mb.add(dm);

        return mb;
    }


    protected JediTermWidget openSession(TerminalWidget terminal) {
        if (terminal.canOpenSession()) {
            return openSession(terminal, createTtyConnector());
        }
        return null;
    }

    public JediTermWidget openSession(TerminalWidget terminal, TtyConnector ttyConnector) {
        JediTermWidget session = terminal.createTerminalSession(ttyConnector);
        session.start();
        return session;
    }

    public abstract TtyConnector createTtyConnector();

    protected AbstractTerminalFrame() {
        myTerminal = createTabbedTerminalWidget();

        final JFrame frame = new JFrame("JediTerm");

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent e) {
                System.exit(0);
            }
        });

        final JMenuBar mb = getJMenuBar();
        frame.setJMenuBar(mb);
        sizeFrameForTerm(frame);
        frame.getContentPane().add("Center", myTerminal.getComponent());

        frame.pack();
        frame.setLocationByPlatform(true);
        frame.setVisible(true);

        frame.setResizable(true);

        myTerminal.setTerminalPanelListener(new TerminalPanelListener() {
            public void onPanelResize(final Dimension pixelDimension, final RequestOrigin origin) {
                if (origin == RequestOrigin.Remote) {
                    sizeFrameForTerm(frame);
                }
                frame.pack();
            }

            @Override
            public void onSessionChanged(final TerminalSession currentSession) {
                frame.setTitle(currentSession.getSessionName());
            }

            @Override
            public void onTitleChanged(String title) {
                frame.setTitle(myTerminal.getCurrentSession().getSessionName());
            }
        });

        openSession(myTerminal);
    }


    protected AbstractTabbedTerminalWidget createTabbedTerminalWidget() {
        return new TabbedTerminalWidget(new DefaultTabbedSettingsProvider(), this::openSession) {
            @Override
            public JediTermWidget createInnerTerminalWidget() {
                return createTerminalWidget(getSettingsProvider());
            }
        };
    }

    protected JediTermWidget createTerminalWidget(TabbedSettingsProvider settingsProvider) {
        return new JediTermWidget(settingsProvider);
    }

    private void sizeFrameForTerm(final JFrame frame) {
        SwingUtilities.invokeLater(() -> {
            Dimension d = myTerminal.getPreferredSize();

            d.width += frame.getWidth() - frame.getContentPane().getWidth();
            d.height += frame.getHeight() - frame.getContentPane().getHeight();
            frame.setSize(d);
        });
    }

    private void showBuffers() {
        SwingUtilities.invokeLater(() -> {
            myBufferFrame = new JFrame("buffers");
            final JPanel panel = new BufferPanel(myTerminal.getCurrentSession());

            myBufferFrame.getContentPane().add(panel);
            myBufferFrame.pack();
            myBufferFrame.setLocationByPlatform(true);
            myBufferFrame.setVisible(true);
            myBufferFrame.setSize(800, 600);

            myBufferFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    myBufferFrame = null;
                }
            });
        });
    }

}
