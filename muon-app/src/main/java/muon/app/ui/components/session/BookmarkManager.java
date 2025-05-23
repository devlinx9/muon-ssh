package muon.app.ui.components.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.util.Constants;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public final class BookmarkManager {
    public static synchronized Map<String, List<String>> getAll() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File bookmarkFile = new File(App.getCONTEXT().getConfigDir(), Constants.BOOKMARKS_FILE);
        if (bookmarkFile.exists()) {
            try {
                Map<String, List<String>> bookmarkMap = objectMapper.readValue(bookmarkFile,
                                                                               new TypeReference<>() {
                                                                               });
                return Collections.synchronizedMap(bookmarkMap);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return Collections.synchronizedMap(new HashMap<>());
    }

    public static synchronized void save(Map<String, List<String>> bookmarks) {
        ObjectMapper objectMapper = new ObjectMapper();
        File bookmarkFile = new File(App.getCONTEXT().getConfigDir(), Constants.BOOKMARKS_FILE);
        try {
            objectMapper.writeValue(bookmarkFile, bookmarks);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    public static synchronized void addEntry(String id, String path) {
        if (id == null) {
            id = "";
        }
        Map<String, List<String>> bookmarkMap = BookmarkManager.getAll();
        List<String> bookmarks = bookmarkMap.get(id);
        if (bookmarks == null) {
            bookmarks = new ArrayList<>();
        }
        bookmarks.add(path);
        bookmarkMap.put(id, bookmarks);
        save(bookmarkMap);
    }

    public static synchronized void addEntry(String id, List<String> path) {
        if (id == null) {
            id = "";
        }
        Map<String, List<String>> bookmarkMap = BookmarkManager.getAll();
        List<String> bookmarks = bookmarkMap.get(id);
        if (bookmarks == null) {
            bookmarks = new ArrayList<>();
        }
        bookmarks.addAll(path);
        bookmarkMap.put(id, bookmarks);
        save(bookmarkMap);
    }

    public static synchronized List<String> getBookmarks(String id) {
        if (id == null) {
            id = "";
        }
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File bookmarkFile = new File(App.getCONTEXT().getConfigDir(), Constants.BOOKMARKS_FILE);
        if (bookmarkFile.exists()) {
            try {
                Map<String, List<String>> bookmarkMap = objectMapper.readValue(bookmarkFile,
                        new TypeReference<>() {
                        });
                List<String> bookmarks = bookmarkMap.get(id);
                if (bookmarks != null) {
                    return new ArrayList<>(bookmarks);
                }

            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return new ArrayList<>();
    }
}
