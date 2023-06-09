package net.minecraft.data;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.mojang.logging.LogUtils;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.WorldVersion;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;

public class HashCache
{
    static final Logger LOGGER = LogUtils.getLogger();
    private static final String HEADER_MARKER = "// ";
    private final Path rootDir;
    private final Path cacheDir;
    private final String versionId;
    private final Map<DataProvider, HashCache.ProviderCache> existingCaches;
    private final Map<DataProvider, HashCache.CacheUpdater> cachesToWrite = new HashMap<>();
    private final Set<Path> cachePaths = new HashSet<>();
    private final int initialCount;

    private Path getProviderCachePath(DataProvider p_236110_)
    {
        return this.cacheDir.resolve(Hashing.sha1().hashString(p_236110_.getName(), StandardCharsets.UTF_8).toString());
    }

    public HashCache(Path p_236087_, List<DataProvider> p_236088_, WorldVersion p_236089_) throws IOException
    {
        this.versionId = p_236089_.getName();
        this.rootDir = p_236087_;
        this.cacheDir = p_236087_.resolve(".cache");
        Files.createDirectories(this.cacheDir);
        Map<DataProvider, HashCache.ProviderCache> map = new HashMap<>();
        int i = 0;

        for (DataProvider dataprovider : p_236088_)
        {
            Path path = this.getProviderCachePath(dataprovider);
            this.cachePaths.add(path);
            HashCache.ProviderCache hashcache$providercache = readCache(p_236087_, path);
            map.put(dataprovider, hashcache$providercache);
            i += hashcache$providercache.count();
        }

        this.existingCaches = map;
        this.initialCount = i;
    }

    private static HashCache.ProviderCache readCache(Path p_236093_, Path p_236094_)
    {
        if (Files.isReadable(p_236094_))
        {
            try
            {
                return HashCache.ProviderCache.load(p_236093_, p_236094_);
            }
            catch (Exception exception)
            {
                LOGGER.warn("Failed to parse cache {}, discarding", p_236094_, exception);
            }
        }

        return new HashCache.ProviderCache("unknown");
    }

    public boolean shouldRunInThisVersion(DataProvider p_236091_)
    {
        HashCache.ProviderCache hashcache$providercache = (HashCache.ProviderCache)this.existingCaches.get(p_236091_);
        return hashcache$providercache == null || !hashcache$providercache.version.equals(this.versionId);
    }

    public CachedOutput getUpdater(DataProvider p_236108_)
    {
        return this.cachesToWrite.computeIfAbsent(p_236108_, (p_236112_) ->
        {
            HashCache.ProviderCache hashcache$providercache = (HashCache.ProviderCache)this.existingCaches.get(p_236112_);

            if (hashcache$providercache == null)
            {
                throw new IllegalStateException("Provider not registered: " + p_236112_.getName());
            }
            else {
                HashCache.CacheUpdater hashcache$cacheupdater = new HashCache.CacheUpdater(this.versionId, hashcache$providercache);
                this.existingCaches.put(p_236112_, hashcache$cacheupdater.newCache);
                return hashcache$cacheupdater;
            }
        });
    }

    public void purgeStaleAndWrite() throws IOException
    {
        MutableInt mutableint = new MutableInt();
        this.cachesToWrite.forEach((p_236100_, p_236101_) ->
        {
            Path path = this.getProviderCachePath(p_236100_);
            p_236101_.newCache.save(this.rootDir, path, DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now()) + "\t" + p_236100_.getName());
            mutableint.add(p_236101_.writes);
        });
        Set<Path> set = new HashSet<>();
        this.existingCaches.values().forEach((p_236097_) ->
        {
            set.addAll(p_236097_.data().keySet());
        });
        set.add(this.rootDir.resolve("version.json"));
        MutableInt mutableint1 = new MutableInt();
        MutableInt mutableint2 = new MutableInt();
        Stream<Path> stream = Files.walk(this.rootDir);

        try
        {
            stream.forEach((p_236106_) ->
            {
                if (!Files.isDirectory(p_236106_))
                {
                    if (!this.cachePaths.contains(p_236106_))
                    {
                        mutableint1.increment();

                        if (!set.contains(p_236106_))
                        {
                            try
                            {
                                Files.delete(p_236106_);
                            }
                            catch (IOException ioexception)
                            {
                                LOGGER.warn("Failed to delete file {}", p_236106_, ioexception);
                            }

                            mutableint2.increment();
                        }
                    }
                }
            });
        }
        catch (Throwable throwable1)
        {
            if (stream != null)
            {
                try
                {
                    stream.close();
                }
                catch (Throwable throwable)
                {
                    throwable1.addSuppressed(throwable);
                }
            }

            throw throwable1;
        }

