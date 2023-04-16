package thegoches.client;

public class Client {
    public static Client INSTANCE = new Client();

    public String CLIENT_NAME = "TheGoches Client 1.19.2";
    public String CLIENT_AUTHOR = "F4STER";

    public void startup() {
        System.out.println("\nStarting " + CLIENT_NAME + " by " + CLIENT_AUTHOR + "\n");
    }

    public void shutdown() {
        System.out.println("\nShutting down " + CLIENT_NAME + " by " + CLIENT_AUTHOR + "\n");
    }
}
