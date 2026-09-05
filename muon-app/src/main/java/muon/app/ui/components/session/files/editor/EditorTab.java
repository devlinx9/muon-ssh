package muon.app.ui.components.session.files.editor;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.ui.components.session.Page;
import muon.app.util.FontAwesomeContants;
import org.fife.rsta.ui.GoToDialog;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMakerFactory;
import org.fife.ui.rtextarea.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.zip.CRC32;

@Slf4j
public class EditorTab extends Page implements SearchListener {
    @Getter
    private final FileInfo info;
    private final RSyntaxTextArea textArea;
    private final JComboBox<String> cmbSyntax;

    @Getter
    private transient Path localFile;
    private boolean hasChanges;
    private final JPanel replaceToolBar;
    private boolean replaceToolBarVisible = false;
    private final GoToDialog goToDialog;
    private boolean wrapText = false;
    private final TabHeader header;
    private final ReplaceToolBar toolbar;
    private long checkSum;

    public EditorTab(FileInfo info, String text, String localFile, TabHeader header) {
        super(new BorderLayout());
        this.header = header;
        this.info = info;
        this.localFile = Path.of(localFile);
        this.textArea = new RSyntaxTextArea(20, 80);
        setBorder(new LineBorder(new Color(240, 240, 240), 1));
        this.goToDialog = new GoToDialog(App.getAppWindow());
        RTextScrollPane sp = new RTextScrollPane(textArea);
        sp.setBorder(null);
        Gutter gutter = sp.getGutter();
        gutter.setBorder(new Gutter.GutterBorder(0, 0, 0, 0));
        add(sp);


        textArea.setText(text);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL);
        textArea.setCodeFoldingEnabled(true);
        textArea.discardAllEdits();

        replaceToolBar = new JPanel(new BorderLayout());
        this.toolbar = new ReplaceToolBar(this);
        replaceToolBar.add(this.toolbar);

        JPanel xp = getXp();
        replaceToolBar.add(xp, BorderLayout.EAST);

        TokenMakerFactory factory = TokenMakerFactory.getDefaultInstance();
        Set<String> styles = factory.keySet();
        String[] stylesArr = new String[styles.size()];
        stylesArr = styles.toArray(stylesArr);

        cmbSyntax = new JComboBox<>(stylesArr);
        cmbSyntax.addItemListener(e -> textArea.setSyntaxEditingStyle(e.getItem() + ""));

        cmbSyntax.setMaximumSize(
                new Dimension(50, cmbSyntax.getPreferredSize().height));

        add(cmbSyntax, BorderLayout.NORTH);

        updateCheckSum();

