package muon.app.ui.components.session.terminal.snippets;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
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

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SnippetItem)) return false;
        SnippetItem that = (SnippetItem) o;
        return Objects.equals(name, that.name) && Objects.equals(command, that.command) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, command, id);
    }
}
