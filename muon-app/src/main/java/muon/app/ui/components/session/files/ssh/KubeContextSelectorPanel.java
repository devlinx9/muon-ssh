package muon.app.ui.components.session.files.ssh;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.util.PlatformUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class KubeContextSelectorPanel extends JPanel {

    private final transient ScheduledExecutorService k8sContextUpdater = Executors.newSingleThreadScheduledExecutor();

    private final Box verticalBox;
    @Getter
    private boolean commandWorking = false;

    @Getter
    private String currentContext = "";

    public KubeContextSelectorPanel() {
        setLayout(new BorderLayout());
        verticalBox = Box.createVerticalBox();

        JScrollPane jsp = new JScrollPane(verticalBox);
        jsp.setBorder(null);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(jsp, BorderLayout.CENTER);
        startK8sContextUpdater();
    }

    public void showContexts() {
        verticalBox.removeAll();

        String contexts = executeK8sCommands("kubectl config get-contexts");
        if (contexts == null || contexts.isEmpty()) {
            log.warn("No output from 'kubectl config get-contexts' or failed to retrieve contexts.");
            return;
        }

        String[] lines = contexts.split("\n");
        if (lines.length == 0) {
            log.warn("No lines to parse from 'kubectl config get-contexts'");
            return;
        }

        currentContext = getActualContext(lines);
        App.getAppWindow().getLblK8sContext().setText(currentContext);

        verticalBox.revalidate();
        verticalBox.repaint();
    }

    private String getActualContext(String[] lines) {
        boolean isFirst = true;
        String actualContext = "";

        for (String line : lines) {
            String trimmed = line.trim();

            if (!isFirst && !trimmed.isEmpty()) {
                String[] parts = trimmed.split("\\s+");
                if (parts.length >= 2) {
                    // Determine if current context (if line starts with "*")
                    boolean isCurrent = trimmed.startsWith("*");
                    // Context name is in different columns depending on whether line starts with '*'
                    String contextName = isCurrent ? parts[1] : parts[0];
                    actualContext = isCurrent ? contextName : actualContext;

                    JLabel label = createContextLabel(contextName, isCurrent);
                    verticalBox.add(label);
                }
            }
            isFirst = false;
        }
        return actualContext;
    }

    private @NotNull JLabel createContextLabel(String contextName, boolean isCurrent) {
        JLabel label = new JLabel(contextName);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        Font baseFont = label.getFont();
        Font labelFont = baseFont.deriveFont(isCurrent ? Font.BOLD : Font.PLAIN, 14f);
        label.setFont(labelFont);

        label.setForeground(isCurrent ? App.getCONTEXT().getSkin().getDefaultSelectionForeground() : App.getCONTEXT().getSkin().getDefaultForeground());

        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        label.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        label.setOpaque(true);
        label.setBackground(isCurrent ? App.getCONTEXT().getSkin().getDefaultSelectionBackground() : App.getCONTEXT().getSkin().getDefaultBackground());


        // Add a click handler to switch context
        label.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    try {
                        switchContext(contextName);
                    } catch (Exception ex) {
                        log.error("Error switching context: {}", ex.getMessage(), ex);
                    }
                });

            }
        });
        return label;
    }

    /**
     * Switches to the specified Kubernetes context by running
     * "kubectl config use-context CONTEXT" locally.
     */
    private void switchContext(String context) {
        String output = executeK8sCommands("kubectl config use-context " + context);
        if (output != null && !output.isEmpty()) {
            log.info("Switched to context: {}", context);
        } else {
            log.warn("Attempted to switch context, but got an empty/failed response.");
        }

        currentContext = context;
        App.getAppWindow().getLblK8sContext().setText(context);

        Container parent = this.getParent();
        if (parent != null) {
            parent.setVisible(false);
            parent.revalidate();
            parent.repaint();
        }
    }

    private String executeK8sCommands(String command) {
        String output = "";
        try {
            StringBuilder sb = new StringBuilder();
            int exitCode = PlatformUtils.executeLocalCommand(command, sb);
            if (exitCode == 0) {
                output = sb.toString().trim();
                log.debug("Local K8s response:\n{}", output);
            } else {
                log.error("Non-zero exit code {} for command.", exitCode);
            }
        } catch (Exception e) {
            log.error("Exception switching context: {}", e.getMessage(), e);
        }
        return output;
    }


    private void startK8sContextUpdater() {
        AtomicReference<String> context = new AtomicReference<>(executeK8sCommands("kubectl config current-context"));
        if (context.get() == null || context.get().isEmpty()) {
            log.error("Error uploading K8s context pluging");
            return;
        }

        commandWorking = true;
        // Periodically refresh the current K8s context and show it in the main window label
        k8sContextUpdater.scheduleAtFixedRate(() -> {
            context.set(executeK8sCommands("kubectl config current-context"));
            currentContext = context.get();
            if (App.getAppWindow().getLblK8sContext() != null) {
                App.getAppWindow().getLblK8sContext().setText(context.get());
            }
            log.info("Auto-updating local K8s context: {}", context);
        }, 0, 30, TimeUnit.SECONDS);

    }
}
