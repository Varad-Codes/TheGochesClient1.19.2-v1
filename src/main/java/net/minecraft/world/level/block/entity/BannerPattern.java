package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;

public class BannerPattern
{
    final String hashname;

    public BannerPattern(String p_222696_)
    {
        this.hashname = p_222696_;
    }

    public static ResourceLocation location(ResourceKey<BannerPattern> p_222698_, boolean p_222699_)
    {
        String s = p_222699_ ? "banner" : "shield";
        ResourceLocation resourcelocation = p_222698_.location();
        return new ResourceLocation(resourcelocation.getNamespace(), "entity/" + s + "/" + resourcelocation.getPath());
    }

    public String getHashname()
    {
        return this.hashname;
    }

    @Nullable
    public static Holder<BannerPattern> byHash(String pHash)
    {
        return Registry.BANNER_PATTERN.holders().filter((p_222704_) ->
        {
            return (p_222704_.value()).hashname.equals(pHash);
        }).findAny().orElse((Holder.Reference<BannerPattern>)null);
    }

    public static class Builder
    {
        private final List<Pair<Holder<BannerPattern>, DyeColor>> patterns = Lists.newArrayList();

        public BannerPattern.Builder addPattern(ResourceKey<BannerPattern> pPattern, DyeColor pColor)
        {
            return this.addPattern(Registry.BANNER_PATTERN.getHolderOrThrow(pPattern), pColor);
        }

        public BannerPattern.Builder addPattern(Holder<BannerPattern> pPattern, DyeColor pColor)
        {
            return this.addPattern(Pair.of(pPattern, pColor));
        }

        public BannerPattern.Builder addPattern(Pair<Holder<BannerPattern>, DyeColor> pPattern)
        {
            this.patterns.add(pPattern);
            return this;
        }

        public ListTag toListTag()
        {
            ListTag listtag = new ListTag();

            for (Pair<Holder<BannerPattern>, DyeColor> pair : this.patterns)
            {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putString("Pattern", (pair.getFirst().value()).hashname);
                compoundtag.putInt("Color", pair.getSecond().getId());
                listtag.add(compoundtag);
            }

            return listtag;
        }
    }
}
