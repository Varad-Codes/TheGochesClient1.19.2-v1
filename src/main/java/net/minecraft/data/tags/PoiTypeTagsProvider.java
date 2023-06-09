package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;

public class PoiTypeTagsProvider extends TagsProvider<PoiType>
{
    public PoiTypeTagsProvider(DataGenerator p_236434_)
    {
        super(p_236434_, Registry.POINT_OF_INTEREST_TYPE);
    }

    protected void addTags()
    {
        this.tag(PoiTypeTags.ACQUIRABLE_JOB_SITE).a(PoiTypes.ARMORER, PoiTypes.BUTCHER, PoiTypes.CARTOGRAPHER, PoiTypes.CLERIC, PoiTypes.FARMER, PoiTypes.FISHERMAN, PoiTypes.FLETCHER, PoiTypes.LEATHERWORKER, PoiTypes.LIBRARIAN, PoiTypes.MASON, PoiTypes.SHEPHERD, PoiTypes.TOOLSMITH, PoiTypes.WEAPONSMITH);
        this.tag(PoiTypeTags.VILLAGE).addTag(PoiTypeTags.ACQUIRABLE_JOB_SITE).a(PoiTypes.HOME, PoiTypes.MEETING);
        this.tag(PoiTypeTags.BEE_HOME).a(PoiTypes.BEEHIVE, PoiTypes.BEE_NEST);
    }
}
