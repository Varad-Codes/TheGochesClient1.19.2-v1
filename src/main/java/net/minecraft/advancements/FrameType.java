package net.minecraft.advancements;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum FrameType
{
    TASK("task", 0, ChatFormatting.GREEN),
    CHALLENGE("challenge", 26, ChatFormatting.DARK_PURPLE),
    GOAL("goal", 52, ChatFormatting.GREEN);

    private final String name;
    private final int texture;
    private final ChatFormatting chatColor;
    private final Component displayName;

    private FrameType(String p_15545_, int p_15546_, ChatFormatting p_15547_)
    {
        this.name = p_15545_;
        this.texture = p_15546_;
        this.chatColor = p_15547_;
        this.displayName = Component.translatable("advancements.toast." + p_15545_);
    }

    public String getName()
    {
        return this.name;
    }

    public int getTexture()
    {
        return this.texture;
    }

    public static FrameType byName(String pName)
    {
        for (FrameType frametype : values())
        {
            if (frametype.name.equals(pName))
            {
                return frametype;
            }
        }

        throw new IllegalArgumentException("Unknown frame type '" + pName + "'");
    }

    public ChatFormatting getChatColor()
    {
        return this.chatColor;
    }

    public Component getDisplayName()
    {
        return this.displayName;
    }
}
