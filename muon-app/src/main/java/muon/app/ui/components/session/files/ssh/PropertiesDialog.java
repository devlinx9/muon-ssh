package muon.app.ui.components.session.files.ssh;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.FileInfo;
import muon.app.ui.components.session.files.FileBrowser;
import muon.app.util.FormatUtils;
import muon.app.util.enums.FileType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static muon.app.App.bundle;

@Slf4j
public class PropertiesDialog extends JDialog {
    public static final int S_IRUSR = 00400; // read by owner
    public static final int S_IWUSR = 00200; // write by owner
    public static final int S_IXUSR = 00100; // execute/search by owner
    public static final int S_IRGRP = 00040; // read by group
    public static final int S_IWGRP = 00020; // write by group
    public static final int S_IXGRP = 00010; // execute/search by group
    public static final int S_IROTH = 00004; // read by others
    public static final int S_IWOTH = 00002; // write by others
    public static final int S_IXOTH = 00001; // execute/search by others
    static final int[] PERMS = new int[]{S_IRUSR, S_IWUSR, S_IXUSR, S_IRGRP,
                                         S_IWGRP, S_IXGRP, S_IROTH, S_IWOTH, S_IXOTH};
    private static final String USER_GROUP_REGEX = "^[^\\s]+\\s+[^\\s]+\\s+([^\\s]+)\\s+([^\\s]+)";
    private static final Pattern DU_PATTERN = Pattern
            .compile("([\\d]+)\\s+(.+)");
    private static final Pattern DF_PATTERN = Pattern.compile(
            "[^\\s]+\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+%)\\s+[^\\s]+");
    private final JCheckBox[] chkPermissons;
    private final JTextField txtSize;
    private final JTextField txtFreeSpace;
    private final FileBrowser fileBrowser;
    private final AtomicBoolean modified = new AtomicBoolean(false);
    private final JButton btnOK;
    @Getter
    private int dialogResult = JOptionPane.CANCEL_OPTION;
    private FileInfo[] details;
    private JTextField txtName;
    private JTextField txtType;
    private JTextField txtOwner;
    private JTextField txtGroup;
    private JTextField txtModified;
    private JTextField txtCreated;
    private JTextField txtPath;
    private JTextField txtFileCount;
    private JButton btnCalculate1;
    private JButton btnCalculate2;
    private Pattern pattern;
    public PropertiesDialog(FileBrowser holder, Window window,
                            boolean multimode) {
        super(window);
        this.fileBrowser = holder;
        setResizable(true);
        setModal(true);
        setTitle("Properties");
        chkPermissons = new JCheckBox[9];
        for (int i = 0; i < 9; i++) {
            String[] labels = new String[]{"read", "write", "execute"};
            chkPermissons[i] = new JCheckBox(labels[i % 3]);
            chkPermissons[i].setAlignmentX(Box.LEFT_ALIGNMENT);
            chkPermissons[i].addActionListener(e -> {
                modified.set(true);
                updateButtonState();
            });
        }
        JLabel lblOwner = new JLabel("Owner permissions");
        lblOwner.setAlignmentX(Box.LEFT_ALIGNMENT);
        JLabel lblGroup = new JLabel("Group permissions");
        lblGroup.setAlignmentX(Box.LEFT_ALIGNMENT);
        JLabel lblOther = new JLabel("Other permissions");
        lblOther.setAlignmentX(Box.LEFT_ALIGNMENT);

        Box b = Box.createVerticalBox();

        JButton btnGetDiskSpaceUsed;
        if (multimode) {
            txtFileCount = new JTextField(30);
            b.add(addPropertyField(txtFileCount, "Total"));
            b.add(Box.createVerticalStrut(10));

            txtSize = new JTextField(30);
            Box boxSize = (Box) addPropertyField(txtSize, "Size");
            boxSize.add(Box.createVerticalGlue());
            btnCalculate1 = new JButton("Calculate");
            btnCalculate1.addActionListener(e -> calculateDirSize());
            btnCalculate1.setEnabled(false);
            boxSize.add(btnCalculate1);
            b.add(boxSize);
            b.add(Box.createVerticalStrut(10));

            txtFreeSpace = new JTextField(30);
            Box boxFree = (Box) addPropertyField(txtFreeSpace, "Free space");
            boxFree.add(Box.createVerticalGlue());
            btnGetDiskSpaceUsed = new JButton("Get free space");
            btnGetDiskSpaceUsed.addActionListener(e -> calculateFreeSpace());
            boxFree.add(btnGetDiskSpaceUsed);
            b.add(boxFree);
            b.add(Box.createVerticalStrut(10));
        } else {
            this.pattern = Pattern.compile(USER_GROUP_REGEX);
            txtName = new JTextField(30);
            b.add(addPropertyField(txtName, "Name"));
            b.add(Box.createVerticalStrut(10));

            txtPath = new JTextField(30);
            b.add(addPropertyField(txtPath, "Path"));
            b.add(Box.createVerticalStrut(10));

            txtSize = new JTextField(30);

            Box boxSize = (Box) addPropertyField(txtSize, "Size");
            boxSize.add(Box.createVerticalGlue());
            btnCalculate2 = new JButton("Calculate");
            btnCalculate2.addActionListener(e -> calculateDirSize());
            boxSize.add(btnCalculate2);
            btnCalculate2.setEnabled(false);

            b.add(boxSize);
            b.add(Box.createVerticalStrut(10));

            txtOwner = new JTextField(30);
            b.add(addPropertyField(txtOwner, "Owner"));
            b.add(Box.createVerticalStrut((10)));

            txtType = new JTextField(30);
            b.add(addPropertyField(txtType, "Type"));
            b.add(Box.createVerticalStrut((10)));

            txtGroup = new JTextField(30);
            b.add(addPropertyField(txtGroup, "Group"));
            b.add(Box.createVerticalStrut((10)));

            txtModified = new JTextField(30);
            b.add(addPropertyField(txtModified, "Last modified"));
            b.add(Box.createVerticalStrut((10)));

            txtFreeSpace = new JTextField(30);
            Box boxFree = (Box) addPropertyField(txtFreeSpace, "Free space");
            boxFree.add(Box.createVerticalGlue());
            btnGetDiskSpaceUsed = new JButton("Get free space");
            btnGetDiskSpaceUsed.addActionListener(e -> calculateFreeSpace());
            boxFree.add(btnGetDiskSpaceUsed);
            b.add(boxFree);
            b.add(Box.createVerticalStrut(10));
        }

        b.add(lblOwner);

        for (int i = 0; i < 3; i++) {
            b.add(chkPermissons[i]);
        }
        b.add(Box.createVerticalStrut((10)));
        b.add(lblGroup);
        for (int i = 3; i < 6; i++) {
            b.add(chkPermissons[i]);
        }
        b.add(Box.createVerticalStrut((10)));
        b.add(lblOther);
        for (int i = 6; i < 9; i++) {
            b.add(chkPermissons[i]);
        }

        Box b2 = Box.createHorizontalBox();
        btnOK = new JButton("Change permissions");
        btnOK.setEnabled(false);
        btnOK.addActionListener(e -> {
            dialogResult = JOptionPane.OK_OPTION;
            chmodAsync(getPermissions(), details);
            dispose();
        });
        JButton btnCancel = new JButton(bundle.getString("cancel"));
        btnCancel.addActionListener(e -> {
            dialogResult = JOptionPane.CANCEL_OPTION;
            dispose();
        });
        b2.setAlignmentX(Box.LEFT_ALIGNMENT);
        b2.add(Box.createHorizontalGlue());
        b2.add(btnOK);
        b2.add(Box.createHorizontalStrut((10)));
        b2.add(btnCancel);
        b.add(Box.createVerticalGlue());

        int w = Math.max(btnOK.getPreferredSize().width,
                         btnCancel.getPreferredSize().width);
        btnOK.setPreferredSize(
                new Dimension(w, btnOK.getPreferredSize().height));
        btnCancel.setPreferredSize(
                new Dimension(w, btnCancel.getPreferredSize().height));

        b.setBorder(new EmptyBorder((10), (10), (10), (10)));
        b2.setBorder(new EmptyBorder((10), (10), (10), (10)));
        add(b);
        add(b2, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(App.getAppWindow());
    }

    private boolean[] extractPermissions(int permissions) {
        boolean[] perms = new boolean[9];
        for (int i = 0; i < 9; i++) {
            perms[i] = (permissions & PropertiesDialog.PERMS[i]) != 0;
        }
        return perms;
    }

    public void setDetails(FileInfo details) {
        this.details = new FileInfo[1];
        this.details[0] = details;
        log.info("Extra: {}", details.getExtra());
        btnCalculate2.setEnabled(details.getType() == FileType.DIRECTORY
                || details.getType() == FileType.DIR_LINK);
        int permissions = details.getPermission();
        if (this.pattern != null && details.getExtra() != null
                && !details.getExtra().isEmpty()) {
            Matcher matcher = pattern.matcher(details.getExtra());
            if (matcher.find()) {
                String user = matcher.group(1);
                String group = matcher.group(2);

                txtOwner.setText(user);
                txtGroup.setText(group);
            }
        }
        this.txtModified.setText(details.getLastModified()
                .format(DateTimeFormatter.ISO_DATE_TIME));
        this.txtName.setText(details.getName());
        this.txtPath.setText(details.getPath());
        this.txtSize.setText(details.getType() == FileType.DIRECTORY
                || details.getType() == FileType.DIR_LINK ? "---"
                                                          : FormatUtils.humanReadableByteCount(details.getSize(),
                true));
        this.txtType.setText(details.getType() == FileType.DIRECTORY
                || details.getType() == FileType.DIR_LINK ? "Directory"
                                                          : "File");
        boolean[] perms = extractPermissions(permissions);
        for (int i = 0; i < 9; i++) {
            chkPermissons[i].setSelected(perms[i]);
        }
    }

    public void setMultipleDetails(FileInfo[] files) {
        this.details = files;
        boolean hasAnyDir = false;
        long totalSize = 0;
        for (FileInfo file : files) {
            if (file.getType() == FileType.DIR_LINK
                    || file.getType() == FileType.DIRECTORY) {
                hasAnyDir = true;
                break;
            }
        }
        if (!hasAnyDir) {
            for (FileInfo file : files) {
                if (file.getType() == FileType.FILE
                        || file.getType() == FileType.FILE_LINK) {
                    totalSize += file.getSize();
                }
            }
            txtSize.setText(
                    FormatUtils.humanReadableByteCount(totalSize, true));
        }
        btnCalculate1.setEnabled(hasAnyDir);
        int fc = 0;
        int dc = 0;
        for (FileInfo f : files) {
            if (f.getType() == FileType.DIRECTORY
                    || f.getType() == FileType.DIR_LINK) {
                dc++;
            } else {
                fc++;
            }
        }
        txtFileCount.setText(fc + " files, " + dc + " folders");
    }

    public int getPermissions() {
        int perms = 0;
        for (int i = 0; i < 9; i++) {
            if (chkPermissons[i].isSelected()) {
                perms |= PropertiesDialog.PERMS[i];
            }
        }
        return perms;
    }

    private Component addPropertyField(JTextField txt, String label) {
        txt.setEditable(false);
        txt.setBackground(App.SKIN.getDefaultBackground());
        txt.setBorder(null);
        JLabel lblFileName = new JLabel(label);
        lblFileName.setPreferredSize(
                new Dimension((150), lblFileName.getPreferredSize().height));
        Box b11 = Box.createHorizontalBox();
        b11.setAlignmentX(Box.LEFT_ALIGNMENT);
        b11.add(lblFileName);
        b11.add(txt);
        return b11;
    }

    private void calculateDirSize() {
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        JDialog dlg = new JDialog(this);
        dlg.setModal(true);
        JLabel lbl = new JLabel("Calculating...");
        lbl.setBorder(new EmptyBorder(10, 10, 10, 10));
        dlg.add(lbl);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                lbl.setText("Cancelling...");
                stopFlag.set(true);
            }
        });
        dlg.pack();
        AtomicBoolean disposed = new AtomicBoolean(false);
        dlg.setLocationRelativeTo(this);
        calcSize(details, (a, b) -> SwingUtilities.invokeLater(() -> {
            dlg.dispose();
            disposed.set(true);
            log.info("Total size: {}", a);
            if (Boolean.TRUE.equals(b)) {
                txtSize.setText(
                        FormatUtils.humanReadableByteCount(a, true));
            }
        }), stopFlag);
        if (!disposed.get()) {
            dlg.setVisible(true);
        }
    }

    private void calculateFreeSpace() {
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        JDialog dlg = new JDialog(this);
        dlg.setModal(true);
        JLabel lbl = new JLabel("Calculating...");
        lbl.setBorder(new EmptyBorder(10, 10, 10, 10));
        dlg.add(lbl);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                lbl.setText("Cancelling...");
                stopFlag.set(true);
            }
        });
        dlg.pack();
        AtomicBoolean disposed = new AtomicBoolean(false);
        dlg.setLocationRelativeTo(this);
        calcFreeSpace(details, (a, b) -> SwingUtilities.invokeLater(() -> {
            dlg.dispose();
            disposed.set(true);
            log.info("Total size: {}", a);
            if (Boolean.TRUE.equals(b)) {
                txtFreeSpace.setText(a);
            }
        }), stopFlag);
        if (!disposed.get()) {
            dlg.setVisible(true);
        }
    }

    public void calcSize(FileInfo[] files, BiConsumer<Long, Boolean> biConsumer,
                         AtomicBoolean stopFlag) {
        StringBuilder command = new StringBuilder();
        command.append(
                "export POSIXLY_CORRECT=1; export BLOCKSIZE=512; du -s ");
        for (FileInfo fileInfo : files) {
            command.append("\"").append(fileInfo.getPath()).append("\" ");
        }
        log.info("Command to execute: {}", command);
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            try {
                long total = 0;
                StringBuilder output = new StringBuilder();
                boolean ret = fileBrowser.getSessionInstance().exec(
                        command.toString(), stopFlag, output,
                        new StringBuilder()) == 0;
                if (stopFlag.get()) {
                    biConsumer.accept(0L, false);
                    return;
                }
                if (!ret && !fileBrowser.isSessionClosed()) {
                        JOptionPane.showMessageDialog(null, bundle.getString("operation_errors")
                                );
                    }

                for (String line : output.toString().split("\n")) {
                    Matcher matcher = DU_PATTERN.matcher(line.trim());
                    if (matcher.find()) {
                        total += Long.parseLong(matcher.group(1).trim()) * 512;
                    }
                }
                biConsumer.accept(total, true);
                return;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            biConsumer.accept(-1L, false);
        });
    }

    public void calcFreeSpace(FileInfo[] files,
                              BiConsumer<String, Boolean> biConsumer, AtomicBoolean stopFlag) {
        StringBuilder command = new StringBuilder();
        command.append("export POSIXLY_CORRECT=1; export BLOCKSIZE=1024; df -P -k \"").append(files[0].getPath()).append("\"");
        log.info("Command to execute: {}", command);
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            try {
                StringBuilder output = new StringBuilder();
                boolean ret = fileBrowser.getSessionInstance().exec(
                        command.toString(), stopFlag, output,
                        new StringBuilder()) == 0;
                log.info(output.toString());
                if (stopFlag.get()) {
                    log.info("stop flag");
                    biConsumer.accept(null, false);
                    return;
                }
                if (!ret && !fileBrowser.isSessionClosed()) {
                        JOptionPane.showMessageDialog(null,
                                                      bundle.getString("operation_errors"));
                    }


                String[] lines = output.toString().split("\n");
                if (lines.length >= 2) {
                    Matcher matcher = DF_PATTERN.matcher(lines[1]);
                    if (matcher.find()) {
                        long total = Long.parseLong(matcher.group(1).trim())
                                * 1024;
                        long free = Long.parseLong(matcher.group(3).trim())
                                * 1024;
                        long freePct = 100 - Long.parseLong(
                                matcher.group(4).replace("%", "").trim());
                        String result = String.format("Free %s of %s (%s)",
                                FormatUtils.humanReadableByteCount(free, true),
                                FormatUtils.humanReadableByteCount(total, true),
                                freePct + "%");
                        biConsumer.accept(result, true);
                        return;
                    } else {
                        log.info(
                                "Did not match with [^\\s]+\\s+(\\d+)\\s+(\\d+)\\s+(\\d+)\\s+(\\d+%)\\s[^\\s+]+");
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            biConsumer.accept(null, false);
        });
    }

    private void chmodAsync(int perm, FileInfo[] paths) {
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        JDialog dlg = new JDialog(this);
        dlg.setModal(true);
        JLabel lbl = new JLabel("Calculating...");
        lbl.setBorder(new EmptyBorder(10, 10, 10, 10));
        dlg.add(lbl);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                lbl.setText("Cancelling...");
                stopFlag.set(true);
            }
        });
        dlg.pack();
        AtomicBoolean disposed = new AtomicBoolean(false);
        dlg.setLocationRelativeTo(this);
        fileBrowser.getHolder().EXECUTOR.submit(() -> {
            try {
                for (FileInfo path : paths) {
                    fileBrowser.getSSHFileSystem().chmod(perm, path.getPath());
                    log.info("Permissions changed");
                }
                modified.set(true);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                if (!fileBrowser.isSessionClosed()) {
                    JOptionPane.showMessageDialog(null, App.bundle.getString("operation_failed"));
                }
            }
            SwingUtilities.invokeLater(() -> {
                dlg.dispose();
                disposed.set(true);
                updateButtonState();
            });
        });

        if (!disposed.get()) {
            dlg.setVisible(true);
        }
    }

    private void updateButtonState() {
        btnOK.setEnabled(modified.get());
    }
}
