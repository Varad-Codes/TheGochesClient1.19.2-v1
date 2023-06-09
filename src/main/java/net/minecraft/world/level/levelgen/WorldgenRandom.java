package net.minecraft.world.level.levelgen;

import java.util.function.LongFunction;
import net.minecraft.util.RandomSource;

public class WorldgenRandom extends LegacyRandomSource
{
    private final RandomSource randomSource;
    private int count;

    public WorldgenRandom(RandomSource p_224680_)
    {
        super(0L);
        this.randomSource = p_224680_;
    }

    public int getCount()
    {
        return this.count;
    }

    public RandomSource fork()
    {
        return this.randomSource.fork();
    }

    public PositionalRandomFactory forkPositional()
    {
        return this.randomSource.forkPositional();
    }

    public int next(int pBits)
    {
        ++this.count;
        RandomSource randomsource = this.randomSource;

        if (randomsource instanceof LegacyRandomSource legacyrandomsource)
        {
            return legacyrandomsource.next(pBits);
        }
        else
        {
            return (int)(this.randomSource.nextLong() >>> 64 - pBits);
        }
    }

    public synchronized void setSeed(long pSeed)
    {
        if (this.randomSource != null)
        {
            this.randomSource.setSeed(pSeed);
        }
    }

    public long setDecorationSeed(long pLevelSeed, int p_64692_, int pMinChunkBlockX)
    {
        this.setSeed(pLevelSeed);
        long i = this.nextLong() | 1L;
        long j = this.nextLong() | 1L;
        long k = (long)p_64692_ * i + (long)pMinChunkBlockX * j ^ pLevelSeed;
        this.setSeed(k);
        return k;
    }

    public void setFeatureSeed(long p_190065_, int p_190066_, int p_190067_)
    {
        long i = p_190065_ + (long)p_190066_ + (long)(10000 * p_190067_);
        this.setSeed(i);
    }

    public void setLargeFeatureSeed(long p_190069_, int p_190070_, int p_190071_)
    {
        this.setSeed(p_190069_);
        long i = this.nextLong();
        long j = this.nextLong();
        long k = (long)p_190070_ * i ^ (long)p_190071_ * j ^ p_190069_;
        this.setSeed(k);
    }

    public void setLargeFeatureWithSalt(long p_190059_, int p_190060_, int p_190061_, int p_190062_)
    {
        long i = (long)p_190060_ * 341873128712L + (long)p_190061_ * 132897987541L + p_190059_ + (long)p_190062_;
        this.setSeed(i);
    }

    public static RandomSource seedSlimeChunk(int pChunkX, int pChunkZ, long pLevelSeed, long p_224685_)
    {
        return RandomSource.create(pLevelSeed + (long)(pChunkX * pChunkX * 4987142) + (long)(pChunkX * 5947611) + (long)(pChunkZ * pChunkZ) * 4392871L + (long)(pChunkZ * 389711) ^ p_224685_);
    }

    public static enum Algorithm
    {
        LEGACY(LegacyRandomSource::new),
        XOROSHIRO(XoroshiroRandomSource::new);

        private final LongFunction<RandomSource> constructor;

        private Algorithm(LongFunction<RandomSource> p_190082_)
        {
            this.constructor = p_190082_;
        }

        public RandomSource newInstance(long p_224688_)
        {
            return this.constructor.apply(p_224688_);
        }
    }
}
