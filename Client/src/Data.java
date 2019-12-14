import javax.crypto.*;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class Data {

    public static String[] readConfig() {
        Properties prop = new Properties();
        String[] settings = null;
        try {
            settings = new String[5];
            prop.load(new FileInputStream("server_config.properties"));
            settings[0] = prop.getProperty("ip");
            settings[1] = prop.getProperty("port");
            settings[2] = prop.getProperty("service_name");
            settings[3] = prop.getProperty("max_cells");
            settings[4] = prop.getProperty("tag_size");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return settings;

    }

    public static void writeDataToDisk(Client client) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, Secure.getSecretKey());

            SealedObject sealedObject = new SealedObject(client, cipher);
            CipherOutputStream cipherOutputStream = new CipherOutputStream(new BufferedOutputStream(new FileOutputStream(Secure.createHashedFilename())), cipher);
            ObjectOutputStream outputStream = new ObjectOutputStream(cipherOutputStream);
            outputStream.writeObject(sealedObject);
            outputStream.close();

        } catch (IOException ex) {
            System.out.println("IOException is caught");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public static Client readDataFromDisk() {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, Secure.getSecretKey());
            CipherInputStream cipherInputStream = new CipherInputStream(new BufferedInputStream(new FileInputStream(Secure.createHashedFilename())), cipher);
            ObjectInputStream inputStream = new ObjectInputStream(cipherInputStream);
            SealedObject sealedObject = (SealedObject) inputStream.readObject();
            return (Client) sealedObject.getObject(cipher);

        } catch (IOException ex) {
            System.out.println("IOException is caught");
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFoundException is caught");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
