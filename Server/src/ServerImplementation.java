import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.util.List;
import java.util.*;


public class ServerImplementation extends UnicastRemoteObject implements Server {
    private List<Map<String, byte[]>> boxes;
    private int MAX_CELLS;
    private GUI ui;
    private DefaultTableModel model;

    protected ServerImplementation() throws RemoteException {
        JFrame frame = new JFrame("Controlpanel");
        ui = new GUI();
        MAX_CELLS = Integer.parseInt(Data.readConfig()[3]);
        JButton resetButton = ui.getResetButton();
        resetButton.addActionListener(actionEvent -> {
            Data.resetBoxes();
            resetBoxes();
            updateTable();
        });
        frame.setContentPane(ui.getMainPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                Data.saveBoxes(boxes);
            }
        });
        frame.pack();
        frame.setVisible(true);

        List<Map<String, byte[]>> loadBoxes = Data.loadBoxes();
        if (loadBoxes != null) {
            boxes = loadBoxes;
        } else {
            resetBoxes();
        }

        ui.getTable().setFont(new Font("Corbel", Font.BOLD, 14));
        Object[] columns = {"ID", "Hash - message"};
        model = new DefaultTableModel();
        model.setColumnIdentifiers(columns);
        ui.getTable().setModel(model);

        updateTable();

    }

    private void resetBoxes() {
        boxes = new ArrayList<>();
        for (int i = 0; i < MAX_CELLS; i++) {
            Map<String, byte[]> map = new HashMap<>();
            boxes.add(i, map);
        }
    }

    @Override
    public void add(String idx, byte[] message, byte[] hash) {
        System.out.println("Add: " + idx + " " + message + " " + Base64.getEncoder().encodeToString(hash));
        boxes.get(Integer.parseInt(idx)).put(Base64.getEncoder().encodeToString(hash), message);
        printBoxes();
        updateTable();
    }

    @Override
    public byte[] get(String idx, String tag) {
        System.out.println("Get: " + idx + " " + tag);

        MessageDigest message_digest = null;
        try {
            message_digest = MessageDigest.getInstance(Constants.HASH_ALG);
            String hashed_tag = Base64.getEncoder().encodeToString(message_digest.digest(tag.getBytes()));

            if (boxes.get(Integer.parseInt(idx)).containsKey(hashed_tag)) {
                System.out.println("Contains key: " + hashed_tag);
                Map<String, byte[]> entry = boxes.get(Integer.parseInt(idx));
                byte[] encrypted = entry.get(hashed_tag);
                entry.remove(hashed_tag);
                updateTable();
                return encrypted;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void removeRows() {
        Object[] columns = {"ID", "Hash - message"};
        model = new DefaultTableModel();
        model.setColumnIdentifiers(columns);
        ui.getTable().setModel(model);
    }

    private void updateTable() {
        removeRows();
        Object[] row = new Object[2];

        for (int i = 0; i < boxes.size(); i++) {
            for (Map.Entry<String, byte[]> entry : boxes.get(i).entrySet()) {
                StringBuilder string_builder = new StringBuilder();
                string_builder.append("Hash: ")
                        .append(entry.getKey());
                row[0] = i;
                row[1] = string_builder.toString();
                model.addRow(row);
            }

        }
    }

    private void printBoxes() {
        for (int i = 0; i < boxes.size(); i++) {
            for (Map.Entry<String, byte[]> entry : boxes.get(i).entrySet()) {
                System.out.println("key: " + (entry.getKey()) + " value: " + Base64.getEncoder().encodeToString(entry.getValue()));
            }

        }
    }

}
