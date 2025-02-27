package muon.app.util.enums;

import lombok.Getter;

import static muon.app.App.bundle;

@Getter
public enum ConflictAction {


    OVERWRITE(0, bundle.getString("overwrite")), AUTORENAME(1, bundle.getString("autorename")), SKIP(2, bundle.getString("skip")), PROMPT(3, bundle.getString("prompt")), CANCEL(4, bundle.getString("cancel"));
    private int key;
    private String value;

    ConflictAction(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static void update() {
        OVERWRITE.setValue(bundle.getString("overwrite"));
        AUTORENAME.setValue(bundle.getString("autorename"));
        SKIP.setValue(bundle.getString("skip"));
        PROMPT.setValue(bundle.getString("prompt"));
        CANCEL.setValue(bundle.getString("cancel"));
    }

    public void setKey(int key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}