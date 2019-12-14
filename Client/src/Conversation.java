
import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.LinkedList;

public class Conversation implements Serializable {
    private String idx_send, idx_rec, tag_send, tag_rec, receiver_name;
    private byte[] salt;
    private SecretKey key_to, key_from;
    private Server server;
    private LinkedList<String> messages;


    public Conversation(String idx_send, String idx_rec, String tag_send, String tag_rec, String receiver_name, byte[] salt, SecretKey key_to, SecretKey key_from, Server server) {
        this.idx_send = idx_send;
        this.idx_rec = idx_rec;
        this.tag_send = tag_send;
        this.tag_rec = tag_rec;
        this.receiver_name = receiver_name;
        this.salt = salt;
        this.key_to = key_to;
        this.key_from = key_from;
        this.server = server;
        this.messages = new LinkedList<>();
    }

    public String getReceiver_name() {
        return receiver_name;
    }

    public LinkedList<String> getMessages() {
        return messages;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    private byte[] encrypt(String message) {
        //encrypt message with key_to, derive new key after encrypting
        try {
            String[] config = Data.readConfig();
            Cipher cipher = Cipher.getInstance(Constants.ENCRYPT_ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key_to);

            //generate new tag, key and id
            SecureRandom secureRandom = new SecureRandom();
            SecretKey key = deriveKey(key_to);
            idx_send = Integer.toString(secureRandom.nextInt(Integer.parseInt(config[3])));
            byte[] tag_bin = new byte[Integer.parseInt(config[4])];
            secureRandom.nextBytes(tag_bin);
            tag_send = Base64.getEncoder().encodeToString(tag_bin);

            StringBuilder stringBuilder = new StringBuilder(message)
                    .append(Constants.DELIMITER)
                    .append(idx_send)
                    .append(Constants.DELIMITER)
                    .append(tag_send);

            byte[] cipher_text = cipher.doFinal(stringBuilder.toString().getBytes());

            key_to = key;


            return cipher_text;
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

    private String decrypt(byte[] cipher_text) {
        //decrypt message, derive new key after decrypting
        try {
            Cipher cipher = Cipher.getInstance(Constants.ENCRYPT_ALG);
            cipher.init(Cipher.DECRYPT_MODE, key_from);
            String decrypted = new String(cipher.doFinal(cipher_text));
            SecretKey key = deriveKey(key_from);
            if (key != null) {
                key_from = key;
            }
            return decrypted;
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


    public void send(String message, onUpdate onUpdate) {
        String send_idx = idx_send;
        String send_tag = tag_send;
        //add time to message
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String messageWithTime = formatter.format(new Date(System.currentTimeMillis())) + " " + message;
        byte[] encrypted = encrypt(messageWithTime);

        if(server != null){
            MessageDigest message_digest = null;
            try {
                message_digest = MessageDigest.getInstance(Constants.HASH_ALG);
                byte[] hashed_tag = message_digest.digest(send_tag.getBytes());
                server.add(send_idx, encrypted, hashed_tag);
                messages.add(messageWithTime);
                onUpdate.onUpdateMessages();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void receive(onUpdate onUpdate) {
        try {
            byte[] encrypted = server.get(idx_rec, tag_rec);

            while(encrypted != null) {
                String decrypted = decrypt(encrypted);
                if (decrypted != null) {
                    String[] message = decrypted.split(Constants.DELIMITER);
                    idx_rec = message[1];
                    tag_rec = message[2];
                    messages.add(message[0]);
                    onUpdate.onUpdateMessages();
                    encrypted = server.get(idx_rec, tag_rec);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private String convertKeyToString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    private SecretKey deriveKey(SecretKey key) {
        //omzetten van key naar Base64 encoding om vervolgens passwd based key derivation te gebruiken
        //mbv afgesproken salt, nieuwe key maken
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(Constants.KDF_ALG);
            KeySpec spec = new PBEKeySpec(convertKeyToString(key).toCharArray(), salt, Constants.ITERATION_COUNT, Constants.KEY_SIZE);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), Constants.ENCRYPT_ALG);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;

    }
}
