package net.minecraft.data.tags;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.FlatLevelGeneratorPresetTags;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPreset;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorPresets;

public class FlatLevelGeneratorPresetTagsProvider extends TagsProvider<FlatLevelGeneratorPreset>
{
    public FlatLevelGeneratorPresetTagsProvider(DataGenerator p_236423_)
    {
        super(p_236423_, BuiltinRegistries.FLAT_LEVEL_GENERATOR_PRESET);
    }

    protected void addTags()
    {
        this.tag(FlatLevelGeneratorPresetTags.VISIBLE).a(FlatLevelGeneratorPresets.CLASSIC_FLAT).a(FlatLevelGeneratorPresets.TUNNELERS_DREAM).a(FlatLevelGeneratorPresets.WATER_WORLD).a(FlatLevelGeneratorPresets.OVERWORLD).a(FlatLevelGeneratorPresets.SNOWY_KINGDOM).a(FlatLevelGeneratorPresets.BOTTOMLESS_PIT).a(FlatLevelGeneratorPresets.DESERT).a(FlatLevelGeneratorPresets.REDSTONE_READY).a(FlatLevelGeneratorPresets.THE_VOID);
    }
}
