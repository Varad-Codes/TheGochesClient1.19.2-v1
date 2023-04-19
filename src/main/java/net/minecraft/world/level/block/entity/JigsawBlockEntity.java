package net.minecraft.world.level.block.entity;

import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

public class JigsawBlockEntity extends BlockEntity
{
    public static final String TARGET = "target";
    public static final String POOL = "pool";
    public static final String JOINT = "joint";
    public static final String NAME = "name";
    public static final String FINAL_STATE = "final_state";
    private ResourceLocation name = new ResourceLocation("empty");
    private ResourceLocation target = new ResourceLocation("empty");
    private ResourceKey<StructureTemplatePool> pool = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation("empty"));
    private JigsawBlockEntity.JointType joint = JigsawBlockEntity.JointType.ROLLABLE;
    private String finalState = "minecraft:air";

    public JigsawBlockEntity(BlockPos pWorldPosition, BlockState pBlockState)
    {
        super(BlockEntityType.JIGSAW, pWorldPosition, pBlockState);
    }

    public ResourceLocation getName()
    {
        return this.name;
    }

    public ResourceLocation getTarget()
    {
        return this.target;
    }

    public ResourceKey<StructureTemplatePool> getPool()
    {
        return this.pool;
    }

    public String getFinalState()
    {
        return this.finalState;
    }

    public JigsawBlockEntity.JointType getJoint()
    {
        return this.joint;
    }

    public void setName(ResourceLocation pName)
    {
        this.name = pName;
    }

    public void setTarget(ResourceLocation pTarget)
    {
        this.target = pTarget;
    }

    public void setPool(ResourceKey<StructureTemplatePool> pPool)
    {
        this.pool = pPool;
    }

    public void setFinalState(String pFinalState)
    {
        this.finalState = pFinalState;
    }

    public void setJoint(JigsawBlockEntity.JointType pJoint)
    {
        this.joint = pJoint;
    }

    protected void saveAdditional(CompoundTag p_187504_)
    {
        super.saveAdditional(p_187504_);
        p_187504_.putString("name", this.name.toString());
        p_187504_.putString("target", this.target.toString());
        p_187504_.putString("pool", this.pool.location().toString());
        p_187504_.putString("final_state", this.finalState);
        p_187504_.putString("joint", this.joint.getSerializedName());
    }

    public void load(CompoundTag pTag)
    {
        super.load(pTag);
        this.name = new ResourceLocation(pTag.getString("name"));
        this.target = new ResourceLocation(pTag.getString("target"));
        this.pool = ResourceKey.create(Registry.TEMPLATE_POOL_REGISTRY, new ResourceLocation(pTag.getString("pool")));
        this.finalState = pTag.getString("final_state");
        this.joint = JigsawBlockEntity.JointType.byName(pTag.getString("joint")).orElseGet(() ->
        {
            return JigsawBlock.getFrontFacing(this.getBlockState()).getAxis().isHorizontal() ? JigsawBlockEntity.JointType.ALIGNED : JigsawBlockEntity.JointType.ROLLABLE;
        });
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag()
    {
        return this.saveWithoutMetadata();
    }

    public void generate(ServerLevel p_59421_, int p_59422_, boolean p_59423_)
    {
        BlockPos blockpos = this.getBlockPos().relative(this.getBlockState().getValue(JigsawBlock.ORIENTATION).front());
        Registry<StructureTemplatePool> registry = p_59421_.registryAccess().registryOrThrow(Registry.TEMPLATE_POOL_REGISTRY);
        Holder<StructureTemplatePool> holder = registry.getHolderOrThrow(this.pool);
        JigsawPlacement.generateJigsaw(p_59421_, holder, this.target, p_59422_, blockpos, p_59423_);
    }

    public static enum JointType implements StringRepresentable
    {
        ROLLABLE("rollable"),
        ALIGNED("aligned");

        private final String name;

        private JointType(String p_59455_)
        {
            this.name = p_59455_;
        }

        public String getSerializedName()
        {
            return this.name;
        }

        public static Optional<JigsawBlockEntity.JointType> byName(String pName)
        {
            return Arrays.stream(values()).filter((p_59461_) ->
            {
                return p_59461_.getSerializedName().equals(pName);
            }).findFirst();
        }

        public Component getTranslatedName()
        {
            return Component.translatable("jigsaw_block.joint." + this.name);
        }
    }
}
