import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.Serializable;
import java.util.Base64;
import java.util.Map;

public class Bump extends JFrame implements Serializable {
    private JButton generatBUMP;
    private JButton readBUMP;
    private JPanel panel;
    private JButton cancelBumpButton;
    private onUpdate onUpdate;

    public String getReceiverName() {
        String receiverName = JOptionPane.showInputDialog(panel, "What's the other person's name?");
        if(receiverName == null){
            JOptionPane.showMessageDialog(panel,"Please enter a valid name.");
            getReceiverName();
        }
        else{
            if(receiverName.length()<3){
                JOptionPane.showMessageDialog(panel,"Please enter a valid name. Longer then 3 characters.");
                getReceiverName();
            }
        }
        return receiverName;
    }

    public void CenteredFrame(JFrame objFrame) {
        Dimension objDimension = Toolkit.getDefaultToolkit().getScreenSize();
        int iCoordX = (objDimension.width - objFrame.getWidth()) / 2;
        int iCoordY = (objDimension.height - objFrame.getHeight()) / 2;
        objFrame.setLocation(iCoordX, iCoordY);
        objFrame.setResizable(false);
    }

    public Bump(Client client, onUpdate onUpdate) {
        add(panel);
        this.onUpdate = onUpdate;
        setTitle("Bump");
        setSize(500, 200);
        CenteredFrame(this);
        generatBUMP.addActionListener(e -> {
            String filename = "", receiverName = "";
            Map<String, String> secrets = null;
            final JFileChooser fc = new JFileChooser();
            fc.addChoosableFileFilter(new FileNameExtensionFilter("Bump-files", "bump"));
            int response = fc.showOpenDialog(panel);
            if (response == JFileChooser.APPROVE_OPTION) {
                filename = fc.getSelectedFile().toString();
                secrets = client.writeSecrets(filename);
            } else {
                JOptionPane.showMessageDialog(panel, "Something went wrong please try again.");
            }


            client.addConversation(secrets.get("idx_send"), secrets.get("idx_rec"), secrets.get("tag_send"), secrets.get("tag_rec"), getReceiverName(), Base64.getDecoder().decode(secrets.get("salt"))
                    , client.createKeyFromString(secrets.get("key_to"))
                    , client.createKeyFromString(secrets.get("key_from")));
            //TODO: save conversation in client file


            onUpdate.onUpdateChats();
            dispose();

        });
        cancelBumpButton.addActionListener(e ->{
            dispose();
        });
        readBUMP.addActionListener(e -> {
            String filename = "";
            Map<String, String> secrets = null;
            final JFileChooser fc = new JFileChooser();
            fc.addChoosableFileFilter(new FileNameExtensionFilter("Bump files", "bump"));
            int response = fc.showOpenDialog(panel);
            if (response == JFileChooser.APPROVE_OPTION) {
                filename = fc.getSelectedFile().toString();
                secrets = client.readSecrets(filename);

            } else {
                JOptionPane.showMessageDialog(panel, "Something went wrong please try again.");
            }
            client.addConversation(secrets.get("idx_rec"), secrets.get("idx_send"), secrets.get("tag_rec"), secrets.get("tag_send"), getReceiverName(),
                    Base64.getDecoder().decode(secrets.get("salt"))
                    , client.createKeyFromString(secrets.get("key_from"))
                    , client.createKeyFromString(secrets.get("key_to")));

            onUpdate.onUpdateChats();
            dispose();

        });
    }

}
