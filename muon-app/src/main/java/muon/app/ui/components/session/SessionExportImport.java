package muon.app.ui.components.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.session.importer.SSHConfigImporter;
import util.Constants;
import util.enums.ConflictAction;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static muon.app.App.bundle;
import static muon.app.ui.components.session.SessionStore.load;
import static muon.app.ui.components.session.SessionStore.save;

@Slf4j
public class SessionExportImport {
    public static synchronized void exportSessions() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showSaveDialog(App.getAppWindow()) == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
                for (File f : Objects.requireNonNull(new File(App.CONFIG_DIR).listFiles())) {
                    ZipEntry ent = new ZipEntry(f.getName());
                    out.putNextEntry(ent);
                    out.write(Files.readAllBytes(f.toPath()));
                    out.closeEntry();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
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
                                          bundle.getString("replace_data_warning")) != JOptionPane.YES_OPTION) {
            return false;
        }
        byte[] b = new byte[8192];
        try (ZipInputStream in = new ZipInputStream(new FileInputStream(f))) {
            ZipEntry ent = in.getNextEntry();
            File file = new File(App.CONFIG_DIR, Objects.requireNonNull(ent).getName());
            try (OutputStream out = new FileOutputStream(file)) {
                while (true) {
                    int x = in.read(b);
                    if (x == -1) {
                        break;
                    }
                    out.write(b, 0, x);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    public static synchronized boolean importSessionsSSHConfig() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(App.getAppWindow()) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File f = jfc.getSelectedFile();

        DefaultComboBoxModel<ConflictAction> conflictOptionsCmb = new DefaultComboBoxModel<>(ConflictAction.values());
        conflictOptionsCmb.removeAllElements();
        for (ConflictAction conflictActionCmb : ConflictAction.values()) {
            if (conflictActionCmb.getKey() < 3) {
                conflictOptionsCmb.addElement(conflictActionCmb);
            }
        }
        JComboBox<ConflictAction> cmbOptionsExistingInfo = new JComboBox<>(conflictOptionsCmb);

        if (JOptionPane.showOptionDialog(App.getAppWindow(), new Object[]{bundle.getString("repeated_sessions"), cmbOptionsExistingInfo}, bundle.getString("import_sessions"),
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
                    if (cmbOptionsExistingInfo.getSelectedItem() == ConflictAction.SKIP) {
                        continue;
                    }
                    if (cmbOptionsExistingInfo.getSelectedItem() == ConflictAction.AUTORENAME) {
                        sessionFolder.setName("Copy of " + sessionFolder.getName());
                        folders.add(sessionFolder);
                    } else if (cmbOptionsExistingInfo.getSelectedItem() == ConflictAction.OVERWRITE) {
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

            JOptionPane.showMessageDialog(App.getAppWindow(), String.format(bundle.getString("imported_totals"), total, imported, skiped, overwrited)
                    , bundle.getString("session_info"), JOptionPane.INFORMATION_MESSAGE);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    public static synchronized void importOnFirstRun() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            SavedSessionTree savedSessionTree = objectMapper.readValue(new File(System.getProperty("user.home")
                                                                                + File.separator + "muon-ssh" + File.separator + "session-store.json"),
                                                                       new TypeReference<>() {
                                                                       });
            save(savedSessionTree.getFolder(), savedSessionTree.getLastSelection(),
                 new File(App.CONFIG_DIR, Constants.SESSION_DB_FILE));
            Files.copy(Paths.get(System.getProperty("user.home"), "muon-ssh", "snippets.json"),
                       Paths.get(App.CONFIG_DIR, Constants.SNIPPETS_FILE));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
