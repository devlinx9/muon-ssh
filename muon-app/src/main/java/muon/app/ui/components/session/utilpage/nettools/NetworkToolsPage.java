
package muon.app.ui.components.session.utilpage.nettools;

import lombok.extern.slf4j.Slf4j;
import muon.app.App;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.common.SkinnedTextArea;
import muon.app.ui.components.session.SessionContentPanel;
import muon.app.ui.components.session.utilpage.UtilPageItemView;
import muon.app.util.OptionPaneUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * @author subhro
 */
@Slf4j
public class NetworkToolsPage extends UtilPageItemView {
    private JTextArea txtOutput;
    private JComboBox<String> cmbHost;
    private JComboBox<String> cmbPort;
    private JComboBox<String> cmbDNSTool;

    
    public NetworkToolsPage(SessionContentPanel holder) {
        super(holder);
    }

    @Override
    protected void createUI() {
        DefaultComboBoxModel<String> modelHost = new DefaultComboBoxModel<>();
        DefaultComboBoxModel<String> modelPort = new DefaultComboBoxModel<>();

        cmbHost = new JComboBox<>(modelHost);
        cmbPort = new JComboBox<>(modelPort);
        cmbHost.setEditable(true);
        cmbPort.setEditable(true);

        cmbDNSTool = new JComboBox<>(new String[]{"nslookup", "dig",
                                                  "dig +short", "host", "getent ahostsv4"});

        JPanel grid = new JPanel(new GridLayout(1, 4, 10, 10));
        grid.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton btn1 = new JButton("Ping");
        JButton btn2 = new JButton("Port check");
        JButton btn3 = new JButton("Traceroute");
        JButton btn4 = new JButton("DNS lookup");

        btn1.addActionListener(e -> {
            if (OptionPaneUtils.showOptionDialog(this,
                                                 new Object[]{App.getCONTEXT().getBundle().getString("host_ping"), cmbHost}, "Ping") == JOptionPane.OK_OPTION) {
                executeAsync("ping -c 4 " + cmbHost.getSelectedItem());
            }
        });

        btn2.addActionListener(e -> {
            if (OptionPaneUtils.showOptionDialog(this,
                                                 new Object[]{App.getCONTEXT().getBundle().getString("host_name"), cmbHost, App.getCONTEXT().getBundle().getString("port_number"),
                                                              cmbPort},
                                                 "Port check") == JOptionPane.OK_OPTION) {
                executeAsync("bash -c 'test cat</dev/tcp/"
                             + cmbHost.getSelectedItem() + "/"
                             + cmbPort.getSelectedItem()
                             + " && echo \"Port Reachable\" || echo \"Port Not reachable\"'");
            }
        });

        btn3.addActionListener(e -> {
            if (OptionPaneUtils.showOptionDialog(this,
                                                 new Object[]{App.getCONTEXT().getBundle().getString("host_name"), cmbHost}, "Traceroute") == JOptionPane.OK_OPTION) {
                executeAsync("traceroute " + cmbHost.getSelectedItem());
            }
        });

        btn4.addActionListener(e -> {
            if (OptionPaneUtils.showOptionDialog(this,
                                                 new Object[]{App.getCONTEXT().getBundle().getString("host_name"), cmbHost, App.getCONTEXT().getBundle().getString("tool_use"),
                                                              cmbDNSTool},
                                                 "DNS lookup") == JOptionPane.OK_OPTION) {
                executeAsync(cmbDNSTool.getSelectedItem() + " "
                             + cmbHost.getSelectedItem());
            }
        });

        grid.add(btn1);
        grid.add(btn2);
        grid.add(btn3);
        grid.add(btn4);

        this.setBorder(new EmptyBorder(5, 5, 5, 5));
        this.add(grid, BorderLayout.NORTH);

        txtOutput = new SkinnedTextArea();
        txtOutput.setEditable(false);
        JScrollPane jsp = new SkinnedScrollPane(txtOutput);
        jsp.setBorder(new LineBorder(App.getCONTEXT().getSkin().getDefaultBorderColor()));
        this.add(jsp);
    }

    private void executeAsync(String cmd) {
        AtomicBoolean stopFlag = new AtomicBoolean(false);
        holder.disableUi(stopFlag);
        holder.EXECUTOR.submit(() -> {
            StringBuilder outText = new StringBuilder();
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                if (holder.getRemoteSessionInstance().execBin(cmd, stopFlag,
                                                              bout, null) == 0) {
                    outText.append(bout.toString(StandardCharsets.UTF_8)).append("\n");
                    log.info("Command stdout: {}", outText);
                } else {
                    JOptionPane.showMessageDialog(this,
                                                  App.getCONTEXT().getBundle().getString("executed_errors"));
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                SwingUtilities.invokeLater(() -> this.txtOutput.setText(outText.toString()));
                holder.enableUi();
            }
        });
    }

    @Override
    protected void onComponentVisible() {


    }

    @Override
    protected void onComponentHide() {


    }
}
