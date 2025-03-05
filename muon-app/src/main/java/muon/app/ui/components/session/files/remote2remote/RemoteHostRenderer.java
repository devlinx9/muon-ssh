package muon.app.ui.components.session.files.remote2remote;

import muon.app.App;
import muon.app.util.FontAwesomeContants;

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

        lblIcon.setFont(App.getContext().getSkin().getIconFont().deriveFont(24.0f));
        lblText.setFont(App.getContext().getSkin().getDefaultFont().deriveFont(14.0f));
        lblHost.setFont(App.getContext().getSkin().getDefaultFont().deriveFont(12.0f));

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
        panel.setBackground(App.getContext().getSkin().getDefaultBackground());
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
            panel.setBackground(App.getContext().getSkin().getDefaultSelectionBackground());
            lblText.setForeground(App.getContext().getSkin().getDefaultSelectionForeground());
            lblHost.setForeground(App.getContext().getSkin().getDefaultSelectionForeground());
            lblIcon.setForeground(App.getContext().getSkin().getDefaultSelectionForeground());
        } else {
            panel.setBackground(list.getBackground());
            lblText.setForeground(App.getContext().getSkin().getDefaultForeground());
            lblHost.setForeground(App.getContext().getSkin().getInfoTextForeground());
            lblIcon.setForeground(App.getContext().getSkin().getDefaultForeground());
        }
        return panel;
    }

}
