package muon.app.ui.components.session.dialog;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedSplitPane;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.ui.components.session.*;
import muon.app.util.OptionPaneUtils;
import muon.app.util.enums.ImportOption;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import static muon.app.ui.components.session.dialog.TreeManager.getNewUuid;
import static muon.app.ui.components.session.dialog.TreeManager.getNode;
import static muon.app.util.ScalingUtil.scale;
import static muon.app.util.ScalingUtil.getScaledEmptyBorder;

@Slf4j
public class NewSessionDlg extends JDialog implements ActionListener, TreeSelectionListener, TreeModelListener {

    private static final long serialVersionUID = -1182844921331289546L;
    public static final String BUTTON_NAME = "button.name";

    private TreeManager treeManager;

    private DefaultTreeModel treeModel;
    private JTree tree;
    private DefaultMutableTreeNode rootNode;
    private SessionInfoPanel sessionInfoPanel;
    private JButton btnConnect;
    private JButton btnCancel;
    private JButton btnEdit;
    private boolean editMode = false;
    private boolean pendingEditOnSelect = false;
    private JTextField txtName;
    private NamedItem selectedInfo;
    private SessionInfo info;
    private JLabel lblName;
    private JPopupMenu groupPopupMenu;

    public NewSessionDlg(Window wnd) {
        super(wnd);
        createUI();
    }

