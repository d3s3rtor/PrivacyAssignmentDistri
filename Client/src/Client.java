import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

public class Client implements Serializable {
    private String name;
    private Map<String, Conversation> conversations;
    private Server server;
    private String[] server_config;

    public Client(String name) {
        this.name = name;
        this.conversations = new TreeMap();
        new Thread(() -> {
            startServer();
        }).start();
    }

    public Server startServer() {
        try {
            server_config = Data.readConfig();
            System.out.println(Arrays.toString(server_config));
            Registry myRegistry = LocateRegistry.getRegistry(server_config[0], Integer.parseInt(server_config[1]));
            server = ((Server) myRegistry.lookup(server_config[2]));
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
        return server;
    }

    public Map<String, Conversation> getConversations() {
        return conversations;
    }


    public void addConversation(String idx_send, String idx_rec, String tag_send, String tag_rec, String receiver_name, byte[] salt, SecretKey key_to, SecretKey key_from) {
        Conversation conv = new Conversation(idx_send, idx_rec, tag_send, tag_rec, receiver_name, salt, key_to, key_from, server);
        conversations.put(receiver_name, conv);
    }

    public String getName() {
        return name;
    }

    public Map<String, String> readSecrets(String filename) {
        Map<String, String> secrets = new HashMap<>();
        try {
            Reader reader = new FileReader(filename);
            BufferedReader in = new BufferedReader(reader);
            String s = in.readLine();
            System.out.println("read: " + s);
            in.close();
            reader.close();
            return secretsStringToMap(s);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Map<String, String> secretsStringToMap(String s) {
        Map<String, String> secrets = new HashMap<>();
        String[] secret = s.split(Constants.DELIMITER);
        secrets.put("key_to", secret[0]);
        secrets.put("key_from", secret[1]);
        secrets.put("salt", secret[2]);
        secrets.put("idx_send", secret[3]);
        secrets.put("idx_rec", secret[4]);
        secrets.put("tag_send", secret[5]);
        secrets.put("tag_rec", secret[6]);

        return secrets;
    }

    private String generateSecrets() {
        KeyGenerator keyGenerator = null;
        StringBuilder stringBuilder = null;
        try {
            if(server_config == null) server_config = Data.readConfig();
            //generate tags, keys, id and salt
            keyGenerator = KeyGenerator.getInstance("AES");

            keyGenerator.init(Constants.KEY_SIZE);
            SecretKey key_ab = keyGenerator.generateKey();
            SecretKey key_ba = keyGenerator.generateKey();
            String key_to = "", key_from = "", salt_string = "", tag_send = "", idx_send = "", tag_rec = "", idx_rec;

            if ((key_ab != null) && (key_ba != null)) {
                key_to = Base64.getEncoder().encodeToString(key_ab.getEncoded());
                key_from = Base64.getEncoder().encodeToString(key_ba.getEncoded());
            }

            SecureRandom secureRandom = new SecureRandom();
            byte[] salt = new byte[Constants.KEY_SIZE];
            secureRandom.nextBytes(salt);
            salt_string = Base64.getEncoder().encodeToString(salt);

            idx_send = Integer.toString(secureRandom.nextInt(Integer.parseInt(server_config[3])));
            idx_rec = Integer.toString(secureRandom.nextInt(Integer.parseInt(server_config[3])));

            byte[] tag_bin = new byte[Integer.parseInt(server_config[4])];
            secureRandom.nextBytes(tag_bin);
            tag_send = Base64.getEncoder().encodeToString(tag_bin);
            secureRandom.nextBytes(tag_bin);
            tag_rec = Base64.getEncoder().encodeToString(tag_bin);


            stringBuilder = new StringBuilder(key_to)
                    .append(Constants.DELIMITER)
                    .append(key_from)
                    .append(Constants.DELIMITER)
                    .append(salt_string)
                    .append(Constants.DELIMITER)
                    .append(idx_send)
                    .append(Constants.DELIMITER)
                    .append(idx_rec)
                    .append(Constants.DELIMITER)
                    .append(tag_send)
                    .append(Constants.DELIMITER)
                    .append(tag_rec);


        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public Map<String, String> writeSecrets(String filename) {
        Map<String, String> secrets = new HashMap<>();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
            Date date = new Date(System.currentTimeMillis());
            //Writer writer = new FileWriter(formatter.format(date));

            //testing purpose
            Writer writer = new FileWriter(filename);

            String s = generateSecrets();
            secrets = secretsStringToMap(s);
            System.out.println("written: " + s);
            writer.write(s);
            writer.close();
            return secrets;

        } catch (IOException e) {

        }
        return null;
    }

    public SecretKey createKeyFromString(String secret) {
        byte[] decodedKey = Base64.getDecoder().decode(secret);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

    }

}
