
package muon.app.ui.components.session.logviewer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.util.Constants;
import muon.app.util.OptionPaneUtils;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;


/**
 * @author subhro
 */
@Slf4j
public class StartPage extends JPanel {
    private static final Cursor HAND_CURSOR = new Cursor(Cursor.HAND_CURSOR);
    private static final Cursor DEFAULT_CURSOR = new Cursor(
            Cursor.DEFAULT_CURSOR);
    private final DefaultListModel<String> pinnedLogsModel;
    private final JList<String> pinnedLogList;
    private final List<String> finalPinnedLogs;
    private final String sessionId;
    private boolean hover = false;

    private static Map<String, List<String>> pinnedLogs = new HashMap<>();


    
    public StartPage(Consumer<String> callback, String sessionId) {
        super(new BorderLayout());
        this.sessionId = sessionId;
        List<String> defaultPinnedLogs = List.of("/var/log/gpu-manager.log", "/var/log/syslog");
        loadPinnedLogs();
        if (pinnedLogs.containsKey(sessionId)) {
            defaultPinnedLogs = pinnedLogs.get(sessionId);
        }

        this.finalPinnedLogs = defaultPinnedLogs;

        pinnedLogsModel = new DefaultListModel<>();
        pinnedLogsModel.addAll(finalPinnedLogs);
        pinnedLogList = new JList<>(pinnedLogsModel);
        pinnedLogList.setCellRenderer(new PinnedLogsRenderer());
        pinnedLogList.setBackground(App.getCONTEXT().getSkin().getSelectedTabColor());
        JScrollPane jsp = new SkinnedScrollPane(pinnedLogList);
        jsp.setBorder(new EmptyBorder(0, 10, 0, 10));
        this.add(jsp);
        JButton btnAddLog = new JButton(App.getCONTEXT().getBundle().getString("add_log"));
        JButton btnDelLog = new JButton(App.getCONTEXT().getBundle().getString("delete"));
        btnAddLog.addActionListener(e -> {
            String logPath = promptLogPath();
            if (logPath != null) {
                finalPinnedLogs.add(logPath);
                pinnedLogsModel.addElement(logPath);
                pinnedLogs.put(sessionId, finalPinnedLogs);
                savePinnedLogs();
            }
        });
        btnDelLog.addActionListener(e -> {
            int index = pinnedLogList.getSelectedIndex();
            if (index != -1) {
                pinnedLogsModel.remove(index);
            }
        });
        Box bottomBox = Box.createHorizontalBox();
        bottomBox.add(Box.createHorizontalGlue());
        bottomBox.add(btnAddLog);
        bottomBox.add(Box.createHorizontalStrut(10));
        bottomBox.add(btnDelLog);
        bottomBox.setBorder(new EmptyBorder(10, 10, 10, 10));
        this.add(bottomBox, BorderLayout.SOUTH);
        pinnedLogList.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                int index = pinnedLogList.locationToIndex(e.getPoint());
                if (index != -1) {
                    Rectangle r = pinnedLogList.getCellBounds(index, index);
                    if (r != null && r.contains(e.getPoint())) {
                        if (!pinnedLogList.isSelectedIndex(index)) {
                            pinnedLogList.setSelectedIndex(index);
                        }
                        if (hover) {
                            return;
                        }
                        hover = true;
                        pinnedLogList.setCursor(HAND_CURSOR);
                        return;
                    }
                }
                if (hover) {
                    hover = false;
                    pinnedLogList.setCursor(DEFAULT_CURSOR);
                }
            }
        });
        pinnedLogList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (hover) {
                    int index = pinnedLogList.getSelectedIndex();
                    if (index != -1) {
                        callback.accept(pinnedLogsModel.elementAt(index));
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hover = false;
                pinnedLogList.setCursor(DEFAULT_CURSOR);
            }
        });

    }

    private String promptLogPath() {
        JTextField txt = new SkinnedTextField(30);
        if (OptionPaneUtils.showOptionDialog(this,
                                             new Object[]{App.getCONTEXT().getBundle().getString("provide_log_file_path"),
                                                          txt},
                                             "Input") == JOptionPane.OK_OPTION && !txt.getText().isEmpty()) {
            return txt.getText();
        }
        return null;
    }

    public void pinLog(String logPath) {
        pinnedLogsModel.addElement(logPath);
        finalPinnedLogs.add(logPath);
        pinnedLogs.put(sessionId, finalPinnedLogs);
        savePinnedLogs();
    }

    static class PinnedLogsRenderer extends JLabel
            implements ListCellRenderer<String> {
        /**
         *
         */
        public PinnedLogsRenderer() {
            setOpaque(true);
            setBorder(new CompoundBorder(
                    new MatteBorder(0, 0, 2, 0,
                                    App.getCONTEXT().getSkin().getDefaultBackground()),
                    new EmptyBorder(10, 10, 10, 10)));
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends String> list, String value, int index,
                boolean isSelected, boolean cellHasFocus) {
            setBackground(isSelected ? App.getCONTEXT().getSkin().getDefaultSelectionBackground()
                                     : list.getBackground());
            setForeground(isSelected ? App.getCONTEXT().getSkin().getDefaultSelectionForeground()
                                     : list.getForeground());
            setText(value);
            return this;
        }
    }

    private static synchronized void loadPinnedLogs() {
        File file = new File(App.getCONTEXT().getConfigDir(), Constants.PINNED_LOGS);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        if (file.exists()) {
            try {
                pinnedLogs = objectMapper.readValue(file, new TypeReference<>() {
                });
                return;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        pinnedLogs = new HashMap<>();
    }

    private static synchronized void savePinnedLogs() {
        File file = new File(App.getCONTEXT().getConfigDir(), Constants.PINNED_LOGS);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writeValue(file, pinnedLogs);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
