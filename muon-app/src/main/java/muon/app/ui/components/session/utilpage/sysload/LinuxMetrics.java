/**
 *
 */
package muon.app.ui.components.session.utilpage.sysload;

import muon.app.ssh.RemoteSessionInstance;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author subhro
 *
 */
public class LinuxMetrics {
    private double cpuUsage;
    private double memoryUsage;
    private double swapUsage;
    private long totalMemory;
    private long usedMemory;
    private long totalSwap;
    private long usedSwap;
    private long prevIdle;
    private long prevTotal;
    private String os;

    public void updateMetrics(RemoteSessionInstance instance) throws Exception {
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        int ret = instance.exec(
                "uname; head -1 /proc/stat;grep -E \"MemTotal|MemFree|Cached|SwapTotal|SwapFree\" /proc/meminfo",
                new AtomicBoolean(), out, err);
        if (ret != 0)
            throw new Exception("Error while getting metrics");
        updateStats(out.toString());
    }

    private void updateStats(String str) {
        String[] lines = str.split("\n");
        os = lines[0];
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
        long memTotalK = 0;
        long memFreeK = 0;
        long memCachedK = 0;
        long swapTotalK = 0;
        long swapFreeK = 0;
        long swapCachedK = 0;
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
            this.usedSwap = this.totalSwap - freeSwap - swapCachedK * 1024;
            this.swapUsage = ((double) (this.totalSwap - freeSwap
                    - swapCachedK * 1024) * 100) / this.totalSwap;
        }
    }

    /**
     * @return the cpuUsage
     */
    public double getCpuUsage() {
        return cpuUsage;
    }

    /**
     * @return the memoryUsage
     */
    public double getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * @return the swapUsage
     */
    public double getSwapUsage() {
        return swapUsage;
    }

    /**
     * @return the totalMemory
     */
    public long getTotalMemory() {
        return totalMemory;
    }

    /**
     * @return the usedMemory
     */
    public long getUsedMemory() {
        return usedMemory;
    }

    /**
     * @return the totalSwap
     */
    public long getTotalSwap() {
        return totalSwap;
    }

    /**
     * @return the usedSwap
     */
    public long getUsedSwap() {
        return usedSwap;
    }

    /**
     * @return the oS
     */
    public String getOs() {
        return os;
    }
}
