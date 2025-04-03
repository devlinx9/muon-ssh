package muon.app.ui.components.session.utilpage.sysload;

import lombok.Setter;
import muon.app.App;
import muon.app.util.FormatUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;



public class SystemLoadPanel extends JPanel {
    private final double[] cpuStats = new double[10];
    private final double[] memStats = new double[10];
    private final double[] swpStats = new double[10];
    private final JLabel cpuLabel;
    private final JLabel memoryLabel;
    private final JLabel swapLabel;
    @Setter
    private long totalMemory;
    @Setter
    private long usedMemory;
    @Setter
    private long totalSwap;
    @Setter
    private long usedSwap;
    private double cpuUsage;
    private double memoryUsage;
    private double swapUsage;

    public SystemLoadPanel() {
        super(new BorderLayout(5, 5));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setMinimumSize(new Dimension(200, 100));
        setPreferredSize(new Dimension(300, 400));
        Box b1 = Box.createVerticalBox();

        cpuLabel = new JLabel(App.getCONTEXT().getBundle().getString("cpu_usage"));
        cpuLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        cpuLabel.setAlignmentX(Box.LEFT_ALIGNMENT);
        b1.add(cpuLabel);

        LineGraph cpuGraph = new LineGraph();
        cpuGraph.setValues(cpuStats);
        cpuGraph.setAlignmentX(Box.LEFT_ALIGNMENT);
        b1.add(cpuGraph);

        memoryLabel = new JLabel(App.getCONTEXT().getBundle().getString("memory_usage"));
        memoryLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        memoryLabel.setAlignmentX(Box.LEFT_ALIGNMENT);
        b1.add(memoryLabel);

        LineGraph memGraph = new LineGraph();
        memGraph.setValues(memStats);
        memGraph.setAlignmentX(Box.LEFT_ALIGNMENT);
        b1.add(memGraph);

        swapLabel = new JLabel(App.getCONTEXT().getBundle().getString("swap_usage"));
        swapLabel.setBorder(new EmptyBorder(20, 0, 10, 0));
        swapLabel.setAlignmentX(Box.LEFT_ALIGNMENT);
        b1.add(swapLabel);

        LineGraph swpGraph = new LineGraph();
        swpGraph.setValues(swpStats);
        swpGraph.setAlignmentX(Box.LEFT_ALIGNMENT);
        b1.add(swpGraph);

        add(b1);
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
        if (this.cpuUsage != 0) {
            System.arraycopy(cpuStats, 1, cpuStats, 0, cpuStats.length - 1);
            cpuStats[cpuStats.length - 1] = cpuUsage;
        }
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
        if (this.memoryUsage != 0) {
            System.arraycopy(memStats, 1, memStats, 0, memStats.length - 1);
            memStats[memStats.length - 1] = memoryUsage;
        }
    }

    public void setSwapUsage(double swapUsage) {
        this.swapUsage = swapUsage;
        if (this.swapUsage != 0) {
            System.arraycopy(swpStats, 1, swpStats, 0, swpStats.length - 1);
            swpStats[swpStats.length - 1] = swapUsage;
        }
    }

    public void refreshUi() {
        this.cpuLabel
                .setText(String.format(App.getCONTEXT().getBundle().getString("cpu_usage") + ": %.1f", cpuUsage) + "% ");
        this.memoryLabel.setText(String.format(App.getCONTEXT().getBundle().getString("memory_usage") + ": %.1f",
                                               memoryUsage)
                                 + "%"
                                 + (totalMemory != 0
                                    ? (", (Total: " + FormatUtils
                .humanReadableByteCount(totalMemory, true)
                                       + ", " + App.getCONTEXT().getBundle().getString("used") + ": "
                                       + FormatUtils.humanReadableByteCount(usedMemory,
                                                                            true)
                                       + ")")
                                    : ""));
        this.swapLabel.setText(String.format(App.getCONTEXT().getBundle().getString("swap_usage") + ": %.1f", swapUsage)
                               + "% "
                               + (totalSwap != 0
                                  ? (", ( Total: "
                                     + FormatUtils.humanReadableByteCount(totalSwap,
                                                                          true)
                                     + ", " + App.getCONTEXT().getBundle().getString("used") + ": " + FormatUtils
                                             .humanReadableByteCount(usedSwap, true)
                                     + ")")
                                  : ""));
        this.revalidate();
        this.repaint();
    }
}
