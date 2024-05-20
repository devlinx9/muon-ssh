package util;

public enum Language {
    ENGLISH("en", "English"),
    CHINESE("cn", "Chinese"),
    SPANISH("es", "Español"),
    PORTUGUESE("pt", "Portuguese"),
    RUSSIAN("ru", "Русский"),
    GERMAN("de", "Deutsch"),
    FRENCH("fr", "Français");
    private final String full;
    private final String langAbbr;

    Language(String langAbbr, String full) {
        this.full = full;
        this.langAbbr = langAbbr;
    }

    public String getFull() {
        return full;
    }

    public String getLangAbbr() {
        return langAbbr;
    }

    @Override
    public String toString() {
        return full;
    }
}
