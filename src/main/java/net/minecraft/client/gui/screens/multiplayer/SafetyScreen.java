package net.minecraft.client.gui.screens.multiplayer;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SafetyScreen extends WarningScreen
{
    private static final Component TITLE = Component.translatable("multiplayerWarning.header").withStyle(ChatFormatting.BOLD);
    private static final Component CONTENT = Component.translatable("multiplayerWarning.message");
    private static final Component CHECK = Component.translatable("multiplayerWarning.check");
    private static final Component NARRATION = TITLE.copy().append("\n").append(CONTENT);
    private final Screen previous;

    public SafetyScreen(Screen pPrevious)
    {
        super(TITLE, CONTENT, CHECK, NARRATION);
        this.previous = pPrevious;
    }

    protected void initButtons(int p_210904_)
    {
        this.addRenderableWidget(new Button(this.width / 2 - 155, 100 + p_210904_, 150, 20, CommonComponents.GUI_PROCEED, (p_210908_) ->
        {
            if (this.stopShowing.selected())
            {
                this.minecraft.options.skipMultiplayerWarning = true;
                this.minecraft.options.save();
            }

            this.minecraft.setScreen(new JoinMultiplayerScreen(this.previous));
        }));
        this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, 100 + p_210904_, 150, 20, CommonComponents.GUI_BACK, (p_210906_) ->
        {
            this.minecraft.setScreen(this.previous);
        }));
    }
}