        if (stream != null)
        {
            stream.close();
        }

        LOGGER.info("Caching: total files: {}, old count: {}, new count: {}, removed stale: {}, written: {}", mutableint1, this.initialCount, set.size(), mutableint2, mutableint);
    }

    static class CacheUpdater implements CachedOutput
    {
        private final HashCache.ProviderCache oldCache;
        final HashCache.ProviderCache newCache;
        int writes;

        CacheUpdater(String p_236117_, HashCache.ProviderCache p_236118_)
        {
            this.oldCache = p_236118_;
            this.newCache = new HashCache.ProviderCache(p_236117_);
        }

        private boolean shouldWrite(Path p_236120_, HashCode p_236121_)
        {
            return !Objects.equals(this.oldCache.get(p_236120_), p_236121_) || !Files.exists(p_236120_);
        }

        public void writeIfNeeded(Path p_236123_, byte[] p_236124_, HashCode p_236125_) throws IOException
        {
            if (this.shouldWrite(p_236123_, p_236125_))
            {
                ++this.writes;
                Files.createDirectories(p_236123_.getParent());
                Files.write(p_236123_, p_236124_);
            }

            this.newCache.put(p_236123_, p_236125_);
        }
    }

    static record ProviderCache(String version, Map<Path, HashCode> data)
    {
        ProviderCache(String p_236129_)
        {
            this(p_236129_, new HashMap<>());
        }
        @Nullable
        public HashCode get(Path p_236135_)
        {
            return this.data.get(p_236135_);
        }
        public void put(Path p_236137_, HashCode p_236138_)
        {
            this.data.put(p_236137_, p_236138_);
        }
        public int count()
        {
            return this.data.size();
        }
        public static HashCache.ProviderCache load(Path p_236140_, Path p_236141_) throws IOException
        {
            BufferedReader bufferedreader = Files.newBufferedReader(p_236141_, StandardCharsets.UTF_8);
            HashCache.ProviderCache hashcache$providercache;

            try
            {
                String s = bufferedreader.readLine();

                if (!s.startsWith("// "))
                {
                    throw new IllegalStateException("Missing cache file header");
                }

                String[] astring = s.substring("// ".length()).split("\t", 2);
                String s1 = astring[0];
                Map<Path, HashCode> map = new HashMap<>();
                bufferedreader.lines().forEach((p_236149_) ->
                {
                    int i = p_236149_.indexOf(32);
                    map.put(p_236140_.resolve(p_236149_.substring(i + 1)), HashCode.fromString(p_236149_.substring(0, i)));
                });
                hashcache$providercache = new HashCache.ProviderCache(s1, Map.copyOf(map));
            }
            catch (Throwable throwable1)
            {
                if (bufferedreader != null)
                {
                    try
                    {
                        bufferedreader.close();
                    }
                    catch (Throwable throwable)
                    {
                        throwable1.addSuppressed(throwable);
                    }
                }

                throw throwable1;
            }

            if (bufferedreader != null)
            {
                bufferedreader.close();
            }

            return hashcache$providercache;
        }
        public void save(Path p_236143_, Path p_236144_, String p_236145_)
        {
            try
            {
                BufferedWriter bufferedwriter = Files.newBufferedWriter(p_236144_, StandardCharsets.UTF_8);

                try
                {
                    bufferedwriter.write("// ");
                    bufferedwriter.write(this.version);
                    bufferedwriter.write(9);
                    bufferedwriter.write(p_236145_);
                    bufferedwriter.newLine();

                    for (Map.Entry<Path, HashCode> entry : this.data.entrySet())
                    {
                        bufferedwriter.write(entry.getValue().toString());
                        bufferedwriter.write(32);
                        bufferedwriter.write(p_236143_.relativize(entry.getKey()).toString());
                        bufferedwriter.newLine();
                    }
                }
                catch (Throwable throwable1)
                {
                    if (bufferedwriter != null)
                    {
                        try
                        {
                            bufferedwriter.close();
                        }
                        catch (Throwable throwable)
                        {
                            throwable1.addSuppressed(throwable);
                        }
                    }

                    throw throwable1;
                }

                if (bufferedwriter != null)
                {
                    bufferedwriter.close();
                }
            }
            catch (IOException ioexception)
            {
                HashCache.LOGGER.warn("Unable write cachefile {}: {}", p_236144_, ioexception);
            }
        }
    }
}
