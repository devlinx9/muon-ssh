
package muon.app.ui.components.session.utilpage.sysload;

import lombok.Getter;
import muon.app.ssh.RemoteSessionInstance;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author subhro
 */
public class LinuxMetrics {

    @Getter
    private double cpuUsage;
    @Getter
    private double memoryUsage;
    @Getter
    private double swapUsage;

    @Getter
    private long totalMemory;
    @Getter
    private long usedMemory;
    @Getter
    private long totalSwap;
    @Getter
    private long usedSwap;
    private long prevIdle;
    private long prevTotal;

    @Getter
    private String OS;

    public void updateMetrics(RemoteSessionInstance instance) throws Exception {
        StringBuilder out = new StringBuilder(), err = new StringBuilder();
        int ret = instance.exec(
                "uname; head -1 /proc/stat;grep -E \"MemTotal|MemFree|Cached|SwapTotal|SwapFree\" /proc/meminfo",
                new AtomicBoolean(), out, err);
        if (ret != 0) {
            throw new Exception("Error while getting metrics");
        }
        updateStats(out.toString());
    }

    private void updateStats(String str) {
        String[] lines = str.split("\n");
        OS = lines[0];
        String cpuStr = lines[1];
        updateCpu(cpuStr);
        updateMemory(lines);
    }

    private void updateCpu(String line) {
        String[] cols = line.split("\\s+");
        long idle = Long.parseLong(cols[4]);
        long total = 0;
        for (int i = 1; i < cols.length; i++) {
            total += Long.parseLong(cols[i]);
        }
        long diffIdle = idle - prevIdle;
        long diffTotal = total - prevTotal;
        this.cpuUsage = (1000 * ((double) diffTotal - diffIdle) / diffTotal
                         + 5) / 10;
        this.prevIdle = idle;
        this.prevTotal = total;
    }

    private void updateMemory(String[] lines) {
        long memTotalK = 0, memFreeK = 0, memCachedK = 0, swapTotalK = 0,
                swapFreeK = 0;
        for (int i = 2; i < lines.length; i++) {
            String[] arr = lines[i].split("\\s+");
            if (arr.length >= 2) {
                if (arr[0].trim().equals("MemTotal:")) {
                    memTotalK = Long.parseLong(arr[1].trim());
                }
                if (arr[0].trim().equals("Cached:")) {
                    memFreeK = Long.parseLong(arr[1].trim());
                }
                if (arr[0].trim().equals("MemFree:")) {
                    memCachedK = Long.parseLong(arr[1].trim());
                }
                if (arr[0].trim().equals("SwapTotal:")) {
                    swapTotalK = Long.parseLong(arr[1].trim());
                }
                if (arr[0].trim().equals("SwapFree:")) {
                    swapFreeK = Long.parseLong(arr[1].trim());
                }
            }
        }

        this.totalMemory = memTotalK * 1024;
        this.totalSwap = swapTotalK * 1024;
        long freeMemory = memFreeK * 1024;
        long freeSwap = swapFreeK * 1024;

        if (this.totalMemory > 0) {
            this.usedMemory = this.totalMemory - freeMemory - memCachedK * 1024;
            this.memoryUsage = ((double) (this.totalMemory - freeMemory
                                          - memCachedK * 1024) * 100) / this.totalMemory;
        }

        if (this.totalSwap > 0) {
            this.usedSwap = this.totalSwap - freeSwap;
            this.swapUsage = ((double) (this.totalSwap - freeSwap) * 100) / this.totalSwap;
        }
    }

}
