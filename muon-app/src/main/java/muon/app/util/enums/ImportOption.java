package muon.app.util.enums;

public enum ImportOption {
    PUTTY("Putty"),
    WINSCP("WinSCP"),
    MUON_SESSION_STORE("Muon session store"),
    SSH_CONFIG_FILE("SSH config file"),
    PREVIOUS_MUON_VERSIONS("Previous muon versions (Snowflake)");

    private final String displayName;

    ImportOption(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}