package muon.app.ui.components.session;

import muon.app.App;
import muon.app.ui.components.common.SkinnedScrollPane;
import muon.app.ui.components.common.SkinnedTextField;
import muon.app.util.FontAwesomeContants;
import muon.app.util.OptionPaneUtils;
import muon.app.util.enums.PortForwardingType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;


public class PortForwardingPanel extends JPanel {
    private final PFTableModel model;
    private final JTable table;
    private SessionInfo info;

    public PortForwardingPanel() {
        super(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 0, 0, 10));
        model = new PFTableModel();
        table = new JTable(model);

        JLabel lblTitle = new JLabel("Port forwarding rules");

        JScrollPane scrollPane = new SkinnedScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        Box b1 = Box.createVerticalBox();
        JButton btnAdd = new JButton(FontAwesomeContants.FA_PLUS);
        btnAdd.setFont(App.getCONTEXT().getSkin().getIconFont());
        JButton btnDel = new JButton(FontAwesomeContants.FA_MINUS);
        btnDel.setFont(App.getCONTEXT().getSkin().getIconFont());
        JButton btnEdit = new JButton(FontAwesomeContants.FA_PENCIL);
        btnEdit.setFont(App.getCONTEXT().getSkin().getIconFont());

        btnAdd.addActionListener(e -> {
            PortForwardingRule ent = addOrEditEntry(null);
            if (ent != null) {
                model.addRule(ent);
                updatePFRules();
            }
        });

        btnEdit.addActionListener(e -> {
            int index = table.getSelectedRow();
            if (index != -1) {
                PortForwardingRule ent = model.get(index);
                if (addOrEditEntry(ent) != null) {
                    model.refreshTable();
                    updatePFRules();
                }
            }
        });

        btnDel.addActionListener(e -> {
            int index = table.getSelectedRow();
            if (index != -1) {
                model.remove(index);
                updatePFRules();
            }
        });

        b1.add(btnAdd);
        b1.add(Box.createVerticalStrut(10));

        b1.add(btnEdit);
        b1.add(Box.createVerticalStrut(10));

        b1.add(btnDel);
        b1.add(Box.createVerticalStrut(10));

        this.add(lblTitle, BorderLayout.NORTH);
        this.add(scrollPane);
        this.add(b1, BorderLayout.EAST);
    }

    private void updatePFRules() {
        this.info.setPortForwardingRules(model.getRules());
    }

    public void setInfo(SessionInfo info) {
        this.info = info;
        model.setRules(this.info.getPortForwardingRules());
    }

    private PortForwardingRule addOrEditEntry(PortForwardingRule r) {
        JComboBox<String> cmbPFType = new JComboBox<>(new String[]{App.getCONTEXT().getBundle().getString("local"), App.getCONTEXT().getBundle().getString("remote")});

        JTextField txtHost = new SkinnedTextField(30);

        JSpinner spSourcePort = new JSpinner(new SpinnerNumberModel(0, 0, SessionInfoPanel.DEFAULT_MAX_PORT, 1));
        JSpinner.NumberEditor editorSource = new JSpinner.NumberEditor(spSourcePort, "#");
        spSourcePort.setEditor(editorSource);
        JSpinner spTargetPort = new JSpinner(new SpinnerNumberModel(0, 0, SessionInfoPanel.DEFAULT_MAX_PORT, 1));
        JSpinner.NumberEditor editorTarget = new JSpinner.NumberEditor(spTargetPort, "#");
        spTargetPort.setEditor(editorTarget);

        JTextField txtBindAddress = new SkinnedTextField(30);
        txtBindAddress.setText("127.0.0.1");

        if (r != null) {
            txtHost.setText(r.getHost());
            spSourcePort.setValue(r.getSourcePort());
            spTargetPort.setValue(r.getTargetPort());
            txtBindAddress.setText(r.getBindHost());
            cmbPFType.setSelectedIndex(r.getType() == PortForwardingType.LOCAL ? 0 : 1);
        }

        while (OptionPaneUtils.showOptionDialog(this,
                                                new Object[]{"Port forwarding type", cmbPFType, "Host", txtHost, "Source Port", spSourcePort,
                                                             "Target Port", spTargetPort, "Bind Address", txtBindAddress},
                                                "Port forwarding rule") == JOptionPane.OK_OPTION) {

            String host = txtHost.getText();
            int port1 = (Integer) spSourcePort.getValue();
            int port2 = (Integer) spTargetPort.getValue();
            String bindAddress = txtBindAddress.getText();

            if (host.isEmpty() || bindAddress.isEmpty() || port1 <= 0 || port2 <= 0) {
                JOptionPane.showMessageDialog(this, App.getCONTEXT().getBundle().getString("invalid_input"));
                continue;
            }

            if (r == null) {
                r = new PortForwardingRule();
            }
            r.setType(cmbPFType.getSelectedIndex() == 0 ? PortForwardingType.LOCAL : PortForwardingType.REMOTE);
            r.setHost(host);
            r.setBindHost(bindAddress);
            r.setSourcePort(port1);
            r.setTargetPort(port2);
            return r;
        }
        return null;
    }

    private static class PFTableModel extends AbstractTableModel {

        private final String[] columns = {App.getCONTEXT().getBundle().getString("type"), App.getCONTEXT().getBundle().getString("host"), App.getCONTEXT().getBundle().getString("source_port"), App.getCONTEXT()
                .getBundle().getString("target_port"), App.getCONTEXT().getBundle().getString("bind_host")};
        private final List<PortForwardingRule> list = new ArrayList<>();

        @Override
        public int getRowCount() {
            return list.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PortForwardingRule pf = list.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return pf.getType();
                case 1:
                    return pf.getHost();
                case 2:
                    return pf.getSourcePort();
                case 3:
                    return pf.getTargetPort();
                case 4:
                    return pf.getBindHost();
            }
            return "";
        }

        private List<PortForwardingRule> getRules() {
            return list;
        }

        private void setRules(List<PortForwardingRule> rules) {
            list.clear();
            if (rules != null) {
                list.addAll(rules);
            }
            fireTableDataChanged();
        }

        private void addRule(PortForwardingRule r) {
            this.list.add(r);
            fireTableDataChanged();
        }

        private void refreshTable() {
            fireTableDataChanged();
        }

        private void remove(int index) {
            list.remove(index);
            fireTableDataChanged();
        }

        private PortForwardingRule get(int index) {
            return list.get(index);
        }
    }
}
