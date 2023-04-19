package net.minecraft.server.packs;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystemAlreadyExistsException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.resource.ResourceCacheManager;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import org.slf4j.Logger;

public class VanillaPackResources implements PackResources
{
    @Nullable
    public static Path generatedDir;
    private static final Logger LOGGER = LogUtils.getLogger();
    public static Class<?> clientObject;
    private static final Map<PackType, Path> ROOT_DIR_BY_TYPE = Util.make(() ->
    {
        synchronized (VanillaPackResources.class)
        {
            ImmutableMap.Builder<PackType, Path> builder = ImmutableMap.builder();

            for (PackType packtype : PackType.values())
            {
                String s = "/" + packtype.getDirectory() + "/.mcassetsroot";
                URL url = VanillaPackResources.class.getResource(s);

                if (url == null)
                {
                    LOGGER.error("File {} does not exist in classpath", (Object)s);
                }
                else
                {
                    try
                    {
                        URI uri = url.toURI();
                        String s1 = uri.getScheme();

                        if (!"jar".equals(s1) && !"file".equals(s1))
                        {
                            LOGGER.warn("Assets URL '{}' uses unexpected schema", (Object)uri);
                        }

                        Path path = safeGetPath(uri);
                        builder.put(packtype, path.getParent());
                    }
                    catch (Exception exception1)
                    {
                        LOGGER.error("Couldn't resolve path to vanilla assets", (Throwable)exception1);
                    }
                }
            }

            return builder.build();
        }
    });
    public final PackMetadataSection packMetadata;
    public final Set<String> namespaces;
    private static final boolean ON_WINDOWS = Util.getPlatform() == Util.OS.WINDOWS;
    private static final boolean FORGE = Reflector.ForgeHooksClient.exists();
    private final ResourceCacheManager cacheManager = new ResourceCacheManager(false, ForgeConfig.COMMON.indexVanillaPackCachesOnThread, (packType, namespace) ->
    {
        return ROOT_DIR_BY_TYPE.get(packType).resolve(namespace);
    });

    private static Path safeGetPath(URI p_182298_) throws IOException
    {
        try
        {
            return Paths.get(p_182298_);
        }
        catch (FileSystemNotFoundException filesystemnotfoundexception)
        {
        }
        catch (Throwable throwable)
        {
            LOGGER.warn("Unable to get path for: {}", p_182298_, throwable);
        }

        try
        {
            FileSystems.newFileSystem(p_182298_, Collections.emptyMap());
        }
        catch (FileSystemAlreadyExistsException filesystemalreadyexistsexception)
        {
        }

        return Paths.get(p_182298_);
    }

    public VanillaPackResources(PackMetadataSection p_143761_, String... p_143762_)
    {
        this.packMetadata = p_143761_;
        this.namespaces = ImmutableSet.copyOf(p_143762_);
    }

    public InputStream getRootResource(String pFileName) throws IOException
    {
        if (!pFileName.contains("/") && !pFileName.contains("\\"))
        {
            if (generatedDir != null)
            {
                Path path = generatedDir.resolve(pFileName);

                if (Files.exists(path))
                {
                    return Files.newInputStream(path);
                }
            }

            return this.getResourceAsStream(pFileName);
        }
        else
        {
            throw new IllegalArgumentException("Root resources can only be filenames, not paths (no / allowed!)");
        }
    }

    public InputStream getResource(PackType pType, ResourceLocation pLocation) throws IOException
    {
        InputStream inputstream = this.getResourceAsStream(pType, pLocation);

        if (inputstream != null)
        {
            return inputstream;
        }
        else
        {
            throw new FileNotFoundException(pLocation.getPath());
        }
    }

