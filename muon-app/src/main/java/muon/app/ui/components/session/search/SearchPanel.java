
package muon.app.ui.components.session.search;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.ui.components.session.Page;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.util.FontAwesomeContants;
import muon.app.util.PathUtils;
import muon.app.util.ScriptLoader;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @author subhro
 *
 */
@Slf4j
public class SearchPanel extends Page {
    private static final String LS_REGEX_1 = "([dflo])\\|(.*)";
    private final SessionContentPanel holder;
    private final AtomicBoolean init = new AtomicBoolean(false);
    private JTextField txtName;
    private JComboBox<String> cmbSize;
    private JComboBox<String> cmbSizeUnit;
    private JTextField txtSize;
    private JRadioButton radAny;
    private JRadioButton radWeek;
    private JRadioButton radCust;
    private JRadioButton radFile;
    private JRadioButton radFolder;
    private JSpinner spDate1;
    private JSpinner spDate2;
    private JTextField txtFolder;
    private SearchTableModel model;
    private JTable table;
    private JLabel lblStat;
    private JLabel lblCount;
    private Pattern pattern;
    private JRadioButton radFileName;
    private JRadioButton radFileContents;
    private JCheckBox chkIncludeCompressed;
    private String searchScript;
    private JButton btnShowInBrowser;
    private JButton btnCopyPath;

    
    public SearchPanel(SessionContentPanel holder) {
        this.holder = holder;
    }

