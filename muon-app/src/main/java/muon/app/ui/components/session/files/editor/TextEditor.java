package muon.app.ui.components.session.files.editor;


import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.util.FontAwesomeContants;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static muon.app.util.Constants.SMALL_TEXT_SIZE;
import static muon.app.util.FontAwesomeContants.*;

@Slf4j
public class TextEditor extends JPanel {
    private final JTabbedPane tabs;
    private final transient ExecutorService executorService = Executors
            .newSingleThreadExecutor();
    @Setter
    @Getter
    private boolean savingFile = false;
    private boolean reloading = false;
    private final boolean localEditor;
    private final CardLayout cardLayout;
    private final JPanel content;
    private JTextField txtFullFilePath;
    private final JTextField txtFilePath = new JTextField(30);
    private final JCheckBox btnWrapText;
    SessionContentPanel holder;
    JSpinner spFontSize;


    public TextEditor(SessionContentPanel holder, boolean isLocalEditor) {
        super(new BorderLayout());
        this.localEditor = isLocalEditor;
        this.holder = holder;
        tabs = new JTabbedPane();

        cardLayout = new CardLayout();

        installKeyboardShortcuts();

        this.btnWrapText = new JCheckBox("Wrap text");
        Box toolBox = getToolBox();

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(toolBox, BorderLayout.NORTH);
        panel.add(tabs);
        panel.add(txtFullFilePath, BorderLayout.SOUTH);

        content = new JPanel(cardLayout);
        content.add(panel, "Tabs");
        content.setBorder(new EmptyBorder(5, 5, 5, 5));

        var msgPanel = getStartupPanel();
        content.add(msgPanel, "Labels");

        add(content);

        cardLayout.show(content, "Labels");

        tabs.addChangeListener(e -> {
            if (tabs.getTabCount() == 0) {
                txtFilePath.setText("");
                cardLayout.show(content, "Labels");
                return;
            }
            int index = tabs.getSelectedIndex();
            if (index >= 0) {
                EditorTab tab = (EditorTab) tabs.getComponentAt(index);
                txtFullFilePath.setText(tab.getLocalFile().toAbsolutePath().toString());
                btnWrapText.setSelected(tab.getWrapText());
                cardLayout.show(content, "Tabs");
                revalidate();
                repaint();
            }
        });
    }

