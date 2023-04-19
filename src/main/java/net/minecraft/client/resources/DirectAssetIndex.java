package net.minecraft.client.resources;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class DirectAssetIndex extends AssetIndex
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private final File assetsDirectory;

    public DirectAssetIndex(File pAssetsDirectory)
    {
        this.assetsDirectory = pAssetsDirectory;
    }

    public File getFile(ResourceLocation pLocation)
    {
        return new File(this.assetsDirectory, pLocation.toString().replace(':', '/'));
    }

    public File getRootFile(String pPath)
    {
        return new File(this.assetsDirectory, pPath);
    }

    public Collection<ResourceLocation> getFiles(String p_235016_, String p_235017_, Predicate<ResourceLocation> p_235018_)
    {
        Path path = this.assetsDirectory.toPath().resolve(p_235017_);

        try
        {
            Stream<Path> stream = Files.walk(path.resolve(p_235016_));
            Collection collection;

            try
            {
                collection = stream.filter((p_118655_) ->
                {
                    return Files.isRegularFile(p_118655_);
                }).filter((p_118648_) ->
                {
                    return !p_118648_.endsWith(".mcmeta");
                }).map((p_235022_) ->
                {
                    return new ResourceLocation(p_235017_, path.relativize(p_235022_).toString().replaceAll("\\\\", "/"));
                }).filter(p_235018_).collect(Collectors.toList());
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

            return collection;
        }
        catch (NoSuchFileException nosuchfileexception)
        {
        }
        catch (IOException ioexception)
        {
            LOGGER.warn("Unable to getFiles on {}", p_235016_, ioexception);
        }

        return Collections.emptyList();
    }
}