    public void resizeColumnWidth(JTable table) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            TableColumn col = columnModel.getColumn(column);
            if (column == 0) {
                col.setPreferredWidth(300);
            } else if (column == 1) {
                col.setPreferredWidth(100);
            } else {
                col.setPreferredWidth(400);
            }
        }
    }

    private void disableButtons() {
        btnShowInBrowser.setEnabled(false);
        btnCopyPath.setEnabled(false);
    }

    private void enableButtons() {
        btnShowInBrowser.setEnabled(true);
        btnCopyPath.setEnabled(true);
    }

    private void find() {
        StringBuilder criteriaBuffer = new StringBuilder();

        String folder = txtFolder.getText();

        criteriaBuffer.append(" ");

        if (!txtSize.getText().isEmpty()) {
            criteriaBuffer.append("-size");
            switch (cmbSize.getSelectedIndex()) {
                case 1:
                    criteriaBuffer.append(" -");
                    break;
                case 2:
                    criteriaBuffer.append(" +");
                    break;
                default:
                    criteriaBuffer.append(" ");
            }
            long sizeFactor = 1;
            switch (cmbSizeUnit.getSelectedIndex()) {
                case 0:
                    sizeFactor = 1024 * 1024 * 1024;
                    break;
                case 1:
                    sizeFactor = 1024 * 1024;
                    break;
                case 2:
                    sizeFactor = 1024;
                    break;
            }
            try {
                long size = Long.parseLong(txtSize.getText()) * sizeFactor;
                criteriaBuffer.append(size).append("c");
                criteriaBuffer.append(" ");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("invalid_size"));
                return;
            }
        }

        if (radFile.isSelected() || radFileContents.isSelected()) {
            criteriaBuffer.append(" -type f");
        } else if (radFolder.isSelected()) {
            criteriaBuffer.append(" -type d");
        }

        if (radWeek.isSelected()) {
            criteriaBuffer.append(" -mtime -7");
        } else if (radCust.isSelected()) {
            Date d1 = (Date) spDate1.getValue();
            Date d2 = (Date) spDate2.getValue();

            if (!d1.before(d2)) {
                JOptionPane.showMessageDialog(this, App.getCONTEXT().getBundle().getString("invalid_date_range")
                        );
                return;
            }

            LocalDate now = LocalDate.now();
            LocalDate date1 = d1.toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate date2 = d2.toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();

            long days1 = ChronoUnit.DAYS.between(date1, now);
            long days2 = ChronoUnit.DAYS.between(date2, now);

            criteriaBuffer.append(" -mtime +").append(days2).append(" -a -mtime -").append(days1);
        }

        StringBuilder scriptBuffer = new StringBuilder();

        if (!txtName.getText().isEmpty() && radFileName.isSelected()) {
            scriptBuffer.append("export NAME='").append(txtName.getText()).append("'\n");
        }

        scriptBuffer.append("export LOCATION=\"").append(folder).append("\"\n");
        scriptBuffer.append("export CRITERIA='").append(criteriaBuffer).append("'\n");
        if (radFileContents.isSelected()) {
            scriptBuffer.append("export CONTENT=1\n");
            scriptBuffer.append("export PATTERN='").append(txtName.getText()).append("'\n");
            if (chkIncludeCompressed.isSelected()) {
                scriptBuffer.append("export UNCOMPRESS=1\n");
            }
        }

        AtomicBoolean stopFlag = new AtomicBoolean(false);
        this.holder.disableUi(stopFlag);
        holder.EXECUTOR.submit(() -> findAsync(scriptBuffer, stopFlag));
    }

    private void findAsync(StringBuilder scriptBuffer, AtomicBoolean stopFlag) {
        SwingUtilities.invokeLater(() -> {
            model.clear();
            lblStat.setText(App.getCONTEXT().getBundle().getString("searching"));
            lblCount.setText(String.format("%d items", model.getRowCount()));
            disableButtons();
        });

        log.info("Starting search.. ");
        try {
            if (searchScript == null) {
                searchScript = ScriptLoader
                        .loadShellScript("/scripts/search.sh");
            }

            scriptBuffer.append(searchScript);

            String findCmd = scriptBuffer.toString();
            log.info(findCmd);

            StringBuilder output = new StringBuilder();

            if (holder.getRemoteSessionInstance().exec(findCmd, stopFlag,
                    output) != 0) {
                log.info("Error in search");
            }

            log.info("search output\n{}", output);

            String[] lines = output.toString().split("\n");
            SwingUtilities.invokeLater(() -> {
                for (String line : lines) {
                    if (!line.isEmpty()) {
                        SearchResult res = parseOutput(line);
                        if (res != null) {
                            model.add(res);
                        }
                    }
                }
                lblCount.setText(
                        String.format("%d items", model.getRowCount()));
            });

            lblStat.setText(App.getCONTEXT().getBundle().getString("idle"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            SwingUtilities.invokeLater(() -> {
                lblStat.setText(App.getCONTEXT().getBundle().getString("idle"));
                lblCount.setText(
                        String.format("%d items", model.getRowCount()));
                this.holder.enableUi();
            });
        }
    }

    private SearchResult parseOutput(String text) {
        if (this.pattern == null) {
            this.pattern = Pattern.compile(LS_REGEX_1);
        }

        Matcher matcher = this.pattern.matcher(text);
        if (matcher.matches()) {
            String type = matcher.group(1);
            String path = matcher.group(2);

            String fileType = "Other";

            switch (type) {
                case "d":
                    fileType = "Folder";
                    break;
                case "l":
                    fileType = "Link";
                    break;
                case "f":
                    fileType = "File";
                    break;
            }

            return new SearchResult(PathUtils.getFileName(path), path,
                    fileType);
        }

        return null;
    }

    @Override
    public void onLoad() {
        if (!init.get()) {
            createUI();
            init.set(true);
        }
    }

    @Override
    public String getIcon() {
        return FontAwesomeContants.FA_SEARCH;
    }

    @Override
    public String getText() {
        return App.getCONTEXT().getBundle().getString("file_search");
    }

    private void createUI() {
        setLayout(new BorderLayout());
        chkIncludeCompressed = new JCheckBox(App.getCONTEXT().getBundle().getString("in_compressed_files"));
        chkIncludeCompressed.setAlignmentX(LEFT_ALIGNMENT);
        radFileName = new JRadioButton(App.getCONTEXT().getBundle().getString("in_filename"));
        radFileName.setAlignmentX(LEFT_ALIGNMENT);
        radFileContents = new JRadioButton(App.getCONTEXT().getBundle().getString("in_filecontent"));
        radFileContents.setAlignmentX(LEFT_ALIGNMENT);

        ButtonGroup bg = new ButtonGroup();
        bg.add(radFileName);
        bg.add(radFileContents);

        radFileName.setSelected(true);

        setLayout(new BorderLayout(1, 1));
        Box b1 = Box.createVerticalBox();
        b1.setOpaque(true);

        JLabel lblName = new JLabel(App.getCONTEXT().getBundle().getString("search_for"));
        lblName.setAlignmentX(LEFT_ALIGNMENT);
        txtName = new SkinnedTextField(20);
        txtName.addActionListener(e -> find());
        Dimension pref = txtName.getPreferredSize();
        txtName.setMaximumSize(pref);
        txtName.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblFolder = new JLabel(App.getCONTEXT().getBundle().getString("search_in"));
        lblFolder.setAlignmentX(LEFT_ALIGNMENT);
        txtFolder = new SkinnedTextField(20);
        txtFolder.setPreferredSize(pref);
        txtFolder.setMaximumSize(pref);
        txtFolder.setAlignmentX(LEFT_ALIGNMENT);

        txtFolder.setText("$HOME");

        JLabel lblSize = new JLabel(App.getCONTEXT().getBundle().getString("size"));
        lblSize.setAlignmentX(LEFT_ALIGNMENT);

        txtSize = new SkinnedTextField();
        txtSize.setAlignmentX(LEFT_ALIGNMENT);
        Dimension txtSizeD = new Dimension(60,
                txtSize.getPreferredSize().height);
        txtSize.setPreferredSize(txtSizeD);
        txtSize.setMaximumSize(txtSizeD);

        cmbSizeUnit = new JComboBox<>(new String[]{"GB", "MB", "KB", "B"});
        cmbSizeUnit.setMaximumSize(cmbSizeUnit.getPreferredSize());

        cmbSize = new JComboBox<>(new String[]{"=", "<", ">"});
        cmbSize.setMaximumSize(
                new Dimension(20, cmbSize.getPreferredSize().height));
        cmbSize.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblMtime = new JLabel(App.getCONTEXT().getBundle().getString("modified"));
        lblMtime.setAlignmentX(LEFT_ALIGNMENT);

        ButtonGroup btnGroup1 = new ButtonGroup();
        radAny = new JRadioButton(App.getCONTEXT().getBundle().getString("any_time"));
        radAny.setAlignmentX(LEFT_ALIGNMENT);
        radWeek = new JRadioButton(App.getCONTEXT().getBundle().getString("this_week"));
        radWeek.setAlignmentX(LEFT_ALIGNMENT);
        radCust = new JRadioButton(App.getCONTEXT().getBundle().getString("between"));
        radCust.setAlignmentX(LEFT_ALIGNMENT);

        btnGroup1.add(radAny);
        btnGroup1.add(radWeek);
        btnGroup1.add(radCust);

        ActionListener radSelected = new ActionListener() {

            private void disableSpinners() {
                spDate1.setEnabled(false);
                spDate2.setEnabled(false);
            }

            private void enableSpinners() {
                spDate1.setEnabled(true);
                spDate2.setEnabled(true);
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == radAny) {
                    disableSpinners();
                } else if (e.getSource() == radWeek) {
                    disableSpinners();
                } else {
                    enableSpinners();
                }
            }
        };

        radAny.addActionListener(radSelected);
        radWeek.addActionListener(radSelected);
        radCust.addActionListener(radSelected);

        radAny.setSelected(true);

        JLabel lblFrom = new JLabel(App.getCONTEXT().getBundle().getString("from"));
        lblFrom.setAlignmentX(LEFT_ALIGNMENT);
        JLabel lblTo = new JLabel(App.getCONTEXT().getBundle().getString("to"));
        lblTo.setAlignmentX(LEFT_ALIGNMENT);

        SpinnerDateModel sm1 = new SpinnerDateModel();
        sm1.setEnd(new Date());
        spDate1 = new JSpinner(sm1);
        spDate1.setPreferredSize(pref);
        spDate1.setMaximumSize(pref);
        spDate1.setAlignmentX(LEFT_ALIGNMENT);
        spDate1.setEditor(new JSpinner.DateEditor(spDate1, "dd/MM/yyyy"));

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        spDate1.setValue(cal.getTime());
        spDate1.setEnabled(false);

        SpinnerDateModel sm2 = new SpinnerDateModel();
        sm2.setEnd(new Date());
        spDate2 = new JSpinner(sm2);
        spDate2.setMaximumSize(pref);
        spDate2.setPreferredSize(pref);
        spDate2.setAlignmentX(LEFT_ALIGNMENT);
        spDate2.setEditor(new JSpinner.DateEditor(spDate2, "dd/MM/yyyy"));
        spDate2.setEnabled(false);

        JLabel lblLookfor = new JLabel(App.getCONTEXT().getBundle().getString("look_for"));
        lblLookfor.setAlignmentX(LEFT_ALIGNMENT);

        ButtonGroup btnGroup2 = new ButtonGroup();
        JRadioButton radBoth = new JRadioButton(App.getCONTEXT().getBundle().getString("both_file_folder"));
        radBoth.setAlignmentX(LEFT_ALIGNMENT);
        radFile = new JRadioButton(App.getCONTEXT().getBundle().getString("file_only"));
        radFile.setAlignmentX(LEFT_ALIGNMENT);
        radFolder = new JRadioButton(App.getCONTEXT().getBundle().getString("folder_only"));
        radFolder.setAlignmentX(LEFT_ALIGNMENT);

        btnGroup2.add(radBoth);
        btnGroup2.add(radFile);
        btnGroup2.add(radFolder);

        radBoth.setSelected(true);

        JButton btnSearch = new JButton(App.getCONTEXT().getBundle().getString("search"));
        btnSearch.setAlignmentX(LEFT_ALIGNMENT);

        btnSearch.addActionListener(e -> find());

        model = new SearchTableModel();

        table = new JTable(model);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }

            if (table.getSelectedRowCount() > 0) {
                enableButtons();
            } else {
                disableButtons();
            }
        });

        table.setIntercellSpacing(new Dimension(0, 0));
        table.setRowHeight(24);
        table.setShowGrid(false);
        resizeColumnWidth(table);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        SearchTableRenderer r = new SearchTableRenderer(model);
        table.setDefaultRenderer(String.class, r);
        table.setDefaultRenderer(Date.class, r);
        table.setDefaultRenderer(Integer.class, r);
        table.setDefaultRenderer(Long.class, r);

        JScrollPane jsp = new SkinnedScrollPane(table);
        jsp.setBorder(null);

        lblStat = new JLabel(App.getCONTEXT().getBundle().getString("ready"));
        lblCount = new JLabel("");
        lblCount.setHorizontalAlignment(JLabel.RIGHT);


        b1.add(lblName);
        b1.add(Box.createVerticalStrut(3));
        b1.add(txtName);

        b1.add(Box.createVerticalStrut(10));

        b1.add(radFileName);
        b1.add(Box.createVerticalStrut(3));
        b1.add(radFileContents);
        b1.add(Box.createVerticalStrut(3));
        b1.add(chkIncludeCompressed);

        b1.add(Box.createVerticalStrut(10));

        b1.add(lblFolder);
        b1.add(Box.createVerticalStrut(3));
        b1.add(txtFolder);

        b1.add(Box.createVerticalStrut(10));

        Box boxSize = Box.createHorizontalBox();
        boxSize.setAlignmentX(LEFT_ALIGNMENT);
        boxSize.add(lblSize);
        boxSize.add(Box.createHorizontalGlue());
        boxSize.add(cmbSize);
        boxSize.add(Box.createRigidArea(new Dimension(3, 0)));
        boxSize.add(txtSize);
        boxSize.add(Box.createRigidArea(new Dimension(3, 0)));
        boxSize.add(cmbSizeUnit);

        b1.add(boxSize);

        b1.add(Box.createVerticalStrut(10));

        b1.add(lblMtime);
        b1.add(Box.createVerticalStrut(3));
        b1.add(radAny);
        b1.add(Box.createVerticalStrut(3));
        b1.add(radWeek);
        b1.add(Box.createVerticalStrut(3));
        b1.add(radCust);

        b1.add(Box.createVerticalStrut(10));

        b1.add(lblFrom);
        b1.add(Box.createVerticalStrut(3));
        b1.add(spDate1);
        b1.add(Box.createVerticalStrut(3));
        b1.add(lblTo);
        b1.add(Box.createVerticalStrut(3));
        b1.add(spDate2);

        b1.add(Box.createVerticalStrut(10));

        b1.add(lblLookfor);
        b1.add(Box.createVerticalStrut(3));
        b1.add(radBoth);
        b1.add(Box.createVerticalStrut(3));
        b1.add(radFile);
        b1.add(Box.createVerticalStrut(3));
        b1.add(radFolder);

        b1.add(Box.createVerticalStrut(10));

        b1.setMinimumSize(b1.getPreferredSize());
        b1.setMaximumSize(b1.getPreferredSize());

        b1.setBorder(new EmptyBorder(10, 10, 10, 10));


        Box statBox = Box.createHorizontalBox();
        statBox.setOpaque(true);
        statBox.add(Box.createRigidArea(new Dimension(10, 25)));
        statBox.add(lblStat);
        statBox.add(Box.createHorizontalGlue());
        statBox.add(lblCount);
        statBox.add(Box.createRigidArea(new Dimension(10, 25)));
        statBox.setBorder(
                new MatteBorder(1, 0, 0, 0, App.getCONTEXT().getSkin().getDefaultBorderColor()));

        btnShowInBrowser = new JButton(App.getCONTEXT().getBundle().getString("show_location"));
        btnCopyPath = new JButton(App.getCONTEXT().getBundle().getString("copy_path"));

        disableButtons();

        btnCopyPath.addActionListener(e -> {
            int index = table.getSelectedRow();
            if (index != -1) {
                SearchResult res = model.getItemAt(index);
                String path = res.getPath();
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(path), null);
            }
        });

        btnShowInBrowser.addActionListener(e -> {
            int index = table.getSelectedRow();
            if (index != -1) {
                SearchResult res = model.getItemAt(index);
                String path = res.getPath();
                path = PathUtils.getParent(path);
                if (!Objects.requireNonNull(path).isEmpty()) {
                    holder.openFileInBrowser(path);
                }
            }
        });

        Box bActions = Box.createHorizontalBox();
        bActions.setOpaque(true);
        bActions.setBorder(new EmptyBorder(5, 10, 5, 10));
        bActions.add(Box.createHorizontalGlue());
        bActions.add(btnShowInBrowser);
        bActions.add(Box.createHorizontalStrut(10));
        bActions.add(btnCopyPath);

        JScrollPane jspB1 = new JScrollPane(b1,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        jspB1.setBorder(null);



        JPanel p = new JPanel(new BorderLayout(1, 1));

        p.add(jsp, BorderLayout.CENTER);
        p.add(bActions, BorderLayout.SOUTH);

        JPanel pp = new JPanel(new BorderLayout());
        pp.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 0, 1, App.getCONTEXT().getSkin().getSelectedTabColor()),
                new EmptyBorder(5, 5, 5, 5)));
        pp.add(jspB1);

        JPanel buttonHolder = new JPanel(new BorderLayout());
        buttonHolder.setBorder(new EmptyBorder(5, 10, 5, 10));
        buttonHolder.add(btnSearch);
        pp.add(buttonHolder, BorderLayout.SOUTH);

        JPanel splitPane = new JPanel(new BorderLayout());
        splitPane.add(p);
        splitPane.add(pp, BorderLayout.WEST);

        add(splitPane);
        add(statBox, BorderLayout.SOUTH);
    }

}
