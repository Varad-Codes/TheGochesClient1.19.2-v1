package net.minecraft.data.tags;

import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;

public class StructureTagsProvider extends TagsProvider<Structure>
{
    public StructureTagsProvider(DataGenerator p_236437_)
    {
        super(p_236437_, BuiltinRegistries.STRUCTURES);
    }

    protected void addTags()
    {
        this.tag(StructureTags.VILLAGE).a(BuiltinStructures.VILLAGE_PLAINS).a(BuiltinStructures.VILLAGE_DESERT).a(BuiltinStructures.VILLAGE_SAVANNA).a(BuiltinStructures.VILLAGE_SNOWY).a(BuiltinStructures.VILLAGE_TAIGA);
        this.tag(StructureTags.MINESHAFT).a(BuiltinStructures.MINESHAFT).a(BuiltinStructures.MINESHAFT_MESA);
        this.tag(StructureTags.OCEAN_RUIN).a(BuiltinStructures.OCEAN_RUIN_COLD).a(BuiltinStructures.OCEAN_RUIN_WARM);
        this.tag(StructureTags.SHIPWRECK).a(BuiltinStructures.SHIPWRECK).a(BuiltinStructures.SHIPWRECK_BEACHED);
        this.tag(StructureTags.RUINED_PORTAL).a(BuiltinStructures.RUINED_PORTAL_DESERT).a(BuiltinStructures.RUINED_PORTAL_JUNGLE).a(BuiltinStructures.RUINED_PORTAL_MOUNTAIN).a(BuiltinStructures.RUINED_PORTAL_NETHER).a(BuiltinStructures.RUINED_PORTAL_OCEAN).a(BuiltinStructures.RUINED_PORTAL_STANDARD).a(BuiltinStructures.RUINED_PORTAL_SWAMP);
        this.tag(StructureTags.CATS_SPAWN_IN).a(BuiltinStructures.SWAMP_HUT);
        this.tag(StructureTags.CATS_SPAWN_AS_BLACK).a(BuiltinStructures.SWAMP_HUT);
        this.tag(StructureTags.EYE_OF_ENDER_LOCATED).a(BuiltinStructures.STRONGHOLD);
        this.tag(StructureTags.DOLPHIN_LOCATED).addTag(StructureTags.OCEAN_RUIN).addTag(StructureTags.SHIPWRECK);
        this.tag(StructureTags.ON_WOODLAND_EXPLORER_MAPS).a(BuiltinStructures.WOODLAND_MANSION);
        this.tag(StructureTags.ON_OCEAN_EXPLORER_MAPS).a(BuiltinStructures.OCEAN_MONUMENT);
        this.tag(StructureTags.ON_TREASURE_MAPS).a(BuiltinStructures.BURIED_TREASURE);
    }
}
