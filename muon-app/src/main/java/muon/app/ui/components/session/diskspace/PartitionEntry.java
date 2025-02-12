package muon.app.ui.components.session.diskspace;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PartitionEntry {
    private String fileSystem;
    private String mountPoint;
    private long totalSize;
    private long used;
    private long available;
    private double usedPercent;

    public PartitionEntry(String fileSystem, String mountPoint, long totalSize,
                          long used, long available, double usedPercent) {
        this.fileSystem = fileSystem;
        this.mountPoint = mountPoint;
        this.totalSize = totalSize;
        this.used = used;
        this.available = available;
        this.usedPercent = usedPercent;
    }

}
