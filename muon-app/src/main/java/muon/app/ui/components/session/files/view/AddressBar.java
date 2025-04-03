package muon.app.ui.components.session.files.view;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.ui.components.session.files.AddressBarComboBoxEditor;
import muon.app.util.LayoutUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

@Slf4j
public class AddressBar extends JPanel {
    public static final String TOGGLE_SELECTED = "toggle.selected";
    private final AddressBarBreadCrumbs addressBar;
    private final JComboBox<String> txtAddressBar;
    private final JButton btnEdit;
    private final JPanel addrPanel;
    private boolean updating = false;
    private ActionListener a;
    private JPopupMenu popup;
    private final char separator;
    private final JPanel panBtn2;

    public AddressBar(char separator, ActionListener popupTriggeredListener) {
        setLayout(new BorderLayout());
        addrPanel = new JPanel(new BorderLayout());
        addrPanel.setBorder(new EmptyBorder(3, 3, 3, 3));
        this.separator = separator;

        UIDefaults toolbarSkin = App.getCONTEXT().getSkin().createToolbarSkin();

        JButton btnRoot = new JButton();
        btnRoot.putClientProperty("Nimbus.Overrides", toolbarSkin);
        btnRoot.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnRoot.setText("\uf0a0");
        btnRoot.addActionListener(e -> createAndShowPopup());

        DefaultComboBoxModel<String> model1 = new DefaultComboBoxModel<>();
        txtAddressBar = new JComboBox<>(model1);
        txtAddressBar.setEditor(new AddressBarComboBoxEditor());
        txtAddressBar.putClientProperty("paintNoBorder", "True");

        txtAddressBar.addActionListener(e -> {
            if (updating) {
                return;
            }
            log.info("calling action listener");
            String item = (String) txtAddressBar.getSelectedItem();
            if (e.getActionCommand().equals("comboBoxEdited")) {
                log.info("Edited");
                ComboBoxModel<String> model = txtAddressBar.getModel();
                boolean found = false;
                for (int i = 0; i < model.getSize(); i++) {
                    if (model.getElementAt(i).equals(item)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    txtAddressBar.addItem(item);
                }
                if (a != null) {
                    a.actionPerformed(new ActionEvent(this, 0, item));
                }
            }
        });
        txtAddressBar.setEditable(true);
        ComboBoxEditor cmdEdit = new BasicComboBoxEditor() {
            @Override
            protected JTextField createEditorComponent() {
                JTextField textField = new SkinnedTextField(10);
                textField.setBorder(new LineBorder(Color.black, 0));
                textField.setName("ComboBox.textField");
                return textField;
            }
        };
        txtAddressBar.setEditor(cmdEdit);
        log.debug("Editor: {}", txtAddressBar.getEditor());
        addressBar = new AddressBarBreadCrumbs(separator == '/', popupTriggeredListener);
        addressBar.addActionListener(e -> {
            if (a != null) {
                log.debug("Performing action");
                a.actionPerformed(new ActionEvent(this, 0, e.getActionCommand()));
            }
        });

        panBtn2 = new JPanel(new BorderLayout());
        panBtn2.setBorder(new EmptyBorder(3, 3, 3, 3));

        btnEdit = new JButton();
        btnEdit.putClientProperty("Nimbus.Overrides", toolbarSkin);
        btnEdit.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnEdit.setText("\uf023");
        btnEdit.addActionListener(e -> {
            if (!isSelected()) {
                switchToText();
            } else {
                switchToPathBar();
            }
            revalidate();
            repaint();
        });
        LayoutUtilities.equalizeSize(btnRoot, btnEdit);

        panBtn2.add(btnRoot);

        addrPanel.add(addressBar);
        add(addrPanel);
        JPanel panBtn = new JPanel(new BorderLayout());
        panBtn.setBorder(new EmptyBorder(3, 3, 3, 3));
        panBtn.add(btnEdit);
        add(panBtn, BorderLayout.EAST);
        add(panBtn2, BorderLayout.WEST);
        btnEdit.putClientProperty(TOGGLE_SELECTED, Boolean.FALSE);
    }

    public void switchToPathBar() {
        add(panBtn2, BorderLayout.WEST);
        addrPanel.remove(txtAddressBar);
        addrPanel.add(addressBar);
        btnEdit.setIcon(UIManager.getIcon("AddressBar.edit"));
        btnEdit.putClientProperty(TOGGLE_SELECTED, Boolean.FALSE);
        btnEdit.setText("\uf023");
    }

    public void switchToText() {
        addrPanel.remove(addressBar);
        addrPanel.add(txtAddressBar);
        remove(panBtn2);
        btnEdit.setIcon(UIManager.getIcon("AddressBar.toggle"));
        btnEdit.putClientProperty(TOGGLE_SELECTED, Boolean.TRUE);
        txtAddressBar.getEditor().selectAll();
        btnEdit.setText("\uf13e");
    }

    public String getText() {
        return isSelected() ? (String) txtAddressBar.getSelectedItem() : addressBar.getSelectedText();
    }

    public void setText(String text) {
        log.debug("Setting text: {}", text);
        updating = true;
        txtAddressBar.setSelectedItem(text);
        addressBar.setPath(text);
        updating = false;
        log.debug("Setting text done: {}", text);
    }

    public void addActionListener(ActionListener e) {
        this.a = e;
    }

    private boolean isSelected() {
        return btnEdit.getClientProperty(TOGGLE_SELECTED) == Boolean.TRUE;
    }

    private void createAndShowPopup() {
        if (popup == null) {
            popup = new JPopupMenu();
        } else {
            popup.removeAll();
        }

        if (separator == '/') {
            JMenuItem item = new JMenuItem("ROOT");
            item.putClientProperty("item.path", "/");
            item.addActionListener(e -> {
                String selectedText = (String) item.getClientProperty("item.path");
                if (a != null) {
                    a.actionPerformed(new ActionEvent(this, 0, selectedText));
                }
            });
            popup.add(item);
        } else {
            File[] roots = File.listRoots();
            for (File f : roots) {
                JMenuItem item = new JMenuItem(f.getAbsolutePath());
                item.putClientProperty("item.path", f.getAbsolutePath());
                item.addActionListener(e -> {
                    String selectedText = (String) item.getClientProperty("item.path");
                    if (a != null) {
                        a.actionPerformed(new ActionEvent(this, 0, selectedText));
                    }
                });
                popup.add(item);
            }
        }

        popup.show(this, 0, getHeight());
    }
}
