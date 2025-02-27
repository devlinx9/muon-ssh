package muon.app.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.session.SavedSessionTree;
import muon.app.ui.components.session.SessionFolder;
import muon.app.ui.components.session.SessionInfo;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.UnrecoverableKeyException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static muon.app.App.bundle;

@Slf4j
public final class PasswordStore {
    private static KeyStore keyStore;
    private static PasswordStore instance;

    private final AtomicBoolean unlocked = new AtomicBoolean(false);
    private KeyStore.PasswordProtection protParam;

    private Map<String, char[]> passwordMap = new HashMap<>();

    private PasswordStore() throws KeyStoreException {
        keyStore = KeyStore.getInstance("PKCS12");
    }

    public static synchronized PasswordStore getSharedInstance() throws Exception {
        if (instance == null) {
            instance = new PasswordStore();
        }
        return instance;
    }

    public boolean isUnlocked() {
        return unlocked.get();
    }

    public synchronized void unlockStore(char[] password) throws Exception {
        protParam = new KeyStore.PasswordProtection(password, "PBEWithHmacSHA256AndAES_256", null);
        File filePasswordStore = new File(App.CONFIG_DIR, "passwords.pfx");
        if (!filePasswordStore.exists()) {
            keyStore.load(null, protParam.getPassword());
            unlocked.set(true);
            return;
        }
        try (InputStream in = new FileInputStream(filePasswordStore)) {
            keyStore.load(in, protParam.getPassword());
            loadPasswords();
            unlocked.set(true);
        }
    }

    private synchronized void loadPasswords() throws Exception {

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBE");
        KeyStore.SecretKeyEntry ske = (KeyStore.SecretKeyEntry) keyStore.getEntry("passwords", protParam);

        PBEKeySpec keySpec = (PBEKeySpec) factory.getKeySpec(ske.getSecretKey(), PBEKeySpec.class);

        char[] chars = keySpec.getPassword();

        this.passwordMap = deserializePasswordMap(chars);
    }

    private Map<String, char[]> deserializePasswordMap(char[] chars) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper.readValue(new CharArrayReader(chars), new TypeReference<>() {
        });
    }

    private char[] serializePasswordMap(Map<String, char[]> map) throws Exception {
        CharArrayWriter writer = new CharArrayWriter();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(writer, map);
        return writer.toCharArray();
    }

    public synchronized char[] getSavedPassword(String alias) {
        return this.passwordMap.get(alias);
    }

    public synchronized void savePassword(String alias, char[] password) {
        this.passwordMap.put(alias, password);
    }

    public synchronized void saveKeyStore() throws Exception {

        SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBE");
        SecretKey generatedSecret = secretKeyFactory
                .generateSecret(new PBEKeySpec(serializePasswordMap(this.passwordMap)));
        keyStore.setEntry("passwords", new SecretKeyEntry(generatedSecret), protParam);

        log.info("Password protection: {}", protParam.getProtectionAlgorithm());

        try (OutputStream out = new FileOutputStream(new File(App.CONFIG_DIR, "passwords.pfx"))) {
            keyStore.store(out, protParam.getPassword());
        }
    }

    private boolean unlockStore() {
        if (this.isUnlocked()) {
            return true;
        }

        if (App.getGlobalSettings().isUsingMasterPassword()) {
            return unlockUsingMasterPassword();
        }

        try {
            unlockStore(new char[0]);
            return true;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return false;
        }

    }

    public synchronized void populatePassword(SavedSessionTree savedSessionTree) {
        if (!unlockStore()) {
            throw new IllegalStateException("Unable to unlock password store");
        }
        if (savedSessionTree != null) {
            populatePassword(savedSessionTree.getFolder());
        }

    }

    private void populatePassword(SessionFolder folder) {
        for (SessionInfo info : folder.getItems()) {
            try {
                char[] password = this.getSavedPassword(info.getId());
                if (password != null) {
                    info.setPassword(new String(password));
                } else {
                    log.debug("The info {} has no password", info.getHost());
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        for (SessionFolder f : folder.getFolders()) {
            populatePassword(f);
        }
    }

    public void savePasswords(SavedSessionTree savedSessionTree) {
        if (!this.isUnlocked()) {
            if (App.getGlobalSettings().isUsingMasterPassword()) {
                if (!unlockUsingMasterPassword()) {
                    return;
                }
            } else {
                try {
                    unlockStore(new char[0]);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return;
                }
            }
        }
        savePassword(savedSessionTree.getFolder());
        try {
            saveKeyStore();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void savePassword(SessionFolder folder) {
        for (SessionInfo info : folder.getItems()) {
            String password = info.getPassword();
            if (password != null && !password.isEmpty()) {
                try {
                    savePassword(info.getId(), password.toCharArray());
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        for (SessionFolder f : folder.getFolders()) {
            savePassword(f);
        }
    }

    public boolean unlockUsingMasterPassword() {
        int tries = 0;
        while (tries < 3) {
            try {
                JPasswordField txtPass = new JPasswordField(30);
                if (JOptionPane.showOptionDialog(App.getAppWindow(), new Object[]{App.bundle.getString("master_password"), txtPass},
                                                 App.bundle.getString("master_password"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null,
                                                 null) == JOptionPane.OK_OPTION) {
                    this.unlockStore(txtPass.getPassword());
                    return true;
                } else {
                    return false;
                }
            } catch (IOException e) {
                if (e.getCause() instanceof UnrecoverableKeyException) {
                    JOptionPane.showMessageDialog(App.getAppWindow(),
                                                  App.bundle.getString("incorrect_password"),bundle.getString("error"), JOptionPane.ERROR_MESSAGE);

                }
                tries++;

            } catch (Exception e) {
                log.error(e.getMessage(), e);
                JOptionPane.showMessageDialog(App.getAppWindow(),
                                              App.bundle.getString("error_loading_password"),bundle.getString("error"), JOptionPane.ERROR_MESSAGE);
                tries++;
            }
        }
        return false;
    }

    public boolean changeStorePassword(char[] newPassword) throws Exception {
        if (!unlockStore()) {
            return false;
        }

        Enumeration<String> aliases = keyStore.aliases();
        Map<String, char[]> passMap = new HashMap<>();

        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            passMap.put(alias, getSavedPassword(alias));
            keyStore.deleteEntry(alias);
        }

        protParam = new KeyStore.PasswordProtection(newPassword, "PBEWithHmacSHA256AndAES_256", null);
        for (Map.Entry<String, char[]> entry : passMap.entrySet()) {
            savePassword(entry.getKey(), entry.getValue());
        }
        saveKeyStore();
        return true;
    }
}
