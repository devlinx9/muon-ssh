package muon.app.ui.components.session.files;

import muon.app.ui.components.common.SkinnedTextField;

import javax.swing.*;
import java.awt.*;

public class AddressBarComboBoxEditor extends SkinnedTextField implements ComboBoxEditor {

    public AddressBarComboBoxEditor() {
        super.putClientProperty("paintNoBorder", "True");
    }

    @Override
    public Component getEditorComponent() {
        return this;
    }

    @Override
    public Object getItem() {
        return this.getText();
    }

    @Override
    public void setItem(Object anObject) {
        if (anObject != null) {
            this.setText(anObject.toString());
        }
    }

}
