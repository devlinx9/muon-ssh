
package muon.app.ui.components.session.logviewer;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.util.FontAwesomeContants;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

/**
 * @author subhro
 *
 */
@Slf4j
public class PagedLogSearchPanel extends JPanel {
    private final JTextField txtSearch;
    private JLabel lblResults;
    private final SearchListener searchListener;
    private long index;
    private RandomAccessFile raf;
    private long resultCount = 0;

    public PagedLogSearchPanel(SearchListener searchListener) {
        super(new BorderLayout());
        this.searchListener = searchListener;
        txtSearch = new SkinnedTextField(20);
        txtSearch.addActionListener(e -> startSearch());

        UIDefaults skin = App.getCONTEXT().getSkin().createTabButtonSkin();

        JButton btnSearch = new JButton();
        btnSearch.putClientProperty("Nimbus.Overrides", skin);
        btnSearch.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnSearch.setText(FontAwesomeContants.FA_SEARCH);
        btnSearch.addActionListener(e -> startSearch());

        JButton btnNext = new JButton();
        btnNext.putClientProperty("Nimbus.Overrides", skin);
        btnNext.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnNext.setText(FontAwesomeContants.FA_ANGLE_DOWN);
        btnNext.addActionListener(e -> {
            if (raf == null || this.resultCount < 1)
                return;
            if (index >= this.resultCount - 1) {
                index = 0;
            } else {
                index++;
            }

            log.info("Index: {}", index);
            long lineNo = getLineNumber();
            log.info("Line number: {}", lineNo);
            if (lineNo != -1) {
                searchListener.select(lineNo);
                this.lblResults.setText((index + 1) + "/" + this.resultCount);
            }
        });

        JButton btnPrev = new JButton();
        btnPrev.putClientProperty("Nimbus.Overrides", skin);
        btnPrev.setFont(App.getCONTEXT().getSkin().getIconFont());
        btnPrev.setText(FontAwesomeContants.FA_ANGLE_UP);
        btnPrev.addActionListener(e -> {
            if (raf == null || this.resultCount < 1)
                return;
            if (index <= 0) {
                index = this.resultCount - 1;
            } else {
                index--;
            }
            long lineNo = getLineNumber();
            log.info("Line number: {}", lineNo);
            if (lineNo != -1) {
                searchListener.select(lineNo);
                this.lblResults.setText((index + 1) + "/" + this.resultCount);
            }
        });

        lblResults = new JLabel();

        Box b1 = Box.createHorizontalBox();
        b1.add(new JLabel("Search"));
        b1.add(Box.createHorizontalStrut(10));
        b1.add(txtSearch);
        b1.add(btnSearch);
        b1.add(btnPrev);
        b1.add(btnNext);
        b1.add(Box.createHorizontalStrut(5));
        b1.add(lblResults);

        setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, App.getCONTEXT().getSkin().getDefaultBorderColor()),
                new EmptyBorder(5, 5, 5, 5)));

        add(b1);
    }

    
    private void startSearch() {
        String text = txtSearch.getText();
        if (text.isEmpty())
            return;
        this.lblResults.setText("");
        this.index = 0;
        if (raf != null) {
            try {
                raf.close();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            raf = null;
        }
        searchListener.search(text);
    }

    public void setResults(RandomAccessFile raf, long len) {
        this.lblResults.setText("0/0");
        if (len / 8 < 1) {
            return;
        }
        this.raf = raf;
        this.index = 0;
        this.resultCount = len / 8;
        log.info("Total items found: {}", this.resultCount);
        this.lblResults
                .setText(resultCount > 0 ? 1 + "/" + resultCount : "0/0");
        if (resultCount > 0) {
            this.index = 0;
            searchListener.select(getLineNumber());
        }
    }

    private long getLineNumber() {
        try {
            long offset = index * 8;
            raf.seek(offset);
            byte[] b = new byte[8];
            if (raf.read(b) != 8) {
                return -1;
            }
            return ByteBuffer.wrap(b).getLong() - 1;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return -1;
    }

    public void close() {
        if (raf != null) {
            try {
                raf.close();
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
            raf = null;
        }
    }
}
