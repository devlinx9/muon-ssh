
package muon.app.ui.components.session.diskspace;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.session.Page;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.util.FontAwesomeContants;
import muon.app.util.OptionPaneUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static muon.app.util.Constants.SMALL_TEXT_SIZE;


/**
 * @author subhro
 */
@Slf4j
public class DiskspaceAnalyzer extends Page {
    private final CardLayout cardLayout;
    private final SessionContentPanel holder;
    private PartitionTableModel model;
    private JTable table;
    private DefaultTreeModel treeModel;

    
    public DiskspaceAnalyzer(SessionContentPanel holder) {
        this.holder = holder;
        Component firstPanel = createFirstPanel();
        cardLayout = new CardLayout();
        this.setLayout(cardLayout);
        this.add(firstPanel, "firstPanel");
        this.add(createVolumesPanel(), "volPanel");
        this.add(createResultPanel(), "resultPanel");
    }

    private Component createResultPanel() {
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("results", true), true);
        JTree resultTree = new JTree(treeModel);
        resultTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        JButton btnStart = new JButton(App.getCONTEXT().getBundle().getString("start_another_analysis"));
        btnStart.addActionListener(e -> cardLayout.show(this, "firstPanel"));

        Box resultBox = Box.createHorizontalBox();
        resultBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        resultBox.add(Box.createHorizontalGlue());
        resultBox.add(Box.createHorizontalStrut(10));
        resultBox.add(btnStart);