    public Collection<ResourceLocation> getResources(PackType p_215346_, String p_215347_, String p_215348_, Predicate<ResourceLocation> p_215349_)
    {
        Set<ResourceLocation> set = Sets.newHashSet();

        if (generatedDir != null)
        {
            try
            {
                getResources(set, p_215347_, generatedDir.resolve(p_215346_.getDirectory()), p_215348_, p_215349_);
            }
            catch (IOException ioexception2)
            {
            }

            if (p_215346_ == PackType.CLIENT_RESOURCES)
            {
                Enumeration<URL> enumeration = null;

                try
                {
                    enumeration = clientObject.getClassLoader().getResources(p_215346_.getDirectory() + "/");
                }
                catch (IOException ioexception1)
                {
                }

                while (enumeration != null && enumeration.hasMoreElements())
                {
                    try
                    {
                        URI uri = enumeration.nextElement().toURI();

                        if ("file".equals(uri.getScheme()))
                        {
                            getResources(set, p_215347_, Paths.get(uri), p_215348_, p_215349_);
                        }
                    }
                    catch (URISyntaxException | IOException ioexception1)
                    {
                    }
                }
            }
        }

        try
        {
            Path path = ROOT_DIR_BY_TYPE.get(p_215346_);

            if (path != null)
            {
                if (ResourceCacheManager.shouldUseCache() && this.cacheManager.hasCached(p_215346_, p_215347_))
                {
                    set.addAll(this.cacheManager.getResources(p_215346_, p_215347_, path.getFileSystem().getPath(p_215348_), p_215349_));
                }
                else
                {
                    getResources(set, p_215347_, path, p_215348_, p_215349_);
                }
            }
            else
            {
                LOGGER.error("Can't access assets root for type: {}", (Object)p_215346_);
            }
        }
        catch (FileNotFoundException | NoSuchFileException nosuchfileexception)
        {
        }
        catch (IOException ioexception3)
        {
            LOGGER.error("Couldn't get a list of all vanilla resources", (Throwable)ioexception3);
        }

        return set;
    }

