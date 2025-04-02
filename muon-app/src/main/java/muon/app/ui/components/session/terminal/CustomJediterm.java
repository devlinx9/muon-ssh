package muon.app.ui.components.session.terminal;

import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

@Setter
@Getter
@Slf4j
public class CustomJediterm extends JediTermWidget {
    private boolean started = false;

    public CustomJediterm(SettingsProvider settingsProvider) {
        super(settingsProvider);
        setFont(settingsProvider.getTerminalFont());
        getTerminal().setAutoNewLine(false);
        getTerminalPanel().setFont(settingsProvider.getTerminalFont());
        getTerminalPanel().setFocusable(true);
        setFocusable(true);

        addAncestorListener(new AncestorListener() {

            @Override
            public void ancestorRemoved(AncestorEvent event) {
                log.debug("ancestorRemoved");
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
                log.debug("ancestorMoved");
            }

            @Override
            public void ancestorAdded(AncestorEvent event) {
                getTerminalPanel().requestFocusInWindow();
            }
        });
    }

    @Override
    protected JScrollBar createScrollBar() {
        return new JScrollBar();
    }

    @Override
    public void start() {
        started = true;
        super.start();
    }

    public void sendCommand(String command) {
        doWithTerminalStarter(starter -> starter.sendString(command, true));
    }

}
