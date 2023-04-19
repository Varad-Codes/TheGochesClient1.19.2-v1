package net.optifine.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.optifine.config.Option;

public class GuiOtherSettingsOF extends GuiScreenOF
{
    private Screen prevScreen;
    private Options settings;
    private TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderOptions());

    public GuiOtherSettingsOF(Screen guiscreen, Options gamesettings)
    {
        super(Component.literal(I18n.a("of.options.otherTitle")));
        this.prevScreen = guiscreen;
        this.settings = gamesettings;
    }

    public void init()
    {
        this.clearWidgets();
        OptionInstance optioninstance = OptionFullscreenResolution.make();
        OptionInstance[] aoptioninstance = new OptionInstance[] {Option.LAGOMETER, Option.PROFILER, Option.SHOW_FPS, Option.ADVANCED_TOOLTIPS, Option.WEATHER, Option.TIME, this.settings.FULLSCREEN, Option.AUTOSAVE_TICKS, Option.SCREENSHOT_SIZE, Option.SHOW_GL_ERRORS, Option.TELEMETRY, null, optioninstance, null};

        for (int i = 0; i < aoptioninstance.length; ++i)
        {
            OptionInstance optioninstance1 = aoptioninstance[i];

            if (optioninstance1 != null)
            {
                int j = this.width / 2 - 155 + i % 2 * 160;
                int k = this.height / 6 + 21 * (i / 2) - 12;
                AbstractWidget abstractwidget = this.addRenderableWidget(optioninstance1.createButton(this.minecraft.options, j, k, 150));

                if (optioninstance1 == optioninstance)
                {
                    abstractwidget.setWidth(310);
                }
            }
        }

        this.addRenderableWidget(new GuiButtonOF(210, this.width / 2 - 100, this.height / 6 + 168 + 11 - 44, I18n.a("of.options.other.reset")));
        this.addRenderableWidget(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 168 + 11, I18n.a("gui.done")));
    }

    protected void actionPerformed(AbstractWidget guiElement)
    {
        if (guiElement instanceof GuiButtonOF guibuttonof)
        {
            if (guibuttonof.active)
            {
                if (guibuttonof.id == 200)
                {
                    this.minecraft.options.save();
                    this.minecraft.getWindow().changeFullscreenVideoMode();
                    this.minecraft.setScreen(this.prevScreen);
                }

                if (guibuttonof.id == 210)
                {
                    this.minecraft.options.save();
                    String s = I18n.a("of.message.other.reset");
                    ConfirmScreen confirmscreen = new ConfirmScreen(this::confirmResult, Component.literal(s), Component.literal(""));
                    this.minecraft.setScreen(confirmscreen);
                }
            }
        }
    }

    public void removed()
    {
        this.minecraft.options.save();
        this.minecraft.getWindow().changeFullscreenVideoMode();
        super.removed();
    }

    public void confirmResult(boolean flag)
    {
        if (flag)
        {
            this.minecraft.options.resetSettings();
        }

        this.minecraft.setScreen(this);
    }

    public void render(PoseStack matrixStackIn, int x, int y, float partialTicks)
    {
        this.renderBackground(matrixStackIn);
        drawCenteredString(matrixStackIn, this.fontRenderer, this.title, this.width / 2, 15, 16777215);
        super.render(matrixStackIn, x, y, partialTicks);
        this.tooltipManager.drawTooltips(matrixStackIn, x, y, this.getButtonList());
    }
}
