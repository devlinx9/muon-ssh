package muon.app.ui.components.session.terminal;

import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import lombok.Getter;
import lombok.Setter;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;

@Setter
@Getter
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
            }

            @Override
            public void ancestorMoved(AncestorEvent event) {
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
    public Dimension getPreferredSize() {
        
        return super.getPreferredSize();
    }

    @Override
    public void start() {
        started = true;
        super.start();
    }

}
