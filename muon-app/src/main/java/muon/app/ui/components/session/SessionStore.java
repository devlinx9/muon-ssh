package muon.app.ui.components.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.common.PasswordStore;
import muon.app.ui.components.session.dialog.TreeManager;
import muon.app.util.Constants;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.List;



@Slf4j
public class SessionStore {

    protected SessionStore() {

    }

    public static synchronized SavedSessionTree load() {
        File file = Paths.get(App.getContext().getConfigDir().getAbsolutePath(), Constants.SESSION_DB_FILE).toFile();
        return load(file);
    }

    public static synchronized SavedSessionTree load(File file) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS, true);

        try {
            SavedSessionTree savedSessionTree = objectMapper.readValue(preprocessJson(file), new TypeReference<>() {
            });
            try {
                log.debug("Loading passwords...");
                PasswordStore.getSharedInstance().populatePassword(savedSessionTree);
                log.debug("Loading passwords... done");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(App.getAppWindow(),
                                              String.format(App.getContext().getBundle().getString("error_occurred"), e.getMessage()), App.getContext().getBundle().getString("error"), JOptionPane.ERROR_MESSAGE);
                return null;
            }
            return savedSessionTree;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            SessionFolder rootFolder = new SessionFolder();
            rootFolder.setName("My sites");
            SavedSessionTree tree = new SavedSessionTree();
            tree.setFolder(rootFolder);
            return tree;
        }
    }

    public static synchronized void save(SessionFolder folder, String lastSelectionPath) {
        File file = new File(App.getContext().getConfigDir(), Constants.SESSION_DB_FILE);
        save(folder, lastSelectionPath, file);
    }

    public static synchronized void save(SessionFolder folder, String lastSelectionPath, File file) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            SavedSessionTree tree = new SavedSessionTree();
            tree.setFolder(folder);
            tree.setLastSelection(lastSelectionPath);
            objectMapper.writeValue(file, tree);
            try {
                PasswordStore.getSharedInstance().savePasswords(tree);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static synchronized SessionFolder convertModelFromTree(DefaultMutableTreeNode node) {
        SessionFolder folder = new SessionFolder();
        folder.setName(node.getUserObject() + "");
        String folderId = node.getUserObject().toString();
        if (!folderId.equals("Empty_Root")) {
            folderId = ((NamedItem) node.getUserObject()).getId();
        }
        folder.setId(folderId == null ? TreeManager.getNewUuid(node) : folderId);
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
                    log.info("Remote folders saving: {}", remoteFolders);
                    info.setFavouriteRemoteFolders(remoteFolders);
                }
                if (localFolders != null) {
                    log.info("Local folders saving: {}", localFolders);
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

    public static String preprocessJson(File file) throws IOException {
        String content = new String(Files.readAllBytes(file.toPath()));
        content = content.replace("\"TcpForwarding\"", "\"TCP_FORWARDING\"");
        content = content.replace("\"PortForwarding\"", "\"PORT_FORWARDING\"");
        content = content.replace("\"DragDrop\"", "\"DRAG_DROP\"");
        content = content.replace("\"DirLink\"", "\"DIR_LINK\"");
        content = content.replace("\"FileLink\"", "\"FILE_LINK\"");
        content = content.replace("\"KeyStore\"", "\"KEY_STORE\"");
        return content;
    }
}
