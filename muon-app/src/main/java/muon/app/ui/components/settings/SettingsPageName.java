package muon.app.ui.components.settings;

public enum SettingsPageName {

    GENERAL("General", 0),
    TERMINAL("Terminal", 1),
    EDITOR("Editor", 2),
    DISPLAY("Display", 3),
    SECURITY("Security", 4);

    public final String name;
    public final int index;

    SettingsPageName(String name, int index) {
        this.name = name;
        this.index = index;
    }
}
