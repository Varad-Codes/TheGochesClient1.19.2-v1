package thegoches.client;

import thegoches.discord.DiscordRP;

public class Client {
    public static Client INSTANCE = new Client();

    public String CLIENT_NAME = "TheGoches Client 1.19.2";
    public String CLIENT_AUTHOR = "F4STER";

    public void startup() {
        DiscordRP.INSTANCE.startup();
        System.out.println("\n\nStarting " + CLIENT_NAME + " by " + CLIENT_AUTHOR + "\n");
    }

    public void shutdown() {
        DiscordRP.INSTANCE.shutdown();
        System.out.println("\n\nShutting down " + CLIENT_NAME + " by " + CLIENT_AUTHOR + "\n");
    }

    public void onCrash() {
        System.out.println("\n\n" + CLIENT_NAME + " by " + CLIENT_AUTHOR + " has crashed!\n");
    }
}
