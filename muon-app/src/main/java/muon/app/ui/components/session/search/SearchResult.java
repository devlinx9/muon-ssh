package muon.app.ui.components.session.search;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SearchResult {
    private String name;
    private String path;
    private String type;

    public SearchResult(String name, String path, String type) {
        super();
        this.name = name;
        this.path = path;
        this.type = type;
    }

}

