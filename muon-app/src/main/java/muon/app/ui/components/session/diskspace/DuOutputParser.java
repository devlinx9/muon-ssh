package muon.app.ui.components.session.diskspace;

import lombok.extern.slf4j.Slf4j;
import muon.app.util.PathUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public final class DuOutputParser {
    private static final Pattern DU_PATTERN = Pattern
            .compile("([\\d]+)\\s+(.+)");
    private final DiskUsageEntry root;

    public DuOutputParser(String folder) {
        root = new DiskUsageEntry(PathUtils.getFileName(folder), "", 0, 0,
                true);
    }

    public DiskUsageEntry parseList(List<String> lines, int prefixLen) {
        for (String line : lines) {
            Matcher matcher = DU_PATTERN.matcher(line);
            if (matcher.find()) {
                try {
                    long size = Long.parseLong(matcher.group(1)) * 512;
                    String path = matcher.group(2).substring(prefixLen);
                    addEntry(size, path);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        return root;
    }

    private void addEntry(long size, String path) {
        if (path.isEmpty() || path.equals("/")) {
            root.setSize(size);
            return;
        }
        String[] arr = path.split("\\/");
        DiskUsageEntry node = root;
        for (int i = 1; i < arr.length - 1; i++) {
            String s = arr[i];
            boolean found = false;
            for (int j = 0; j < node.getChildren().size(); j++) {
                DiskUsageEntry entry = node.getChildren().get(j);
                if (entry.getName().equals(s)) {
                    node = entry;
                    found = true;
                    break;
                }
            }
            if (!found) {
                DiskUsageEntry entry = new DiskUsageEntry(s,
                        node.getPath() + "/" + s, -1, 0, true);
                entry.setDirectory(true);
                node.getChildren().add(entry);
                node = entry;
            }
        }
        String name = arr[arr.length - 1];
        DiskUsageEntry entry = null;
        for (DiskUsageEntry ent : node.getChildren()) {
            if (ent.getName().equals(name)) {
                entry = ent;
                break;
            }
        }
        if (entry == null) {
            entry = new DiskUsageEntry(arr[arr.length - 1],
                    node.getPath() + "/" + name, size, 0, true);
            node.getChildren().add(entry);
        } else {
            entry.setSize(size);
        }
    }
}