    private static void getResources(Collection<ResourceLocation> p_215358_, String pType, Path pNamespace, String pPath, Predicate<ResourceLocation> pMaxDepth) throws IOException
    {
        Path path = pNamespace.resolve(pType);
        Stream<Path> stream = Files.walk(path.resolve(pPath));

        try
        {
            stream.filter((p_215350_0_) ->
            {
                return !p_215350_0_.endsWith(".mcmeta") && Files.isRegularFile(p_215350_0_);
            }).<ResourceLocation>mapMulti((p_242539_2_, p_242539_3_) ->
            {
                String s = path.relativize(p_242539_2_).toString().replaceAll("\\\\", "/");
                ResourceLocation resourcelocation = ResourceLocation.tryBuild(pType, s);

                if (resourcelocation == null)
                {
                    Util.logAndPauseIfInIde(String.format(Locale.ROOT, "Invalid path in datapack: %s:%s, ignoring", pType, s));
                }
                else {
                    p_242539_3_.accept(resourcelocation);
                }
            }).filter(pMaxDepth).forEach(p_215358_::add);
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
    }

    @Nullable
    protected InputStream getResourceAsStream(PackType pType, ResourceLocation pLocation)
    {
        String s = createPath(pType, pLocation);
        InputStream inputstream = ReflectorForge.getOptiFineResourceStream(s);

        if (inputstream != null)
        {
            return inputstream;
        }
        else
        {
            if (generatedDir != null)
            {
                Path path = generatedDir.resolve(pType.getDirectory() + "/" + pLocation.getNamespace() + "/" + pLocation.getPath());

                if (Files.exists(path))
                {
                    try
                    {
                        return Files.newInputStream(path);
                    }
                    catch (IOException ioexception1)
                    {
                    }
                }
            }

            try
            {
                URL url = VanillaPackResources.class.getResource(s);
                return isResourceUrlValid(s, url) ? (FORGE ? this.getExtraInputStream(pType, s) : url.openStream()) : null;
            }
            catch (IOException ioexception1)
            {
                return VanillaPackResources.class.getResourceAsStream(s);
            }
        }
    }

    private static String createPath(PackType pPackType, ResourceLocation pLocation)
    {
        return "/" + pPackType.getDirectory() + "/" + pLocation.getNamespace() + "/" + pLocation.getPath();
    }

    private static boolean isResourceUrlValid(String pPath, @Nullable URL pUrl) throws IOException
    {
        return pUrl != null && (pUrl.getProtocol().equals("jar") || validatePath(new File(pUrl.getFile()), pPath));
    }

    @Nullable
    protected InputStream getResourceAsStream(String pPath)
    {
        return FORGE ? this.getExtraInputStream(PackType.SERVER_DATA, "/" + pPath) : VanillaPackResources.class.getResourceAsStream("/" + pPath);
    }

    public boolean hasResource(PackType pType, ResourceLocation pLocation)
    {
        String s = createPath(pType, pLocation);
        InputStream inputstream = ReflectorForge.getOptiFineResourceStream(s);

        if (inputstream != null)
        {
            return true;
        }
        else
        {
            if (generatedDir != null)
            {
                Path path = generatedDir.resolve(pType.getDirectory() + "/" + pLocation.getNamespace() + "/" + pLocation.getPath());

                if (Files.exists(path))
                {
                    return true;
                }
            }

            try
            {
                URL url = VanillaPackResources.class.getResource(s);
                return isResourceUrlValid(s, url);
            }
            catch (IOException ioexception1)
            {
                return false;
            }
        }
    }

    public Set<String> getNamespaces(PackType pType)
    {
        return this.namespaces;
    }

    @Nullable
    public <T> T getMetadataSection(MetadataSectionSerializer<T> pDeserializer) throws IOException
    {
        try
        {
            InputStream inputstream = this.getRootResource("pack.mcmeta");
            Object object;
            label61:
            {
                try
                {
                    if (inputstream != null)
                    {
                        T t = AbstractPackResources.getMetadataFromStream(pDeserializer, inputstream);

                        if (t != null)
                        {
                            object = t;
                            break label61;
                        }
                    }
                }
                catch (Throwable throwable11)
                {
                    if (inputstream != null)
                    {
                        try
                        {
                            inputstream.close();
                        }
                        catch (Throwable throwable)
                        {
                            throwable11.addSuppressed(throwable);
                        }
                    }

                    throw throwable11;
                }

                if (inputstream != null)
                {
                    inputstream.close();
                }

                return (T)(pDeserializer == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
            }

            if (inputstream != null)
            {
                inputstream.close();
            }

            return (T)object;
        }
        catch (RuntimeException | FileNotFoundException filenotfoundexception)
        {
            return (T)(pDeserializer == PackMetadataSection.SERIALIZER ? this.packMetadata : null);
        }
    }

    public String getName()
    {
        return "Default";
    }

    public void close()
    {
    }

    public ResourceProvider asProvider()
    {
        return (p_215343_1_) ->
        {
            return Optional.of(new Resource(this.getName(), () -> {
                return this.getResource(PackType.CLIENT_RESOURCES, p_215343_1_);
            }));
        };
    }

    private static boolean validatePath(File file, String path) throws IOException
    {
        String s = file.getPath();

        if (s.startsWith("file:"))
        {
            if (ON_WINDOWS)
            {
                s = s.replace("\\", "/");
            }

            return s.endsWith(path);
        }
        else
        {
            return FolderPackResources.validatePath(file, path);
        }
    }

    public void initForNamespace(String nameSpace)
    {
        if (ResourceCacheManager.shouldUseCache())
        {
            this.cacheManager.index(nameSpace);
        }
    }

    public void init(PackType packType)
    {
        this.initForNamespace("minecraft");
        this.initForNamespace("realms");
    }

    private InputStream getExtraInputStream(PackType type, String resource)
    {
        try
        {
            Path path = ROOT_DIR_BY_TYPE.get(type);
            return path != null ? Files.newInputStream(path.resolve(resource)) : VanillaPackResources.class.getResourceAsStream(resource);
        }
        catch (IOException ioexception)
        {
            return VanillaPackResources.class.getResourceAsStream(resource);
        }
    }
}
