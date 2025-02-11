package muon.app.ui.components.session;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Setter
@Getter
public class SessionFolder extends NamedItem {

    private List<SessionFolder> folders = new ArrayList<>();

    private List<SessionInfo> items = new ArrayList<>();

    @Override
    public String toString() {
        return name;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    public SessionFolder copy() {
        SessionFolder folder = new SessionFolder();
        folder.setId(UUID.randomUUID().toString());
        folder.setFolders(this.folders);
        folder.setItems(this.items);
        folder.setName("Copy of " + name);
        return folder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionFolder that = (SessionFolder) o;
        return Objects.equals(folders, that.folders) && Objects.equals(items, that.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folders, items);
    }
}

