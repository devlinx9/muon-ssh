package muon.app.ui.components.session.utilpage.portview;

import muon.app.App;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;



public class SocketTableModel extends AbstractTableModel {
    private final String[] columns = {App.getCONTEXT().getBundle().getString("processes"), App.getCONTEXT().getBundle().getString("pid"), App.getCONTEXT().getBundle().getString("host"), App.getCONTEXT().getBundle().getString("port")};
    private final List<SocketEntry> list = new ArrayList<>();

    public void addEntry(SocketEntry e) {
        list.add(e);
        fireTableDataChanged();
    }

    public void addEntries(List<SocketEntry> entries) {
        if (entries != null) {
            list.addAll(entries);
            fireTableDataChanged();
        }
    }

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
        SocketEntry e = list.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return e.getApp();
            case 1:
                return e.getPid();
            case 2:
                return e.getHost();
            case 3:
                return e.getPort();
            default:
                return "";
        }
    }

    public void clear() {
        list.clear();
    }
}