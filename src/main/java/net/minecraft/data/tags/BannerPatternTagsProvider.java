package net.minecraft.data.tags;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.tags.BannerPatternTags;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;

public class BannerPatternTagsProvider extends TagsProvider<BannerPattern>
{
    public BannerPatternTagsProvider(DataGenerator p_236411_)
    {
        super(p_236411_, Registry.BANNER_PATTERN);
    }

    protected void addTags()
    {
        this.tag(BannerPatternTags.NO_ITEM_REQUIRED).a(BannerPatterns.SQUARE_BOTTOM_LEFT, BannerPatterns.SQUARE_BOTTOM_RIGHT, BannerPatterns.SQUARE_TOP_LEFT, BannerPatterns.SQUARE_TOP_RIGHT, BannerPatterns.STRIPE_BOTTOM, BannerPatterns.STRIPE_TOP, BannerPatterns.STRIPE_LEFT, BannerPatterns.STRIPE_RIGHT, BannerPatterns.STRIPE_CENTER, BannerPatterns.STRIPE_MIDDLE, BannerPatterns.STRIPE_DOWNRIGHT, BannerPatterns.STRIPE_DOWNLEFT, BannerPatterns.STRIPE_SMALL, BannerPatterns.CROSS, BannerPatterns.STRAIGHT_CROSS, BannerPatterns.TRIANGLE_BOTTOM, BannerPatterns.TRIANGLE_TOP, BannerPatterns.TRIANGLES_BOTTOM, BannerPatterns.TRIANGLES_TOP, BannerPatterns.DIAGONAL_LEFT, BannerPatterns.DIAGONAL_RIGHT, BannerPatterns.DIAGONAL_LEFT_MIRROR, BannerPatterns.DIAGONAL_RIGHT_MIRROR, BannerPatterns.CIRCLE_MIDDLE, BannerPatterns.RHOMBUS_MIDDLE, BannerPatterns.HALF_VERTICAL, BannerPatterns.HALF_HORIZONTAL, BannerPatterns.HALF_VERTICAL_MIRROR, BannerPatterns.HALF_HORIZONTAL_MIRROR, BannerPatterns.BORDER, BannerPatterns.CURLY_BORDER, BannerPatterns.GRADIENT, BannerPatterns.GRADIENT_UP, BannerPatterns.BRICKS);
        this.tag(BannerPatternTags.PATTERN_ITEM_FLOWER).a(BannerPatterns.FLOWER);
        this.tag(BannerPatternTags.PATTERN_ITEM_CREEPER).a(BannerPatterns.CREEPER);
        this.tag(BannerPatternTags.PATTERN_ITEM_SKULL).a(BannerPatterns.SKULL);
        this.tag(BannerPatternTags.PATTERN_ITEM_MOJANG).a(BannerPatterns.MOJANG);
        this.tag(BannerPatternTags.PATTERN_ITEM_GLOBE).a(BannerPatterns.GLOBE);
        this.tag(BannerPatternTags.PATTERN_ITEM_PIGLIN).a(BannerPatterns.PIGLIN);
    }
}
