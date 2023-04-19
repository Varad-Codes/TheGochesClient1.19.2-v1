package net.minecraft.data.tags;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.WorldPresetTags;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

public class WorldPresetTagsProvider extends TagsProvider<WorldPreset>
{
    public WorldPresetTagsProvider(DataGenerator p_236457_)
    {
        super(p_236457_, BuiltinRegistries.WORLD_PRESET);
    }

    protected void addTags()
    {
        this.tag(WorldPresetTags.NORMAL).a(WorldPresets.NORMAL).a(WorldPresets.FLAT).a(WorldPresets.LARGE_BIOMES).a(WorldPresets.AMPLIFIED).a(WorldPresets.SINGLE_BIOME_SURFACE);
        this.tag(WorldPresetTags.EXTENDED).addTag(WorldPresetTags.NORMAL).a(WorldPresets.DEBUG);
    }
}
