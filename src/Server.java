import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Server extends Remote {
    void add(String idx, byte[] message, byte[] hash) throws RemoteException;
    byte[] get(String idx, String tag) throws RemoteException;
}
