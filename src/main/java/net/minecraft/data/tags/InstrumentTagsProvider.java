package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.Instruments;

public class InstrumentTagsProvider extends TagsProvider<Instrument>
{
    public InstrumentTagsProvider(DataGenerator p_236428_)
    {
        super(p_236428_, Registry.INSTRUMENT);
    }

    protected void addTags()
    {
        this.tag(InstrumentTags.REGULAR_GOAT_HORNS).a(Instruments.PONDER_GOAT_HORN).a(Instruments.SING_GOAT_HORN).a(Instruments.SEEK_GOAT_HORN).a(Instruments.FEEL_GOAT_HORN);
        this.tag(InstrumentTags.SCREAMING_GOAT_HORNS).a(Instruments.ADMIRE_GOAT_HORN).a(Instruments.CALL_GOAT_HORN).a(Instruments.YEARN_GOAT_HORN).a(Instruments.DREAM_GOAT_HORN);
        this.tag(InstrumentTags.GOAT_HORNS).addTag(InstrumentTags.REGULAR_GOAT_HORNS).addTag(InstrumentTags.SCREAMING_GOAT_HORNS);
    }
}
