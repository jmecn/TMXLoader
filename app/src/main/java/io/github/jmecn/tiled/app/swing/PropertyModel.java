package io.github.jmecn.tiled.app.swing;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * desc:
 *
 * @author yanmaoyuan
 */
public class PropertyModel extends DefaultTableModel {

    private String[] columnNames = {"Name", "Value"};
    private transient final List<PropertyPair> list;

    public PropertyModel() {
        list = new ArrayList<>();
    }

    @Override
    public int getRowCount() {
        if (list == null) {
            return 0;
        }
        return list.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else {
            return Object.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return false;
        } else {
            if (list == null) {
                return false;
            }
            return list.get(rowIndex).isEditable();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (list == null) {
            return null;
        }
        if (columnIndex == 0) {
            return list.get(rowIndex).getName();
        } else {
            return list.get(rowIndex).getValue();
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (list == null) {
            return;
        }
        if (columnIndex == 1) {
            PropertyPair pair = list.get(rowIndex);
            if (pair.isEditable()) {
                pair.setValue(aValue);
            }
        }
    }

    public void clear() {
        list.clear();
    }

    public void addProperty(PropertyPair pair) {
        list.add(pair);
    }
}
