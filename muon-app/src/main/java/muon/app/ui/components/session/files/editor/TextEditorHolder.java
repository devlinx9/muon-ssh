package muon.app.ui.components.session.files.editor;

import lombok.Getter;
import muon.app.App;
import muon.app.ui.components.session.Page;
import muon.app.util.FontAwesomeContants;

import java.awt.*;

@Getter
public class TextEditorHolder extends Page {
    private final TextEditor editor;

    public TextEditorHolder(TextEditor editor) {
        super(new BorderLayout());
        this.editor = editor;
        add(this.editor);
    }

    @Override
    public void onLoad() {

    }

    @Override
    public String getIcon() {
        return FontAwesomeContants.FA_PENCIL_SQUARE_O;
    }

    @Override
    public String getText() {
        return App.getCONTEXT().getBundle().getString("editor");
    }
}
