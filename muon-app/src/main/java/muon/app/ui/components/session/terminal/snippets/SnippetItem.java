package muon.app.ui.components.session.terminal.snippets;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * @author subhro
 */
@Setter
@Getter
public class SnippetItem {

    private String name;
    private String command;
    private String id;

    
    public SnippetItem() {
        
    }

    public SnippetItem(String name, String command) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.command = command;
    }

}
