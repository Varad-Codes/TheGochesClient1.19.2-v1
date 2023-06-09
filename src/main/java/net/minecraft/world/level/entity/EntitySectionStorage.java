package net.minecraft.world.level.entity;

import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongAVLTreeSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import java.util.Objects;
import java.util.PrimitiveIterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public class EntitySectionStorage<T extends EntityAccess>
{
    private final Class<T> entityClass;
    private final Long2ObjectFunction<Visibility> intialSectionVisibility;
    private final Long2ObjectMap<EntitySection<T>> sections = new Long2ObjectOpenHashMap<>();
    private final LongSortedSet sectionIds = new LongAVLTreeSet();
    private boolean updated;

    public EntitySectionStorage(Class<T> pEntityClass, Long2ObjectFunction<Visibility> pIntialSectionVisibility)
    {
        this.entityClass = pEntityClass;
        this.intialSectionVisibility = pIntialSectionVisibility;
    }

    public void forEachAccessibleNonEmptySection(AABB p_188363_, Consumer<EntitySection<T>> p_188364_)
    {
        int i = 2;
        int j = SectionPos.posToSectionCoord(p_188363_.minX - 2.0D);
        int k = SectionPos.posToSectionCoord(p_188363_.minY - 4.0D);
        int l = SectionPos.posToSectionCoord(p_188363_.minZ - 2.0D);
        int i1 = SectionPos.posToSectionCoord(p_188363_.maxX + 2.0D);
        int j1 = SectionPos.posToSectionCoord(p_188363_.maxY + 0.0D);
        int k1 = SectionPos.posToSectionCoord(p_188363_.maxZ + 2.0D);

        for (int l1 = j; l1 <= i1; ++l1)
        {
            long i2 = SectionPos.asLong(l1, 0, 0);
            long j2 = SectionPos.asLong(l1, -1, -1);
            LongIterator longiterator = this.sectionIds.subSet(i2, j2 + 1L).iterator();

            while (longiterator.hasNext())
            {
                long k2 = longiterator.nextLong();
                int l2 = SectionPos.y(k2);
                int i3 = SectionPos.z(k2);

                if (l2 >= k && l2 <= j1 && i3 >= l && i3 <= k1)
                {
                    EntitySection<T> entitysection = this.sections.get(k2);

                    if (entitysection != null && !entitysection.isEmpty() && entitysection.getStatus().isAccessible())
                    {
                        p_188364_.accept(entitysection);
                    }
                }
            }
        }
    }

    public LongStream getExistingSectionPositionsInChunk(long p_156862_)
    {
        int i = ChunkPos.getX(p_156862_);
        int j = ChunkPos.getZ(p_156862_);
        LongSortedSet longsortedset = this.getChunkSections(i, j);

        if (longsortedset.isEmpty())
        {
            return LongStream.empty();
        }
        else
        {
            PrimitiveIterator.OfLong oflong = longsortedset.iterator();
            return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(oflong, 1301), false);
        }
    }

    private LongSortedSet getChunkSections(int p_156859_, int p_156860_)
    {
        long i = SectionPos.asLong(p_156859_, 0, p_156860_);
        long j = SectionPos.asLong(p_156859_, -1, p_156860_);
        return this.sectionIds.subSet(i, j + 1L);
    }

    public Stream<EntitySection<T>> getExistingSectionsInChunk(long p_156889_)
    {
        return this.getExistingSectionPositionsInChunk(p_156889_).mapToObj(this.sections::get).filter(Objects::nonNull);
    }

    private static long getChunkKeyFromSectionKey(long p_156900_)
    {
        return ChunkPos.asLong(SectionPos.x(p_156900_), SectionPos.z(p_156900_));
    }

    public EntitySection<T> getOrCreateSection(long p_156894_)
    {
        int i = this.sections.size();
        EntitySection entitysection;

        try
        {
            entitysection = this.sections.computeIfAbsent(p_156894_, this::createSection);
        }
        finally
        {
            if (this.sections.size() != i)
            {
                this.updated = true;
            }
        }

        return entitysection;
    }

    @Nullable
    public EntitySection<T> getSection(long p_156896_)
    {
        return this.sections.get(p_156896_);
    }

    private EntitySection<T> createSection(long p_156902_)
    {
        long i = getChunkKeyFromSectionKey(p_156902_);
        Visibility visibility = this.intialSectionVisibility.get(i);
        this.sectionIds.add(p_156902_);
        return new EntitySection<>(this.entityClass, visibility);
    }

    public LongSet getAllChunksWithExistingSections()
    {
        LongSet longset = new LongOpenHashSet();
        this.sections.keySet().forEach((long p_156884_1_) ->
        {
            longset.add(getChunkKeyFromSectionKey(p_156884_1_));
        });
        return longset;
    }

    public void getEntities(AABB p_156891_, Consumer<T> p_156892_)
    {
        this.forEachAccessibleNonEmptySection(p_156891_, (p_188365_2_) ->
        {
            p_188365_2_.getEntities(p_156891_, p_156892_);
        });
    }

    public <U extends T> void getEntities(EntityTypeTest<T, U> p_156864_, AABB p_156865_, Consumer<U> p_156866_)
    {
        this.forEachAccessibleNonEmptySection(p_156865_, (p_188357_3_) ->
        {
            p_188357_3_.getEntities(p_156864_, p_156865_, p_156866_);
        });
    }

    public void remove(long p_156898_)
    {
        int i = this.sections.size();
        this.sections.remove(p_156898_);
        this.sectionIds.remove(p_156898_);

        if (this.sections.size() != i)
        {
            this.updated = true;
        }
    }

    @VisibleForDebug
    public int count()
    {
        return this.sectionIds.size();
    }

    public boolean isUpdated()
    {
        return this.updated;
    }

    public boolean resetUpdated()
    {
        boolean flag = this.updated;
        this.updated = false;
        return flag;
    }

    public LongSet getSectionKeys()
    {
        return this.sections.keySet();
    }
}
