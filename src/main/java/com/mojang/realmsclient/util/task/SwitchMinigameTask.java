package com.mojang.realmsclient.util.task;

import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.exception.RetryCallException;
import com.mojang.realmsclient.gui.screens.RealmsConfigureWorldScreen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

public class SwitchMinigameTask extends LongRunningTask
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final long worldId;
    private final WorldTemplate worldTemplate;
    private final RealmsConfigureWorldScreen lastScreen;

    public SwitchMinigameTask(long pWorldId, WorldTemplate p_90452_, RealmsConfigureWorldScreen pWorldTemplate)
    {
        this.worldId = pWorldId;
        this.worldTemplate = p_90452_;
        this.lastScreen = pWorldTemplate;
    }

    public void run()
    {
        RealmsClient realmsclient = RealmsClient.create();
        this.setTitle(Component.translatable("mco.minigame.world.starting.screen.title"));

        for (int i = 0; i < 25; ++i)
        {
            try
            {
                if (this.aborted())
                {
                    return;
                }

                if (realmsclient.putIntoMinigameMode(this.worldId, this.worldTemplate.id))
                {
                    setScreen(this.lastScreen);
                    break;
                }
            }
            catch (RetryCallException retrycallexception)
            {
                if (this.aborted())
                {
                    return;
                }

                pause((long)retrycallexception.delaySeconds);
            }
            catch (Exception exception)
            {
                if (this.aborted())
                {
                    return;
                }

                LOGGER.error("Couldn't start mini game!");
                this.error(exception.toString());
            }
        }
    }
}
