package net.optifine.gui;

import java.util.Collections;
import java.util.List;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.TooltipAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public class GuiOptionButtonOF extends Button implements TooltipAccessor, IOptionControl
{
    private final OptionInstance option;

    public GuiOptionButtonOF(int xIn, int yIn, int widthIn, int heightIn, OptionInstance optionIn, Component textIn, Button.OnPress pressableIn)
    {
        super(xIn, yIn, widthIn, heightIn, textIn, pressableIn);
        this.option = optionIn;
    }

    public OptionInstance getOption()
    {
        return this.option;
    }

    public OptionInstance getControlOption()
    {
        return this.option;
    }

    public List<FormattedCharSequence> getTooltip()
    {
        return Collections.EMPTY_LIST;
    }
}
