package net.optifine.util;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.optifine.config.BiomeId;
import net.optifine.override.ChunkCacheOF;

public class BiomeUtils
{
    private static Registry<Biome> biomeRegistry = getBiomeRegistry(Minecraft.getInstance().level);
    public static Biome PLAINS = biomeRegistry.get(Biomes.PLAINS);
    public static Biome SUNFLOWER_PLAINS = biomeRegistry.get(Biomes.SUNFLOWER_PLAINS);
    public static Biome SNOWY_PLAINS = biomeRegistry.get(Biomes.SNOWY_PLAINS);
    public static Biome ICE_SPIKES = biomeRegistry.get(Biomes.ICE_SPIKES);
    public static Biome DESERT = biomeRegistry.get(Biomes.DESERT);
    public static Biome WINDSWEPT_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_HILLS);
    public static Biome WINDSWEPT_GRAVELLY_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_GRAVELLY_HILLS);
    public static Biome MUSHROOM_FIELDS = biomeRegistry.get(Biomes.MUSHROOM_FIELDS);
    public static Biome SWAMP = biomeRegistry.get(Biomes.SWAMP);
    public static Biome SWAMP_HILLS = SWAMP;
    public static Biome THE_VOID = biomeRegistry.get(Biomes.THE_VOID);

    public static void onWorldChanged(Level worldIn)
    {
        biomeRegistry = getBiomeRegistry(worldIn);
        PLAINS = biomeRegistry.get(Biomes.PLAINS);
        SUNFLOWER_PLAINS = biomeRegistry.get(Biomes.SUNFLOWER_PLAINS);
        SNOWY_PLAINS = biomeRegistry.get(Biomes.SNOWY_PLAINS);
        ICE_SPIKES = biomeRegistry.get(Biomes.ICE_SPIKES);
        DESERT = biomeRegistry.get(Biomes.DESERT);
        WINDSWEPT_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_HILLS);
        WINDSWEPT_GRAVELLY_HILLS = biomeRegistry.get(Biomes.WINDSWEPT_GRAVELLY_HILLS);
        MUSHROOM_FIELDS = biomeRegistry.get(Biomes.MUSHROOM_FIELDS);
        SWAMP = biomeRegistry.get(Biomes.SWAMP);
        SWAMP_HILLS = SWAMP;
        THE_VOID = biomeRegistry.get(Biomes.THE_VOID);
    }

    private static Biome getBiomeSafe(Registry<Biome> registry, ResourceKey<Biome> biomeKey, Supplier<Biome> biomeDefault)
    {
        Biome biome = registry.get(biomeKey);

        if (biome == null)
        {
            biome = biomeDefault.get();
        }

        return biome;
    }

    public static Registry<Biome> getBiomeRegistry(Level worldIn)
    {
        return worldIn != null ? worldIn.registryAccess().ownedRegistryOrThrow(Registry.BIOME_REGISTRY) : BuiltinRegistries.BIOME;
    }

    public static Registry<Biome> getBiomeRegistry()
    {
        return biomeRegistry;
    }

    public static ResourceLocation getLocation(Biome biome)
    {
        return getBiomeRegistry().getKey(biome);
    }

    public static int getId(Biome biome)
    {
        return getBiomeRegistry().getId(biome);
    }

    public static int getId(ResourceLocation loc)
    {
        Biome biome = getBiome(loc);
        return getBiomeRegistry().getId(biome);
    }

    public static BiomeId getBiomeId(ResourceLocation loc)
    {
        return BiomeId.make(loc);
    }

    public static Biome getBiome(ResourceLocation loc)
    {
        return getBiomeRegistry().get(loc);
    }

    public static Set<ResourceLocation> getLocations()
    {
        return getBiomeRegistry().keySet();
    }

    public static List<Biome> getBiomes()
    {
        return Lists.newArrayList(biomeRegistry);
    }

    public static List<BiomeId> getBiomeIds()
    {
        return getBiomeIds(getLocations());
    }

    public static List<BiomeId> getBiomeIds(Collection<ResourceLocation> locations)
    {
        List<BiomeId> list = new ArrayList<>();

        for (ResourceLocation resourcelocation : locations)
        {
            BiomeId biomeid = BiomeId.make(resourcelocation);

            if (biomeid != null)
            {
                list.add(biomeid);
            }
        }

        return list;
    }

    public static Biome getBiome(BlockAndTintGetter lightReader, BlockPos blockPos)
    {
        Biome biome = PLAINS;

        if (lightReader instanceof ChunkCacheOF)
        {
            biome = ((ChunkCacheOF)lightReader).getBiome(blockPos);
        }
        else if (lightReader instanceof LevelReader)
        {
            biome = ((LevelReader)lightReader).getBiome(blockPos).value();
        }

        return biome;
    }

    public static BiomeCategory getBiomeCategory(Holder<Biome> holder)
    {
        if (holder.value() == THE_VOID)
        {
            return BiomeCategory.NONE;
        }
        else if (holder.is(BiomeTags.IS_TAIGA))
        {
            return BiomeCategory.TAIGA;
        }
        else if (holder.value() != WINDSWEPT_HILLS && holder.value() != WINDSWEPT_GRAVELLY_HILLS)
        {
            if (holder.is(BiomeTags.IS_JUNGLE))
            {
                return BiomeCategory.JUNGLE;
            }
            else if (holder.is(BiomeTags.IS_BADLANDS))
            {
                return BiomeCategory.MESA;
            }
            else if (holder.value() != PLAINS && holder.value() != PLAINS)
            {
                if (holder.is(BiomeTags.IS_SAVANNA))
                {
                    return BiomeCategory.SAVANNA;
                }
                else if (holder.value() != SNOWY_PLAINS && holder.value() != ICE_SPIKES)
                {
                    if (holder.is(BiomeTags.IS_END))
                    {
                        return BiomeCategory.THEEND;
                    }
                    else if (holder.is(BiomeTags.IS_BEACH))
                    {
                        return BiomeCategory.BEACH;
                    }
                    else if (holder.is(BiomeTags.IS_FOREST))
                    {
                        return BiomeCategory.FOREST;
                    }
                    else if (holder.is(BiomeTags.IS_OCEAN))
                    {
                        return BiomeCategory.OCEAN;
                    }
                    else if (holder.value() == DESERT)
                    {
                        return BiomeCategory.DESERT;
                    }
                    else if (holder.is(BiomeTags.IS_RIVER))
                    {
                        return BiomeCategory.RIVER;
                    }
                    else if (holder.value() != SWAMP && holder.value() != SWAMP_HILLS)
                    {
                        if (holder.value() == MUSHROOM_FIELDS)
                        {
                            return BiomeCategory.MUSHROOM;
                        }
                        else if (holder.is(BiomeTags.IS_NETHER))
                        {
                            return BiomeCategory.NETHER;
                        }
                        else if (holder.is(BiomeTags.PLAYS_UNDERWATER_MUSIC))
                        {
                            return BiomeCategory.UNDERGROUND;
                        }
                        else
                        {
                            return holder.is(BiomeTags.IS_MOUNTAIN) ? BiomeCategory.MOUNTAIN : BiomeCategory.PLAINS;
                        }
                    }
                    else
                    {
                        return BiomeCategory.SWAMP;
                    }
                }
                else
                {
                    return BiomeCategory.ICY;
                }
            }
            else
            {
                return BiomeCategory.PLAINS;
            }
        }
        else
        {
            return BiomeCategory.EXTREME_HILLS;
        }
    }
}
