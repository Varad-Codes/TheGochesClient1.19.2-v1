package net.minecraft.world.level.block;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Silverfish;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class InfestedBlock extends Block
{
    private final Block hostBlock;
    private static final Map<Block, Block> BLOCK_BY_HOST_BLOCK = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> HOST_TO_INFESTED_STATES = Maps.newIdentityHashMap();
    private static final Map<BlockState, BlockState> INFESTED_TO_HOST_STATES = Maps.newIdentityHashMap();

    public InfestedBlock(Block pHostBlock, BlockBehaviour.Properties pProperties)
    {
        super(pProperties.destroyTime(pHostBlock.defaultDestroyTime() / 2.0F).explosionResistance(0.75F));
        this.hostBlock = pHostBlock;
        BLOCK_BY_HOST_BLOCK.put(pHostBlock, this);
    }

    public Block getHostBlock()
    {
        return this.hostBlock;
    }

    public static boolean isCompatibleHostBlock(BlockState pState)
    {
        return BLOCK_BY_HOST_BLOCK.containsKey(pState.getBlock());
    }

    private void spawnInfestation(ServerLevel pLevel, BlockPos pPos)
    {
        Silverfish silverfish = EntityType.SILVERFISH.create(pLevel);
        silverfish.moveTo((double)pPos.getX() + 0.5D, (double)pPos.getY(), (double)pPos.getZ() + 0.5D, 0.0F, 0.0F);
        pLevel.addFreshEntity(silverfish);
        silverfish.spawnAnim();
    }

    public void spawnAfterBreak(BlockState p_221360_, ServerLevel p_221361_, BlockPos p_221362_, ItemStack p_221363_, boolean p_221364_)
    {
        super.spawnAfterBreak(p_221360_, p_221361_, p_221362_, p_221363_, p_221364_);

        if (p_221361_.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, p_221363_) == 0)
        {
            this.spawnInfestation(p_221361_, p_221362_);
        }
    }

    public static BlockState infestedStateByHost(BlockState p_153431_)
    {
        return getNewStateWithProperties(HOST_TO_INFESTED_STATES, p_153431_, () ->
        {
            return BLOCK_BY_HOST_BLOCK.get(p_153431_.getBlock()).defaultBlockState();
        });
    }

    public BlockState hostStateByInfested(BlockState p_153433_)
    {
        return getNewStateWithProperties(INFESTED_TO_HOST_STATES, p_153433_, () ->
        {
            return this.getHostBlock().defaultBlockState();
        });
    }

    private static BlockState getNewStateWithProperties(Map<BlockState, BlockState> p_153424_, BlockState p_153425_, Supplier<BlockState> p_153426_)
    {
        return p_153424_.computeIfAbsent(p_153425_, (p_153429_) ->
        {
            BlockState blockstate = p_153426_.get();

            for (Property property : p_153429_.getProperties())
            {
                blockstate = blockstate.hasProperty(property) ? blockstate.setValue(property, p_153429_.getValue(property)) : blockstate;
            }

            return blockstate;
        });
    }
}
