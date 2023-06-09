package net.minecraft.data.worldgen;

import com.google.common.collect.ImmutableList;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class Pools
{
    public static final ResourceKey<StructureTemplatePool> EMPTY = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation("empty"));
    private static final Holder<StructureTemplatePool> BUILTIN_EMPTY = register(new StructureTemplatePool(EMPTY.location(), EMPTY.location(), ImmutableList.of(), StructureTemplatePool.Projection.RIGID));

    public static Holder<StructureTemplatePool> register(StructureTemplatePool pPool)
    {
        return BuiltinRegistries.register(BuiltinRegistries.TEMPLATE_POOL, pPool.getName(), pPool);
    }

    @Deprecated
    public static void forceBootstrap()
    {
        bootstrap(BuiltinRegistries.TEMPLATE_POOL);
    }

    public static Holder<StructureTemplatePool> bootstrap(Registry<StructureTemplatePool> p_236492_)
    {
        BastionPieces.bootstrap();
        PillagerOutpostPools.bootstrap();
        VillagePools.bootstrap();
        AncientCityStructurePieces.bootstrap();
        return BUILTIN_EMPTY;
    }
}
