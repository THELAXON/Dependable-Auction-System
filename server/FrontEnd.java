import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;

public class FrontEnd implements Auction { //Implements Auction to communicate Replica
    public static Auction primaryReplica;
    public static String primaryReplicaName;
    public static Registry primaryRegistry;

    

    private static void NewReplicaPrimary(){
        try {
            primaryReplica = null;
            primaryRegistry = LocateRegistry.getRegistry("localhost");
            String[] registryList =  primaryRegistry.list();
            for(String replicalist : registryList)
            {
                if(replicalist.startsWith("Replica"))  // All replicas begin with the name "Replica"
                {
                    try {   
                        primaryReplica = (Auction) primaryRegistry.lookup(replicalist); 
                        primaryReplicaName = replicalist;
                        System.out.println("Connected to " + replicalist);
                        break;
                    } catch (Exception e) {
                        System.out.println(replicalist + " Failed");
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Integer register(String email, PublicKey pubKey) throws RemoteException {
        // Forward the request for registering to the primary replica
        try {
            return primaryReplica.register(email, pubKey);
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.register(email, pubKey);
        }
    }

    @Override
    public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException {
        // Forward the request for challenge to the primary replica
        try {
            return primaryReplica.challenge(userID, clientChallenge);
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.challenge(userID, clientChallenge);
        }
        
    }

    @Override
    public TokenInfo authenticate(int userID, byte[] signature) throws RemoteException {
        // Forward the request to authenticate within the primary replica
        try {
            return primaryReplica.authenticate(userID, signature);
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.authenticate(userID, signature);
        }
     
    }

    @Override
    public AuctionItem getSpec(int userID, int itemID, String token) throws RemoteException {
        // Forward the getSpec request to the primary replica
        try {
            return primaryReplica.getSpec(userID, itemID, token);
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.getSpec(userID, itemID, token);
        }
    
    }

    @Override
    public Integer newAuction(int userID, AuctionSaleItem item, String token) throws RemoteException {
        // Forward the request to create a new auction to the primary replica
        try {
            return primaryReplica.newAuction(userID, item, token);
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.newAuction(userID, item, token);
        }
       
    }

    @Override
    public AuctionItem[] listItems(int userID, String token) throws RemoteException {
        // Forward the request to list items to the primary replica
        try {
            return primaryReplica.listItems(userID, token);
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.listItems(userID, token);
        }
      
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID, String token) throws RemoteException {
        // Forward the request to close auction to the primary replica
        try {
            return primaryReplica.closeAuction(userID, itemID, token);
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.closeAuction(userID, itemID, token);
        }
       
    }

    @Override
    public boolean bid(int userID, int itemID, int price, String token) throws RemoteException {
        // Forward the request to bid on the primary replica
        try {
            return primaryReplica.bid(userID, itemID, price, token);
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.bid(userID, itemID, price, token);
        }
    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        //Forawrds the requrest to the primary replica to get the replcia ID
        try {
            return primaryReplica.getPrimaryReplicaID();
        } catch (Exception e) {
            System.out.println("Primary Replica has Failed");
            NewReplicaPrimary();
            return primaryReplica.getPrimaryReplicaID();
        }
    }
    //This is the main driver that runs the frontend and binds it to the rmiregistry
    public static void main(String[] args) {

        try {
            FrontEnd FrontEnd = new FrontEnd();
            String name = "FrontEnd";
            Auction stub = (Auction) UnicastRemoteObject.exportObject(FrontEnd, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("FrontEnd ready");
            NewReplicaPrimary();
            
            
        } catch (Exception e) {
            System.err.println("Auction server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
