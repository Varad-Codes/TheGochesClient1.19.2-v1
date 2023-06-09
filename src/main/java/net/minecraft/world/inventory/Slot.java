package net.minecraft.world.inventory;

import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class Slot
{
    private final int slot;
    public final Container container;
    public int index;
    public final int x;
    public final int y;

    public Slot(Container pContainer, int pIndex, int pX, int pY)
    {
        this.container = pContainer;
        this.slot = pIndex;
        this.x = pX;
        this.y = pY;
    }

    public void onQuickCraft(ItemStack pStack, ItemStack pAmount)
    {
        int i = pAmount.getCount() - pStack.getCount();

        if (i > 0)
        {
            this.onQuickCraft(pAmount, i);
        }
    }

    protected void onQuickCraft(ItemStack pStack, int pAmount)
    {
    }

    protected void onSwapCraft(int pNumItemsCrafted)
    {
    }

    protected void checkTakeAchievements(ItemStack pStack)
    {
    }

    public void onTake(Player pPlayer, ItemStack pStack)
    {
        this.setChanged();
    }

    public boolean mayPlace(ItemStack pStack)
    {
        return true;
    }

    public ItemStack getItem()
    {
        return this.container.getItem(this.slot);
    }

    public boolean hasItem()
    {
        return !this.getItem().isEmpty();
    }

    public void set(ItemStack pStack)
    {
        this.container.setItem(this.slot, pStack);
        this.setChanged();
    }

    public void initialize(ItemStack p_219997_)
    {
        this.container.setItem(this.slot, p_219997_);
        this.setChanged();
    }

    public void setChanged()
    {
        this.container.setChanged();
    }

    public int getMaxStackSize()
    {
        return this.container.getMaxStackSize();
    }

    public int getMaxStackSize(ItemStack pStack)
    {
        return Math.min(this.getMaxStackSize(), pStack.getMaxStackSize());
    }

    @Nullable
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon()
    {
        return null;
    }

    public ItemStack remove(int pAmount)
    {
        return this.container.removeItem(this.slot, pAmount);
    }

    public boolean mayPickup(Player pPlayer)
    {
        return true;
    }

    public boolean isActive()
    {
        return true;
    }

    public Optional<ItemStack> tryRemove(int p_150642_, int p_150643_, Player p_150644_)
    {
        if (!this.mayPickup(p_150644_))
        {
            return Optional.empty();
        }
        else if (!this.allowModification(p_150644_) && p_150643_ < this.getItem().getCount())
        {
            return Optional.empty();
        }
        else
        {
            p_150642_ = Math.min(p_150642_, p_150643_);
            ItemStack itemstack = this.remove(p_150642_);

            if (itemstack.isEmpty())
            {
                return Optional.empty();
            }
            else
            {
                if (this.getItem().isEmpty())
                {
                    this.set(ItemStack.EMPTY);
                }

                return Optional.of(itemstack);
            }
        }
    }

    public ItemStack safeTake(int p_150648_, int p_150649_, Player p_150650_)
    {
        Optional<ItemStack> optional = this.tryRemove(p_150648_, p_150649_, p_150650_);
        optional.ifPresent((p_150655_) ->
        {
            this.onTake(p_150650_, p_150655_);
        });
        return optional.orElse(ItemStack.EMPTY);
    }

    public ItemStack safeInsert(ItemStack p_150660_)
    {
        return this.safeInsert(p_150660_, p_150660_.getCount());
    }

    public ItemStack safeInsert(ItemStack p_150657_, int p_150658_)
    {
        if (!p_150657_.isEmpty() && this.mayPlace(p_150657_))
        {
            ItemStack itemstack = this.getItem();
            int i = Math.min(Math.min(p_150658_, p_150657_.getCount()), this.getMaxStackSize(p_150657_) - itemstack.getCount());

            if (itemstack.isEmpty())
            {
                this.set(p_150657_.split(i));
            }
            else if (ItemStack.isSameItemSameTags(itemstack, p_150657_))
            {
                p_150657_.shrink(i);
                itemstack.grow(i);
                this.set(itemstack);
            }

            return p_150657_;
        }
        else
        {
            return p_150657_;
        }
    }

    public boolean allowModification(Player p_150652_)
    {
        return this.mayPickup(p_150652_) && this.mayPlace(this.getItem());
    }

    public int getContainerSlot()
    {
        return this.slot;
    }
}
