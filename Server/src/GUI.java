import javax.swing.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

public class GUI {
    private JTable table;
    private JPanel mainPanel;
    private JButton resetButton;

    public JTable getTable() {
        return table;
    }

    public JButton getResetButton() {
        return resetButton;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public GUI() {
    }

    public static void main(String[] args) {
        GUI ui = new GUI();
        ui.startServer();

    }

    public void startServer() {
        try {
            String[] config = Data.readConfig();
            System.setProperty("java.rmi.server.hostname", config[0]);
            System.out.println(Arrays.toString(config));
            Registry registry = LocateRegistry.createRegistry(Integer.parseInt(config[1]));
            registry.rebind(config[2], new ServerImplementation());
            System.out.println("Server started on port: " + config[1] + ", service: " + config[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