    private void createUI() {
        setBackground(new Color(245, 245, 245));
        setLayout(new BorderLayout());

        setSize(scale(800), scale(600));
        setModal(true);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                log.info("Saving before exit");
                save();
                dispose();
            }
        });

        setTitle(App.getCONTEXT().getBundle().getString("session_manager"));

        treeModel = new DefaultTreeModel(null, true);
        treeModel.addTreeModelListener(this);
        tree = new AutoScrollingJTree(treeModel);
        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new TreeTransferHandler());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.getSelectionModel().addTreeSelectionListener(this);
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        if (node != null && node.getChildCount() > 0) {
                            tree.setSelectionPath(path);
                            groupPopupMenu.show(tree, e.getX(), e.getY());
                        }
                    }
                } else if (e.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) {
                        return;
                    }
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
                    if (node == null || node.getAllowsChildren()) {
                        return;
                    }
                    connectClicked();
                }
            }
        });


        tree.setEditable(false);
        treeManager = new TreeManager();
        JScrollPane jsp = new JScrollPane(tree);
        jsp.setBorder(new LineBorder(App.getCONTEXT().getSkin().getDefaultBorderColor(), 1));

        JButton btnNewHost = new JButton(App.getCONTEXT().getBundle().getString("new_site"));
        btnNewHost.addActionListener(this);
        btnNewHost.putClientProperty(BUTTON_NAME, "btnNewHost");
        JButton btnNewFolder = new JButton(App.getCONTEXT().getBundle().getString("new_folder"));
        btnNewFolder.addActionListener(this);
        btnNewFolder.putClientProperty(BUTTON_NAME, "btnNewFolder");
        JButton btnDel = new JButton(App.getCONTEXT().getBundle().getString("remove"));
        btnDel.addActionListener(this);
        btnDel.putClientProperty(BUTTON_NAME, "btnDel");
        JButton btnDup = new JButton(App.getCONTEXT().getBundle().getString("duplicate"));
        btnDup.addActionListener(this);
        btnDup.putClientProperty(BUTTON_NAME, "btnDup");

        btnEdit = new JButton(App.getCONTEXT().getBundle().getString("edit"));
        btnEdit.addActionListener(this);
        btnEdit.putClientProperty(BUTTON_NAME, "btnEdit");

        btnConnect = new JButton(App.getCONTEXT().getBundle().getString("connect"));
        btnConnect.addActionListener(this);
        btnConnect.putClientProperty(BUTTON_NAME, "btnConnect");

        btnCancel = new JButton(App.getCONTEXT().getBundle().getString("cancel"));
        btnCancel.addActionListener(this);
        btnCancel.putClientProperty(BUTTON_NAME, "btnCancel");

        JButton btnExport = new JButton(App.getCONTEXT().getBundle().getString("export"));
        btnExport.addActionListener(this);
        btnExport.putClientProperty(BUTTON_NAME, "btnExport");

        JButton btnImport = new JButton(App.getCONTEXT().getBundle().getString("import"));
        btnImport.addActionListener(this);
        btnImport.putClientProperty(BUTTON_NAME, "btnImport");

        normalizeButtonSize();

        Box box1 = Box.createHorizontalBox();
        box1.setBorder(getScaledEmptyBorder(10, 10, 10, 10));
        box1.add(Box.createHorizontalGlue());
        box1.add(Box.createHorizontalStrut(10));
        box1.add(btnEdit);
        box1.add(Box.createHorizontalStrut(10));
        box1.add(btnConnect);
        box1.add(Box.createHorizontalStrut(10));
        box1.add(btnCancel);

        GridLayout gl = new GridLayout(3, 2, 5, 5);
        JPanel btnPane = new JPanel(gl);
        btnPane.setBorder(getScaledEmptyBorder(10, 0, 0, 0));
        btnPane.add(btnNewHost);
        btnPane.add(btnNewFolder);
        btnPane.add(btnDup);
        btnPane.add(btnDel);
        btnPane.add(btnExport);
        btnPane.add(btnImport);

        JSplitPane splitPane = new SkinnedSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        JPanel treePane = new JPanel(new BorderLayout());
        treePane.setBorder(getScaledEmptyBorder(10, 10, 10, 0));
        treePane.add(jsp);
        treePane.add(btnPane, BorderLayout.SOUTH);

        add(treePane, BorderLayout.WEST);

        sessionInfoPanel = new SessionInfoPanel();

        JPanel namePanel = new JPanel();

        JPanel pp = new JPanel(new BorderLayout());
        pp.add(namePanel, BorderLayout.NORTH);
        pp.add(sessionInfoPanel);

        JPanel pdet = new JPanel(new BorderLayout());

        JScrollPane scrollPane = new JScrollPane(pp);
        scrollPane.setBorder(null);
        pdet.add(scrollPane);
        pdet.add(box1, BorderLayout.SOUTH);


        BoxLayout boxLayout = new BoxLayout(namePanel, BoxLayout.PAGE_AXIS);
        namePanel.setLayout(boxLayout);

        namePanel.setBorder(getScaledEmptyBorder(10, 0, 0, 10));

        lblName = new JLabel(App.getCONTEXT().getBundle().getString("name"));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblName.setHorizontalAlignment(JLabel.LEADING);
        lblName.setBorder(getScaledEmptyBorder(0, 0, 5, 0));


        txtName = new SkinnedTextField(10);
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtName.setHorizontalAlignment(JLabel.LEADING);
        txtName.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                updateName();
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                updateName();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                updateName();
            }

            private void updateName() {
                selectedInfo.setName(txtName.getText());
                TreePath parentPath = tree.getSelectionPath();
                DefaultMutableTreeNode parentNode;

                if (parentPath != null) {
                    parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
                    if (parentNode != null) {
                        treeModel.nodeChanged(parentNode);
                    }
                }
            }
        });

        namePanel.add(lblName);
        namePanel.add(txtName);

        JPanel prgPanel = new JPanel();

        JLabel lbl = new JLabel(App.getCONTEXT().getBundle().getString("connecting"));
        prgPanel.add(lbl);

        splitPane.setLeftComponent(treePane);
        splitPane.setRightComponent(pdet);

        add(splitPane);

        lblName.setVisible(false);
        txtName.setVisible(false);
        sessionInfoPanel.setVisible(false);
        btnConnect.setVisible(false);
        setEditMode(false);

        // --- Add popup menu for sorting ---
        groupPopupMenu = new JPopupMenu();
        JMenuItem sortAZMenuItem = new JMenuItem("Sort A-Z");
        JMenuItem sortZAMenuItem = new JMenuItem("Sort Z-A");
        groupPopupMenu.add(sortAZMenuItem);
        groupPopupMenu.add(sortZAMenuItem);

        sortAZMenuItem.addActionListener(e -> sortGroup(true));
        sortZAMenuItem.addActionListener(e -> sortGroup(false));
        // --- End popup menu ---

        rootNode = treeManager.loadTree(SessionStore.load(), treeModel, tree);
    }

    private void sortGroup(boolean ascending) {
        TreePath path = tree.getSelectionPath();
        if (path == null) return;
        DefaultMutableTreeNode groupNode = (DefaultMutableTreeNode) path.getLastPathComponent();
        if (!(groupNode.getChildCount() > 0)) return;

        java.util.List<DefaultMutableTreeNode> children = new java.util.ArrayList<>();
        for (int i = 0; i < groupNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) groupNode.getChildAt(i);
            if (child.getUserObject() instanceof SessionInfo) {
                children.add(child);
            }
        }
        // Remove all SessionInfo children
        for (DefaultMutableTreeNode child : children) {
            treeModel.removeNodeFromParent(child);
        }
        // Sort
        children.sort((a, b) -> {
            String nameA = ((SessionInfo) a.getUserObject()).getName();
            String nameB = ((SessionInfo) b.getUserObject()).getName();
            return ascending ? nameA.compareToIgnoreCase(nameB) : nameB.compareToIgnoreCase(nameA);
        });
        // Re-insert
        for (DefaultMutableTreeNode child : children) {
            treeModel.insertNodeInto(child, groupNode, groupNode.getChildCount());
        }
        tree.expandPath(path);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JButton btn = (JButton) e.getSource();
        TreePath parentPath = tree.getSelectionPath();
        DefaultMutableTreeNode parentNode = null;

        if (parentPath != null) {
            parentNode = (DefaultMutableTreeNode) (parentPath.getLastPathComponent());
        }

        switch ((String) btn.getClientProperty(BUTTON_NAME)) {
            case "btnNewHost":
                createNewHost(parentNode);
                break;
            case "btnNewFolder":
                createNewFolder(parentNode);
                break;
            case "btnDel":
                deleteNode();
                break;
            case "btnDup":
                duplicateNode();
                break;
            case "btnEdit":
                toggleEditMode();
                break;
            case "btnConnect":
                connectClicked();
                break;
            case "btnCancel":
                save();
                dispose();
                break;
            case "btnImport":
                importSessions(parentNode);
                break;
            case "btnExport":
                SessionExportImport.exportSessions();
                break;
            default:
                break;
        }
    }

    private void importSessions(DefaultMutableTreeNode parentNode) {
        if (parentNode == null) {
            parentNode = rootNode;
        }
        if (parentNode.getUserObject() instanceof SessionInfo) {
            parentNode = (DefaultMutableTreeNode) parentNode.getParent();
        }
        JComboBox<ImportOption> cmbImports = new JComboBox<>(ImportOption.values());

        if (OptionPaneUtils.showOptionDialog(this, new Object[]{App.getCONTEXT().getBundle().getString("import_from"), cmbImports}, App.getCONTEXT().getBundle().getString("import_sessions")) == JOptionPane.OK_OPTION) {
            manageImportOptions(parentNode, cmbImports);
        }
    }

    private void manageImportOptions(DefaultMutableTreeNode parentNode, JComboBox<ImportOption> cmbImports) {
        ImportOption selectedOption = (ImportOption) cmbImports.getSelectedItem();
        if (selectedOption == null) {
            return;
        }

        switch (selectedOption) {
            case PUTTY:
            case WINSCP:
                new ImportDlg(this, cmbImports.getSelectedIndex(), parentNode).setVisible(true);
                treeModel.nodeStructureChanged(parentNode);
                break;
            case SSH_CONFIG_FILE:
                if (SessionExportImport.importSessionsSSHConfig()) {
                    rootNode = treeManager.loadTree(SessionStore.load(), treeModel, tree);
                }
                break;
            case MUON_SESSION_STORE:
                if (SessionExportImport.importMuonSessions()) {
                    rootNode = treeManager.loadTree(SessionStore.load(), treeModel, tree);
                }
                break;
            case PREVIOUS_MUON_VERSIONS:
                if (SessionExportImport.importSessionsPreviousVersion()) {
                    rootNode = treeManager.loadTree(SessionStore.load(), treeModel, tree);
                }
                break;
            default:
                break;
        }
    }

    private void duplicateNode() {
        DefaultMutableTreeNode node1 = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node1 != null && node1.getParent() != null && (node1.getUserObject() instanceof SessionInfo)) {
            SessionInfo sessionInfo = ((SessionInfo) node1.getUserObject()).copy();
            sessionInfo.setId(getNewUuid(rootNode));
            DefaultMutableTreeNode child = new DefaultMutableTreeNode(sessionInfo);
            child.setAllowsChildren(false);
            treeModel.insertNodeInto(child, (MutableTreeNode) node1.getParent(), node1.getParent().getChildCount());
            treeManager.selectNode(sessionInfo.getId(), child, tree);
        } else if (node1 != null && node1.getParent() != null && (node1.getUserObject() instanceof NamedItem)) {
            SessionFolder newFolder = new SessionFolder();
            newFolder.setId(getNewUuid(rootNode));
            newFolder.setName("Copy of " + ((NamedItem) node1.getUserObject()).getName());
            Enumeration<TreeNode> childrens = node1.children();
            DefaultMutableTreeNode newFolderTree = new DefaultMutableTreeNode(newFolder);
            while (childrens.hasMoreElements()) {
                DefaultMutableTreeNode defaultMutableTreeNode = (DefaultMutableTreeNode) childrens.nextElement();
                if (defaultMutableTreeNode.getUserObject() instanceof SessionInfo) {
                    SessionInfo newCopyInfo = ((SessionInfo) defaultMutableTreeNode.getUserObject()).copy();
                    newCopyInfo.setName("Copy of " + newCopyInfo.getName());
                    DefaultMutableTreeNode subChild = new DefaultMutableTreeNode(newCopyInfo);
                    subChild.setAllowsChildren(false);
                    newFolderTree.add(subChild);
                }
            }
            MutableTreeNode parent = (MutableTreeNode) node1.getParent();
            treeModel.insertNodeInto(newFolderTree, parent, node1.getParent().getChildCount());
            treeManager.selectNode(newFolder.getId(), newFolderTree, tree);
        }
    }

    private void deleteNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
        if (node != null && node.getParent() != null) {
            // guard: do not delete root
            if (node.getUserObject() != null && "Empty_Root".equals(node.getUserObject().toString())) {
                return;
            }
            if (!confirmDeletion(node)) {
                return;
            }
            DefaultMutableTreeNode sibling = getSibling(node);
            if (sibling != null) {
                String id = ((NamedItem) sibling.getUserObject()).getId();
                treeManager.selectNode(id, sibling, tree);
            } else {
                DefaultMutableTreeNode parentNode1 = (DefaultMutableTreeNode) node.getParent();
                if (!parentNode1.getUserObject().toString().equals("Empty_Root")) {
                    tree.setSelectionPath(new TreePath(parentNode1.getPath()));
                }
            }
            treeModel.removeNodeFromParent(node);
        }
    }

    private boolean confirmDeletion(DefaultMutableTreeNode node) {
        boolean isFolder = isFolderNode(node);
        if (isFolder) {
            SessionFolder folder = extractFolder(node);
            return confirmFolderByName(folder);
        }

        String msgKey = "confirm_delete_session";
        int res = JOptionPane.showConfirmDialog(this,
                App.getCONTEXT().getBundle().getString(msgKey),
                App.getCONTEXT().getBundle().getString("delete"),
                JOptionPane.YES_NO_OPTION);
        return res == JOptionPane.YES_OPTION;
    }

    private boolean isFolderNode(DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();
        if (obj instanceof SessionFolder) return true;
        // fallback: any node that allows children but is not a SessionInfo is treated as folder
        if (node.getAllowsChildren() && !(obj instanceof SessionInfo)) return true;
        return false;
    }

    private SessionFolder extractFolder(DefaultMutableTreeNode node) {
        Object obj = node.getUserObject();
        if (obj instanceof SessionFolder) {
            return (SessionFolder) obj;
        }
        // fabricate minimal folder info for prompt
        SessionFolder folder = new SessionFolder();
        folder.setName(obj == null ? "" : obj.toString());
        return folder;
    }

    private boolean confirmFolderByName(SessionFolder folder) {
        String expected = folder.getName();
        String prompt = String.format(App.getCONTEXT().getBundle().getString("confirm_delete_folder_name"), expected);

        JPanel panel = new JPanel(new BorderLayout(0, scale(8)));
        panel.add(new JLabel(prompt), BorderLayout.NORTH);
        JTextField input = new JTextField();
        panel.add(input, BorderLayout.CENTER);

        JButton ok = new JButton(App.getCONTEXT().getBundle().getString("ok"));
        JButton cancel = new JButton(App.getCONTEXT().getBundle().getString("cancel"));
        ok.setEnabled(false);

        final boolean[] confirmed = {false};

        ActionListener closeOk = e -> {
            confirmed[0] = true;
            SwingUtilities.getWindowAncestor(panel).dispose();
        };
        ActionListener closeCancel = e -> {
            confirmed[0] = false;
            SwingUtilities.getWindowAncestor(panel).dispose();
        };
        ok.addActionListener(closeOk);
        cancel.addActionListener(closeCancel);

        input.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void update() {
                ok.setEnabled(expected.equals(input.getText().trim()));
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        input.addActionListener(closeOk);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, scale(8), 0));
        buttons.add(cancel);
        buttons.add(ok);

        JPanel container = new JPanel(new BorderLayout(0, scale(10)));
        container.setBorder(BorderFactory.createEmptyBorder(scale(10), scale(10), scale(10), scale(10)));
        container.add(panel, BorderLayout.CENTER);
        container.add(buttons, BorderLayout.SOUTH);

        JDialog dialog = new JDialog(this, App.getCONTEXT().getBundle().getString("delete"), true);
        dialog.getContentPane().add(container);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);

        return confirmed[0];
    }

    private int countSites(DefaultMutableTreeNode node) {
        int count = 0;
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            Object uo = child.getUserObject();
            if (uo instanceof SessionInfo) count++;
            if (uo instanceof SessionFolder) count += countSites(child);
        }
        return count;
    }

    private static DefaultMutableTreeNode getSibling(DefaultMutableTreeNode node) {
        DefaultMutableTreeNode sibling = node.getNextSibling();
        if (sibling == null) {
            sibling = node.getPreviousSibling();
        }
        return sibling;
    }

    private void createNewFolder(DefaultMutableTreeNode parentNode) {
        if (parentNode == null) {
            parentNode = rootNode;
        }
        Object objFolder = parentNode.getUserObject();
        if (objFolder instanceof SessionInfo) {
            parentNode = (DefaultMutableTreeNode) parentNode.getParent();
        }
        SessionFolder folder = new SessionFolder();
        folder.setId(getNewUuid(rootNode));
        folder.setName(App.getCONTEXT().getBundle().getString("new_folder"));
        DefaultMutableTreeNode childNode1 = new DefaultMutableTreeNode(folder);
        treeModel.insertNodeInto(childNode1, parentNode, parentNode.getChildCount());
        tree.scrollPathToVisible(new TreePath(childNode1.getPath()));
        TreePath path2 = new TreePath(childNode1.getPath());
        pendingEditOnSelect = true;
        tree.clearSelection();
        tree.setSelectionPath(path2);
        setEditMode(true);
    }

    private void createNewHost(DefaultMutableTreeNode parentNode) {
        if (parentNode == null) {
            parentNode = rootNode;
        }
        Object obj = parentNode.getUserObject();
        if (obj instanceof SessionInfo) {
            parentNode = (DefaultMutableTreeNode) parentNode.getParent();
        }

        DefaultMutableTreeNode childNode = getNode(parentNode, rootNode, treeModel);
        tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        TreePath path = new TreePath(childNode.getPath());
        pendingEditOnSelect = true;
        tree.clearSelection();
        tree.setSelectionPath(path);
        setEditMode(true);
    }

    private void connectClicked() {
        save();
        this.info = (SessionInfo) selectedInfo;
        if (this.info.getHost() == null || this.info.getHost().isEmpty()) {
            JOptionPane.showMessageDialog(this, App.getCONTEXT().getBundle().getString("no_hostname"));
            this.info = null;
            log.debug("Returned");
        } else {
            log.debug("Returned disposing");
            dispose();
        }
    }

    public SessionInfo newSession() {
        setLocationRelativeTo(App.getAppWindow());
        setVisible(true);
        return this.info;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        log.debug("value changed");
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        if (tree.getRowCount() == 0) {
            lblName.setVisible(false);
            txtName.setVisible(false);
            sessionInfoPanel.setVisible(false);
            btnConnect.setVisible(false);
        }
        // Nothing is selected
        if (node == null) {
            return;
        }

        boolean shouldEdit = pendingEditOnSelect || editMode;
        pendingEditOnSelect = false;
        setEditMode(shouldEdit);

        Object nodeInfo = node.getUserObject();
        if (nodeInfo instanceof SessionInfo) {
            sessionInfoPanel.setVisible(true);
            SessionInfo sessionInfo = (SessionInfo) nodeInfo;
            sessionInfoPanel.setSessionInfo(sessionInfo);
            selectedInfo = sessionInfo;
            txtName.setVisible(true);
            lblName.setVisible(true);
            txtName.setText(selectedInfo.getName());
            btnConnect.setVisible(true);
        } else if (nodeInfo instanceof NamedItem) {
            selectedInfo = (NamedItem) nodeInfo;
            lblName.setVisible(true);
            txtName.setVisible(true);
            txtName.setText(selectedInfo.getName());
            sessionInfoPanel.setVisible(false);
            btnConnect.setVisible(false);
        }

        revalidate();
        repaint();
    }

    private void save() {
        String id = null;
        TreePath path = tree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            NamedItem item = (NamedItem) node.getUserObject();
            id = item.getId();
            if (id == null || id.isEmpty()) {
                id = getNewUuid(rootNode);
            }
        }
        removeInvalidSessionNodes(rootNode);
        SessionStore.save(SessionStore.convertModelFromTree(rootNode), id);
    }

    private void removeInvalidSessionNodes(DefaultMutableTreeNode node) {
        for (int i = node.getChildCount() - 1; i >= 0; i--) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            Object userObj = child.getUserObject();
            if (userObj instanceof SessionInfo) {
                SessionInfo session = (SessionInfo) userObj;
                String host = session.getHost();
                if (host == null || host.trim().isEmpty()) {
                    treeModel.removeNodeFromParent(child);
                }
            } else {
                removeInvalidSessionNodes(child);
            }
        }
    }

    @Override
    public void treeNodesChanged(TreeModelEvent e) {
        log.debug("treeNodesChanged");
    }

    @Override
    public void treeNodesInserted(TreeModelEvent e) {
        log.debug("treeNodesInserted");
    }

    @Override
    public void treeNodesRemoved(TreeModelEvent e) {
        log.debug("treeNodesRemoved");
    }

    @Override
    public void treeStructureChanged(TreeModelEvent e) {
        log.debug("treeStructureChanged");
    }

    private void toggleEditMode() {
        setEditMode(!editMode);
    }

    private void setEditMode(boolean enable) {
        this.editMode = enable;
        if (btnEdit != null) {
            btnEdit.setText(App.getCONTEXT().getBundle().getString(enable ? "save" : "edit"));
        }
        if (txtName != null) {
            // Keep field enabled; only toggle editability to keep colors consistent with Host in view mode.
            txtName.setEnabled(true);
            txtName.setEditable(enable);
            txtName.setFocusable(enable);
            if (enable) {
                txtName.setBackground(null);
                txtName.setForeground(null);
            } else {
                Color readOnlyBg = App.getCONTEXT().getSkin().getReadOnlyFieldBackground();
                Color readOnlyFg = App.getCONTEXT().getSkin().getReadOnlyFieldForeground();
                txtName.setBackground(readOnlyBg);
                txtName.setForeground(readOnlyFg);
                txtName.setCaretColor(readOnlyFg);
            }
        }
        if (sessionInfoPanel != null) {
            sessionInfoPanel.setEditable(enable);
        }
    }

    private void normalizeButtonSize() {
        int width = Math.max(btnConnect.getPreferredSize().width, btnCancel.getPreferredSize().width);
        width = Math.max(width, btnEdit.getPreferredSize().width);
        btnConnect.setPreferredSize(scale(new Dimension(width, btnConnect.getPreferredSize().height)));
        btnCancel.setPreferredSize(scale(new Dimension(width, btnCancel.getPreferredSize().height)));
        btnEdit.setPreferredSize(scale(new Dimension(width, btnEdit.getPreferredSize().height)));
    }
}