    private JComponent getStartupPanel() {
        JLabel lblTitle = new JLabel(
                "Please enter full path of the file below to open");

        JButton btnOpenFile = new JButton("Open");
        JLabel lblTitle2 = new JLabel(
                "Alternatively you can select the file from file browser");

        Box textBox = Box.createHorizontalBox();
        textBox.add(txtFilePath);
        textBox.add(Box.createHorizontalStrut(10));
        textBox.add(btnOpenFile);

        ActionListener act = e -> {
            String text = txtFilePath.getText();
            if (text.trim().isEmpty()) {
                if (localEditor) {
                    JFileChooser jfc = new JFileChooser();
                    jfc.setFileHidingEnabled(false);
                    jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                    if (jfc.showOpenDialog(App.getAppWindow()) != JFileChooser.APPROVE_OPTION) {
                        return;
                    }
                    txtFilePath.setText(jfc.getSelectedFile().getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(App.getAppWindow(),
                                                  "Please enter full path of the file to be opened");
                    return;
                }
            }

            if (localEditor) {
                SwingUtilities.invokeLater(() -> {
                    var tempFileContent = readTempFile(txtFilePath.getText());
                    createNewTab(null, tempFileContent, txtFilePath.getText());
                });
                return;
            }

            FileInfo fileInfo = new FileInfo(Path.of(text).getFileName().toString(), Path.of(text).toAbsolutePath().toString());
            try {
                App.getExternalEditorHandler().openRemoteFile(fileInfo, holder.fileBrowser.getSSHFileSystem(),
                                                              holder.fileBrowser.getActiveSessionId(), false, "muon-editor", holder);
            } catch (IOException e1) {
                log.error(e1.getMessage(), e1);
            }
        };


        btnOpenFile.addActionListener(act);
        txtFilePath.addActionListener(act);


        Box startPanel = Box.createVerticalBox();
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        textBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblTitle2.setAlignmentX(Component.CENTER_ALIGNMENT);
        startPanel.add(Box.createVerticalStrut(50));
        startPanel.add(lblTitle);
        startPanel.add(Box.createVerticalStrut(10));
        startPanel.add(textBox);
        startPanel.add(Box.createVerticalStrut(5));
        startPanel.add(lblTitle2);

        JPanel msgPanel = new JPanel();
        msgPanel.add(startPanel);
        return msgPanel;
    }

    private @NotNull Box getToolBox() {
        Box toolBox = Box.createHorizontalBox();

        JButton btnOpen = new JButton();
        btnOpen.addActionListener(e -> open());
        btnOpen.setFont(App.getCONTEXT().getSkin().getIconFont(SMALL_TEXT_SIZE));
        btnOpen.setText(FontAwesomeContants.FA_FOLDER_OPEN_O);
        btnOpen.setToolTipText("Open file");
        btnOpen.putClientProperty("Nimbus.Overrides", App.getAppWindow());

        JButton btnSave = new JButton();
        btnSave.addActionListener(e -> saveAs());
        btnSave.setFont(App.getCONTEXT().getSkin().getIconFont(SMALL_TEXT_SIZE));
        btnSave.setText(FA_FLOPPY_O);
        btnSave.setToolTipText("Save");
        btnSave.putClientProperty("Nimbus.Overrides", App.getAppWindow());

        JButton btnReload = new JButton();
        btnReload.addActionListener(e -> reloadFile());
        btnReload.setFont(App.getCONTEXT().getSkin().getIconFont(SMALL_TEXT_SIZE));
        btnReload.setText(FA_REFRESH);
        btnReload.setToolTipText("Reload");
        btnReload.putClientProperty("Nimbus.Overrides", App.getAppWindow());

        JButton btnFind = new JButton();
        btnFind.addActionListener(e -> findText());
        btnFind.setFont(App.getCONTEXT().getSkin().getIconFont(SMALL_TEXT_SIZE));
        btnFind.setText(FA_SEARCH);
        btnFind.setToolTipText("Find and replace");
        btnFind.putClientProperty("Nimbus.Overrides", App.getAppWindow());

        JButton btnCut = new JButton();
        btnCut.addActionListener(e -> cutText());
        btnCut.setFont(App.getCONTEXT().getSkin().getIconFont(SMALL_TEXT_SIZE));
        btnCut.setText("\uf0c4");
        btnCut.setToolTipText("Cut");
        btnCut.putClientProperty("Nimbus.Overrides", App.getAppWindow());

        JButton btnCopy = new JButton();
        btnCopy.addActionListener(e -> copyText());
        btnCopy.setFont(App.getCONTEXT().getSkin().getIconFont(SMALL_TEXT_SIZE));
        btnCopy.setText("\uf0c5");
        btnCopy.setToolTipText("Copy");
        btnCopy.putClientProperty("Nimbus.Overrides", App.getAppWindow());

        JButton btnPaste = new JButton();
        btnPaste.addActionListener(e -> pasteText());
        btnPaste.setFont(App.getCONTEXT().getSkin().getIconFont(SMALL_TEXT_SIZE));
        btnPaste.setText("\uf0ea");
        btnPaste.setToolTipText("Paste");
        btnPaste.putClientProperty("Nimbus.Overrides", App.getAppWindow());

        btnWrapText.addActionListener(e -> wrapText(btnWrapText.isSelected()));

        JButton btnGotoLine = new JButton();
        btnGotoLine.addActionListener(e -> gotoLine());
        btnGotoLine.setFont(App.getCONTEXT().getSkin().getIconFont(SMALL_TEXT_SIZE));
        btnGotoLine.setText("\uf0cb");
        btnGotoLine.setToolTipText("Goto line");
        btnGotoLine.putClientProperty("Nimbus.Overrides",
                                      App.getAppWindow());

        JLabel lblFont = new JLabel("Font size");
        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(12, 5,
                                                                       72, 1);
        spFontSize = new JSpinner(spinnerNumberModel);
        spFontSize.setMaximumSize(spFontSize.getPreferredSize());
        spFontSize.addChangeListener(e -> {
            int fontSize = (int) spinnerNumberModel.getValue();
            setFontSize(fontSize);
        });

        txtFullFilePath = new JTextField();
        txtFullFilePath.setEditable(false);
        txtFullFilePath.setBorder(null);

        toolBox.add(Box.createHorizontalStrut(5));
        toolBox.add(btnOpen);
        toolBox.add(btnSave);
        toolBox.add(btnReload);
        toolBox.add(btnFind);
        toolBox.add(btnCut);
        toolBox.add(btnCopy);
        toolBox.add(btnPaste);
        toolBox.add(btnGotoLine);
        toolBox.add(Box.createHorizontalStrut(10));
        toolBox.add(btnWrapText);
        toolBox.add(Box.createHorizontalGlue());
        toolBox.add(lblFont);
        toolBox.add(Box.createHorizontalStrut(5));
        toolBox.add(spFontSize);
        toolBox.add(Box.createHorizontalStrut(10));
        return toolBox;
    }

    private void installKeyboardShortcuts() {
        InputMap inpMap = getInputMap(
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        ActionMap actMap = getActionMap();

        KeyStroke ksOpen = KeyStroke.getKeyStroke(KeyEvent.VK_O,
                                                  InputEvent.CTRL_DOWN_MASK);
        inpMap.put(ksOpen, "openKey");
        actMap.put("openKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });

        KeyStroke ksSave = KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                  InputEvent.CTRL_DOWN_MASK);
        inpMap.put(ksSave, "saveKey");
        actMap.put("saveKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        KeyStroke ksSaveAs = KeyStroke.getKeyStroke(KeyEvent.VK_S,
                                                    InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
        inpMap.put(ksSaveAs, "saveKeyAs");
        actMap.put("ksSaveAs", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAs();
            }
        });

        KeyStroke ksFind = KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                  InputEvent.CTRL_DOWN_MASK);
        inpMap.put(ksFind, "findKey");
        actMap.put("findKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                findText();
            }
        });

        KeyStroke ksReplace = KeyStroke.getKeyStroke(KeyEvent.VK_H,
                                                     InputEvent.CTRL_DOWN_MASK);
        inpMap.put(ksReplace, "ksReplace");
        actMap.put("ksReplace", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                replaceText();
            }
        });

        KeyStroke ksReload = KeyStroke.getKeyStroke(KeyEvent.VK_R,
                                                    InputEvent.CTRL_DOWN_MASK);
        inpMap.put(ksReload, "reloadKey");
        actMap.put("reloadKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reloadFile();
            }
        });

        KeyStroke ksGotoLine = KeyStroke.getKeyStroke(KeyEvent.VK_G,
                                                      InputEvent.CTRL_DOWN_MASK);
        inpMap.put(ksGotoLine, "gotoKey");
        actMap.put("gotoKey", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                gotoLine();
            }
        });

    }

    private void gotoLine() {
        log.info("Goto line");
        EditorTab tab = (EditorTab) tabs.getSelectedComponent();
        if (tab != null) {
            try {
                tab.gotoLine();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void setFontSize(int fontSize) {
        EditorTab tab = (EditorTab) tabs.getSelectedComponent();
        if (tab != null) {
            try {
                tab.setFontSize(fontSize);
            } catch (Exception e) {
                log.error(e.getMessage());

            }
        }
    }

    private void pasteText() {
        log.info("pasteText");
        EditorTab tab = (EditorTab) tabs.getSelectedComponent();
        if (tab != null) {
            try {
                tab.pasteText();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void copyText() {
        log.info("copyText");
        EditorTab tab = (EditorTab) tabs.getSelectedComponent();
        if (tab != null) {
            try {
                tab.copyText();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void cutText() {
        log.info("cutText");
        EditorTab tab = (EditorTab) tabs.getSelectedComponent();
        if (tab != null) {
            try {
                tab.cutText();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }

    private void reloadFile() {
        EditorTab tab = (EditorTab) tabs.getSelectedComponent();
        var sb = readTempFile(tab.getLocalFile().toAbsolutePath().toString());
        tab.setContent(sb);
        tab.discardEdits();
    }

    private void replaceText() {
        ((EditorTab) tabs.getSelectedComponent()).openFindReplace();
    }

    private void findText() {
        ((EditorTab) tabs.getSelectedComponent()).openFindReplace();
    }

    private void saveAs() {

        EditorTab tab = (EditorTab) tabs.getSelectedComponent();
        if (localEditor) {
            JFileChooser fileChooser = new JFileChooser();

            // Optional: Set dialog title and current directory
            fileChooser.setDialogTitle("Save File");
            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                if (fileToSave.exists() && JOptionPane.showConfirmDialog(this,
                                                                         "Overwrite", "Unsaved changes",
                                                                         JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
                    return;
                }

                try (FileWriter fw = new FileWriter(fileToSave)) {
                    fw.write(tab.getContent());
                    tab.updateLocalFile(fileToSave.toPath());
                    txtFullFilePath.setText(tab.getLocalFile().toAbsolutePath().toString());
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
            }
        }

    }

    private void save() {
        log.info("Save");
        var tab = (EditorTab) tabs.getSelectedComponent();
        if (tab != null && tab.hasUnsavedChanges()) {
            try {
                tab.saveContentsToLocal();
                tab.setHasChanges(false);
                tab.updateCheckSum();
            } catch (Exception e) {
                log.error(e.getMessage());

            }
        }
    }

    private void open() {
        log.info("Open");
        var ref = new Object() {
            String text;
        };
        if (localEditor) {
            ref.text = "";
            JFileChooser jfc = new JFileChooser();
            jfc.setFileHidingEnabled(false);
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            if (jfc.showOpenDialog(App.getAppWindow()) != JFileChooser.APPROVE_OPTION) {
                return;
            }
            txtFilePath.setText(jfc.getSelectedFile().getAbsolutePath());
            ref.text = jfc.getSelectedFile().getAbsolutePath();
        } else {
            ref.text = JOptionPane.showInputDialog(
                    "Please enter full path of the file to be opened");
            if (ref.text == null) {
                return;
            }

            if (ref.text.trim().isEmpty()) {

                JOptionPane.showMessageDialog(App.getAppWindow(),
                                              "Please enter full path of the file to be opened");
                return;

            }
        }

        SwingUtilities.invokeLater(() -> {
            if (showTab(ref.text)) {
                return;
            }
            var sb = readTempFile(ref.text);
            createNewTab(null, sb, ref.text);
        });
    }

    public String readTempFile(String file) {
        var tempFile = Paths.get(file);
        if (!Files.isRegularFile(tempFile)) {
            JOptionPane.showMessageDialog(App.getAppWindow(),
                                          "File doesn't exists or is invalid");
            throw new RuntimeException("File doesn't exists or is invalid");
        }

        try {
            return Files.readString(tempFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setTabContent(String sb) {
        log.info("Setting tab content");
        this.reloading = false;
        ((EditorTab) tabs.getSelectedComponent()).setContent(sb);
    }

    private void createNewTab(FileInfo fileInfo, String sb,
                              String tempFile) {
        cardLayout.show(content, "Tabs");
        int index = tabs.getTabCount();
        String titleHeader = Path.of(tempFile).getFileName().toString();
        if (!localEditor) {
            titleHeader = fileInfo.getName();
        }
        TabHeader tabHeader = new TabHeader(titleHeader);

        EditorTab tab = new EditorTab(fileInfo, sb, tempFile, tabHeader);

        int count = tabs.getTabCount();
        tabHeader.getBtnClose().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = tabs.indexOfTabComponent(tabHeader);
                log.info("Closing tab at: " + index);
                closeTab(index);
            }
        });
        tabs.addTab("test", tab);
        tabs.setTabComponentAt(count, tabHeader);
        tabs.setSelectedIndex(index);
        cardLayout.show(content, "Tabs");
    }

    private boolean showTab(String filename) {
        log.info(filename);
        boolean founded = false;
        for (int i = 0; i < tabs.getTabCount(); i++) {
            EditorTab tab = (EditorTab) tabs.getComponentAt(i);
            if (tab.getLocalFile().toString().equals(filename)) {
                tabs.setSelectedIndex(i);
                founded = true;
                break;
            }
        }

        return founded;
    }

    public void openRemoteFile(FileInfo fileInfo, String tempFile) {
        log.info("Local file: " + tempFile);
        if (showTab(tempFile)) {
            return;
        }

        this.executorService.submit(() -> {
            String sb = readTempFile(tempFile);
            SwingUtilities.invokeLater(() -> {
                if (reloading) {
                    setTabContent(sb);
                } else {
                    createNewTab(fileInfo, sb, tempFile);
                }
                this.setVisible(true);
            });
        });

    }

    public void closeTab(int index) {
        var tab = (EditorTab) tabs.getComponentAt(index);
        if (tab.hasUnsavedChanges() && JOptionPane.showConfirmDialog(this,
                                                                     "Changes will be lost, continue?", "Unsaved changes",
                                                                     JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            return;
        }

        tabs.removeTabAt(index);
    }

    private void wrapText(boolean selected) {
        log.info("wrapText");
        EditorTab tab = (EditorTab) tabs.getSelectedComponent();
        if (tab != null) {
            try {
                tab.setWrapText(selected);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}
