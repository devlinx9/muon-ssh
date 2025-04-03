
package muon.app.ui.components.session.utilpage.portview;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.utilpage.UtilPageItemView;
import muon.app.util.SudoUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;



/**
 * @author subhro
 *
 */
@Slf4j
public class PortViewer extends UtilPageItemView {
    private static final String SEPARATOR = UUID.randomUUID().toString();
    public static final String LSOF_COMMAND = "sh -c \"export PATH=$PATH:/usr/sbin; echo;echo "
            + SEPARATOR + ";lsof -b -n -i tcp -P -s tcp:LISTEN -F cn 2>&1\"";
    private final SocketTableModel model = new SocketTableModel();
    private JTextField txtFilter;
    private JCheckBox chkRunAsSuperUser;
    private List<SocketEntry> list;

    
    public PortViewer(SessionContentPanel holder) {
        super(holder);
        setBorder(new EmptyBorder(10, 10, 10, 10));

    }

    private void filter() {
        String text = txtFilter.getText();
        model.clear();
        if (!text.isEmpty()) {
            List<SocketEntry> filteredList = new ArrayList<>();
            for (SocketEntry entry : list) {
                if (entry.getApp().contains(text)
                        || (entry.getPort() + "").contains(text)
                        || entry.getHost().contains(text)
                        || (entry.getPid() + "").contains(text)) {
                    filteredList.add(entry);
                }
            }
            model.addEntries(filteredList);
        } else {
            model.addEntries(list);
        }
        model.fireTableDataChanged();
    }

    public boolean getUseSuperUser() {
        return chkRunAsSuperUser.isSelected();
    }

    public List<SocketEntry> parseSocketList(String text) {
        log.debug("text: {}", text);
        List<SocketEntry> list = new ArrayList<>();
        SocketEntry ent = null;
        boolean start = false;
        for (String line1 : text.split("\n")) {
            String line = line1.trim();
            log.debug("LINE={}", line);
            if (!start) {
                if (line.trim().equals(SEPARATOR)) {
                    start = true;
                }
                continue;
            }
            char ch = line.charAt(0);
            if (ch == 'p') {
                if (ent != null) {
                    list.add(ent);
                }
                ent = new SocketEntry();
                ent.setPid(Integer.parseInt(line.substring(1)));
            }
            if (ch == 'c') {
                Objects.requireNonNull(ent).setApp(line.substring(1));
            }
            if (ch == 'n') {
                String hostStr = line.substring(1);
                int index = hostStr.lastIndexOf(":");
                if (index != -1) {
                    int port = Integer.parseInt(hostStr.substring(index + 1));
                    String host = hostStr.substring(0, index);
                    if (Objects.requireNonNull(ent).getHost() != null) {
                        // if listening on multiple interfaces, ports
                        SocketEntry ent1 = new SocketEntry();
                        ent1.setPort(port);
                        ent1.setHost(host);
                        ent1.setApp(ent.getApp());
                        ent1.setPid(ent.getPid());
                        list.add(ent1);
                    } else {
                        ent.setPort(port);
                        ent.setHost(host);
                    }
                }
            }
        }
        if (ent != null) {
            list.add(ent);
        }
        return list;
    }

    public void setSocketData(List<SocketEntry> list) {
        this.list = list;
        filter();
    }

    @Override
    protected void createUI() {
        JTable table = new JTable(model);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);

        JLabel lbl1 = new JLabel(App.getCONTEXT().getBundle().getString("search"));
        txtFilter = new SkinnedTextField(30);
        JButton btnFilter = new JButton(App.getCONTEXT().getBundle().getString("search"));

        Box b1 = Box.createHorizontalBox();
        b1.add(lbl1);
        b1.add(Box.createHorizontalStrut(5));
        b1.add(txtFilter);
        b1.add(Box.createHorizontalStrut(5));
        b1.add(btnFilter);

        add(b1, BorderLayout.NORTH);

        btnFilter.addActionListener(e -> filter());
        table.setAutoCreateRowSorter(true);
        add(new SkinnedScrollPane(table));

        Box box = Box.createHorizontalBox();
        box.setBorder(new EmptyBorder(10, 0, 0, 0));
        JButton btnRefresh = new JButton(App.getCONTEXT().getBundle().getString("refresh"));
        btnRefresh.addActionListener(e -> getListingSockets());

        chkRunAsSuperUser = new JCheckBox(
                App.getCONTEXT().getBundle().getString("actions_sudo"));
        box.add(chkRunAsSuperUser);

        box.add(Box.createHorizontalGlue());
        box.add(btnRefresh);
        box.add(Box.createHorizontalStrut(5));

        add(box, BorderLayout.SOUTH);

        getListingSockets();
    }

    @Override
    protected void onComponentVisible() {
        

    }

    @Override
    protected void onComponentHide() {
        

    }

    private void getListingSockets() {
        String cmd = LSOF_COMMAND;
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        holder.disableUi(stopFlag);

        boolean elevated = this.getUseSuperUser();
        if (cmd != null) {
            holder.EXECUTOR.submit(() -> {
                try {
                    StringBuilder output = new StringBuilder();
                    if (elevated) {
                        try {
                            if (SudoUtils.runSudoWithOutput(cmd,
                                    holder.getRemoteSessionInstance(), output,
                                    new StringBuilder(),holder.getInfo().getPassword()) == 0) {
                                java.util.List<SocketEntry> list = this
                                        .parseSocketList(output.toString());
                                SwingUtilities.invokeAndWait(() -> setSocketData(list));
                                return;
                            }
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    } else {
                        log.debug("Command was: {}", cmd);
                        try {
                            if (holder.getRemoteSessionInstance().exec(cmd,
                                    stopFlag, output) == 0) {
                                log.debug("Command was: {} {}", cmd, output);
                                java.util.List<SocketEntry> list = this
                                        .parseSocketList(output.toString());
                                SwingUtilities.invokeAndWait(() -> setSocketData(list));
                                return;
                            }
                            log.error("Error: {}", output);
                        } catch (Exception ex) {
                            log.error(ex.getMessage(), ex);
                        }
                    }
                    if (!holder.isSessionClosed()) {
                        JOptionPane.showMessageDialog(null, App.getCONTEXT().getBundle().getString("operation_failed"));
                    }
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                } finally {
                    holder.enableUi();
                }
            });
        }
    }
}
