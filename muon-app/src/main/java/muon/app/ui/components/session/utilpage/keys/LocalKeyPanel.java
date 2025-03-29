package muon.app.ui.components.session.utilpage.keys;

import muon.app.App;
import muon.app.ui.components.common.SkinnedTextArea;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.ui.components.session.SessionInfo;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;



public class LocalKeyPanel extends JPanel {
    private final JTextField txtKeyFile;
    private final JTextArea txtPubKey;

    public LocalKeyPanel(SessionInfo info, Consumer<?> callback1,
                         Consumer<?> callback2) {
        super(new BorderLayout());
        JLabel lblTitle = new JLabel(App.getCONTEXT().getBundle().getString("public_key_file"));
        txtKeyFile = new SkinnedTextField(20);
        txtKeyFile.setBackground(App.getCONTEXT().getSkin().getDefaultBackground());
        txtKeyFile.setBorder(null);
        txtKeyFile.setEditable(false);
        Box hbox = Box.createHorizontalBox();
        hbox.setBorder(new EmptyBorder(10, 10, 10, 10));
        hbox.add(lblTitle);
        hbox.add(Box.createHorizontalStrut(10));
        hbox.add(Box.createHorizontalGlue());
        hbox.add(txtKeyFile);
        add(hbox, BorderLayout.NORTH);

        txtPubKey = new SkinnedTextArea();
        txtPubKey.setLineWrap(true);
        JScrollPane jScrollPane = new JScrollPane(txtPubKey);
        add(jScrollPane);

        JButton btnGenNewKey = new JButton(App.getCONTEXT().getBundle().getString("generate_new_key"));
        JButton btnRefresh = new JButton(App.getCONTEXT().getBundle().getString("refresh"));

        btnGenNewKey.addActionListener(e -> callback1.accept(null));

        btnRefresh.addActionListener(e -> callback2.accept(null));

        Box hbox1 = Box.createHorizontalBox();
        hbox1.add(Box.createHorizontalGlue());
        hbox1.add(btnGenNewKey);
        hbox1.add(Box.createHorizontalStrut(10));
        hbox1.add(btnRefresh);
        hbox1.setBorder(new EmptyBorder(10, 10, 10, 10));

        add(hbox1, BorderLayout.SOUTH);
    }

    public void setKeyData(SshKeyHolder holder) {
        this.txtKeyFile.setText(holder.getLocalPubKeyFile());
        this.txtPubKey.setText(holder.getLocalPublicKey());
        this.txtPubKey.setEditable(false);
    }
}
