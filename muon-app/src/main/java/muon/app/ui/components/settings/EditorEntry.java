package muon.app.ui.components.settings;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class EditorEntry {
    private String name;
    private String path;
    
    public EditorEntry(String name, String path) {
        super();
        this.name = name;
        this.path = path;
    }

}
