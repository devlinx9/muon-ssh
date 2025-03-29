package muon.app.util.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;
import lombok.Getter;
import muon.app.App;


@Getter
public enum TransferMode {

    @JsonEnumDefaultValue NORMAL(0, App.getCONTEXT().getBundle().getString("transfer_normally")), BACKGROUND(1, App.getCONTEXT().getBundle().getString("transfer_background"));

    private int key;
    private String value;

    TransferMode(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public static void update() {
        NORMAL.setValue(App.getCONTEXT().getBundle().getString("transfer_normally"));
        BACKGROUND.setValue(App.getCONTEXT().getBundle().getString("transfer_background"));
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