package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.PaintingVariantTags;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.entity.decoration.PaintingVariants;

public class PaintingVariantTagsProvider extends TagsProvider<PaintingVariant>
{
    public PaintingVariantTagsProvider(DataGenerator p_236431_)
    {
        super(p_236431_, Registry.PAINTING_VARIANT);
    }

    protected void addTags()
    {
        this.tag(PaintingVariantTags.PLACEABLE).a(PaintingVariants.KEBAB, PaintingVariants.AZTEC, PaintingVariants.ALBAN, PaintingVariants.AZTEC2, PaintingVariants.BOMB, PaintingVariants.PLANT, PaintingVariants.WASTELAND, PaintingVariants.POOL, PaintingVariants.COURBET, PaintingVariants.SEA, PaintingVariants.SUNSET, PaintingVariants.CREEBET, PaintingVariants.WANDERER, PaintingVariants.GRAHAM, PaintingVariants.MATCH, PaintingVariants.BUST, PaintingVariants.STAGE, PaintingVariants.VOID, PaintingVariants.SKULL_AND_ROSES, PaintingVariants.WITHER, PaintingVariants.FIGHTERS, PaintingVariants.POINTER, PaintingVariants.PIGSCENE, PaintingVariants.BURNING_SKULL, PaintingVariants.SKELETON, PaintingVariants.DONKEY_KONG);
    }
}
