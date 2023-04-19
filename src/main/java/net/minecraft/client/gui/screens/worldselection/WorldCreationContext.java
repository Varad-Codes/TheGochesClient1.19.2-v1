package net.minecraft.client.gui.screens.worldselection;

import com.mojang.serialization.Lifecycle;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.world.level.levelgen.WorldGenSettings;

public record WorldCreationContext(WorldGenSettings worldGenSettings, Lifecycle worldSettingsStability, RegistryAccess.Frozen registryAccess, ReloadableServerResources dataPackResources)
{
    public WorldCreationContext withSettings(WorldGenSettings p_232998_)
    {
        return new WorldCreationContext(p_232998_, this.worldSettingsStability, this.registryAccess, this.dataPackResources);
    }
    public WorldCreationContext withSettings(WorldCreationContext.SimpleUpdater p_233000_)
    {
        WorldGenSettings worldgensettings = p_233000_.apply(this.worldGenSettings);
        return this.withSettings(worldgensettings);
    }
    public WorldCreationContext withSettings(WorldCreationContext.Updater p_233002_)
    {
        WorldGenSettings worldgensettings = p_233002_.apply(this.registryAccess, this.worldGenSettings);
        return this.withSettings(worldgensettings);
    }
    @FunctionalInterface
    public interface SimpleUpdater extends UnaryOperator<WorldGenSettings>
    {
    }
    @FunctionalInterface
    public interface Updater extends BiFunction<RegistryAccess.Frozen, WorldGenSettings, WorldGenSettings>
    {
    }
}
