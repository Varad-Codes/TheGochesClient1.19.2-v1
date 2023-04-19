package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.tags.PoiTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;

public class PoiManager extends SectionStorage<PoiSection>
{
    public static final int MAX_VILLAGE_DISTANCE = 6;
    public static final int VILLAGE_SECTION_SIZE = 1;
    private final PoiManager.DistanceTracker distanceTracker;
    private final LongSet loadedChunks = new LongOpenHashSet();

    public PoiManager(Path p_217869_, DataFixer p_217870_, boolean p_217871_, RegistryAccess p_217872_, LevelHeightAccessor p_217873_)
    {
        super(p_217869_, PoiSection::codec, PoiSection::new, p_217870_, DataFixTypes.POI_CHUNK, p_217871_, p_217872_, p_217873_);
        this.distanceTracker = new PoiManager.DistanceTracker();
    }

    public void add(BlockPos pPos, Holder<PoiType> pPoiType)
    {
        this.getOrCreate(SectionPos.asLong(pPos)).add(pPos, pPoiType);
    }

    public void remove(BlockPos pPos)
    {
        this.getOrLoad(SectionPos.asLong(pPos)).ifPresent((p_148657_) ->
        {
            p_148657_.remove(pPos);
        });
    }

    public long getCountInRange(Predicate<Holder<PoiType>> p_27122_, BlockPos p_27123_, int p_27124_, PoiManager.Occupancy p_27125_)
    {
        return this.getInRange(p_27122_, p_27123_, p_27124_, p_27125_).count();
    }

    public boolean existsAtPosition(ResourceKey<PoiType> pType, BlockPos pPos)
    {
        return this.exists(pPos, (p_217879_) ->
        {
            return p_217879_.is(pType);
        });
    }

    public Stream<PoiRecord> getInSquare(Predicate<Holder<PoiType>> pTypePredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus)
    {
        int i = Math.floorDiv(pDistance, 16) + 1;
        return ChunkPos.rangeClosed(new ChunkPos(pPos), i).flatMap((p_217938_) ->
        {
            return this.getInChunk(pTypePredicate, p_217938_, pStatus);
        }).filter((p_217971_) ->
        {
            BlockPos blockpos = p_217971_.getPos();
            return Math.abs(blockpos.getX() - pPos.getX()) <= pDistance && Math.abs(blockpos.getZ() - pPos.getZ()) <= pDistance;
        });
    }

    public Stream<PoiRecord> getInRange(Predicate<Holder<PoiType>> p_27182_, BlockPos p_27183_, int p_27184_, PoiManager.Occupancy p_27185_)
    {
        int i = p_27184_ * p_27184_;
        return this.getInSquare(p_27182_, p_27183_, p_27184_, p_27185_).filter((p_217906_) ->
        {
            return p_217906_.getPos().distSqr(p_27183_) <= (double)i;
        });
    }

    @VisibleForDebug
    public Stream<PoiRecord> getInChunk(Predicate<Holder<PoiType>> p_27118_, ChunkPos p_27119_, PoiManager.Occupancy p_27120_)
    {
        return IntStream.range(this.levelHeightAccessor.getMinSection(), this.levelHeightAccessor.getMaxSection()).boxed().map((p_217886_) ->
        {
            return this.getOrLoad(SectionPos.of(p_27119_, p_217886_).asLong());
        }).filter(Optional::isPresent).flatMap((p_217942_) ->
        {
            return p_217942_.get().getRecords(p_27118_, p_27120_);
        });
    }

    public Stream<BlockPos> findAll(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus)
    {
        return this.getInRange(pTypePredicate, pPos, pDistance, pStatus).map(PoiRecord::getPos).filter(pPosPredicate);
    }

    public Stream<Pair<Holder<PoiType>, BlockPos>> findAllWithType(Predicate<Holder<PoiType>> p_217984_, Predicate<BlockPos> p_217985_, BlockPos p_217986_, int p_217987_, PoiManager.Occupancy p_217988_)
    {
        return this.getInRange(p_217984_, p_217986_, p_217987_, p_217988_).filter((p_217982_) ->
        {
            return p_217985_.test(p_217982_.getPos());
        }).map((p_217990_) ->
        {
            return Pair.of(p_217990_.getPoiType(), p_217990_.getPos());
        });
    }

    public Stream<Pair<Holder<PoiType>, BlockPos>> findAllClosestFirstWithType(Predicate<Holder<PoiType>> p_217995_, Predicate<BlockPos> p_217996_, BlockPos p_217997_, int p_217998_, PoiManager.Occupancy p_217999_)
    {
        return this.findAllWithType(p_217995_, p_217996_, p_217997_, p_217998_, p_217999_).sorted(Comparator.comparingDouble((p_217915_) ->
        {
            return p_217915_.getSecond().distSqr(p_217997_);
        }));
    }

