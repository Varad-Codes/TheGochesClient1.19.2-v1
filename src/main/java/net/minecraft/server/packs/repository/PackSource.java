package net.minecraft.server.packs.repository;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public interface PackSource
{
    PackSource DEFAULT = passThrough();
    PackSource BUILT_IN = decorating("pack.source.builtin");
    PackSource WORLD = decorating("pack.source.world");
    PackSource SERVER = decorating("pack.source.server");

    Component decorate(Component p_10541_);

    static PackSource passThrough()
    {
        return (p_10536_) ->
        {
            return p_10536_;
        };
    }

    static PackSource decorating(String pSource)
    {
        Component component = Component.translatable(pSource);
        return (p_10539_) ->
        {
            return Component.a("pack.nameAndSource", p_10539_, component).withStyle(ChatFormatting.GRAY);
        };
    }
}
