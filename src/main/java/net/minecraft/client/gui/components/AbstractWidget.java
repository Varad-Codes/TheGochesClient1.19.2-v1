package net.minecraft.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

public abstract class AbstractWidget extends GuiComponent implements Widget, GuiEventListener, NarratableEntry
{
    public static final ResourceLocation WIDGETS_LOCATION = new ResourceLocation("textures/gui/widgets.png");
    protected int width;
    protected int height;
    public int x;
    public int y;
    private Component message;
    protected boolean isHovered;
    public boolean active = true;
    public boolean visible = true;
    protected float alpha = 1.0F;
    private boolean focused;

    public AbstractWidget(int pX, int pY, int pWidth, int pHeight, Component pMessage)
    {
        this.x = pX;
        this.y = pY;
        this.width = pWidth;
        this.height = pHeight;
        this.message = pMessage;
    }

    public int getHeight()
    {
        return this.height;
    }

    protected int getYImage(boolean pIsHovered)
    {
        int i = 1;

        if (!this.active)
        {
            i = 0;
        }
        else if (pIsHovered)
        {
            i = 2;
        }

        return i;
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        if (this.visible)
        {
            this.isHovered = pMouseX >= this.x && pMouseY >= this.y && pMouseX < this.x + this.width && pMouseY < this.y + this.height;
            this.renderButton(pPoseStack, pMouseX, pMouseY, pPartialTick);
        }
    }

    protected MutableComponent createNarrationMessage()
    {
        return wrapDefaultNarrationMessage(this.getMessage());
    }

    public static MutableComponent wrapDefaultNarrationMessage(Component pMessage)
    {
        return Component.a("gui.narrate.button", pMessage);
    }

    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        int i = this.getYImage(this.isHoveredOrFocused());
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        this.blit(pPoseStack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
        this.blit(pPoseStack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        int j = this.active ? 16777215 : 10526880;
        drawCenteredString(pPoseStack, font, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | Mth.ceil(this.alpha * 255.0F) << 24);
    }

    protected void renderBg(PoseStack pPoseStack, Minecraft pMinecraft, int pMouseX, int pMouseY)
    {
    }

    public void onClick(double pMouseX, double p_93635_)
    {
    }

    public void onRelease(double pMouseX, double p_93670_)
    {
    }

    protected void onDrag(double pMouseX, double p_93637_, double pMouseY, double p_93639_)
    {
    }

    public boolean mouseClicked(double pMouseX, double p_93642_, int pMouseY)
    {
        if (this.active && this.visible)
        {
            if (this.isValidClickButton(pMouseY))
            {
                boolean flag = this.clicked(pMouseX, p_93642_);

                if (flag)
                {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(pMouseX, p_93642_);
                    return true;
                }
            }

            return false;
        }
        else
        {
            return false;
        }
    }

    public boolean mouseReleased(double pMouseX, double p_93685_, int pMouseY)
    {
        if (this.isValidClickButton(pMouseY))
        {
            this.onRelease(pMouseX, p_93685_);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean isValidClickButton(int pButton)
    {
        return pButton == 0;
    }

    public boolean mouseDragged(double pMouseX, double p_93646_, int pMouseY, double p_93648_, double pButton)
    {
        if (this.isValidClickButton(pMouseY))
        {
            this.onDrag(pMouseX, p_93646_, p_93648_, pButton);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected boolean clicked(double pMouseX, double p_93682_)
    {
        return this.active && this.visible && pMouseX >= (double)this.x && p_93682_ >= (double)this.y && pMouseX < (double)(this.x + this.width) && p_93682_ < (double)(this.y + this.height);
    }

    public boolean isHoveredOrFocused()
    {
        return this.isHovered || this.focused;
    }

    public boolean changeFocus(boolean pFocus)
    {
        if (this.active && this.visible)
        {
            this.focused = !this.focused;
            this.onFocusedChanged(this.focused);
            return this.focused;
        }
        else
        {
            return false;
        }
    }

    protected void onFocusedChanged(boolean pFocused)
    {
    }

    public boolean isMouseOver(double pMouseX, double p_93673_)
    {
        return this.active && this.visible && pMouseX >= (double)this.x && p_93673_ >= (double)this.y && pMouseX < (double)(this.x + this.width) && p_93673_ < (double)(this.y + this.height);
    }

    public void renderToolTip(PoseStack pPoseStack, int pMouseX, int pMouseY)
    {
    }

    public void playDownSound(SoundManager pHandler)
    {
        pHandler.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public int getWidth()
    {
        return this.width;
    }

    public void setWidth(int pWidth)
    {
        this.width = pWidth;
    }

    public void setAlpha(float pAlpha)
    {
        this.alpha = pAlpha;
    }

    public void setMessage(Component pMessage)
    {
        this.message = pMessage;
    }

    public Component getMessage()
    {
        return this.message;
    }

    public boolean isFocused()
    {
        return this.focused;
    }

    public boolean isActive()
    {
        return this.visible && this.active;
    }

    protected void setFocused(boolean pFocused)
    {
        this.focused = pFocused;
    }

    public NarratableEntry.NarrationPriority narrationPriority()
    {
        if (this.focused)
        {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        else
        {
            return this.isHovered ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }

    protected void defaultButtonNarrationText(NarrationElementOutput p_168803_)
    {
        p_168803_.add(NarratedElementType.TITLE, this.createNarrationMessage());

        if (this.active)
        {
            if (this.isFocused())
            {
                p_168803_.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
            }
            else
            {
                p_168803_.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
            }
        }
    }
}
