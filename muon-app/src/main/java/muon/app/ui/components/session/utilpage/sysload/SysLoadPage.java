
package muon.app.ui.components.session.utilpage.sysload;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.utilpage.UtilPageItemView;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.concurrent.atomic.AtomicInteger;



/**
 * @author subhro
 *
 */
@Slf4j
public class SysLoadPage extends UtilPageItemView {
    private final AtomicInteger sleepInterval = new AtomicInteger(3);
    private SystemLoadPanel systemLoadPanel;
    private JSpinner spInterval;
    private Timer timer;
    private LinuxMetrics metrics;
    private String OS;

    
    public SysLoadPage(SessionContentPanel holder) {
        super(holder);
    }

    
    private void fetchSystemLoad() {
        holder.EXECUTOR.submit(() -> {
            try {
                if (holder.isSessionClosed()) {
                    SwingUtilities.invokeAndWait(() -> timer.stop());
                    return;
                }
                log.info("Getting system metrics");
                this.metrics
                        .updateMetrics(this.holder.getRemoteSessionInstance());
                if ("Linux".equals(this.metrics.getOS())) {
                    SwingUtilities.invokeAndWait(() -> {
                        // update ui stat
                        systemLoadPanel.setCpuUsage(this.metrics.getCpuUsage());
                        systemLoadPanel
                                .setMemoryUsage(this.metrics.getMemoryUsage());
                        systemLoadPanel
                                .setSwapUsage(this.metrics.getSwapUsage());
                        systemLoadPanel
                                .setTotalMemory(this.metrics.getTotalMemory());
                        systemLoadPanel
                                .setUsedMemory(this.metrics.getUsedMemory());
                        systemLoadPanel
                                .setTotalSwap(this.metrics.getTotalSwap());
                        systemLoadPanel.setUsedSwap(this.metrics.getUsedSwap());
                        systemLoadPanel.refreshUi();
                    });
                } else {
                    this.OS = this.metrics.getOS();
                    this.metrics = null;
                    SwingUtilities.invokeLater(() -> {
                        this.timer.stop();
                        JLabel lblError = new JLabel("Unsupported OS " + this.OS
                                + ", currently only Linux is supported");
                        lblError.setHorizontalAlignment(JLabel.CENTER);
                        lblError.setVerticalAlignment(JLabel.CENTER);
                        this.remove(systemLoadPanel);
                        this.add(lblError);
                        this.revalidate();
                        this.repaint(0);
                    });
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    private void componentVisible() {
        startMonitoring();
    }

    private void componentHidden() {
        stopMonitoring();
    }

    private void startMonitoring() {
        if (metrics != null) {
            timer.start();
        }
    }

    private void stopMonitoring() {
        timer.stop();
    }

    @Override
    protected void createUI() {
        spInterval = new JSpinner(new SpinnerNumberModel(100, 1, 100, 1));
        spInterval.setValue(sleepInterval.get());
        spInterval.setMaximumSize(spInterval.getPreferredSize());
        spInterval.addChangeListener(e -> {
            int interval = (Integer) spInterval.getValue();
            log.info("New interval: {}", interval);
            this.sleepInterval.set(interval);
            timer.stop();
            timer.setDelay(this.sleepInterval.get() * 1000);
            timer.start();
        });

        systemLoadPanel = new SystemLoadPanel();

        Box topPanel = Box.createHorizontalBox();
        topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel titleLabel = new JLabel("System Monitor");
        titleLabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 18));

        topPanel.add(titleLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(new JLabel(App.getCONTEXT().getBundle().getString("refresh_interval")));
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(spInterval);
        topPanel.add(Box.createHorizontalStrut(5));
        topPanel.add(new JLabel("Sec"));
        this.add(topPanel, BorderLayout.NORTH);
        this.add(systemLoadPanel);

        timer = new Timer(this.sleepInterval.get() * 1000, e -> fetchSystemLoad());
        timer.setInitialDelay(0);
        timer.setCoalesce(true);

        metrics = new LinuxMetrics();
    }

    @Override
    protected void onComponentVisible() {
        componentVisible();
    }

    @Override
    protected void onComponentHide() {
        componentHidden();
    }

}
