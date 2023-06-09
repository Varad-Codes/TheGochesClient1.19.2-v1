package net.minecraft.data.worldgen.biome;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.biome.Biome;

public abstract class Biomes
{
    public static Holder<Biome> bootstrap(Registry<Biome> p_236653_)
    {
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.THE_VOID, OverworldBiomes.theVoid());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.PLAINS, OverworldBiomes.plains(false, false, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS, OverworldBiomes.plains(true, false, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SNOWY_PLAINS, OverworldBiomes.plains(false, true, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.ICE_SPIKES, OverworldBiomes.plains(false, true, true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.DESERT, OverworldBiomes.desert());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SWAMP, OverworldBiomes.swamp());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.MANGROVE_SWAMP, OverworldBiomes.mangroveSwamp());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.FOREST, OverworldBiomes.forest(false, false, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.FLOWER_FOREST, OverworldBiomes.forest(false, false, true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.BIRCH_FOREST, OverworldBiomes.forest(true, false, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.DARK_FOREST, OverworldBiomes.darkForest());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.OLD_GROWTH_BIRCH_FOREST, OverworldBiomes.forest(true, true, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.OLD_GROWTH_PINE_TAIGA, OverworldBiomes.oldGrowthTaiga(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.OLD_GROWTH_SPRUCE_TAIGA, OverworldBiomes.oldGrowthTaiga(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.TAIGA, OverworldBiomes.taiga(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SNOWY_TAIGA, OverworldBiomes.taiga(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SAVANNA, OverworldBiomes.savanna(false, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SAVANNA_PLATEAU, OverworldBiomes.savanna(false, true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.WINDSWEPT_HILLS, OverworldBiomes.windsweptHills(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.WINDSWEPT_GRAVELLY_HILLS, OverworldBiomes.windsweptHills(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.WINDSWEPT_FOREST, OverworldBiomes.windsweptHills(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.WINDSWEPT_SAVANNA, OverworldBiomes.savanna(true, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.JUNGLE, OverworldBiomes.jungle());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SPARSE_JUNGLE, OverworldBiomes.sparseJungle());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.BAMBOO_JUNGLE, OverworldBiomes.bambooJungle());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.BADLANDS, OverworldBiomes.badlands(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.ERODED_BADLANDS, OverworldBiomes.badlands(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.WOODED_BADLANDS, OverworldBiomes.badlands(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.MEADOW, OverworldBiomes.meadow());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.GROVE, OverworldBiomes.grove());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SNOWY_SLOPES, OverworldBiomes.snowySlopes());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.FROZEN_PEAKS, OverworldBiomes.frozenPeaks());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.JAGGED_PEAKS, OverworldBiomes.jaggedPeaks());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.STONY_PEAKS, OverworldBiomes.stonyPeaks());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.RIVER, OverworldBiomes.river(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.FROZEN_RIVER, OverworldBiomes.river(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.BEACH, OverworldBiomes.beach(false, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SNOWY_BEACH, OverworldBiomes.beach(true, false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.STONY_SHORE, OverworldBiomes.beach(false, true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.WARM_OCEAN, OverworldBiomes.warmOcean());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.DEEP_LUKEWARM_OCEAN, OverworldBiomes.lukeWarmOcean(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.OCEAN, OverworldBiomes.ocean(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.DEEP_OCEAN, OverworldBiomes.ocean(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.COLD_OCEAN, OverworldBiomes.coldOcean(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.DEEP_COLD_OCEAN, OverworldBiomes.coldOcean(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.FROZEN_OCEAN, OverworldBiomes.frozenOcean(false));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.DEEP_FROZEN_OCEAN, OverworldBiomes.frozenOcean(true));
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.MUSHROOM_FIELDS, OverworldBiomes.mushroomFields());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.DRIPSTONE_CAVES, OverworldBiomes.dripstoneCaves());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.LUSH_CAVES, OverworldBiomes.lushCaves());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.DEEP_DARK, OverworldBiomes.deepDark());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.NETHER_WASTES, NetherBiomes.netherWastes());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.WARPED_FOREST, NetherBiomes.warpedForest());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.CRIMSON_FOREST, NetherBiomes.crimsonForest());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY, NetherBiomes.soulSandValley());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.BASALT_DELTAS, NetherBiomes.basaltDeltas());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.THE_END, EndBiomes.theEnd());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.END_HIGHLANDS, EndBiomes.endHighlands());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.END_MIDLANDS, EndBiomes.endMidlands());
        BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.SMALL_END_ISLANDS, EndBiomes.smallEndIslands());
        return BuiltinRegistries.register(p_236653_, net.minecraft.world.level.biome.Biomes.END_BARRENS, EndBiomes.endBarrens());
    }
}
