package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;

public class InBedChatScreen extends ChatScreen
{
    private Button leaveBedButton;

    public InBedChatScreen()
    {
        super("");
    }

    protected void init()
    {
        super.init();
        this.leaveBedButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 40, 200, 20, Component.translatable("multiplayer.stopSleeping"), (p_96074_) ->
        {
            this.sendWakeUp();
        }));
    }

    public void render(PoseStack p_242941_, int p_242857_, int p_242871_, float p_242925_)
    {
        this.leaveBedButton.visible = this.getDisplayedPreviewText() == null;
        super.render(p_242941_, p_242857_, p_242871_, p_242925_);
    }

    public void onClose()
    {
        this.sendWakeUp();
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (pKeyCode == 256)
        {
            this.sendWakeUp();
        }
        else if (pKeyCode == 257 || pKeyCode == 335)
        {
            if (this.handleChatInput(this.input.getValue(), true))
            {
                this.minecraft.setScreen((Screen)null);
                this.input.setValue("");
                this.minecraft.gui.getChat().resetChatScroll();
            }

            return true;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    private void sendWakeUp()
    {
        ClientPacketListener clientpacketlistener = this.minecraft.player.connection;
        clientpacketlistener.send(new ServerboundPlayerCommandPacket(this.minecraft.player, ServerboundPlayerCommandPacket.Action.STOP_SLEEPING));
    }

    public void onPlayerWokeUp()
    {
        if (this.input.getValue().isEmpty())
        {
            this.minecraft.setScreen((Screen)null);
        }
        else
        {
            this.minecraft.setScreen(new ChatScreen(this.input.getValue()));
        }
    }
}
