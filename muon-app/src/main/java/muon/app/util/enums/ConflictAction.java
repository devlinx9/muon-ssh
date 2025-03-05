package muon.app.util.enums;

import lombok.Getter;
import muon.app.App;


@Getter
public enum ConflictAction {


    OVERWRITE(0, App.getContext().getBundle().getString("overwrite")), AUTORENAME(1, App.getContext().getBundle().getString("autorename")), SKIP(2, App.getContext().getBundle().getString("skip")), PROMPT(3, App.getContext().getBundle().getString("prompt")), CANCEL(4, App.getContext().getBundle().getString("cancel"));
    private int key;
    private String value;

    ConflictAction(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static void update() {
        OVERWRITE.setValue(App.getContext().getBundle().getString("overwrite"));
        AUTORENAME.setValue(App.getContext().getBundle().getString("autorename"));
        SKIP.setValue(App.getContext().getBundle().getString("skip"));
        PROMPT.setValue(App.getContext().getBundle().getString("prompt"));
        CANCEL.setValue(App.getContext().getBundle().getString("cancel"));
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