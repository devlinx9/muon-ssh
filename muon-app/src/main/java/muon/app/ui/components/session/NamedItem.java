package muon.app.ui.components.session;

import lombok.Getter;
import lombok.Setter;

public class NamedItem {
    protected String id;

    @Setter
    @Getter
    protected String name;

    @Override
    public String toString() {
        return this.name;
    }

    public synchronized String getId() {
        return id;
    }

    public synchronized void setId(String id) {
        this.id = id;
    }
}