        this.textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                log.debug("document change event update");
                setHasChanges(checkSum != getCheckSum());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                log.debug("document change event insert");
                setHasChanges(checkSum != getCheckSum());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                log.debug("document change event change");
                setHasChanges(checkSum != getCheckSum());
            }
        });
        selectStyle(localFile);
        setHasChanges(false);



    }

    private @NotNull JPanel getXp() {
        JButton closeToolbar = new JButton();
        closeToolbar.setFont(App.getCONTEXT().getSkin().getIconFont());
        closeToolbar.setText(FontAwesomeContants.FA_WINDOW_CLOSE); // Unicode for 'X' icon
        closeToolbar.addActionListener(e -> {
            this.remove(replaceToolBar);
            this.revalidate();
            this.repaint();
            replaceToolBarVisible = false;
            SearchContext ctx = new SearchContext();
            ctx.setMarkAll(false);
            SearchEngine.markAll(textArea, ctx);
        });

        JPanel xp = new JPanel();
        xp.add(closeToolbar);
        return xp;
    }

    public void saveContentsToLocal() throws IOException {
        Files.write(this.localFile,
                    textArea.getText().getBytes(StandardCharsets.UTF_8));
    }

    public void updateCheckSum() {
        checkSum = getCheckSum();
    }


    public void updateLocalFile(Path localFile) {
        this.localFile = localFile;
        header.setTitle(localFile.getFileName().toString());
    }

    public boolean hasUnsavedChanges() {
        return hasChanges;
    }

    public void setHasChanges(boolean value) {
        header.setTitle(this.localFile.getFileName().toString() + (value ? "*" : ""));
        this.hasChanges = value;
    }

    @Override
    public void searchEvent(SearchEvent e) {
        SearchEvent.Type type = e.getType();
        SearchContext context = e.getSearchContext();
        SearchResult result;

        switch (type) {
            case MARK_ALL:
                result = SearchEngine.markAll(textArea, context);
                if (!result.wasFound()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                }
                break;
            case FIND:
                result = SearchEngine.find(textArea, context);
                if (!result.wasFound()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                }
                break;
            case REPLACE:
                result = SearchEngine.replace(textArea, context);
                if (!result.wasFound()) {
                    UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                }
                break;
            case REPLACE_ALL:
                result = SearchEngine.replaceAll(textArea, context);
                JOptionPane.showMessageDialog(null,
                                              result.getCount() + " occurrences replaced.");
                break;
            default:
                log.error("invalid type: {}", type);
                break;
        }
    }

    @Override
    public String getSelectedText() {
        return textArea.getSelectedText();
    }

    public void openFindReplace() {
        if (!replaceToolBarVisible) {
            this.add(replaceToolBar, BorderLayout.SOUTH);
            replaceToolBarVisible = true;
            this.revalidate();
            this.repaint();
            this.toolbar.requestFocusInWindow();
        }
    }

    public void setContent(String content) {
        this.textArea.setText(content);
        updateCheckSum();
        setHasChanges(false);
        if (!content.isEmpty()) {
            this.textArea.setCaretPosition(0);
        }
    }

    public String getContent() {
        return this.textArea.getText();
    }

    public void cutText() {
        textArea.cut();
    }

    public void copyText() {
        textArea.copy();
    }

    public void pasteText() {
        textArea.paste();
    }

    public void gotoLine() {
        goToDialog.setMaxLineNumberAllowed(textArea.getLineCount());
        goToDialog.setVisible(true);
        int line = goToDialog.getLineNumber();
        if (line > 0) {
            try {
                textArea.setCaretPosition(
                        textArea.getLineStartOffset(line - 1));
            } catch (BadLocationException ble) { // Never happens
                UIManager.getLookAndFeel().provideErrorFeedback(textArea);
                log.error(ble.getMessage());
            }
        }
    }

    public boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean value) {
        this.textArea.setLineWrap(value);
        this.textArea.setWrapStyleWord(value);
        this.wrapText = true;
    }

    public void setFontSize(int fontSize) {
        this.textArea
                .setFont(this.textArea.getFont().deriveFont((float) fontSize));
    }

    void selectStyle(String name) {
        Properties properties = new Properties();
        try (InputStream in = Objects.requireNonNull(getClass()
                                                             .getResource("/file-type-map.properties")).openStream()) {
            properties.load(in);
            int index = name.lastIndexOf('.');
            String ext = name.substring(index + 1);
            for (String key : properties.stringPropertyNames()) {
                String value = properties.getProperty(key);
                String[] items = value.split(",");
                for (String item : items) {
                    if (ext.equalsIgnoreCase(item)) {
                        cmbSyntax.setSelectedItem(key);
                        return;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        cmbSyntax.setSelectedItem("text/plain");
    }

    @Override
    public void onLoad() {
        log.info("onload");
    }

    @Override
    public String getIcon() {
        return FontAwesomeContants.FA_STICKY_NOTE;
    }

    @Override
    public String getText() {
        return App.getCONTEXT().getBundle().getString("editor");
    }

    private long getCheckSum() {
        CRC32 crc = new CRC32();
        crc.update(textArea.getText().getBytes(StandardCharsets.UTF_8));
        return crc.getValue();  // e.g. 2666930069
    }


    public void discardEdits(){
        textArea.discardAllEdits();
    }
}
