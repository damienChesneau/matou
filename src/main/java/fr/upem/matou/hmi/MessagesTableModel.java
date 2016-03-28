package fr.upem.matou.hmi;

import fr.upem.matou.common.Message;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

/**
 * @author Damien Chesneau
 */
class MessagesTableModel extends AbstractTableModel {
    private static final String[] columns = new String[]{"Pseudo", "Message"};
    private final ArrayList<Message> messages = new ArrayList<>();

    @Override
    public int getRowCount() {
        return messages.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns[column];
    }

    @Override
    public Class getColumnClass(int col) {
        return String.class;
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return messages.get(rowIndex).getLogin();
            case 1:
                return messages.get(rowIndex).getMessage();
            default:
                throw new AssertionError();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    public void newMessage(Message message) {
        messages.add(message);
        fireTableDataChanged();
    }
}
