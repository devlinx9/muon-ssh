package muon.app.ui.components.session.files.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.util.FileIconUtil;
import muon.app.util.FormatUtils;
import muon.app.util.enums.FileType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
@Slf4j
public class TableCellLabelRenderer implements TableCellRenderer {
    private final JPanel panel;
    private final JLabel textLabel;
    private final JLabel iconLabel;
    private final JLabel label;
    @Getter
    private final int height;
    private final Color foreground;

    public TableCellLabelRenderer() {
        foreground = App.getCONTEXT().getSkin().getInfoTextForeground();
        panel = new JPanel(new BorderLayout(10, 5));
        panel.setBorder(new EmptyBorder(5, 10, 5, 5));
        textLabel = new JLabel();
        textLabel.setForeground(foreground);
        textLabel.setText("AAA");
        textLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));

        iconLabel = new JLabel();
        iconLabel.setFont(App.getCONTEXT().getSkin().getIconFont().deriveFont(Font.PLAIN, 20.f));
        iconLabel.setText("\uf016");
        iconLabel.setForeground(foreground);

        Dimension d1 = iconLabel.getPreferredSize();
        iconLabel.setText("\uf07b");
        Dimension d2 = iconLabel.getPreferredSize();

        height = Math.max(d1.height, d2.height) + 10;

        iconLabel.setHorizontalAlignment(JLabel.CENTER);
        panel.add(textLabel);
        panel.add(iconLabel, BorderLayout.WEST);

        panel.doLayout();

        log.debug(panel.getPreferredSize().toString());

        label = new JLabel();
        label.setForeground(foreground);
        label.setBorder(new EmptyBorder(5, 5, 5, 5));
        label.setFont(new Font(Font.DIALOG, Font.PLAIN, 14));
        label.setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        FolderViewTableModel folderViewModel = (FolderViewTableModel) table.getModel();

        int r = table.convertRowIndexToModel(row);
        int c = table.convertColumnIndexToModel(column);

        FileInfo ent = folderViewModel.getItemAt(r);

        panel.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

        textLabel.setForeground(isSelected ? table.getSelectionForeground() : foreground);
        iconLabel.setForeground(isSelected ? table.getSelectionForeground() : foreground);
        iconLabel.setText(getIconForType(ent));
        textLabel.setText(ent.getName());

        label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
        label.setForeground(isSelected ? table.getSelectionForeground() : foreground);

        switch (c) {
            case 0:
                label.setText("");
                break;
            case 1:
                label.setText(FormatUtils.formatDate(ent.getLastModified()));
                break;
            case 2:
                if (ent.getType() == FileType.DIRECTORY || ent.getType() == FileType.DIR_LINK) {
                    label.setText("");
                } else {
                    label.setText(FormatUtils.humanReadableByteCount(ent.getSize(), true));
                }
                break;
            case 3:
                label.setText(ent.getType() + "");
                break;
            case 4:
                label.setText(ent.getPermissionString());
                break;
            case 5:
                label.setText(ent.getUser());
                break;
            default:
                break;
        }

        if (c == 0) {
            return panel;
        } else {
            return label;
        }

    }

    public String getIconForType(FileInfo ent) {
        return FileIconUtil.getIconForType(ent);
    }
}
