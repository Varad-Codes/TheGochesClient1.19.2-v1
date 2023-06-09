package net.minecraft.data.worldgen.features;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.ColumnFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.DeltaFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NetherForestVegetationConfig;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceSphereConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SpringConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TwistingVinesConfig;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.material.Fluids;

public class NetherFeatures
{
    public static final Holder < ConfiguredFeature < DeltaFeatureConfiguration, ? >> DELTA = FeatureUtils.register("delta", Feature.DELTA_FEATURE, new DeltaFeatureConfiguration(Blocks.LAVA.defaultBlockState(), Blocks.MAGMA_BLOCK.defaultBlockState(), UniformInt.of(3, 7), UniformInt.of(0, 2)));
    public static final Holder < ConfiguredFeature < ColumnFeatureConfiguration, ? >> SMALL_BASALT_COLUMNS = FeatureUtils.register("small_basalt_columns", Feature.BASALT_COLUMNS, new ColumnFeatureConfiguration(ConstantInt.of(1), UniformInt.of(1, 4)));
    public static final Holder < ConfiguredFeature < ColumnFeatureConfiguration, ? >> LARGE_BASALT_COLUMNS = FeatureUtils.register("large_basalt_columns", Feature.BASALT_COLUMNS, new ColumnFeatureConfiguration(UniformInt.of(2, 3), UniformInt.of(5, 10)));
    public static final Holder < ConfiguredFeature < ReplaceSphereConfiguration, ? >> BASALT_BLOBS = FeatureUtils.register("basalt_blobs", Feature.REPLACE_BLOBS, new ReplaceSphereConfiguration(Blocks.NETHERRACK.defaultBlockState(), Blocks.BASALT.defaultBlockState(), UniformInt.of(3, 7)));
    public static final Holder < ConfiguredFeature < ReplaceSphereConfiguration, ? >> BLACKSTONE_BLOBS = FeatureUtils.register("blackstone_blobs", Feature.REPLACE_BLOBS, new ReplaceSphereConfiguration(Blocks.NETHERRACK.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), UniformInt.of(3, 7)));
    public static final Holder < ConfiguredFeature < NoneFeatureConfiguration, ? >> GLOWSTONE_EXTRA = FeatureUtils.register("glowstone_extra", Feature.GLOWSTONE_BLOB);
    public static final WeightedStateProvider CRIMSON_VEGETATION_PROVIDER = new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder().add(Blocks.CRIMSON_ROOTS.defaultBlockState(), 87).add(Blocks.CRIMSON_FUNGUS.defaultBlockState(), 11).add(Blocks.WARPED_FUNGUS.defaultBlockState(), 1));
    public static final Holder < ConfiguredFeature < NetherForestVegetationConfig, ? >> CRIMSON_FOREST_VEGETATION = FeatureUtils.register("crimson_forest_vegetation", Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(CRIMSON_VEGETATION_PROVIDER, 8, 4));
    public static final Holder < ConfiguredFeature < NetherForestVegetationConfig, ? >> CRIMSON_FOREST_VEGETATION_BONEMEAL = FeatureUtils.register("crimson_forest_vegetation_bonemeal", Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(CRIMSON_VEGETATION_PROVIDER, 3, 1));
    public static final WeightedStateProvider WARPED_VEGETATION_PROVIDER = new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder().add(Blocks.WARPED_ROOTS.defaultBlockState(), 85).add(Blocks.CRIMSON_ROOTS.defaultBlockState(), 1).add(Blocks.WARPED_FUNGUS.defaultBlockState(), 13).add(Blocks.CRIMSON_FUNGUS.defaultBlockState(), 1));
    public static final Holder < ConfiguredFeature < NetherForestVegetationConfig, ? >> WARPED_FOREST_VEGETION = FeatureUtils.register("warped_forest_vegetation", Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(WARPED_VEGETATION_PROVIDER, 8, 4));
    public static final Holder < ConfiguredFeature < NetherForestVegetationConfig, ? >> WARPED_FOREST_VEGETATION_BONEMEAL = FeatureUtils.register("warped_forest_vegetation_bonemeal", Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(WARPED_VEGETATION_PROVIDER, 3, 1));
    public static final Holder < ConfiguredFeature < NetherForestVegetationConfig, ? >> NETHER_SPROUTS = FeatureUtils.register("nether_sprouts", Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(BlockStateProvider.simple(Blocks.NETHER_SPROUTS), 8, 4));
    public static final Holder < ConfiguredFeature < NetherForestVegetationConfig, ? >> NETHER_SPROUTS_BONEMEAL = FeatureUtils.register("nether_sprouts_bonemeal", Feature.NETHER_FOREST_VEGETATION, new NetherForestVegetationConfig(BlockStateProvider.simple(Blocks.NETHER_SPROUTS), 3, 1));
    public static final Holder < ConfiguredFeature < TwistingVinesConfig, ? >> TWISTING_VINES = FeatureUtils.register("twisting_vines", Feature.TWISTING_VINES, new TwistingVinesConfig(8, 4, 8));
    public static final Holder < ConfiguredFeature < TwistingVinesConfig, ? >> TWISTING_VINES_BONEMEAL = FeatureUtils.register("twisting_vines_bonemeal", Feature.TWISTING_VINES, new TwistingVinesConfig(3, 1, 2));
    public static final Holder < ConfiguredFeature < NoneFeatureConfiguration, ? >> WEEPING_VINES = FeatureUtils.register("weeping_vines", Feature.WEEPING_VINES);
    public static final Holder < ConfiguredFeature < RandomPatchConfiguration, ? >> PATCH_CRIMSON_ROOTS = FeatureUtils.register("patch_crimson_roots", Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.CRIMSON_ROOTS))));
    public static final Holder < ConfiguredFeature < NoneFeatureConfiguration, ? >> BASALT_PILLAR = FeatureUtils.register("basalt_pillar", Feature.BASALT_PILLAR);
    public static final Holder < ConfiguredFeature < SpringConfiguration, ? >> SPRING_LAVA_NETHER = FeatureUtils.register("spring_lava_nether", Feature.SPRING, new SpringConfiguration(Fluids.LAVA.defaultFluidState(), true, 4, 1, HolderSet.a(Block::builtInRegistryHolder, Blocks.NETHERRACK, Blocks.SOUL_SAND, Blocks.GRAVEL, Blocks.MAGMA_BLOCK, Blocks.BLACKSTONE)));
    public static final Holder < ConfiguredFeature < SpringConfiguration, ? >> SPRING_NETHER_CLOSED = FeatureUtils.register("spring_nether_closed", Feature.SPRING, new SpringConfiguration(Fluids.LAVA.defaultFluidState(), false, 5, 0, HolderSet.a(Block::builtInRegistryHolder, Blocks.NETHERRACK)));
    public static final Holder < ConfiguredFeature < SpringConfiguration, ? >> SPRING_NETHER_OPEN = FeatureUtils.register("spring_nether_open", Feature.SPRING, new SpringConfiguration(Fluids.LAVA.defaultFluidState(), false, 4, 1, HolderSet.a(Block::builtInRegistryHolder, Blocks.NETHERRACK)));
    public static final Holder < ConfiguredFeature < RandomPatchConfiguration, ? >> PATCH_FIRE = FeatureUtils.register("patch_fire", Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.FIRE)), List.of(Blocks.NETHERRACK)));
    public static final Holder < ConfiguredFeature < RandomPatchConfiguration, ? >> PATCH_SOUL_FIRE = FeatureUtils.register("patch_soul_fire", Feature.RANDOM_PATCH, FeatureUtils.simplePatchConfiguration(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(BlockStateProvider.simple(Blocks.SOUL_FIRE)), List.of(Blocks.SOUL_SOIL)));
}
