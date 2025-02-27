package muon.app.ui.components.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.session.importer.SSHConfigImporter;
import muon.app.util.Constants;
import muon.app.util.enums.ConflictAction;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
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

    protected SessionExportImport() {

    }

    public static final String ERROR = "error";

    public static synchronized void exportSessions() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showSaveDialog(App.getAppWindow()) == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();

            // Ensure the file has a .zip extension
            if (!file.getName().toLowerCase().endsWith(".zip")) {
                file = new File(file.getAbsolutePath() + ".zip");
            }

            try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(file))) {
                for (File f : Objects.requireNonNull(new File(App.CONFIG_DIR).listFiles())) {
                    ZipEntry ent = new ZipEntry(f.getName());
                    out.putNextEntry(ent);
                    out.write(Files.readAllBytes(f.toPath()));
                    out.closeEntry();
                }

                JOptionPane.showMessageDialog(App.getAppWindow(),
                                              bundle.getString("export_sessions_successful"), bundle.getString("success"), JOptionPane.INFORMATION_MESSAGE);

            } catch (IOException e) {
                log.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(App.getAppWindow(),
                                              String.format(bundle.getString("error_occurred"), e.getMessage()), bundle.getString(ERROR), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static synchronized boolean importMuonSessions() {
        JFileChooser jfc = new JFileChooser();

        // Set file filter to allow only .zip files
        jfc.setFileFilter(new FileNameExtensionFilter("ZIP Files (*.zip)", "zip"));

        if (jfc.showOpenDialog(App.getAppWindow()) != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File f = jfc.getSelectedFile();

        if (!f.getName().toLowerCase().endsWith(".zip")) {
            JOptionPane.showMessageDialog(App.getAppWindow(),
                                          bundle.getString("invalid_file_type"),
                                          bundle.getString(ERROR), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // Check if the file exists and is not empty
        if (!f.exists() || f.length() == 0) {
            JOptionPane.showMessageDialog(App.getAppWindow(),
                                          bundle.getString("invalid_zip_file"),
                                          bundle.getString(ERROR), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (JOptionPane.showConfirmDialog(App.getAppWindow(),
                                          bundle.getString("replace_data_warning")) != JOptionPane.YES_OPTION) {
            return false;
        }

        byte[] buffer = new byte[8192];

        try (ZipInputStream in = new ZipInputStream(new FileInputStream(f))) {
            ZipEntry ent;

            while ((ent = in.getNextEntry()) != null) { // Read all entries in the ZIP file
                File file = new File(App.CONFIG_DIR, ent.getName());

                // Prevent directory traversal attack
                if (!file.getCanonicalPath().startsWith(new File(App.CONFIG_DIR).getCanonicalPath())) {
                    log.error("ZIP entry is outside target directory: {}", ent.getName());
                    continue;
                }

                try (OutputStream out = new FileOutputStream(file)) {
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }
                in.closeEntry();
            }

        } catch (IOException e) {
            log.error("Error processing ZIP file: {}", e.getMessage(), e);
            JOptionPane.showMessageDialog(App.getAppWindow(),
                                          bundle.getString("error_processing_zip"),
                                          bundle.getString(ERROR), JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }


    public static synchronized boolean importSessionsSSHConfig() {
        JFileChooser jfc = new JFileChooser();
        if (jfc.showOpenDialog(App.getAppWindow()) != JFileChooser.APPROVE_OPTION) {
            return false;
        }
        File f = jfc.getSelectedFile();

        JComboBox<ConflictAction> cmbOptionsExistingInfo = getUserConflictAction();

        if (JOptionPane.showOptionDialog(App.getAppWindow(), new Object[]{bundle.getString("repeated_sessions"), cmbOptionsExistingInfo}, bundle.getString("import_sessions"),
                                         JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                                         null) != JOptionPane.OK_OPTION) {
            return false;
        }
        try {
            List<SessionInfo> sessions = SSHConfigImporter.getSessionFromFile(f);
            processImportedSessions(sessions, cmbOptionsExistingInfo);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
        return true;
    }

    private static void processImportedSessions(List<SessionInfo> sessions,
                                                JComboBox<ConflictAction> cmbOptionsExistingInfo) {
        int imported = 0;
        int skiped = 0;
        int overwrited = 0;

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
                } else if (cmbOptionsExistingInfo.getSelectedItem() == ConflictAction.AUTORENAME) {
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
    }

    public static JComboBox<ConflictAction> getUserConflictAction() {
        DefaultComboBoxModel<ConflictAction> conflictOptionsCmb = new DefaultComboBoxModel<>(ConflictAction.values());
        conflictOptionsCmb.removeAllElements();
        for (ConflictAction conflictActionCmb : ConflictAction.values()) {
            if (conflictActionCmb.getKey() < 3) {
                conflictOptionsCmb.addElement(conflictActionCmb);
            }
        }
        return new JComboBox<>(conflictOptionsCmb);
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

    public static synchronized boolean importSessionsPreviousVersion() {

        JFileChooser jfc = new JFileChooser();


        if (jfc.showOpenDialog(App.getAppWindow()) != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File f = jfc.getSelectedFile();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            SavedSessionTree savedSessionTree = objectMapper.readValue(f,
                                                                       new TypeReference<>() {
                                                                       });
            save(savedSessionTree.getFolder(), savedSessionTree.getLastSelection(),
                 new File(App.CONFIG_DIR, Constants.SESSION_DB_FILE));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return false;
        }
        return true;
    }


}