    public Optional<BlockPos> find(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, BlockPos pPos, int pDistance, PoiManager.Occupancy pStatus)
    {
        return this.findAll(pTypePredicate, pPosPredicate, pPos, pDistance, pStatus).findFirst();
    }

    public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> p_27193_, BlockPos p_27194_, int p_27195_, PoiManager.Occupancy p_27196_)
    {
        return this.getInRange(p_27193_, p_27194_, p_27195_, p_27196_).map(PoiRecord::getPos).min(Comparator.comparingDouble((p_217977_) ->
        {
            return p_217977_.distSqr(p_27194_);
        }));
    }

    public Optional<Pair<Holder<PoiType>, BlockPos>> findClosestWithType(Predicate<Holder<PoiType>> p_218003_, BlockPos p_218004_, int p_218005_, PoiManager.Occupancy p_218006_)
    {
        return this.getInRange(p_218003_, p_218004_, p_218005_, p_218006_).min(Comparator.comparingDouble((p_217909_) ->
        {
            return p_217909_.getPos().distSqr(p_218004_);
        })).map((p_217959_) ->
        {
            return Pair.of(p_217959_.getPoiType(), p_217959_.getPos());
        });
    }

    public Optional<BlockPos> findClosest(Predicate<Holder<PoiType>> p_148659_, Predicate<BlockPos> p_148660_, BlockPos p_148661_, int p_148662_, PoiManager.Occupancy p_148663_)
    {
        return this.getInRange(p_148659_, p_148661_, p_148662_, p_148663_).map(PoiRecord::getPos).filter(p_148660_).min(Comparator.comparingDouble((p_217918_) ->
        {
            return p_217918_.distSqr(p_148661_);
        }));
    }

    public Optional<BlockPos> take(Predicate<Holder<PoiType>> pTypePredicate, BiPredicate<Holder<PoiType>, BlockPos> pPosPredicate, BlockPos pPos, int pDistance)
    {
        return this.getInRange(pTypePredicate, pPos, pDistance, PoiManager.Occupancy.HAS_SPACE).filter((p_217934_) ->
        {
            return pPosPredicate.test(p_217934_.getPoiType(), p_217934_.getPos());
        }).findFirst().map((p_217881_) ->
        {
            p_217881_.acquireTicket();
            return p_217881_.getPos();
        });
    }

    public Optional<BlockPos> getRandom(Predicate<Holder<PoiType>> pTypePredicate, Predicate<BlockPos> pPosPredicate, PoiManager.Occupancy pStatus, BlockPos pPos, int pDistance, RandomSource pRand)
    {
        List<PoiRecord> list = Util.toShuffledList(this.getInRange(pTypePredicate, pPos, pDistance, pStatus), pRand);
        return list.stream().filter((p_217945_) ->
        {
            return pPosPredicate.test(p_217945_.getPos());
        }).findFirst().map(PoiRecord::getPos);
    }

    public boolean release(BlockPos pPos)
    {
        return this.getOrLoad(SectionPos.asLong(pPos)).map((p_217993_) ->
        {
            return p_217993_.release(pPos);
        }).orElseThrow(() ->
        {
            return Util.pauseInIde(new IllegalStateException("POI never registered at " + pPos));
        });
    }

    public boolean exists(BlockPos p_27092_, Predicate<Holder<PoiType>> p_27093_)
    {
        return this.getOrLoad(SectionPos.asLong(p_27092_)).map((p_217925_) ->
        {
            return p_217925_.exists(p_27092_, p_27093_);
        }).orElse(false);
    }

    public Optional<Holder<PoiType>> getType(BlockPos pPos)
    {
        return this.getOrLoad(SectionPos.asLong(pPos)).flatMap((p_217974_) ->
        {
            return p_217974_.getType(pPos);
        });
    }

    @Deprecated
    @VisibleForDebug
    public int getFreeTickets(BlockPos p_148654_)
    {
        return this.getOrLoad(SectionPos.asLong(p_148654_)).map((p_217912_) ->
        {
            return p_217912_.getFreeTickets(p_148654_);
        }).orElse(0);
    }

    public int sectionsToVillage(SectionPos pSectionPos)
    {
        this.distanceTracker.runAllUpdates();
        return this.distanceTracker.getLevel(pSectionPos.asLong());
    }

    boolean isVillageCenter(long p_27198_)
    {
        Optional<PoiSection> optional = this.get(p_27198_);
        return optional == null ? false : optional.map((p_217883_) ->
        {
            return p_217883_.getRecords((p_217927_) -> {
                return p_217927_.is(PoiTypeTags.VILLAGE);
            }, PoiManager.Occupancy.IS_OCCUPIED).findAny().isPresent();
        }).orElse(false);
    }

    public void tick(BooleanSupplier p_27105_)
    {
        super.tick(p_27105_);
        this.distanceTracker.runAllUpdates();
    }

    protected void setDirty(long pSectionPos)
    {
        super.setDirty(pSectionPos);
        this.distanceTracker.update(pSectionPos, this.distanceTracker.getLevelFromSource(pSectionPos), false);
    }

    protected void onSectionLoad(long p_27145_)
    {
        this.distanceTracker.update(p_27145_, this.distanceTracker.getLevelFromSource(p_27145_), false);
    }

    public void checkConsistencyWithBlocks(ChunkPos pPos, LevelChunkSection pSection)
    {
        SectionPos sectionpos = SectionPos.of(pPos, SectionPos.blockToSectionCoord(pSection.bottomBlockY()));
        Util.ifElse(this.getOrLoad(sectionpos.asLong()), (p_217898_) ->
        {
            p_217898_.refresh((p_217967_) -> {
                if (mayHavePoi(pSection))
                {
                    this.updateFromSection(pSection, sectionpos, p_217967_);
                }
            });
        }, () ->
        {
            if (mayHavePoi(pSection))
            {
                PoiSection poisection = this.getOrCreate(sectionpos.asLong());
                this.updateFromSection(pSection, sectionpos, poisection::add);
            }
        });
    }

    private static boolean mayHavePoi(LevelChunkSection pSection)
    {
        return pSection.maybeHas(PoiTypes.ALL_STATES::contains);
    }

    private void updateFromSection(LevelChunkSection pSection, SectionPos pSectionPos, BiConsumer<BlockPos, Holder<PoiType>> pPosToTypeConsumer)
    {
        pSectionPos.blocksInside().forEach((p_217902_) ->
        {
            BlockState blockstate = pSection.getBlockState(SectionPos.sectionRelative(p_217902_.getX()), SectionPos.sectionRelative(p_217902_.getY()), SectionPos.sectionRelative(p_217902_.getZ()));
            PoiTypes.forState(blockstate).ifPresent((p_217931_) -> {
                pPosToTypeConsumer.accept(p_217902_, p_217931_);
            });
        });
    }

    public void ensureLoadedAndValid(LevelReader pLevelReader, BlockPos pPos, int pCoordinateOffset)
    {
        SectionPos.aroundChunk(new ChunkPos(pPos), Math.floorDiv(pCoordinateOffset, 16), this.levelHeightAccessor.getMinSection(), this.levelHeightAccessor.getMaxSection()).map((p_217979_) ->
        {
            return Pair.of(p_217979_, this.getOrLoad(p_217979_.asLong()));
        }).filter((p_217963_) ->
        {
            return !p_217963_.getSecond().map(PoiSection::isValid).orElse(false);
        }).map((p_217891_) ->
        {
            return p_217891_.getFirst().chunk();
        }).filter((p_217961_) ->
        {
            return this.loadedChunks.add(p_217961_.toLong());
        }).forEach((p_217889_) ->
        {
            pLevelReader.getChunk(p_217889_.x, p_217889_.z, ChunkStatus.EMPTY);
        });
    }

    final class DistanceTracker extends SectionTracker
    {
        private final Long2ByteMap levels = new Long2ByteOpenHashMap();

        protected DistanceTracker()
        {
            super(7, 16, 256);
            this.levels.defaultReturnValue((byte)7);
        }

        protected int getLevelFromSource(long pPos)
        {
            return PoiManager.this.isVillageCenter(pPos) ? 0 : 7;
        }

        protected int getLevel(long pSectionPos)
        {
            return this.levels.get(pSectionPos);
        }

        protected void setLevel(long pSectionPos, int p_27206_)
        {
            if (p_27206_ > 6)
            {
                this.levels.remove(pSectionPos);
            }
            else
            {
                this.levels.put(pSectionPos, (byte)p_27206_);
            }
        }

        public void runAllUpdates()
        {
            super.runUpdates(Integer.MAX_VALUE);
        }
    }

    public static enum Occupancy
    {
        HAS_SPACE(PoiRecord::hasSpace),
        IS_OCCUPIED(PoiRecord::isOccupied),
        ANY((p_27223_) -> {
            return true;
        });

        private final Predicate <? super PoiRecord > test;

        private Occupancy(Predicate <? super PoiRecord > p_27220_)
        {
            this.test = p_27220_;
        }

        public Predicate <? super PoiRecord > getTest()
        {
            return this.test;
        }
    }
}
