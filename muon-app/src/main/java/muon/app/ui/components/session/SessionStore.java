package muon.app.ui.components.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import muon.app.PasswordStore;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;

import static util.Constants.SESSION_DB_FILE;
import static util.Constants.configDir;

public class SessionStore {

    public static synchronized SavedSessionTree load() {
        File file = Paths.get(configDir, SESSION_DB_FILE).toFile();
        return load(file);
    }

    public static synchronized SavedSessionTree load(File file) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            SavedSessionTree savedSessionTree = objectMapper.readValue(file, new TypeReference<SavedSessionTree>() {
            });
            try {
                System.out.println("Loading passwords...");
                PasswordStore.getSharedInstance().populatePassword(savedSessionTree);
                System.out.println("Loading passwords... done");
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return savedSessionTree;
        } catch (IOException e) {
            e.printStackTrace();
            SessionFolder rootFolder = new SessionFolder();
            rootFolder.setName("My sites");
            SavedSessionTree tree = new SavedSessionTree();
            tree.setFolder(rootFolder);
            return tree;
        }
    }

    public static synchronized void save(SessionFolder folder, String lastSelectionPath) {
        File file = Paths.get(configDir, SESSION_DB_FILE).toFile();
        save(folder, lastSelectionPath, file);
    }

    public static synchronized void save(SessionFolder folder, String lastSelectionPath, File file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SavedSessionTree tree = new SavedSessionTree();
            tree.setFolder(folder);
            tree.setLastSelection(lastSelectionPath);
            objectMapper.writeValue(file, tree);
            PasswordStore.getSharedInstance().savePasswords(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized SessionFolder convertModelFromTree(DefaultMutableTreeNode node) {
        SessionFolder folder = new SessionFolder();
        folder.setName(node.getUserObject() + "");
        folder.setId(((NamedItem) node.getUserObject()).getId());
        Enumeration<TreeNode> childrens = node.children();
        while (childrens.hasMoreElements()) {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) childrens.nextElement();
            if (c.getUserObject() instanceof SessionInfo) {
                folder.getItems().add((SessionInfo) c.getUserObject());
            } else {
                folder.getFolders().add(convertModelFromTree(c));
            }
        }
        return folder;
    }

    public static synchronized DefaultMutableTreeNode getNode(SessionFolder folder) {
        NamedItem item = new NamedItem();
        item.setName(folder.getName());
        item.setId(folder.getId());
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(item);
        for (SessionInfo info : folder.getItems()) {
            DefaultMutableTreeNode c = new DefaultMutableTreeNode(info);
            c.setAllowsChildren(false);
            node.add(c);
        }

        for (SessionFolder folderItem : folder.getFolders()) {
            node.add(getNode(folderItem));
        }
        return node;
    }

    public static synchronized void updateFavourites(String id, List<String> localFolders, List<String> remoteFolders) {
        SavedSessionTree tree = load();
        SessionFolder folder = tree.getFolder();

        updateFavourites(folder, id, localFolders, remoteFolders);
        save(folder, tree.getLastSelection());
    }

    private static boolean updateFavourites(SessionFolder folder, String id, List<String> localFolders,
                                            List<String> remoteFolders) {
        for (SessionInfo info : folder.getItems()) {
            if (info.id.equals(id)) {
                if (remoteFolders != null) {
                    System.out.println("Remote folders saving: " + remoteFolders);
                    info.setFavouriteRemoteFolders(remoteFolders);
                }
                if (localFolders != null) {
                    System.out.println("Local folders saving: " + localFolders);
                    info.setFavouriteLocalFolders(localFolders);
                }
                return true;
            }
        }
        for (SessionFolder childFolder : folder.getFolders()) {
            if (updateFavourites(childFolder, id, localFolders, remoteFolders)) {
                return true;
            }
        }
        return false;
    }
}
