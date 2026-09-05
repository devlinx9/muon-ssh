package muon.app.ui.components.session.files.editor;


import lombok.Getter;
import muon.app.App;
import muon.app.util.FontAwesomeContants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

@Getter
public class TabHeader extends JPanel {
    private final JLabel lblTitle;
    private final JLabel btnClose;

    public TabHeader(String title) {
        super(new BorderLayout(10, 10));
        setOpaque(false);
        lblTitle = new JLabel(title);
        lblTitle.setBorder(new EmptyBorder(5, 5, 5, 5));
        add(lblTitle);
        btnClose = new JLabel();
        btnClose.setFont(App.getCONTEXT().getSkin().getIconFont(12.0f));
        btnClose.setText(FontAwesomeContants.FA_WINDOW_CLOSE);
        add(btnClose, BorderLayout.EAST);
    }

    public void setTitle(String text) {
        lblTitle.setText(text);
    }
}
