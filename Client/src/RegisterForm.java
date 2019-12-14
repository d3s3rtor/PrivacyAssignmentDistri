import javax.swing.*;
import java.awt.*;
import java.io.File;

public class RegisterForm extends JFrame {
    private JTextField usernameTextField;
    private boolean checked;
    private JButton submitButton;
    private JPanel registerPanel;
    private JPasswordField passwordField1;
    private JCheckBox showPasswordCheckBoxRegister;
    private JButton dismissButton;
    private String userName;
    private char[] passWord;
    private File[] listOfFiles;
    private boolean fileAlreadyDefined;

    public void CenteredFrame(JFrame objFrame) {
        Dimension objDimension = Toolkit.getDefaultToolkit().getScreenSize();
        int iCoordX = (objDimension.width - objFrame.getWidth()) / 2;
        int iCoordY = (objDimension.height - objFrame.getHeight()) / 2;
        objFrame.setLocation(iCoordX, iCoordY);
        objFrame.setResizable(false);
    }

    public void getFiles() {
        File folder = new File(".");
        listOfFiles = folder.listFiles();
    }

    public RegisterForm() {
        add(registerPanel);
        setTitle("RegisterForm");
        setSize(400, 200);
        CenteredFrame(this);
        submitButton.addActionListener(e -> {
            userName = usernameTextField.getText();
            passWord = passwordField1.getPassword();
            String pwdStr = "";
            for (int i = 0; i < passWord.length; i++) {
                pwdStr = pwdStr + passwordField1.getPassword()[i];
            }
            Secure.setUsername(userName);
            Secure.setPassword(pwdStr);
            String filename = Secure.createHashedFilename();
            getFiles();
            try {
                fileAlreadyDefined = false;
                for (int i = 0; i < listOfFiles.length; i++) {
                    if (listOfFiles[i].getName().equalsIgnoreCase(filename)) {
                        //print dat deze userpassword combi is already defined.
                        JOptionPane.showMessageDialog(registerPanel,
                                "UserpasswordCombi already in use. Please choose another password and or username");
                        fileAlreadyDefined = true;
                        break;
                    }
                }
                if (fileAlreadyDefined == false) {

                    Data.writeDataToDisk(new Client(userName));

                    JOptionPane.showMessageDialog(registerPanel,
                            "Succesfully registered!");
                    dispose();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(registerPanel, ex);
            }
        });

        dismissButton.addActionListener(e -> {
            dispose();
        });
        showPasswordCheckBoxRegister.addActionListener(e -> {
            checked = showPasswordCheckBoxRegister.isSelected();
            if (checked) {
                passwordField1.setEchoChar((char) 0);
            } else {
                passwordField1.setEchoChar((char) '*');
            }
        });
    }
}
