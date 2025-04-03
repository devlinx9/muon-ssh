package muon.app.ui.components.session.dialog;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.session.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;



@Slf4j
public class TreeManager {

    protected TreeManager() {
    }

    protected DefaultMutableTreeNode loadTree(SavedSessionTree stree, DefaultTreeModel treeModel, JTree tree) {
        DefaultMutableTreeNode rootNode = SessionStore.getNode(stree.getFolder());
        DefaultMutableTreeNode emptyRoot = new DefaultMutableTreeNode("Empty_Root");
        String lastSelected = stree.getLastSelection();

        if (rootNode.getUserObject().toString().equals("Empty_Root")) {
            emptyRoot = rootNode;
        } else {
            rootNode.setAllowsChildren(true);
            emptyRoot.add(rootNode);
        }
        emptyRoot.setAllowsChildren(true);
        treeModel.setRoot(emptyRoot);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        try {
            if (lastSelected != null) {
                selectNode(lastSelected, rootNode, tree);
            } else {
                DefaultMutableTreeNode n = findFirstInfoNode(rootNode);
                if (n == null) {
                    n = getNode(rootNode, rootNode, treeModel);
                    tree.scrollPathToVisible(new TreePath(n.getPath()));
                    TreePath path = new TreePath(n.getPath());
                    tree.setSelectionPath(path);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        treeModel.nodeChanged(rootNode);
        return emptyRoot;
    }

    protected boolean selectNode(String id, DefaultMutableTreeNode node, JTree tree) {
        if (id != null && id.equals((((NamedItem) node.getUserObject()).getId()))) {
            TreePath path = new TreePath(node.getPath());
            tree.setSelectionPath(path);
            tree.scrollPathToVisible(path);
            return true;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            if (selectNode(id, child, tree)) {
                return true;
            }
        }
        return false;
    }

    private DefaultMutableTreeNode findFirstInfoNode(DefaultMutableTreeNode node) {
        if (!node.getAllowsChildren()) {
            return node;
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = findFirstInfoNode((DefaultMutableTreeNode) node.getChildAt(i));
            if (child != null) {
                return child;
            }
        }
        return null;
    }

    public static String getNewUuid(DefaultMutableTreeNode node) {
        // Step 1: Collect all existing UUIDs in the tree
        Set<String> existingUuids = collectUuids(node);

        // Step 2: Generate a new UUID and ensure it's unique
        String newUuid;
        do {
            newUuid = UUID.randomUUID().toString();
        }
        while (existingUuids.contains(newUuid));

        return newUuid;
    }

    private static Set<String> collectUuids(DefaultMutableTreeNode node) {
        Set<String> uuids = new HashSet<>();

        // Add the current node's UUID
        if (node.getUserObject() instanceof NamedItem) {
            uuids.add(((NamedItem) node.getUserObject()).getId());
        }

        // Traverse all children and add their UUIDs
        Enumeration<TreeNode> children = node.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
            if (child.getUserObject() instanceof SessionInfo) {
                uuids.add(((SessionInfo) child.getUserObject()).getId());
            } else if (child.getUserObject() instanceof SessionFolder) {
                uuids.add(((SessionFolder) child.getUserObject()).getId());
            }

            // Recursively collect UUIDs from child nodes
            uuids.addAll(collectUuids(child));
        }

        return uuids;
    }

    public static DefaultMutableTreeNode getNode(DefaultMutableTreeNode parentNode, DefaultMutableTreeNode rootNode, DefaultTreeModel treeModel) {
        SessionInfo sessionInfo = new SessionInfo();
        sessionInfo.setName(App.getCONTEXT().getBundle().getString("new_site"));
        sessionInfo.setId(getNewUuid(rootNode));
        DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(sessionInfo);
        childNode.setUserObject(sessionInfo);
        childNode.setAllowsChildren(false);
        treeModel.insertNodeInto(childNode, parentNode, parentNode.getChildCount());
        return childNode;
    }

}