package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public class DebugLevelSource extends ChunkGenerator
{
    public static final Codec<DebugLevelSource> CODEC = RecordCodecBuilder.create((p_208215_) ->
    {
        return commonCodec(p_208215_).and(RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter((p_208210_) -> {
            return p_208210_.biomes;
        })).apply(p_208215_, p_208215_.stable(DebugLevelSource::new));
    });
    private static final int BLOCK_MARGIN = 2;
    private static final List<BlockState> ALL_BLOCKS = StreamSupport.stream(Registry.BLOCK.spliterator(), false).flatMap((p_208208_) ->
    {
        return p_208208_.getStateDefinition().getPossibleStates().stream();
    }).collect(Collectors.toList());
    private static final int GRID_WIDTH = Mth.ceil(Mth.sqrt((float)ALL_BLOCKS.size()));
    private static final int GRID_HEIGHT = Mth.ceil((float)ALL_BLOCKS.size() / (float)GRID_WIDTH);
    protected static final BlockState AIR = Blocks.AIR.defaultBlockState();
    protected static final BlockState BARRIER = Blocks.BARRIER.defaultBlockState();
    public static final int HEIGHT = 70;
    public static final int BARRIER_HEIGHT = 60;
    private final Registry<Biome> biomes;

    public DebugLevelSource(Registry<StructureSet> p_208205_, Registry<Biome> p_208206_)
    {
        super(p_208205_, Optional.empty(), new FixedBiomeSource(p_208206_.getOrCreateHolderOrThrow(Biomes.PLAINS)));
        this.biomes = p_208206_;
    }

    public Registry<Biome> biomes()
    {
        return this.biomes;
    }

    protected Codec <? extends ChunkGenerator > codec()
    {
        return CODEC;
    }

    public void buildSurface(WorldGenRegion p_223978_, StructureManager p_223979_, RandomState p_223980_, ChunkAccess p_223981_)
    {
    }

    public void applyBiomeDecoration(WorldGenLevel p_223983_, ChunkAccess p_223984_, StructureManager p_223985_)
    {
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
        ChunkPos chunkpos = p_223984_.getPos();
        int i = chunkpos.x;
        int j = chunkpos.z;

        for (int k = 0; k < 16; ++k)
        {
            for (int l = 0; l < 16; ++l)
            {
                int i1 = SectionPos.sectionToBlockCoord(i, k);
                int j1 = SectionPos.sectionToBlockCoord(j, l);
                p_223983_.setBlock(blockpos$mutableblockpos.set(i1, 60, j1), BARRIER, 2);
                BlockState blockstate = getBlockStateFor(i1, j1);
                p_223983_.setBlock(blockpos$mutableblockpos.set(i1, 70, j1), blockstate, 2);
            }
        }
    }

    public CompletableFuture<ChunkAccess> fillFromNoise(Executor p_223991_, Blender p_223992_, RandomState p_223993_, StructureManager p_223994_, ChunkAccess p_223995_)
    {
        return CompletableFuture.completedFuture(p_223995_);
    }

    public int getBaseHeight(int p_223964_, int p_223965_, Heightmap.Types p_223966_, LevelHeightAccessor p_223967_, RandomState p_223968_)
    {
        return 0;
    }

    public NoiseColumn getBaseColumn(int p_223959_, int p_223960_, LevelHeightAccessor p_223961_, RandomState p_223962_)
    {
        return new NoiseColumn(0, new BlockState[0]);
    }

    public void addDebugScreenInfo(List<String> p_223987_, RandomState p_223988_, BlockPos p_223989_)
    {
    }

    public static BlockState getBlockStateFor(int pChunkX, int pChunkZ)
    {
        BlockState blockstate = AIR;

        if (pChunkX > 0 && pChunkZ > 0 && pChunkX % 2 != 0 && pChunkZ % 2 != 0)
        {
            pChunkX /= 2;
            pChunkZ /= 2;

            if (pChunkX <= GRID_WIDTH && pChunkZ <= GRID_HEIGHT)
            {
                int i = Mth.abs(pChunkX * GRID_WIDTH + pChunkZ);

                if (i < ALL_BLOCKS.size())
                {
                    blockstate = ALL_BLOCKS.get(i);
                }
            }
        }

        return blockstate;
    }

    public void applyCarvers(WorldGenRegion p_223970_, long p_223971_, RandomState p_223972_, BiomeManager p_223973_, StructureManager p_223974_, ChunkAccess p_223975_, GenerationStep.Carving p_223976_)
    {
    }

    public void spawnOriginalMobs(WorldGenRegion pLevel)
    {
    }

    public int getMinY()
    {
        return 0;
    }

    public int getGenDepth()
    {
        return 384;
    }

    public int getSeaLevel()
    {
        return 63;
    }
}
