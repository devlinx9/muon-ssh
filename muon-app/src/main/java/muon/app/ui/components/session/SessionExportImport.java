package muon.app.ui.components.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import muon.app.App;
import muon.app.ui.components.session.importer.SSHConfigImporter;
import util.Constants;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static muon.app.App.bundle;
import static muon.app.ui.components.session.SessionStore.load;
import static muon.app.ui.components.session.SessionStore.save;
import static util.Constants.SESSION_DB_FILE;
import static util.Constants.configDir;

public class SessionExportImport {
    public static synchronized void exportSessions() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showSaveDialog(App.getAppWindow()) == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
                for (File f : new File(configDir).listFiles()) {
                    ZipEntry ent = new ZipEntry(f.getName());
                    out.putNextEntry(ent);
                    out.write(Files.readAllBytes(f.toPath()));
                    out.closeEntry();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized boolean importSessions() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(App.getAppWindow()) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File f = jfc.getSelectedFile();
        if (JOptionPane.showConfirmDialog(App.getAppWindow(),
                "Existing data will be replaced.\nContinue?") != JOptionPane.YES_OPTION) {
            return false;
        }
        byte[] b = new byte[8192];
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(f))) {
            ZipEntry ent = in.getNextEntry();
            File file = new File(configDir, ent.getName());
            try (OutputStream out = new FileOutputStream(file)) {
                while (true) {
                    int x = in.read(b);
                    if (x == -1)
                        break;
                    out.write(b, 0, x);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static synchronized boolean importSessionsSSHConfig() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(App.getAppWindow()) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File f = jfc.getSelectedFile();

        DefaultComboBoxModel<Constants.ConflictAction> conflictOptionsCmb = new DefaultComboBoxModel<>(Constants.ConflictAction.values());
        conflictOptionsCmb.removeAllElements();
        for (Constants.ConflictAction conflictActionCmb : Constants.ConflictAction.values()) {
            if (conflictActionCmb.getKey() < 3) {
                conflictOptionsCmb.addElement(conflictActionCmb);
            }
        }
        JComboBox<Constants.ConflictAction> cmbOptionsExistingInfo = new JComboBox<>(conflictOptionsCmb);

        if (JOptionPane.showOptionDialog(App.getAppWindow(), new Object[]{"In repeated sessions do:", cmbOptionsExistingInfo}, bundle.getString("import_sessions"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                null) != JOptionPane.OK_OPTION) {
            return false;
        }
        try {
            int imported = 0;
            int skiped = 0;
            int overwrited = 0;

            List<SessionInfo> sessions = SSHConfigImporter.getSessionFromFile(f);

            SavedSessionTree tree = load();
            SessionFolder folder = tree.getFolder();

            List<SessionFolder> folders = folder.getFolders();
            int total = sessions.size();
            SessionFolder sessionFolder;
            for (SessionInfo session : sessions) {
                session.setId(UUID.randomUUID().toString());
                sessionFolder = new SessionFolder();
                sessionFolder.setId(UUID.randomUUID().toString());
                sessionFolder.setName(session.getName());
                List<SessionInfo> item = new ArrayList<>();
                item.add(session);
                sessionFolder.setItems(item);
                if (folders.contains(sessionFolder)) {
                    if (cmbOptionsExistingInfo.getSelectedItem() == Constants.ConflictAction.SKIP) {
                        continue;
                    }
                    if (cmbOptionsExistingInfo.getSelectedItem() == Constants.ConflictAction.AUTORENAME) {
                        sessionFolder.setName("Copy of " + sessionFolder.getName());
                        folders.add(sessionFolder);
                    } else if (cmbOptionsExistingInfo.getSelectedItem() == Constants.ConflictAction.OVERWRITE) {
                        folders.set(folders.indexOf(sessionFolder), sessionFolder);
                    }
                    imported++;
                    continue;
                }
                folders.add(sessionFolder);
                imported++;
            }

            folder.setFolders(folders);
            save(folder, tree.getLastSelection());

            JOptionPane.showMessageDialog(App.getAppWindow(),
                    "Total=" + total +
                            "\nImported=" + imported +
                            "\nSkipped=" + skiped +
                            "\noverwrited=" + overwrited, "Session information", JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static synchronized void importOnFirstRun() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            SavedSessionTree savedSessionTree = objectMapper.readValue(new File(configDir + File.separator + SESSION_DB_FILE),
                    new TypeReference<>() {
                    });
            save(savedSessionTree.getFolder(), savedSessionTree.getLastSelection(),
                    new File(configDir, SESSION_DB_FILE));
//            Files.copy(Paths.get(configDir, SNIPPETS_FILE),
//                    Paths.get(configDir, SNIPPETS_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
