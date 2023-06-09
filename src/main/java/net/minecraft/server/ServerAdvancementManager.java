package net.minecraft.server;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.advancements.TreeNodePosition;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.slf4j.Logger;

public class ServerAdvancementManager extends SimpleJsonResourceReloadListener
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = (new GsonBuilder()).create();
    private AdvancementList advancements = new AdvancementList();
    private final PredicateManager predicateManager;

    public ServerAdvancementManager(PredicateManager p_136027_)
    {
        super(GSON, "advancements");
        this.predicateManager = p_136027_;
    }

    protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler)
    {
        Map<ResourceLocation, Advancement.Builder> map = Maps.newHashMap();
        pObject.forEach((p_136039_, p_136040_) ->
        {
            try {
                JsonObject jsonobject = GsonHelper.convertToJsonObject(p_136040_, "advancement");
                Advancement.Builder advancement$builder = Advancement.Builder.fromJson(jsonobject, new DeserializationContext(p_136039_, this.predicateManager));
                map.put(p_136039_, advancement$builder);
            }
            catch (Exception exception)
            {
                LOGGER.error("Parsing error loading custom advancement {}: {}", p_136039_, exception.getMessage());
            }
        });
        AdvancementList advancementlist = new AdvancementList();
        advancementlist.add(map);

        for (Advancement advancement : advancementlist.getRoots())
        {
            if (advancement.getDisplay() != null)
            {
                TreeNodePosition.run(advancement);
            }
        }

        this.advancements = advancementlist;
    }

    @Nullable
    public Advancement getAdvancement(ResourceLocation pId)
    {
        return this.advancements.get(pId);
    }

    public Collection<Advancement> getAllAdvancements()
    {
        return this.advancements.getAllAdvancements();
    }
}
