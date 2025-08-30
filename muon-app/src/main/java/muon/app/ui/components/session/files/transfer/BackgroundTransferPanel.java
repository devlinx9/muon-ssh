package muon.app.ui.components.session.files.transfer;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.util.FontAwesomeContants;
import muon.app.util.FormatUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static muon.app.util.Constants.MEDIUM_TEXT_SIZE;
import static muon.app.util.ScalingUtil.scale;
import static muon.app.util.ScalingUtil.getScaledEmptyBorder;

@Slf4j
public class BackgroundTransferPanel extends JPanel {
    private final Box verticalBox;
    private final AtomicInteger transferCount = new AtomicInteger(0);
    private final Consumer<Integer> callback;

    /**
     * @param callback callback for notifying number of active transfers
     */
    public BackgroundTransferPanel(Consumer<Integer> callback) {
        super(new BorderLayout());
        this.callback = callback;
        verticalBox = Box.createVerticalBox();
        JScrollPane jsp = new JScrollPane(verticalBox);
        jsp.setBorder(null);
        jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(jsp);
    }

    public void addNewBackgroundTransfer(BackgroundFileTransfer transfer) {
        TransferPanelItem item = new TransferPanelItem(transfer);
        item.setAlignmentX(Box.LEFT_ALIGNMENT);
        this.verticalBox.add(item);
        this.verticalBox.revalidate();
        this.verticalBox.repaint();
        item.handle = transfer.getSession().getBackgroundTransferPool().submit(() -> {
            try {
                transfer.getFileTransfer().run();
                transfer.getSession().addToSessionCache(transfer.getInstance());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    public void removePendingTransfers(int sessionId) {
        if (!SwingUtilities.isEventDispatchThread()) {
            try {
                SwingUtilities.invokeAndWait(() -> stopSession(sessionId));
            } catch (InvocationTargetException | InterruptedException e) {

                log.error(e.getMessage(), e);
            }
        } else {
            stopSession(sessionId);
        }
    }

    private void stopSession(int sessionId) {
        for (int i = 0; i < this.verticalBox.getComponentCount(); i++) {
            Component c = this.verticalBox.getComponent(i);
            if (c instanceof TransferPanelItem) {
                TransferPanelItem tpi = (TransferPanelItem) c;
                if (tpi.fileTransfer.getSession().getActiveSessionId() == sessionId) {
                    tpi.stop();
                }
            }
        }
    }

    class TransferPanelItem extends JPanel implements FileTransferProgress {
        private final BackgroundFileTransfer fileTransfer;
        private final JProgressBar progressBar;
        private final JLabel progressLabel;
        private final JLabel removeLabel;
        private Future<?> handle;

        public TransferPanelItem(BackgroundFileTransfer transfer) {
            super(new BorderLayout());
            transferCount.incrementAndGet();
            callback.accept(transferCount.get());
            this.fileTransfer = transfer;
            transfer.getFileTransfer().setCallback(this);
            progressBar = new JProgressBar();
            progressLabel = new JLabel("Waiting...");
            progressLabel.setBorder(getScaledEmptyBorder(10, 0, 10, 5));
            removeLabel = new JLabel();
            removeLabel.setFont(App.getCONTEXT().getSkin().getIconFont(MEDIUM_TEXT_SIZE));
            removeLabel.setText(FontAwesomeContants.FA_STOP_CIRCLE);

            removeLabel.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (removeLabel.getText().equals(FontAwesomeContants.FA_STOP_CIRCLE)) {
                        fileTransfer.getFileTransfer().stop();
                        fileTransfer.getFileTransfer().setOperationCancelledByUser(new AtomicBoolean(true));
                        return;
                    }

                    SwingUtilities.invokeLater(() -> {
                        BackgroundTransferPanel.this.verticalBox.remove(TransferPanelItem.this);
                        BackgroundTransferPanel.this.revalidate();
                        BackgroundTransferPanel.this.repaint();
                    });

                }
            });

            setBorder(getScaledEmptyBorder(10, 10, 10, 10));
            Box topBox = Box.createHorizontalBox();
            topBox.add(progressLabel);
            topBox.add(Box.createHorizontalGlue());
            topBox.add(removeLabel);

            add(topBox);
            add(progressBar, BorderLayout.SOUTH);

            setMaximumSize(scale(new Dimension(getMaximumSize().width, getPreferredSize().height)));
        }

        public void stop() {
            fileTransfer.getFileTransfer().stop();
            this.handle.cancel(false);
        }

        @Override
        public void init(long totalSize, long files, FileTransfer fileTransfer) {
            SwingUtilities.invokeLater(() -> {
                progressLabel.setText(
                        String.format("<html>Copying %s to %s (%s Mb)<br>File: %s</html>",
                                fileTransfer.getSourceName(),
                                fileTransfer.getTargetName(),
                                fileTransfer.getTotalSize() / 1024 / 1024,
                                FormatUtils.limitTextOutput(fileTransfer.getCurrentSourceFilePath(),40)));
                progressLabel.setToolTipText(String.format("%s to %s",
                        fileTransfer.getCurrentSourceFilePath(),
                        fileTransfer.getCurrentTargetFilePath()));
                progressBar.setValue(0);
            });
        }

        @Override
        public void progress(long processedBytes, long totalBytes, long processedCount, long totalCount,
                             FileTransfer fileTransfer) {
            SwingUtilities.invokeLater(() -> {
                progressLabel.setText(
                        String.format("<html>Copying %s to %s (%s Mb)<br>File: %s</html>",
                                fileTransfer.getSourceName(),
                                fileTransfer.getTargetName(),
                                fileTransfer.getTotalSize() / 1024 / 1024,
                                FormatUtils.limitTextOutput(fileTransfer.getCurrentSourceFilePath(),40)));
                progressLabel.setToolTipText(String.format("%s to %s",
                        fileTransfer.getCurrentSourceFilePath(),
                        fileTransfer.getCurrentTargetFilePath()));
                progressBar.setValue(totalBytes > 0 ? ((int) ((processedBytes * 100) / totalBytes)) : 0);
            });
        }

        @Override
        public void error(String cause, FileTransfer fileTransfer) {
            transferCount.decrementAndGet();
            callback.accept(transferCount.get());
            SwingUtilities.invokeLater(() -> {
                progressLabel.setText(
                        String.format("<html>Error %s to %s<br>File (%s): %s</html>",
                                fileTransfer.getSourceName(),
                                fileTransfer.getTargetName(),
                                FormatUtils.limitTextOutput(cause, 15),
                                FormatUtils.limitTextOutput(fileTransfer.getCurrentSourceFilePath(), 25)));
                progressLabel.setToolTipText(fileTransfer.getCurrentSourceFilePath().concat(": ").concat(cause));
            });
        }

        @Override
        public void done(FileTransfer fileTransfer) {
            transferCount.decrementAndGet();
            callback.accept(transferCount.get());
            this.fileTransfer.getSession().addToSessionCache(this.fileTransfer.getInstance());
            removeLabel.setText(FontAwesomeContants.FA_TRASH);
            String status = "Done transfer!";
            if (this.fileTransfer.getFileTransfer().getOperationCancelledByUser().get()) {
                status = "Transfer cancelled by user";
            }

            log.info(status);

            progressLabel.setText(
                    String.format("<html>Copying %s to %s (%s Mb)<br>%s</html>",
                            fileTransfer.getSourceName(),
                            fileTransfer.getTargetName(),
                            fileTransfer.getTotalSize() / 1024 / 1024,
                            status));
            progressLabel.setToolTipText(status);

            this.fileTransfer.getSession().fileBrowser.reloadView();
        }
    }
}
