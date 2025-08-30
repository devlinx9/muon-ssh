package muon.app.ui.components.session.diskspace;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

import static muon.app.util.ScalingUtil.getScaledEmptyBorder;

public class UsagePercentageRenderer extends JPanel
        implements TableCellRenderer {
    private final JProgressBar progressBar;

    public UsagePercentageRenderer() {
        super(new BorderLayout());
        progressBar = new JProgressBar(0, 100);
        setBorder(getScaledEmptyBorder(5, 5, 5, 5));
        add(progressBar);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
                                                   boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground()
                : table.getBackground());
        setForeground(isSelected ? table.getSelectionForeground()
                : table.getForeground());
        double pct = (Double) value;
        if (pct > 100) {
            pct = 100;
        } else if (pct < 0) {
            pct = 0;
        }
        progressBar.setValue((int) pct);
        return this;
    }
}
