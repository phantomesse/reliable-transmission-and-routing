import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class RoutingTableUI extends JPanel {
    private class RoutingTableModel extends AbstractTableModel {
        private ArrayList<String[]> rows;
        private String[] header = {
                "Destination",
                "Cost",
                "Link",
                "Last Heard From"
        };

        public RoutingTableModel(Collection<Client> clients) {
            rows = new ArrayList<String[]>();
            update(clients);
        }

        public void update(Collection<Client> clients) {
            rows.clear();
            for (Client client : clients) {
                String[] row = {
                        client.getIpAddressPortNumberString(),
                        client.getCost() + "",
                        client.getLink().getIpAddressPortNumberString(),
                        RoutingTable.FORMAT.format(new Date(client
                                .getLastHeardFrom()))
                };
                rows.add(row);
            }
            fireTableDataChanged();
        }

        @Override
        public int getColumnCount() {
            return header.length;
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return rows.get(rowIndex)[columnIndex];
        }

        @Override
        public String getColumnName(int index) {
            return header[index];
        }
    }

    private JTable table;
    private RoutingTableModel model;

    public RoutingTableUI(RoutingTable routingTable) {
        super(new BorderLayout());
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY));

        model = new RoutingTableModel(routingTable.getClients());
        table = new JTable(model);
        table.setForeground(Color.DARK_GRAY);

        add(table.getTableHeader(), BorderLayout.PAGE_START);
        add(table, BorderLayout.CENTER);
    }

    public void update(RoutingTable routingTable) {
        model.update(routingTable.getClients());
    }
}
