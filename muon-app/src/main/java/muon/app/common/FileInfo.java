package muon.app.common;

import lombok.Getter;
import lombok.Setter;
import util.TimeUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
public class FileInfo implements Serializable {
    private static final Pattern USER_REGEX = Pattern
            .compile("^[^\\s]+\\s+[^\\s]+\\s+([^\\s]+)\\s+([^\\s]+)");
    private final String name;
    private String path;
    private long size;
    private FileType type;
    private LocalDateTime lastModified;
    private LocalDateTime created;
    private int permission;
    private String protocol;
    private String permissionString;
    private String extra;
    private String user;
    private boolean hidden;

    public FileInfo(String name, String path, long size, FileType type,
                    long lastModified, int permission, String protocol,
                    String permissionString, long created, String extra,
                    boolean hidden) {
        super();
        this.name = name;
        this.path = path;
        this.size = size;
        this.type = type;
        this.lastModified = TimeUtils.toDateTime(lastModified);
        this.permission = permission;
        this.protocol = protocol;
        this.permissionString = permissionString;
        this.created = TimeUtils.toDateTime(created);
        this.extra = extra;
        if (this.extra != null && !this.extra.isEmpty()) {
            this.user = getUserName();
        }
        this.hidden = hidden;
    }

    private String getUserName() {
        try {
            if (this.extra != null && !this.extra.isEmpty()) {
                Matcher matcher = USER_REGEX.matcher(this.extra);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public void setLastModified(long lastModified) {
        this.lastModified = TimeUtils.toDateTime(lastModified);
    }

    @Override
    public String toString() {
        return name;
    }

    public boolean isDirectory() {
        return getType() == FileType.DIRECTORY || getType() == FileType.DIR_LINK;
    }
}