        JLabel resultTitle = new JLabel(App.getCONTEXT().getBundle().getString("directory_usage"));
        resultTitle.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel resultPanel = new JPanel(new BorderLayout());
        resultPanel.add(resultBox, BorderLayout.SOUTH);
        resultPanel.add(new SkinnedScrollPane(resultTree));
        resultPanel.add(resultTitle, BorderLayout.NORTH);
        return resultPanel;
    }

    private Component createFirstPanel() {
        JRadioButton radFolder = new JRadioButton(App.getCONTEXT().getBundle().getString("analyze_folder"));
        JRadioButton radVolume = new JRadioButton(App.getCONTEXT().getBundle().getString("analyze_volume"));
        radFolder.setFont(App.getCONTEXT().getSkin().getDefaultFont().deriveFont(SMALL_TEXT_SIZE));
        radVolume.setFont(App.getCONTEXT().getSkin().getDefaultFont().deriveFont(SMALL_TEXT_SIZE));
        radFolder.setHorizontalAlignment(JRadioButton.LEFT);
        radVolume.setHorizontalAlignment(JRadioButton.LEFT);
        JLabel lblIcon = new JLabel();
        lblIcon.setFont(App.getCONTEXT().getSkin().getIconFont().deriveFont(128.0f));
        lblIcon.setText(FontAwesomeContants.FA_HDD_O);
        JButton btnNext = new JButton(App.getCONTEXT().getBundle().getString("next"));
        btnNext.addActionListener(e -> {
            if (radVolume.isSelected()) {
                cardLayout.show(this, "volPanel");
                listVolumes();
            } else {
                String text = OptionPaneUtils.showInputDialog(this, "Please enter folder path to analyze", "Input");
                if (text != null) {
                    cardLayout.show(this, "resultPanel");
                    analyze(text);
                }
            }
        });

        ButtonGroup bg = new ButtonGroup();
        bg.add(radVolume);
        bg.add(radFolder);

        radVolume.setSelected(true);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.ipadx = 20;
        gc.gridheight = 3;
        panel.add(lblIcon, gc);
        gc.ipadx = 20;
        gc.ipady = 10;
        gc.gridx = 1;
        gc.gridy = 0;
        gc.gridheight = 1;
        panel.add(radVolume, gc);
        gc.gridx = 1;
        gc.gridy = 1;
        panel.add(radFolder, gc);
        gc.gridx = 1;
        gc.gridy = 2;
        gc.ipadx = 20;
        panel.add(btnNext, gc);
        return panel;
    }

    private Component createVolumesPanel() {
        PartitionRenderer r1 = new PartitionRenderer();
        UsagePercentageRenderer r2 = new UsagePercentageRenderer();
        model = new PartitionTableModel();
        table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, r1);
        table.setDefaultRenderer(Double.class, r2);
        table.setRowHeight(Math.max(r1.getPreferredSize().height, r2.getPreferredSize().height));
        table.setSelectionForeground(App.getCONTEXT().getSkin().getDefaultSelectionForeground());
        JScrollPane jsp = new SkinnedScrollPane(table);

        JButton btnBack = new JButton(App.getCONTEXT().getBundle().getString("back"));
        JButton btnNext = new JButton(App.getCONTEXT().getBundle().getString("next"));
        JButton btnReload = new JButton(App.getCONTEXT().getBundle().getString("reload"));

        btnNext.addActionListener(e -> {
            int x = table.getSelectedRow();
            if (x != -1) {
                int r = table.convertRowIndexToModel(x);
                cardLayout.show(this, "resultPanel");
                analyze(model.get(r).getMountPoint());
            } else {
                JOptionPane.showMessageDialog(this, App.getCONTEXT().getBundle().getString("select_partition"));
            }
        });

        btnBack.addActionListener(e -> cardLayout.show(this, "firstPanel"));

        Box bottomBox = Box.createHorizontalBox();
        bottomBox.add(btnReload);
        bottomBox.add(Box.createHorizontalGlue());
        bottomBox.add(btnBack);
        bottomBox.add(Box.createHorizontalStrut(10));
        bottomBox.add(btnNext);
        bottomBox.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel lblTitle = new JLabel(App.getCONTEXT().getBundle().getString("select_volume"));
        lblTitle.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(lblTitle, BorderLayout.NORTH);
        panel.add(jsp);
        panel.add(bottomBox, BorderLayout.SOUTH);
        return panel;
    }

    @Override
    public void onLoad() {
        cardLayout.show(this, "firstPage");
    }

    @Override
    public String getIcon() {
        return FontAwesomeContants.FA_PIE_CHART;
    }

    @Override
    public String getText() {
        return App.getCONTEXT().getBundle().getString("diskspace");
    }

    private void listPartitions(AtomicBoolean stopFlag) {
        try {
            StringBuilder output = new StringBuilder();
            if (holder.getRemoteSessionInstance().exec("export POSIXLY_CORRECT=1;df -P -k", stopFlag, output) == 0) {
                List<PartitionEntry> list = new ArrayList<>();
                boolean first = true;
                for (String line : output.toString().split("\n")) {
                    if (first) {
                        first = false;
                        continue;
                    }
                    if (!line.trim().startsWith("/dev/")) {
                        continue;
                    }
                    String[] arr = line.split("\\s+");
                    if (arr.length < 6) {
                        continue;
                    }
                    PartitionEntry ent = new PartitionEntry(arr[0], arr[5], Long.parseLong(arr[1].trim()) * 1024,
                                                            Long.parseLong(arr[2].trim()) * 1024, Long.parseLong(arr[3].trim()) * 1024,
                                                            Double.parseDouble(arr[4].replace("%", "").trim()));
                    list.add(ent);
                }
                SwingUtilities.invokeLater(() -> {
                    model.clear();
                    model.add(list);
                });
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("Partition listing done");
        }
    }

    private void listVolumes() {
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        holder.EXECUTOR.submit(() -> {
            try {
                holder.disableUi(stopFlag);
                log.info("Listing partitions");
                listPartitions(stopFlag);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                log.debug("Enabling....");
                holder.enableUi();
            }
        });
    }

    private void analyze(String path) {
        log.info("Analyzing path: {}", path);
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        DiskAnalysisTask task = new DiskAnalysisTask(path, stopFlag, res -> {
            SwingUtilities.invokeLater(() -> {
                if (res != null) {
                    log.info("Result found");
                    DefaultMutableTreeNode root = new DefaultMutableTreeNode(res, true);
                    root.setAllowsChildren(true);
                    createTree(root, res);
                    treeModel.setRoot(root);
                }
            });
            holder.enableUi();
        }, holder.getRemoteSessionInstance());
        cardLayout.show(this, "Results");
        holder.disableUi(stopFlag);
        holder.EXECUTOR.submit(task);
    }

    private void createTree(DefaultMutableTreeNode treeNode, DiskUsageEntry entry) {
        entry.getChildren().sort((a, b) -> Long.compare(b.getSize(), a.getSize()));
        for (DiskUsageEntry ent : entry.getChildren()) {
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(ent, true);
            child.setAllowsChildren(true);
            treeNode.add(child);
            createTree(child, ent);
        }
    }

}
