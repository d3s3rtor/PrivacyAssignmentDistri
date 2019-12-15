import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Properties;

 class Data {

     static String[] readConfig() {
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

     static void writeDataToDisk(Client client) {
        try {
            Cipher cipher = Cipher.getInstance(Constants.ENCRYPT_ALG);
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

     static Client readDataFromDisk() {
        try {
            Cipher cipher = Cipher.getInstance(Constants.ENCRYPT_ALG);
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

     static void writeBumpFile(String filename, String password, String data) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        SecretKeySpec secret = generateSecretFromPassword(password);
        Writer writer = new FileWriter(filename);
        Cipher cipher = Cipher.getInstance(Constants.ENCRYPT_ALG);
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipher_text = cipher.doFinal(data.getBytes());
        writer.write(Base64.getEncoder().encodeToString(cipher_text));
        writer.close();


    }

     static String readBumpFile(String filename, String password) throws IOException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        byte[] decode = Base64.getDecoder().decode(in.readLine());
        SecretKeySpec secret = generateSecretFromPassword(password);
        Cipher cipher = Cipher.getInstance(Constants.ENCRYPT_ALG);
        cipher.init(Cipher.DECRYPT_MODE, secret);
        in.close();
        File file = new File(filename);
        file.delete();
        return new String(cipher.doFinal(decode));
    }

    private static SecretKeySpec generateSecretFromPassword(String password) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), new byte[16], Constants.ITERATION_COUNT, Constants.KEY_SIZE);
            SecretKey key = SecretKeyFactory.getInstance(Constants.KDF_ALG).generateSecret(spec);
            return new SecretKeySpec(key.getEncoded(), Constants.ENCRYPT_ALG);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
