package muon.app.ui.components.session.terminal.snippets;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.util.FontAwesomeContants;
import muon.app.util.OptionPaneUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


@Slf4j
public class SnippetPanel extends JPanel {
    private final DefaultListModel<SnippetItem> listModel = new DefaultListModel<>();
    private final List<SnippetItem> snippetList = new ArrayList<>();
    private final JList<SnippetItem> listView = new JList<>(listModel);
    private final JTextField searchTextField;

    public SnippetPanel(Consumer<String> callback, Consumer<String> callback2) {
        super(new BorderLayout());
        setBorder(new LineBorder(App.getCONTEXT().getSkin().getDefaultBorderColor(), 1));
        Box topBox = Box.createHorizontalBox();
        topBox.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, App.getCONTEXT().getSkin().getDefaultBorderColor()),
                new EmptyBorder(10, 10, 10, 10)));
        JLabel lblSearch = new JLabel();
        lblSearch.setFont(App.getCONTEXT().getSkin().getIconFont());
        lblSearch.setText(FontAwesomeContants.FA_SEARCH);
        topBox.add(lblSearch);
        topBox.add(Box.createHorizontalStrut(10));

        searchTextField = new SkinnedTextField(30);// new
        searchTextField.getDocument()
                .addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        filter();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        filter();
                    }
                });
        topBox.add(searchTextField);

        listView.setCellRenderer(new SnippetListRenderer());
        listView.setBackground(App.getCONTEXT().getSkin().getTableBackgroundColor());

        JButton btnAdd = new JButton(App.getCONTEXT().getBundle().getString("add"));
        JButton btnEdit = new JButton(App.getCONTEXT().getBundle().getString("edit"));
        JButton btnDel = new JButton(App.getCONTEXT().getBundle().getString("delete"));
        JButton btnInsert = new JButton(App.getCONTEXT().getBundle().getString("insert"));
        JButton btnCopy = new JButton(App.getCONTEXT().getBundle().getString("copy"));

        btnAdd.addActionListener(e -> {
            JTextField txtName = new SkinnedTextField(30);
            JTextField txtCommand = new SkinnedTextField(30);

            if (OptionPaneUtils.showOptionDialog(null,
                                                 new Object[]{"Snippet name", txtName, "Command",
                                                              txtCommand},
                                                 "New snippet") == JOptionPane.OK_OPTION) {
                if (txtCommand.getText().isEmpty()
                    || txtName.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("enter_name_command")
                                                 );
                    return;
                }
                App.getCONTEXT().getSnippetManager().getSnippetItems().add(new SnippetItem(
                        txtName.getText(), txtCommand.getText()));
                App.getCONTEXT().getSnippetManager().saveSnippets();
            }
            callback2.accept(null);
        });

        btnEdit.addActionListener(e -> {
            int index = listView.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(null,
                                              App.getCONTEXT().getBundle().getString("select_item_edit"));
                return;
            }

            SnippetItem snippetItem = listModel.get(index);

            JTextField txtName = new SkinnedTextField(30);
            JTextField txtCommand = new SkinnedTextField(30);

            txtName.setText(snippetItem.getName());
            txtCommand.setText(snippetItem.getCommand());

            if (OptionPaneUtils.showOptionDialog(null,
                                                 new Object[]{"Snippet name", txtName, "Command",
                                                              txtCommand},
                                                 "New snippet") == JOptionPane.OK_OPTION) {
                if (txtCommand.getText().isEmpty()
                    || txtName.getText().isEmpty()) {
                    JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("enter_name_command")
                                                 );
                    return;
                }
                snippetItem.setCommand(txtCommand.getText());
                snippetItem.setName(txtName.getText());
                App.getCONTEXT().getSnippetManager().saveSnippets();
            }
            callback2.accept(null);
        });

        btnDel.addActionListener(e -> {
            int index = listView.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("select_item"));
                return;
            }

            SnippetItem snippetItem = listModel.get(index);
            App.getCONTEXT().getSnippetManager().getSnippetItems().remove(snippetItem);
            App.getCONTEXT().getSnippetManager().saveSnippets();
            loadSnippets();
            callback2.accept(null);
        });

        btnCopy.addActionListener(e -> {
            int index = listView.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("select_item"));
                return;
            }

            SnippetItem snippetItem = listModel.get(index);

            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                    new StringSelection(snippetItem.getCommand()), null);
            callback2.accept(null);
        });

        btnInsert.addActionListener(e -> {
            int index = listView.getSelectedIndex();
            if (index < 0) {
                JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("select_item"));
                return;
            }

            SnippetItem snippetItem = listModel.get(index);
            callback.accept(snippetItem.getCommand());
            callback2.accept(null);
        });

        Box bottomBox = Box.createHorizontalBox();
        bottomBox.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, App.getCONTEXT().getSkin().getDefaultBorderColor()),
                new EmptyBorder(10, 10, 10, 10)));
        bottomBox.add(btnInsert);
        bottomBox.add(Box.createHorizontalStrut(5));
        bottomBox.add(btnCopy);
        bottomBox.add(Box.createHorizontalGlue());
        bottomBox.add(Box.createHorizontalStrut(5));
        bottomBox.add(btnAdd);
        bottomBox.add(Box.createHorizontalStrut(5));
        bottomBox.add(btnEdit);
        bottomBox.add(Box.createHorizontalStrut(5));
        bottomBox.add(btnDel);

        setPreferredSize(new Dimension(400, 500));
        add(topBox, BorderLayout.NORTH);
        JScrollPane jScrollPane = new JScrollPane(listView);
        add(jScrollPane);
        add(bottomBox, BorderLayout.SOUTH);

    }

    public void loadSnippets() {
        this.snippetList.clear();
        this.snippetList.addAll(App.getCONTEXT().getSnippetManager().getSnippetItems());
        log.info("Snippet size: {}", snippetList.size());
        filter();
    }

    private void filter() {
        this.listModel.clear();
        String text = searchTextField.getText().trim();
        if (text.isEmpty()) {
            this.listModel.addAll(this.snippetList);
            return;
        }
        for (SnippetItem item : snippetList) {
            if (item.getCommand().contains(text)
                || item.getName().contains(text)) {
                this.listModel.addElement(item);
            }
        }
    }
}
