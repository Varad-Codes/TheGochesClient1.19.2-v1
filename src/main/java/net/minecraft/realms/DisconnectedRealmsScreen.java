package net.minecraft.realms;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class DisconnectedRealmsScreen extends RealmsScreen
{
    private final Component reason;
    private MultiLineLabel message = MultiLineLabel.EMPTY;
    private final Screen parent;
    private int textHeight;

    public DisconnectedRealmsScreen(Screen pParent, Component pTitle, Component pReason)
    {
        super(pTitle);
        this.parent = pParent;
        this.reason = pReason;
    }

    public void init()
    {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setConnectedToRealms(false);
        minecraft.getClientPackSource().clearServerPack();
        this.message = MultiLineLabel.create(this.font, this.reason, this.width - 50);
        this.textHeight = this.message.getLineCount() * 9;
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 2 + this.textHeight / 2 + 9, 200, 20, CommonComponents.GUI_BACK, (p_120663_) ->
        {
            minecraft.setScreen(this.parent);
        }));
    }

    public Component getNarrationMessage()
    {
        return Component.empty().append(this.title).append(": ").append(this.reason);
    }

    public void onClose()
    {
        Minecraft.getInstance().setScreen(this.parent);
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        this.renderBackground(pMatrixStack);
        drawCenteredString(pMatrixStack, this.font, this.title, this.width / 2, this.height / 2 - this.textHeight / 2 - 9 * 2, 11184810);
        this.message.renderCentered(pMatrixStack, this.width / 2, this.height / 2 - this.textHeight / 2);
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }
}
