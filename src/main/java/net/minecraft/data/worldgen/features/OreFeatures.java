package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;

public class OreFeatures
{
    public static final RuleTest NATURAL_STONE = new TagMatchTest(BlockTags.BASE_STONE_OVERWORLD);
    public static final RuleTest STONE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
    public static final RuleTest DEEPSLATE_ORE_REPLACEABLES = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
    public static final RuleTest NETHERRACK = new BlockMatchTest(Blocks.NETHERRACK);
    public static final RuleTest NETHER_ORE_REPLACEABLES = new TagMatchTest(BlockTags.BASE_STONE_NETHER);
    public static final List<OreConfiguration.TargetBlockState> ORE_IRON_TARGET_LIST = List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.IRON_ORE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_IRON_ORE.defaultBlockState()));
    public static final List<OreConfiguration.TargetBlockState> ORE_GOLD_TARGET_LIST = List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.GOLD_ORE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_GOLD_ORE.defaultBlockState()));
    public static final List<OreConfiguration.TargetBlockState> ORE_DIAMOND_TARGET_LIST = List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.DIAMOND_ORE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_DIAMOND_ORE.defaultBlockState()));
    public static final List<OreConfiguration.TargetBlockState> ORE_LAPIS_TARGET_LIST = List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.LAPIS_ORE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_LAPIS_ORE.defaultBlockState()));
    public static final List<OreConfiguration.TargetBlockState> ORE_COPPER_TARGET_LIST = List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.COPPER_ORE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState()));
    public static final List<OreConfiguration.TargetBlockState> ORE_COAL_TARGET_LIST = List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.COAL_ORE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_COAL_ORE.defaultBlockState()));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_MAGMA = FeatureUtils.register("ore_magma", Feature.ORE, new OreConfiguration(NETHERRACK, Blocks.MAGMA_BLOCK.defaultBlockState(), 33));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_SOUL_SAND = FeatureUtils.register("ore_soul_sand", Feature.ORE, new OreConfiguration(NETHERRACK, Blocks.SOUL_SAND.defaultBlockState(), 12));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_NETHER_GOLD = FeatureUtils.register("ore_nether_gold", Feature.ORE, new OreConfiguration(NETHERRACK, Blocks.NETHER_GOLD_ORE.defaultBlockState(), 10));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_QUARTZ = FeatureUtils.register("ore_quartz", Feature.ORE, new OreConfiguration(NETHERRACK, Blocks.NETHER_QUARTZ_ORE.defaultBlockState(), 14));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_GRAVEL_NETHER = FeatureUtils.register("ore_gravel_nether", Feature.ORE, new OreConfiguration(NETHERRACK, Blocks.GRAVEL.defaultBlockState(), 33));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_BLACKSTONE = FeatureUtils.register("ore_blackstone", Feature.ORE, new OreConfiguration(NETHERRACK, Blocks.BLACKSTONE.defaultBlockState(), 33));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_DIRT = FeatureUtils.register("ore_dirt", Feature.ORE, new OreConfiguration(NATURAL_STONE, Blocks.DIRT.defaultBlockState(), 33));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_GRAVEL = FeatureUtils.register("ore_gravel", Feature.ORE, new OreConfiguration(NATURAL_STONE, Blocks.GRAVEL.defaultBlockState(), 33));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_GRANITE = FeatureUtils.register("ore_granite", Feature.ORE, new OreConfiguration(NATURAL_STONE, Blocks.GRANITE.defaultBlockState(), 64));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_DIORITE = FeatureUtils.register("ore_diorite", Feature.ORE, new OreConfiguration(NATURAL_STONE, Blocks.DIORITE.defaultBlockState(), 64));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_ANDESITE = FeatureUtils.register("ore_andesite", Feature.ORE, new OreConfiguration(NATURAL_STONE, Blocks.ANDESITE.defaultBlockState(), 64));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_TUFF = FeatureUtils.register("ore_tuff", Feature.ORE, new OreConfiguration(NATURAL_STONE, Blocks.TUFF.defaultBlockState(), 64));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_COAL = FeatureUtils.register("ore_coal", Feature.ORE, new OreConfiguration(ORE_COAL_TARGET_LIST, 17));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_COAL_BURIED = FeatureUtils.register("ore_coal_buried", Feature.ORE, new OreConfiguration(ORE_COAL_TARGET_LIST, 17, 0.5F));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_IRON = FeatureUtils.register("ore_iron", Feature.ORE, new OreConfiguration(ORE_IRON_TARGET_LIST, 9));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_IRON_SMALL = FeatureUtils.register("ore_iron_small", Feature.ORE, new OreConfiguration(ORE_IRON_TARGET_LIST, 4));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_GOLD = FeatureUtils.register("ore_gold", Feature.ORE, new OreConfiguration(ORE_GOLD_TARGET_LIST, 9));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_GOLD_BURIED = FeatureUtils.register("ore_gold_buried", Feature.ORE, new OreConfiguration(ORE_GOLD_TARGET_LIST, 9, 0.5F));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_REDSTONE = FeatureUtils.register("ore_redstone", Feature.ORE, new OreConfiguration(List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.REDSTONE_ORE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_REDSTONE_ORE.defaultBlockState())), 8));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_DIAMOND_SMALL = FeatureUtils.register("ore_diamond_small", Feature.ORE, new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 4, 0.5F));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_DIAMOND_LARGE = FeatureUtils.register("ore_diamond_large", Feature.ORE, new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 12, 0.7F));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_DIAMOND_BURIED = FeatureUtils.register("ore_diamond_buried", Feature.ORE, new OreConfiguration(ORE_DIAMOND_TARGET_LIST, 8, 1.0F));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_LAPIS = FeatureUtils.register("ore_lapis", Feature.ORE, new OreConfiguration(ORE_LAPIS_TARGET_LIST, 7));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_LAPIS_BURIED = FeatureUtils.register("ore_lapis_buried", Feature.ORE, new OreConfiguration(ORE_LAPIS_TARGET_LIST, 7, 1.0F));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_INFESTED = FeatureUtils.register("ore_infested", Feature.ORE, new OreConfiguration(List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.INFESTED_STONE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.INFESTED_DEEPSLATE.defaultBlockState())), 9));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_EMERALD = FeatureUtils.register("ore_emerald", Feature.ORE, new OreConfiguration(List.of(OreConfiguration.target(STONE_ORE_REPLACEABLES, Blocks.EMERALD_ORE.defaultBlockState()), OreConfiguration.target(DEEPSLATE_ORE_REPLACEABLES, Blocks.DEEPSLATE_EMERALD_ORE.defaultBlockState())), 3));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_ANCIENT_DEBRIS_LARGE = FeatureUtils.register("ore_ancient_debris_large", Feature.SCATTERED_ORE, new OreConfiguration(NETHER_ORE_REPLACEABLES, Blocks.ANCIENT_DEBRIS.defaultBlockState(), 3, 1.0F));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_ANCIENT_DEBRIS_SMALL = FeatureUtils.register("ore_ancient_debris_small", Feature.SCATTERED_ORE, new OreConfiguration(NETHER_ORE_REPLACEABLES, Blocks.ANCIENT_DEBRIS.defaultBlockState(), 2, 1.0F));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_COPPPER_SMALL = FeatureUtils.register("ore_copper_small", Feature.ORE, new OreConfiguration(ORE_COPPER_TARGET_LIST, 10));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_COPPER_LARGE = FeatureUtils.register("ore_copper_large", Feature.ORE, new OreConfiguration(ORE_COPPER_TARGET_LIST, 20));
    public static final Holder < ConfiguredFeature < OreConfiguration, ? >> ORE_CLAY = FeatureUtils.register("ore_clay", Feature.ORE, new OreConfiguration(NATURAL_STONE, Blocks.CLAY.defaultBlockState(), 33));
}
