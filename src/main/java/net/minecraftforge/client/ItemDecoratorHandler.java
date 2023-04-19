package net.minecraftforge.client;

import net.minecraft.client.gui.Font;
import net.minecraft.world.item.ItemStack;

public class ItemDecoratorHandler
{
    private static ItemDecoratorHandler DUMMY = new ItemDecoratorHandler();

    public static ItemDecoratorHandler of(ItemStack stack)
    {
        return DUMMY;
    }

    public void render(Font font, ItemStack stack, int xOffset, int yOffset, float blitOffset)
    {
    }
}
