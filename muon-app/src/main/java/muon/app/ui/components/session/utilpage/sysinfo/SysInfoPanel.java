
package muon.app.ui.components.session.utilpage.sysinfo;

import lombok.extern.slf4j.Slf4j;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.common.SkinnedTextArea;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.utilpage.UtilPageItemView;
import muon.app.util.ScriptLoader;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author subhro
 *
 */
@Slf4j
public class SysInfoPanel extends UtilPageItemView {
    

    private JTextArea textArea;

    public SysInfoPanel(SessionContentPanel holder) {
        super(holder);
    }

    @Override
    protected void createUI() {
        textArea = new SkinnedTextArea();
        textArea.setFont(new Font(
                "Noto Mono"
                , Font.PLAIN, 14));
        JScrollPane scrollPane = new SkinnedScrollPane(textArea);
        this.add(scrollPane);

        AtomicBoolean stopFlag = new AtomicBoolean(false);
        holder.disableUi(stopFlag);
        holder.EXECUTOR.submit(() -> {
            try {
                StringBuilder output = new StringBuilder();
                int ret = holder
                        .getRemoteSessionInstance().exec(
                                ScriptLoader.loadShellScript(
                                        "/scripts/linux-sysinfo.sh"),
                                stopFlag, output);
                if (ret == 0) {
                    SwingUtilities.invokeAndWait(() -> {
                        textArea.setText(output.toString());
                        textArea.setCaretPosition(0);
                    });
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                holder.enableUi();
            }
        });
    }

    @Override
    protected void onComponentVisible() {
        

    }

    @Override
    protected void onComponentHide() {
        

    }
}
