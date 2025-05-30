
package muon.app.ui.components.session.logviewer;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.ClosableTabContent;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.common.SkinnedTextArea;
import muon.app.ui.components.common.TextGutter;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.util.FontAwesomeContants;
import muon.app.util.LayoutUtilities;
import org.tukaani.xz.XZInputStream;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

/**
 * @author subhro
 */
@Slf4j
public class LogContent extends JPanel implements ClosableTabContent {
    public static final String NIMBUS_OVERRIDES = "Nimbus.Overrides";
    public static final String MUON = "muon";
    public static final String INDEX = "index";
    private final SessionContentPanel holder;

    @Getter
    private final String remoteFile;
    private File indexFile;
    private RandomAccessFile raf;
    private long totalLines;
    private static final int LINE_PER_PAGE = 50;
    private long currentPage;
    private long pageCount;
    private final JTextArea textArea;
    private final JLabel lblCurrentPage;
    private final JLabel lblTotalPage;
    private final PagedLogSearchPanel logSearchPanel;
    private final Highlighter.HighlightPainter painter;
    private final TextGutter gutter;
    private final Consumer<String> callback;

    
    public LogContent(SessionContentPanel holder, String remoteLogFile,
                      StartPage startPage, Consumer<String> callback) {
        super(new BorderLayout(), true);
        this.holder = holder;
        this.callback = callback;
        this.remoteFile = remoteLogFile;
        lblCurrentPage = new JLabel();
        lblCurrentPage.setHorizontalAlignment(JLabel.CENTER);
        lblTotalPage = new JLabel();
        lblTotalPage.setHorizontalAlignment(JLabel.CENTER);

        UIDefaults skin = App.getCONTEXT().getSkin().createToolbarSkin();

        JButton btnFirstPage = new JButton();
        btnFirstPage.setToolTipText("First page");
        btnFirstPage.putClientProperty(NIMBUS_OVERRIDES, skin);
        btnFirstPage.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnFirstPage.setText(FontAwesomeContants.FA_FAST_BACKWARD);
        btnFirstPage.addActionListener(e -> firstPage());

        JButton btnNextPage = new JButton();
        btnNextPage.setToolTipText("Next page");
        btnNextPage.putClientProperty(NIMBUS_OVERRIDES, skin);
        btnNextPage.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnNextPage.setText(FontAwesomeContants.FA_STEP_FORWARD);
        btnNextPage.addActionListener(e -> nextPage());

        JButton btnPrevPage = new JButton("");
        btnPrevPage.setToolTipText("Previous page");
        btnPrevPage.putClientProperty(NIMBUS_OVERRIDES, skin);
        btnPrevPage.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnPrevPage.setText(FontAwesomeContants.FA_STEP_BACKWARD);
        btnPrevPage.addActionListener(e -> previousPage());

        JButton btnLastPage = new JButton();
        btnLastPage.setToolTipText("Last page");
        btnLastPage.putClientProperty(NIMBUS_OVERRIDES, skin);
        btnLastPage.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnLastPage.setText(FontAwesomeContants.FA_FAST_FORWARD);
        btnLastPage.addActionListener(e -> lastPage());

        textArea = new SkinnedTextArea();
        textArea.setEditable(false);
        textArea.setBackground(App.getCONTEXT().getSkin().getSelectedTabColor());
        textArea.setWrapStyleWord(true);
        textArea.setFont(textArea.getFont().deriveFont(
                (float) App.getGlobalSettings().getLogViewerFont()));
        this.textArea
                .setLineWrap(App.getGlobalSettings().isLogViewerUseWordWrap());

        gutter = new TextGutter(textArea);
        JScrollPane scrollPane = new SkinnedScrollPane(textArea);
        scrollPane.setRowHeaderView(gutter);
        this.add(scrollPane);

        JCheckBox chkLineWrap = new JCheckBox("Word wrap");

        chkLineWrap.addActionListener(e -> {
            this.textArea.setLineWrap(chkLineWrap.isSelected());
            App.getGlobalSettings()
                    .setLogViewerUseWordWrap(chkLineWrap.isSelected());
            App.getCONTEXT().getSettingsManager().saveSettings();
        });

        chkLineWrap
                .setSelected(App.getGlobalSettings().isLogViewerUseWordWrap());

        SpinnerNumberModel spinnerNumberModel = new SpinnerNumberModel(
                App.getGlobalSettings().getLogViewerFont(), 5, 255, 1);
        JSpinner spFontSize = new JSpinner(spinnerNumberModel);
        spFontSize.setMaximumSize(spFontSize.getPreferredSize());
        spFontSize.addChangeListener(e -> {
            int fontSize = (int) spinnerNumberModel.getValue();
            textArea.setFont(textArea.getFont().deriveFont((float) fontSize));
            gutter.setFont(textArea.getFont());
            gutter.revalidate();
            gutter.repaint();
            App.getGlobalSettings().setLogViewerFont(fontSize);
            App.getCONTEXT().getSettingsManager().saveSettings();
        });

        JButton btnReload = new JButton();
        btnReload.setToolTipText("Reload");
        btnReload.putClientProperty(NIMBUS_OVERRIDES, skin);
        btnReload.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnReload.setText(FontAwesomeContants.FA_UNDO);
        btnReload.addActionListener(e -> {
            try {
                raf.close();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            try {
                Files.delete(this.indexFile.toPath());
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            this.currentPage = 0;
            initPages();
        });

        JButton btnBookMark = new JButton();
        btnBookMark.setToolTipText("Add to bookmark/pin");
        btnBookMark.putClientProperty(NIMBUS_OVERRIDES, skin);
        btnBookMark.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnBookMark.setText(FontAwesomeContants.FA_BOOKMARK);
        btnBookMark.addActionListener(e -> startPage.pinLog(remoteLogFile));

        Box toolbar = Box.createHorizontalBox();
        toolbar.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, App.getCONTEXT().getSkin().getDefaultBorderColor()),
                new EmptyBorder(5, 10, 5, 10)));
        toolbar.add(btnFirstPage);
        toolbar.add(btnPrevPage);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(lblCurrentPage);
        toolbar.add(lblTotalPage);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnNextPage);
        toolbar.add(btnLastPage);
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(chkLineWrap);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(spFontSize);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnReload);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnBookMark);

        this.add(toolbar, BorderLayout.NORTH);

        logSearchPanel = new PagedLogSearchPanel(new SearchListener() {
            @Override
            public void search(String text) {
                AtomicBoolean stopFlag = new AtomicBoolean(false);
                holder.disableUi(stopFlag);
                holder.EXECUTOR.execute(() -> {
                    try {
                        RandomAccessFile searchIndex = LogContent.this
                                .search(text, stopFlag);
                        long len = Objects.requireNonNull(searchIndex).length();
                        SwingUtilities.invokeLater(() -> logSearchPanel.setResults(searchIndex, len));
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        holder.enableUi();
                    }
                });
            }

            @Override
            public void select(long index) {
                log.info("Search item found on line: {}", index);
                int page = (int) index / LINE_PER_PAGE;
                int line = (int) (index % LINE_PER_PAGE);
                log.info("Found on page: {} line: {}", page, line);
                if (currentPage == page) {
                    if (line < textArea.getLineCount() && line != -1) {
                        highlightLine(line);
                    }
                } else {
                    currentPage = page;
                    loadPage(line);
                }
            }
        });
        this.add(logSearchPanel, BorderLayout.SOUTH);

        painter = new DefaultHighlighter.DefaultHighlightPainter(
                App.getCONTEXT().getSkin().getAddressBarSelectionBackground());

        initPages();
    }

    private static void toByteArray(long value, byte[] result) {
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (value & 0xffL);
            value >>= 8;
        }
    }

    private void initPages() {
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        holder.disableUi(stopFlag);
        holder.EXECUTOR.execute(() -> {
            try {
                if ((indexFile(true, stopFlag))
                    || (indexFile(false, stopFlag))) {
                    this.totalLines = this.raf.length() / 16;
                    log.info("Total lines: {}", this.totalLines);
                    if (this.totalLines > 0) {
                        this.pageCount = (long) Math
                                .ceil((double) totalLines / LINE_PER_PAGE);
                        log.info("Number of pages: {}", this.pageCount);
                        if (this.currentPage > this.pageCount) {
                            this.currentPage = this.pageCount;
                        }
                        String pageText = getPageText(this.currentPage,
                                                      stopFlag);
                        SwingUtilities.invokeLater(() -> {

                            this.lblTotalPage.setText(
                                    String.format("/ %d ", this.pageCount));
                            this.lblCurrentPage
                                    .setText((this.currentPage + 1) + "");

                            LayoutUtilities.equalizeSize(this.lblTotalPage,
                                                         this.lblCurrentPage);

                            this.textArea.setText(pageText);
                            if (!Objects.requireNonNull(pageText).isEmpty()) {
                                this.textArea.setCaretPosition(0);
                            }

                            gutter.setLineStart(1);

                            this.revalidate();
                            this.repaint();
                        });
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                holder.enableUi();
            }
        });
    }

    private String getPageText(long page, AtomicBoolean stopFlag)
            throws Exception {
        long lineStart = page * LINE_PER_PAGE;
        long lineEnd = lineStart + LINE_PER_PAGE - 1;

        StringBuilder command = new StringBuilder();

        raf.seek(lineStart * 16);
        byte[] longBytes = new byte[8];
        if (raf.read(longBytes) != 8) {
            throw new Exception("EOF found");
        }

        long startOffset = ByteBuffer.wrap(longBytes).getLong();

        raf.seek(lineEnd * 16);
        if (raf.read(longBytes) != 8) {
            raf.seek(raf.length() - 16);
            raf.read(longBytes);
        }

        long endOffset = ByteBuffer.wrap(longBytes).getLong();
        raf.seek(lineEnd * 16 + 8);
        if (raf.read(longBytes) != 8) {
            raf.seek(raf.length() - 8);
            raf.read(longBytes);
        }
        long lineLength = ByteBuffer.wrap(longBytes).getLong();

        endOffset = endOffset + lineLength;

        long byteRange = endOffset - startOffset;

        if (startOffset < 8192) {
            command.append("dd if=\"")
                    .append(this.remoteFile)
                    .append("\" ibs=1 skip=")
                    .append(startOffset)
                    .append(" count=")
                    .append(byteRange)
                    .append(" 2>/dev/null | sed -ne '1,")
                    .append(LINE_PER_PAGE)
                    .append("p;")
                    .append(LINE_PER_PAGE + 1)
                    .append("q'");
        } else {
            long blockToSkip = startOffset / 8192;
            long bytesToSkip = startOffset % 8192;
            int blocks = (int) Math.ceil((double) byteRange / 8192);


            if (blocks * 8192L - bytesToSkip < byteRange) {
                blocks++;
            }
            command.append("dd if=\"")
                    .append(this.remoteFile)
                    .append("\" ibs=8192 skip=")
                    .append(blockToSkip)
                    .append(" count=")
                    .append(blocks)
                    .append(" 2>/dev/null | dd bs=1 skip=")
                    .append(bytesToSkip)
                    .append(" 2>/dev/null | sed -ne '1,")
                    .append(LINE_PER_PAGE)
                    .append("p;")
                    .append(LINE_PER_PAGE + 1)
                    .append("q'");
        }

        log.debug("Command: {}", command);
        StringBuilder output = new StringBuilder();

        if (holder.getRemoteSessionInstance().exec(command.toString(), stopFlag,
                                                   output) == 0) {
            return output.toString();
        }
        return null;
    }

    private boolean indexFile(boolean xz, AtomicBoolean stopFlag) {
        try {
            File tempFile = Files.createTempFile(
                    MUON + UUID.randomUUID(), INDEX).toFile();
            log.info("Temp file: {}", tempFile);
            try (OutputStream outputStream = new FileOutputStream(tempFile)) {
                String command = "LANG=C awk '{len=length($0); print len; }' \""
                                 + remoteFile + "\" | " + (xz ? "xz" : "gzip") + " |cat";
                log.debug("Command: {}", command);

                if (holder.getRemoteSessionInstance().execBin(command, stopFlag,
                                                              outputStream, null) == 0) {

                    try (InputStream inputStream = new FileInputStream(
                            tempFile);
                         InputStream gzIn = xz
                                            ? new XZInputStream(inputStream)
                                            : new GZIPInputStream(inputStream)) {
                        this.indexFile = createIndexFile(gzIn);
                        this.raf = new RandomAccessFile(this.indexFile, "r");
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return false;
    }

    private File createIndexFile(InputStream inputStream) throws Exception {
        byte[] longBytes = new byte[8];
        long offset = 0;
        File tempFile = Files
                .createTempFile(MUON + UUID.randomUUID(), INDEX)
                .toFile();
        try (OutputStream outputStream = new FileOutputStream(tempFile);
             BufferedReader br = new BufferedReader(
                     new InputStreamReader(inputStream));
             BufferedOutputStream bout = new BufferedOutputStream(
                     outputStream)) {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                toByteArray(offset, longBytes);
                bout.write(longBytes);
                long len = Long.parseLong(line);
                toByteArray(len, longBytes);
                bout.write(longBytes);
                offset += (len + 1);
            }
        }
        return tempFile;
    }

    private void nextPage() {
        if (currentPage < pageCount - 1) {
            currentPage++;
            loadPage();
        }
    }

    private void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            loadPage();
        }
    }

    private void firstPage() {
        currentPage = 0;
        loadPage();
    }

    private void lastPage() {
        if (this.pageCount > 0) {
            currentPage = this.pageCount - 1;
            loadPage();
        }
    }

    public void loadPage() {
        loadPage(-1);
    }

    public void loadPage(int line) {
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        holder.disableUi(stopFlag);
        holder.EXECUTOR.execute(() -> {
            try {
                String pageText = getPageText(this.currentPage, stopFlag);
                SwingUtilities.invokeLater(() -> {
                    this.textArea.setText(pageText);
                    if (!Objects.requireNonNull(pageText).isEmpty()) {
                        this.textArea.setCaretPosition(0);
                    }
                    this.lblCurrentPage.setText((this.currentPage + 1) + "");
                    LayoutUtilities.equalizeSize(this.lblTotalPage,
                                                 this.lblCurrentPage);
                    if (line < textArea.getLineCount() && line != -1) {
                        highlightLine(line);
                    }
                    long lineStart = this.currentPage * LINE_PER_PAGE;
                    gutter.setLineStart(lineStart + 1);
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                holder.enableUi();
            }
        });
    }

    @Override
    public boolean close() {
        try {
            if (raf != null) {
                raf.close();
            }
        } catch (IOException ex) {
            log.error(ex.getMessage(), ex);
        }
        try {
            Files.delete(this.indexFile.toPath());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
        callback.accept(remoteFile);
        return true;
    }

    private RandomAccessFile search(String text, AtomicBoolean stopFlag)
            throws Exception {
        byte[] longBytes = new byte[8];
        File tempFile = Files
                .createTempFile(MUON + UUID.randomUUID(), INDEX)
                .toFile();
        StringBuilder command = new StringBuilder();
        command.append("awk '{if(index(tolower($0),\"").append(text.toLowerCase(Locale.ENGLISH)).append("\")){ print NR}}' \"").append(this.remoteFile).append("\"");
        log.debug("Command: {}", command);
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {

            File searchIndexes = Files.createTempFile(
                    MUON + UUID.randomUUID(), INDEX).toFile();
            if (holder.getRemoteSessionInstance().execBin(command.toString(),
                                                          stopFlag, outputStream, null) == 0) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(new FileInputStream(tempFile)));
                     OutputStream out = new FileOutputStream(searchIndexes);
                     BufferedOutputStream bout = new BufferedOutputStream(
                             out)) {
                    while (true) {
                        String line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        line = line.trim();
                        if (line.isEmpty()) {
                            continue;
                        }
                        long lineNo = Long.parseLong(line);
                        toByteArray(lineNo, longBytes);
                        bout.write(longBytes);
                    }
                    return new RandomAccessFile(searchIndexes, "r");
                }
            }
        }
        return null;
    }

    private void highlightLine(int lineNumber) {
        try {
            int startIndex = textArea.getLineStartOffset(lineNumber);
            int endIndex = textArea.getLineEndOffset(lineNumber);
            log.info("selection: {} {}", startIndex, endIndex);
            textArea.setCaretPosition(startIndex);
            textArea.getHighlighter().removeAllHighlights();
            textArea.getHighlighter().addHighlight(startIndex, endIndex,
                                                   painter);
            log.info(textArea.modelToView2D(startIndex).toString());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
