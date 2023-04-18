package thegoches.discord;

import club.minnced.discord.rpc.*;

// TODO: Add more updates to the presence
public class DiscordRP {
    public static DiscordRP INSTANCE = new DiscordRP();
    DiscordRPC lib = DiscordRPC.INSTANCE;
    String applicationId = "1096827368879960074";
    String steamId = "";

    DiscordEventHandlers handlers = new DiscordEventHandlers();
    DiscordRichPresence presence = new DiscordRichPresence();

    public void startup(){
        System.out.println("Starting Discord Rich Presence");
        handlers.ready = (user) -> System.out.println("Ready!");
        lib.Discord_Initialize(applicationId, handlers, true, steamId);
    }

    public void shutdown(){
        System.out.println("Shutting down Discord Rich Presence");
        lib.Discord_Shutdown();
    }

    public void updatePresence(String details){
        presence.startTimestamp = System.currentTimeMillis() / 1000;
        // epoch second
        presence.details = details;
        lib.Discord_UpdatePresence(presence);

        new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                lib.Discord_RunCallbacks();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }, "RPC-Callback-Handler").start();
    }
}
