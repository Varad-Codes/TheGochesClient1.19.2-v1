package net.minecraft.world.level.block.entity;

import javax.annotation.Nullable;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

public abstract class RandomizableContainerBlockEntity extends BaseContainerBlockEntity
{
    public static final String LOOT_TABLE_TAG = "LootTable";
    public static final String LOOT_TABLE_SEED_TAG = "LootTableSeed";
    @Nullable
    protected ResourceLocation lootTable;
    protected long lootTableSeed;

    protected RandomizableContainerBlockEntity(BlockEntityType<?> p_155629_, BlockPos p_155630_, BlockState p_155631_)
    {
        super(p_155629_, p_155630_, p_155631_);
    }

    public static void setLootTable(BlockGetter pLevel, RandomSource pRandom, BlockPos pPos, ResourceLocation pLootTable)
    {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);

        if (blockentity instanceof RandomizableContainerBlockEntity)
        {
            ((RandomizableContainerBlockEntity)blockentity).setLootTable(pLootTable, pRandom.nextLong());
        }
    }

    protected boolean tryLoadLootTable(CompoundTag pTag)
    {
        if (pTag.contains("LootTable", 8))
        {
            this.lootTable = new ResourceLocation(pTag.getString("LootTable"));
            this.lootTableSeed = pTag.getLong("LootTableSeed");
            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean trySaveLootTable(CompoundTag pTag)
    {
        if (this.lootTable == null)
        {
            return false;
        }
        else
        {
            pTag.putString("LootTable", this.lootTable.toString());

            if (this.lootTableSeed != 0L)
            {
                pTag.putLong("LootTableSeed", this.lootTableSeed);
            }

            return true;
        }
    }

    public void unpackLootTable(@Nullable Player pPlayer)
    {
        if (this.lootTable != null && this.level.getServer() != null)
        {
            LootTable loottable = this.level.getServer().getLootTables().get(this.lootTable);

            if (pPlayer instanceof ServerPlayer)
            {
                CriteriaTriggers.GENERATE_LOOT.trigger((ServerPlayer)pPlayer, this.lootTable);
            }

            this.lootTable = null;
            LootContext.Builder lootcontext$builder = (new LootContext.Builder((ServerLevel)this.level)).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(this.worldPosition)).withOptionalRandomSeed(this.lootTableSeed);

            if (pPlayer != null)
            {
                lootcontext$builder.withLuck(pPlayer.getLuck()).withParameter(LootContextParams.THIS_ENTITY, pPlayer);
            }

            loottable.fill(this, lootcontext$builder.create(LootContextParamSets.CHEST));
        }
    }

    public void setLootTable(ResourceLocation pLootTable, long pLootTableSeed)
    {
        this.lootTable = pLootTable;
        this.lootTableSeed = pLootTableSeed;
    }

    public boolean isEmpty()
    {
        this.unpackLootTable((Player)null);
        return this.getItems().stream().allMatch(ItemStack::isEmpty);
    }

    public ItemStack getItem(int pIndex)
    {
        this.unpackLootTable((Player)null);
        return this.getItems().get(pIndex);
    }

    public ItemStack removeItem(int pIndex, int pCount)
    {
        this.unpackLootTable((Player)null);
        ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), pIndex, pCount);

        if (!itemstack.isEmpty())
        {
            this.setChanged();
        }

        return itemstack;
    }

    public ItemStack removeItemNoUpdate(int pIndex)
    {
        this.unpackLootTable((Player)null);
        return ContainerHelper.takeItem(this.getItems(), pIndex);
    }

    public void setItem(int pIndex, ItemStack pStack)
    {
        this.unpackLootTable((Player)null);
        this.getItems().set(pIndex, pStack);

        if (pStack.getCount() > this.getMaxStackSize())
        {
            pStack.setCount(this.getMaxStackSize());
        }

        this.setChanged();
    }

    public boolean stillValid(Player pPlayer)
    {
        if (this.level.getBlockEntity(this.worldPosition) != this)
        {
            return false;
        }
        else
        {
            return !(pPlayer.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) > 64.0D);
        }
    }

    public void clearContent()
    {
        this.getItems().clear();
    }

    protected abstract NonNullList<ItemStack> getItems();

    protected abstract void setItems(NonNullList<ItemStack> pItemStacks);

    public boolean canOpen(Player pPlayer)
    {
        return super.canOpen(pPlayer) && (this.lootTable == null || !pPlayer.isSpectator());
    }

    @Nullable
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer)
    {
        if (this.canOpen(pPlayer))
        {
            this.unpackLootTable(pInventory.player);
            return this.createMenu(pContainerId, pInventory);
        }
        else
        {
            return null;
        }
    }
}
