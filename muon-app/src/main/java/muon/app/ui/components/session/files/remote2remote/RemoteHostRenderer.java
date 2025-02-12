package muon.app.ui.components.session.files.remote2remote;

import muon.app.App;
import util.FontAwesomeContants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class RemoteHostRenderer implements ListCellRenderer<RemoteServerEntry> {

    private final JPanel panel;
    private final JLabel lblIcon;
    private final JLabel lblText;
    private final JLabel lblHost;

    /**
     *
     */
    public RemoteHostRenderer() {
        lblIcon = new JLabel();
        lblText = new JLabel();
        lblHost = new JLabel();

        lblIcon.setFont(App.SKIN.getIconFont().deriveFont(24.0f));
        lblText.setFont(App.SKIN.getDefaultFont().deriveFont(14.0f));
        lblHost.setFont(App.SKIN.getDefaultFont().deriveFont(12.0f));

        lblText.setText("Sample server");
        lblHost.setText("server host");
        lblIcon.setText(FontAwesomeContants.FA_CUBE);

        JPanel textHolder = new JPanel(new BorderLayout(5, 0));
        textHolder.setOpaque(false);
        textHolder.add(lblText);
        textHolder.add(lblHost, BorderLayout.SOUTH);

        panel = new JPanel(new BorderLayout(5, 5));
        panel.add(lblIcon, BorderLayout.WEST);
        panel.add(textHolder);

        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.setBackground(App.SKIN.getDefaultBackground());
        panel.setOpaque(true);

        Dimension d = panel.getPreferredSize();
        panel.setPreferredSize(d);
        panel.setMaximumSize(d);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends RemoteServerEntry> list, RemoteServerEntry value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {

        lblText.setText(value.getHost());
        lblHost.setText(value.getPath());
        lblIcon.setText(FontAwesomeContants.FA_CUBE);

        if (isSelected) {
            panel.setBackground(App.SKIN.getDefaultSelectionBackground());
            lblText.setForeground(App.SKIN.getDefaultSelectionForeground());
            lblHost.setForeground(App.SKIN.getDefaultSelectionForeground());
            lblIcon.setForeground(App.SKIN.getDefaultSelectionForeground());
        } else {
            panel.setBackground(list.getBackground());
            lblText.setForeground(App.SKIN.getDefaultForeground());
            lblHost.setForeground(App.SKIN.getInfoTextForeground());
            lblIcon.setForeground(App.SKIN.getDefaultForeground());
        }
        return panel;
    }

}
