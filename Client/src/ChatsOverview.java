import com.demo.OrchidDemo;
import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatsOverview extends JFrame implements onUpdate, WindowListener {
    private JPanel panel1;
    private JScrollPane conversationsScrollPane;
    private JButton addConversationButton;
    private JTable table;
    private JTextField chatInputTextField;
    private JButton sendButton;
    private JList messageList;
    private JButton removeConversationButton;
    private JFormattedTextField TorLabel;
    private JScrollPane chatScrollPane;
    private JButton reconnectTorButton;
    private Client client;
    private Conversation selectedConversation;
    private Timer receiveTimer;
    private onUpdate onUpdate;
    private Thread t;
    private TorClient torClient;


    public void CenteredFrame(JFrame objFrame) {
        Dimension objDimension = Toolkit.getDefaultToolkit().getScreenSize();
        int iCoordX = (objDimension.width - objFrame.getWidth()) / 2;
        int iCoordY = (objDimension.height - objFrame.getHeight()) / 2;
        objFrame.setLocation(iCoordX, iCoordY);
    }


    public TorInitializationListener createInitalizationListner() {
        return new TorInitializationListener() {
            @Override
            public void initializationProgress(String message, int percent) {
                TorLabel.setText("Tor status: [ " + percent + "% ]: " + message);
            }

            @Override
            public void initializationCompleted() {
                TorLabel.setText("Tor is ready to go!");
                t = new Thread(() -> {
                    testOrchidUsingSystemPropsProxy();
                });
                t.start();

            }
        };
    }


    public void testOrchidUsingSystemPropsProxy() {
        try {
            System.setProperty("socksProxyHost", "127.0.0.1");
            System.setProperty("socksProxyPort", "9150");
            Document document = Jsoup.connect("https://wtfismyip.com/").get();
            Elements select = document.select("div[id=main");

            String ip = select.get(0).child(1).text();
            String hostname = select.get(0).child(3).text();

            TorLabel.setText("ip: " + ip + " host: " + hostname);


        } catch (Exception ex) {
            Logger.getLogger(OrchidDemo.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public ChatsOverview() {
        try {

            torClient = new TorClient();
            torClient.addInitializationListener(createInitalizationListner());
            torClient.start();
            torClient.enableSocksListener(9150);
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.addWindowListener(this);
        this.client = Data.readDataFromDisk();
        this.receiveTimer = new Timer(1000, this::receiveMessage);
        receiveTimer.start();
        refreshChats();
        add(panel1);
        onUpdate = this;
        conversationsScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        conversationsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        conversationsScrollPane.setViewportBorder(new LineBorder(Color.BLACK));
        conversationsScrollPane.setBorder(new LineBorder(Color.WHITE));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize((int) screenSize.getWidth() / 2, (int) screenSize.getHeight() / 2);
        CenteredFrame(this);

        setTitle("Chats");
        addConversationButton.addActionListener(e -> {
            Bump nBump = new Bump(this.client, onUpdate);
            nBump.setDefaultCloseOperation(Bump.EXIT_ON_CLOSE);
            nBump.setVisible(true);

        });
        removeConversationButton.addActionListener(e -> {
            if (selectedConversation != null) {
                client.getConversations().remove(selectedConversation.getReceiver_name());
                onUpdateChats();
                messageList.setModel(new DefaultListModel());
            }
        });

        reconnectTorButton.addActionListener(e -> {
            try {
                t.interrupt();
                System.setProperty("socksProxyHost", "");
                System.setProperty("socksProxyPort", "");
                torClient.stop();
                torClient.removeInitializationListener(createInitalizationListner());
                torClient = new TorClient();
                torClient.addInitializationListener(createInitalizationListner());
                torClient.enableSocksListener(9150);
                torClient.start();
                torClient.getCircuitManager().startBuildingCircuits();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
        table.getSelectionModel().addListSelectionListener(event -> {
            if (table.getSelectedRow() > -1) {
                String username = table.getValueAt(table.getSelectedRow(), 0).toString();
                selectedConversation = this.client.getConversations().getOrDefault(username, null);
                sendButton.setEnabled(true);
                removeConversationButton.setEnabled(true);
                updateMessages(selectedConversation);
            } else {
                sendButton.setEnabled(false);
                removeConversationButton.setEnabled(false);
            }
        });


        sendButton.addActionListener(e -> {
            if (selectedConversation != null) {
                selectedConversation.setServer(this.client.connectToServer());
                selectedConversation.send(client.getName() + ": " + chatInputTextField.getText(), onUpdate);
                chatInputTextField.setText("");
            }
        });
    }

    public void receiveMessage(ActionEvent e) {
        if (selectedConversation != null) {
            selectedConversation.setServer(client.connectToServer());
            selectedConversation.receive(onUpdate);
        }
    }

    private void updateMessages(Conversation conversation) {
        if (conversation != null) {
            DefaultListModel model = new DefaultListModel();
            for (String message : conversation.getMessages()) {
                model.addElement(message);
            }
            messageList.setModel(model);
        }
    }

    public void refreshChats() {
        Map<String, Conversation> conversations = client.getConversations();
        table.setFont(new Font("Corbel", Font.BOLD, 16));
        Object[] columns = {"Conversations"};
        DefaultTableModel model = new DefaultTableModel();

        model.setColumnIdentifiers(columns);
        table.setModel(model);

        Object[] row = new Object[1];

        for (Map.Entry<String, Conversation> entry : conversations.entrySet()) {
            row[0] = entry.getKey();
            model.addRow(row);
        }

        table.setModel(model);
        table.repaint();
        panel1.updateUI();
    }


    @Override
    public void onUpdateChats() {
        refreshChats();
        Data.writeDataToDisk(client);
    }

    @Override
    public void onUpdateMessages() {
        updateMessages(selectedConversation);
        Data.writeDataToDisk(client);

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        Data.writeDataToDisk(client);
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}


