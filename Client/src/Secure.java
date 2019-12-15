import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

 class Secure {
    private static String username;
    private static String password;

     static String getUsername(){
        return username;
    }
    static void setUsername(String uname) {
        username = uname;
    }

     static void setPassword(String passwd) {
        password = passwd;
    }

     static SecretKeySpec getSecretKey() {
        try {
            byte[] salt = new byte[16];
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, Constants.ITERATION_COUNT, Constants.KEY_SIZE);
            SecretKeyFactory f = SecretKeyFactory.getInstance(Constants.KDF_ALG);
            SecretKey key = f.generateSecret(spec);
            SecretKeySpec keySpec = new SecretKeySpec(key.getEncoded(), Constants.ENCRYPT_ALG);
            return keySpec;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
     static String createHashedFilename() {
        try {
            if (username != null) {
                MessageDigest message_digest = MessageDigest.getInstance(Constants.HASH_ALG);
                byte[] hashedUsername = message_digest.digest(username.getBytes());
                String filename = Base64.getEncoder().encodeToString(hashedUsername);
                String s = filename.replaceAll("/", "_");
                return s + ".ser";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
