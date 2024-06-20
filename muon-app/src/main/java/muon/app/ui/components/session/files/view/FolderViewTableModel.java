package muon.app.ui.components.session.files.view;

import muon.app.common.FileInfo;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

import static muon.app.App.bundle;

public class FolderViewTableModel extends AbstractTableModel implements ListModel<FileInfo> {

    private static final long serialVersionUID = 7212506492710233442L;
    private final List<FileInfo> files = new ArrayList<>();
    private final String[] columns = {bundle.getString("name"), bundle.getString("modified"), bundle.getString("size"), bundle.getString("type"), bundle.getString("permission"), bundle.getString("owner")};
    protected EventListenerList listenerList = new EventListenerList();
    private boolean local = false;

    public FolderViewTableModel(boolean local) {
        this.local = local;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return FileInfo.class;
    }

    public void clear() {
        int rows = files.size();
        files.clear();
        fireTableDataChanged();
        fireContentsChanged(this, 0, rows - 1);
    }

    public void addAll(List<FileInfo> list) {
        if (list.size() > 0) {
            int sz = files.size();
            files.addAll(list);
            fireTableDataChanged();
            fireContentsChanged(this, 0, sz - 1);
        }

    }

    public FileInfo getItemAt(int index) {
        return files.get(index);
    }

    public void add(FileInfo ent) {
        int sz = files.size();
        files.add(ent);
        fireTableRowsInserted(sz, sz);
        fireContentsChanged(this, 0, sz - 1);
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    public int getRowCount() {
        return files.size();
    }

    public int getColumnCount() {
        return local ? 4 : columns.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        return files.get(rowIndex);
    }

    @Override
    public int getSize() {
        return files.size();
    }

    @Override
    public FileInfo getElementAt(int index) {
        return files.get(index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        System.out.println("addListDataListener");
        listenerList.add(ListDataListener.class, l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        listenerList.remove(ListDataListener.class, l);
    }

    public ListDataListener[] getListDataListeners() {
        return listenerList.getListeners(ListDataListener.class);
    }

    protected void fireContentsChanged(Object source, int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.CONTENTS_CHANGED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).contentsChanged(e);
            }
        }
    }

    protected void fireIntervalAdded(Object source, int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.INTERVAL_ADDED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).intervalAdded(e);
            }
        }
    }

    protected void fireIntervalRemoved(Object source, int index0, int index1) {
        Object[] listeners = listenerList.getListenerList();
        ListDataEvent e = null;

        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ListDataListener.class) {
                if (e == null) {
                    e = new ListDataEvent(source, ListDataEvent.INTERVAL_REMOVED, index0, index1);
                }
                ((ListDataListener) listeners[i + 1]).intervalRemoved(e);
            }
        }
    }

}
