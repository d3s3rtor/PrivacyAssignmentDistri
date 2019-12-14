import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.File;

public class Login extends JFrame {
    private JTextField usernameTextField;
    private JPasswordField passwordPasswordField;
    private JCheckBox showPasswordCheckBox;
    private JPanel rootPanel;
    private JButton loginButton;
    private JButton registerButton;
    private File[] listOfFiles;
    private boolean username, password;
    private boolean checked;

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

    public Login() {
        add(rootPanel);
        setTitle("Login");
        setSize(500, 400);
        CenteredFrame(this);


        loginButton.addActionListener(e -> {
            String userName = usernameTextField.getText();
            char[] passWord = passwordPasswordField.getPassword();

            String pwdStr = "";
            for (int i = 0; i < passWord.length; i++) {
                pwdStr = pwdStr + passwordPasswordField.getPassword()[i];
            }

            Secure.setPassword(pwdStr);
            Secure.setUsername(userName);

            String filename = Secure.createHashedFilename();
            getFiles();

            String input = "";
            username = password = false;
            for (int i = 0; i < listOfFiles.length; i++) {
                //check if username == one of the file names in directory
                System.out.println(listOfFiles[i].getName());
                if (listOfFiles[i].getName().equalsIgnoreCase(filename)) {
                    username = true;
                    Client client = Data.readDataFromDisk();

                    if (client != null) {
                        ChatsOverview chats = new ChatsOverview();
                        chats.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                        chats.setVisible(true);
                        password = true;
                        dispose();
                        break;

                    }
                }
            }
            if (username == false) {
                JOptionPane.showMessageDialog(rootPanel,
                        userName + " was not found. Please register first!");
                //TODO: open registery frame
            }
            if (password == false && username == true) {
                JOptionPane.showMessageDialog(rootPanel,
                        "Password incorrect. Please try again.");
            }
        });
        registerButton.addActionListener(e -> {
            RegisterForm registerForm = new RegisterForm();
            registerForm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            registerForm.setVisible(true);

        });
        showPasswordCheckBox.addActionListener((ActionListener) e -> {
            checked = showPasswordCheckBox.isSelected();
            if (checked) {
                passwordPasswordField.setEchoChar((char) 0);
            } else {
                //TODO: make cirkels instead of asterix
                passwordPasswordField.setEchoChar((char) '*');
            }
        });
    }
}
