import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

public class Replica implements Server {
    private int replicaID;
    private Map<Integer, AuctionItem> auctionItems = new HashMap<>();
    private Map<Integer, String> userEmails = new HashMap<>();
    private Map<Integer, PublicKey> userKeyPairs = new HashMap<>();
    private Map<Integer, Integer> highestBidder = new HashMap<>();
    private Map<Integer, Integer> auctionCreators = new HashMap<>();
    private int itemIDCounter = 1;
    public Replica(int replicaID) throws RemoteException {
        this.replicaID = replicaID;
        // Additional initialization for the replica
    }

    /*Stores user email and their public key and is updated on the replica */
    @Override
    public Integer register(String email, PublicKey pubKey) throws RemoteException {
        int userID = userEmails.size() + 1;
        userEmails.put(userID, email);  // Storing the email associated with the userID
        userKeyPairs.put(userID, pubKey);
        System.out.println("User registered: UserID=" + userID + ", Email=" + email);
        livereplicaUpdate();
        return userID;

    }

    /*This function is not necessary for this part of the coursework */
    @Override
    public ChallengeInfo challenge(int userID, String clientChallenge) throws RemoteException {
        return null;
    }

    /*This function is not necessary for this part of the coursework */
    @Override
    public TokenInfo authenticate(int userID, byte[] signature) throws RemoteException {
        return null;
    }


    /*Stores user email and their public key and is updated on the replica */
    @Override
    public AuctionItem getSpec(int userID, int itemID, String token) throws RemoteException {
        livereplicaUpdate();
        return auctionItems.get(itemID);
    }

    /*Let's the user create a new auction with a valid ID and token */
    @Override
    public Integer newAuction(int userID, AuctionSaleItem item, String token) throws RemoteException {
        if (userEmails.get(userID) != null) {
            int itemID = itemIDCounter++;
            AuctionItem auctionItem = new AuctionItem();
            auctionItem.itemID = itemID;
            auctionItem.name = item.name;
            auctionItem.description = item.description;
            auctionItem.highestBid = item.reservePrice;
            auctionItems.put(itemID, auctionItem);
            auctionCreators.put(itemID, userID);  // Store the creator's userID
            System.out.println("New auction created: itemID=" + itemID + ", Item=" + item.name);
            livereplicaUpdate();
            return itemID;
        } else {
            System.out.println("You need to have an account to bid");
            return null;
        }
    }

    @Override
    public AuctionItem[] listItems(int userID, String token) throws RemoteException {
        livereplicaUpdate();
        return auctionItems.values().toArray(new AuctionItem[0]);
    }

    @Override
    public AuctionResult closeAuction(int userID, int itemID, String token) throws RemoteException {
        if (auctionItems.containsKey(itemID)) {
            AuctionItem item = auctionItems.get(itemID);

            // Verify that the user closing the auction is the creator
            int creatorUserID = auctionCreators.get(itemID);
            if (creatorUserID == userID) {
                // Find the highest bidder for this item
                int highestBidderUserID = highestBidder.get(itemID);

                // Retrieve the user's email using the userID of the highest bidder
                String winningEmail = userEmails.get(highestBidderUserID);
                
                int highestBid = item.highestBid;

                System.out.println("Auction closed: AuctionID=" + itemID + ", Highest Bid=" + highestBid +
                        ", Winning Email=" + winningEmail);

                // Create and return the AuctionResult
                AuctionResult auctionresult = new AuctionResult();
                auctionresult.winningEmail = winningEmail;
                auctionresult.winningPrice = highestBid;
                livereplicaUpdate(); 
                return auctionresult;
            } else {
                System.out.println("Unauthorized user tried to close the auction: UserID=" + userID +
                        ", AuctionID=" + itemID);
                return null;
            }
        } else {
            System.out.println("Auction not found: AuctionID=" + itemID);
            return null;
        }
    }

    @Override
    public boolean bid(int userID, int itemID, int price, String token) throws RemoteException {
        if (auctionItems.containsKey(itemID)) {
            AuctionItem item = auctionItems.get(itemID);

            // Check if the bid is higher than the current highest bid
            if (price > item.highestBid) {
                // Update the highest bidder for this item
                highestBidder.put(itemID, userID);

                // Update the highest bid for this item
                item.highestBid = price;

                System.out.println("Bid placed: UserID=" + userID + ", AuctionID=" + itemID + ", Bid=" + price);
                livereplicaUpdate();
                return true;
            } else {
                System.out.println("Bid rejected: UserID=" + userID + ", AuctionID=" + itemID + ", Bid=" + price +
                        " (Not higher than current highest bid so please place a higher bid)");
                return false;
            }
        } else {
            System.out.println("Auction not found: AuctionID=" + itemID);
            return false;
        }

    }

    @Override
    public int getPrimaryReplicaID() throws RemoteException {
        // Return the identifier of this replica
        return replicaID;
    }

    //This function is used to update the data to every other replica in the server
    @Override
    public void livereplicaUpdate() throws RemoteException {

        try{
            Registry backupsRegistry = LocateRegistry.getRegistry("localhost");
            String[] backupReplicas = backupsRegistry.list();

            for(int i = 0; i < backupReplicas.length; i++)
            {
                //connects to all replica that has been found, if connected update their variables
                if(backupReplicas[i].startsWith("Replica"))
                {
                    try {
                        Server backUps = (Server) backupsRegistry.lookup(backupReplicas[i]);
                        backUps.replicaBackup(auctionItems, userEmails, userKeyPairs, highestBidder, auctionCreators);
                    } catch (Exception e) {;
                        e.printStackTrace();
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

    }

    /*This function keeps a backup of replica for additional replicas to take it's place */
    @Override
    public void replicaBackup(Map<Integer, AuctionItem> AuctionItems, Map<Integer, String> UserEmails, Map<Integer, PublicKey> UserKeyPairs, Map<Integer, Integer> HighestBidder, Map<Integer, Integer> AuctionCreators) throws RemoteException {
        auctionItems = AuctionItems;
        userEmails = UserEmails;
        userKeyPairs = UserKeyPairs;
        highestBidder = HighestBidder;
        auctionCreators = AuctionCreators;
    }

    /*Replica binds with the rmiregistry to communicate with FrontEnd */
    public static void main(String[] args) {
        try {
            

            int id = Integer.parseInt(args[0]);
            Replica replica = new Replica(id);
            String name = "Replica" + id;
            Auction stub = (Auction) UnicastRemoteObject.exportObject(replica, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, stub);
            System.out.println("Replica " + id + " is ready");
            
        } catch (Exception e) {
            System.err.println("Auction server exception: " + e.toString());
            e.printStackTrace();
        }


    }
}

