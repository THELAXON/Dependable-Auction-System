import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public interface Server extends Auction{
    public void livereplicaUpdate() throws RemoteException;
    public void replicaBackup(Map<Integer, AuctionItem> AuctionItems, Map<Integer, String> UserEmails, Map<Integer, PublicKey> UserKeyPairs, Map<Integer, Integer> HighestBidder, Map<Integer, Integer> AuctionCreators) throws RemoteException;
}