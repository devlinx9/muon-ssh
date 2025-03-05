/**
 *
 */
package muon.app.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.session.terminal.snippets.SnippetItem;
import muon.app.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author subhro
 */
@Slf4j
public class SnippetManager {
    private List<SnippetItem> snippetItems = new ArrayList<>();

    public synchronized void loadSnippets() {
        File file = new File(App.getContext().getConfigDir(), Constants.SNIPPETS_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (file.exists()) {
            try {
                snippetItems = objectMapper.readValue(file, new TypeReference<>() {
                });
                return;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        snippetItems = new ArrayList<>();
    }

    public synchronized void saveSnippets() {
        File file = new File(App.getContext().getConfigDir(), Constants.SNIPPETS_FILE);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, snippetItems);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public synchronized List<SnippetItem> getSnippetItems() {
        loadSnippets();
        return snippetItems;
    }

}
