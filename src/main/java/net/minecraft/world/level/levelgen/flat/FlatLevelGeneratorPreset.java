package net.minecraft.world.level.levelgen.flat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.world.item.Item;

public record FlatLevelGeneratorPreset(Holder<Item> displayItem, FlatLevelGeneratorSettings settings)
{
    public static final Codec<FlatLevelGeneratorPreset> DIRECT_CODEC = RecordCodecBuilder.create((p_226253_) ->
    {
        return p_226253_.group(RegistryFixedCodec.create(Registry.ITEM_REGISTRY).fieldOf("display").forGetter((p_226258_) -> {
            return p_226258_.displayItem;
        }), FlatLevelGeneratorSettings.CODEC.fieldOf("settings").forGetter((p_226255_) -> {
            return p_226255_.settings;
        })).apply(p_226253_, FlatLevelGeneratorPreset::new);
    });
    public static final Codec<Holder<FlatLevelGeneratorPreset>> CODEC = RegistryFileCodec.create(Registry.FLAT_LEVEL_GENERATOR_PRESET_REGISTRY, DIRECT_CODEC);
}
