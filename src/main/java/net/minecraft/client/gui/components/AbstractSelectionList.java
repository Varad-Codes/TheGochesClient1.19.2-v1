package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.AbstractList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class AbstractSelectionList<E extends AbstractSelectionList.Entry<E>> extends AbstractContainerEventHandler implements Widget, NarratableEntry
{
    protected final Minecraft minecraft;
    protected final int itemHeight;
    private final List<E> children = new AbstractSelectionList.TrackedList();
    protected int width;
    protected int height;
    protected int y0;
    protected int y1;
    protected int x1;
    protected int x0;
    protected boolean centerListVertically = true;
    private double scrollAmount;
    private boolean renderSelection = true;
    private boolean renderHeader;
    protected int headerHeight;
    private boolean scrolling;
    @Nullable
    private E selected;
    private boolean renderBackground = true;
    private boolean renderTopAndBottom = true;
    @Nullable
    private E hovered;

    public AbstractSelectionList(Minecraft pMinecraft, int pWidth, int pHeight, int pY0, int pY1, int pItemHeight)
    {
        this.minecraft = pMinecraft;
        this.width = pWidth;
        this.height = pHeight;
        this.y0 = pY0;
        this.y1 = pY1;
        this.itemHeight = pItemHeight;
        this.x0 = 0;
        this.x1 = pWidth;
    }

    public void setRenderSelection(boolean pRenderSelection)
    {
        this.renderSelection = pRenderSelection;
    }

    protected void setRenderHeader(boolean pRenderHeader, int pHeaderHeight)
    {
        this.renderHeader = pRenderHeader;
        this.headerHeight = pHeaderHeight;

        if (!pRenderHeader)
        {
            this.headerHeight = 0;
        }
    }

    public int getRowWidth()
    {
        return 220;
    }

    @Nullable
    public E getSelected()
    {
        return this.selected;
    }

    public void setSelected(@Nullable E pSelected)
    {
        this.selected = pSelected;
    }

    public void setRenderBackground(boolean pRenderBackground)
    {
        this.renderBackground = pRenderBackground;
    }

    public void setRenderTopAndBottom(boolean pRenderTopAndButton)
    {
        this.renderTopAndBottom = pRenderTopAndButton;
    }

    @Nullable
    public E getFocused()
    {
        return (E)(super.getFocused());
    }

    public final List<E> children()
    {
        return this.children;
    }

    protected final void clearEntries()
    {
        this.children.clear();
    }

    protected void replaceEntries(Collection<E> pEntries)
    {
        this.children.clear();
        this.children.addAll(pEntries);
    }

    protected E getEntry(int pIndex)
    {
        return this.children().get(pIndex);
    }

    protected int addEntry(E pEntry)
    {
        this.children.add(pEntry);
        return this.children.size() - 1;
    }

    protected void addEntryToTop(E p_239858_)
    {
        double d0 = (double)this.getMaxScroll() - this.getScrollAmount();
        this.children.add(0, p_239858_);
        this.setScrollAmount((double)this.getMaxScroll() - d0);
    }

    protected boolean removeEntryFromTop(E p_239046_)
    {
        double d0 = (double)this.getMaxScroll() - this.getScrollAmount();
        boolean flag = this.removeEntry(p_239046_);
        this.setScrollAmount((double)this.getMaxScroll() - d0);
        return flag;
    }

    protected int getItemCount()
    {
        return this.children().size();
    }

    protected boolean isSelectedItem(int pIndex)
    {
        return Objects.equals(this.getSelected(), this.children().get(pIndex));
    }

    @Nullable
    protected final E getEntryAtPosition(double pMouseX, double p_93414_)
    {
        int i = this.getRowWidth() / 2;
        int j = this.x0 + this.width / 2;
        int k = j - i;
        int l = j + i;
        int i1 = Mth.floor(p_93414_ - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
        int j1 = i1 / this.itemHeight;
        return (E)(pMouseX < (double)this.getScrollbarPosition() && pMouseX >= (double)k && pMouseX <= (double)l && j1 >= 0 && i1 >= 0 && j1 < this.getItemCount() ? this.children().get(j1) : null);
    }

    public void updateSize(int pWidth, int pHeight, int pY0, int pY1)
    {
        this.width = pWidth;
        this.height = pHeight;
        this.y0 = pY0;
        this.y1 = pY1;
        this.x0 = 0;
        this.x1 = pWidth;
    }

    public void setLeftPos(int pX0)
    {
        this.x0 = pX0;
        this.x1 = pX0 + this.width;
    }

    protected int getMaxPosition()
    {
        return this.getItemCount() * this.itemHeight + this.headerHeight;
    }

    protected void clickedHeader(int pMouseX, int pMouseY)
    {
    }

    protected void renderHeader(PoseStack pPoseStack, int pX, int pY, Tesselator pTessellator)
    {
    }

    protected void renderBackground(PoseStack pPoseStack)
    {
    }

    protected void renderDecorations(PoseStack pPoseStack, int pMouseX, int pMouseY)
    {
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        this.renderBackground(pPoseStack);
        int i = this.getScrollbarPosition();
        int j = i + 6;
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        this.hovered = this.isMouseOver((double)pMouseX, (double)pMouseY) ? this.getEntryAtPosition((double)pMouseX, (double)pMouseY) : null;

        if (this.renderBackground)
        {
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            float f = 32.0F;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y1 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).uv((float)this.x1 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).uv((float)this.x0 / 32.0F, (float)(this.y0 + (int)this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).endVertex();
            tesselator.end();
        }

        int j1 = this.getRowLeft();
        int k = this.y0 + 4 - (int)this.getScrollAmount();

        if (this.renderHeader)
        {
            this.renderHeader(pPoseStack, j1, k, tesselator);
        }

        this.renderList(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (this.renderTopAndBottom)
        {
            RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
            RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
            RenderSystem.enableDepthTest();
            RenderSystem.depthFunc(519);
            float f1 = 32.0F;
            int l = -100;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)this.y0, -100.0D).uv(0.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y0, -100.0D).uv((float)this.width / 32.0F, (float)this.y0 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), 0.0D, -100.0D).uv((float)this.width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, 0.0D, -100.0D).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.height, -100.0D).uv(0.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.height, -100.0D).uv((float)this.width / 32.0F, (float)this.height / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)(this.x0 + this.width), (double)this.y1, -100.0D).uv((float)this.width / 32.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y1, -100.0D).uv(0.0F, (float)this.y1 / 32.0F).color(64, 64, 64, 255).endVertex();
            tesselator.end();
            RenderSystem.depthFunc(515);
            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int i1 = 4;
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex((double)this.x0, (double)(this.y0 + 4), 0.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y0 + 4), 0.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)this.x1, (double)(this.y1 - 4), 0.0D).color(0, 0, 0, 0).endVertex();
            bufferbuilder.vertex((double)this.x0, (double)(this.y1 - 4), 0.0D).color(0, 0, 0, 0).endVertex();
            tesselator.end();
        }

        int k1 = this.getMaxScroll();

        if (k1 > 0)
        {
            RenderSystem.disableTexture();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            int l1 = (int)((float)((this.y1 - this.y0) * (this.y1 - this.y0)) / (float)this.getMaxPosition());
            l1 = Mth.clamp(l1, 32, this.y1 - this.y0 - 8);
            int i2 = (int)this.getScrollAmount() * (this.y1 - this.y0 - l1) / k1 + this.y0;

            if (i2 < this.y0)
            {
                i2 = this.y0;
            }

            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            bufferbuilder.vertex((double)i, (double)this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)j, (double)this.y1, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)j, (double)this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)this.y0, 0.0D).color(0, 0, 0, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)(i2 + l1), 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double)j, (double)(i2 + l1), 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double)j, (double)i2, 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)i2, 0.0D).color(128, 128, 128, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)(i2 + l1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double)(j - 1), (double)(i2 + l1 - 1), 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double)(j - 1), (double)i2, 0.0D).color(192, 192, 192, 255).endVertex();
            bufferbuilder.vertex((double)i, (double)i2, 0.0D).color(192, 192, 192, 255).endVertex();
            tesselator.end();
        }

        this.renderDecorations(pPoseStack, pMouseX, pMouseY);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    protected void centerScrollOn(E pEntry)
    {
        this.setScrollAmount((double)(this.children().indexOf(pEntry) * this.itemHeight + this.itemHeight / 2 - (this.y1 - this.y0) / 2));
    }

    protected void ensureVisible(E pEntry)
    {
        int i = this.getRowTop(this.children().indexOf(pEntry));
        int j = i - this.y0 - 4 - this.itemHeight;

        if (j < 0)
        {
            this.scroll(j);
        }

        int k = this.y1 - i - this.itemHeight - this.itemHeight;

        if (k < 0)
        {
            this.scroll(-k);
        }
    }

    private void scroll(int pScroll)
    {
        this.setScrollAmount(this.getScrollAmount() + (double)pScroll);
    }

    public double getScrollAmount()
    {
        return this.scrollAmount;
    }

    public void setScrollAmount(double pScroll)
    {
        this.scrollAmount = Mth.clamp(pScroll, 0.0D, (double)this.getMaxScroll());
    }

    public int getMaxScroll()
    {
        return Math.max(0, this.getMaxPosition() - (this.y1 - this.y0 - 4));
    }

    public int getScrollBottom()
    {
        return (int)this.getScrollAmount() - this.height - this.headerHeight;
    }

    protected void updateScrollingState(double pMouseX, double p_93483_, int pMouseY)
    {
        this.scrolling = pMouseY == 0 && pMouseX >= (double)this.getScrollbarPosition() && pMouseX < (double)(this.getScrollbarPosition() + 6);
    }

    protected int getScrollbarPosition()
    {
        return this.width / 2 + 124;
    }

    public boolean mouseClicked(double pMouseX, double p_93421_, int pMouseY)
    {
        this.updateScrollingState(pMouseX, p_93421_, pMouseY);

        if (!this.isMouseOver(pMouseX, p_93421_))
        {
            return false;
        }
        else
        {
            E e = this.getEntryAtPosition(pMouseX, p_93421_);

            if (e != null)
            {
                if (e.mouseClicked(pMouseX, p_93421_, pMouseY))
                {
                    this.setFocused(e);
                    this.setDragging(true);
                    return true;
                }
            }
            else if (pMouseY == 0)
            {
                this.clickedHeader((int)(pMouseX - (double)(this.x0 + this.width / 2 - this.getRowWidth() / 2)), (int)(p_93421_ - (double)this.y0) + (int)this.getScrollAmount() - 4);
                return true;
            }

            return this.scrolling;
        }
    }

    public boolean mouseReleased(double pMouseX, double p_93492_, int pMouseY)
    {
        if (this.getFocused() != null)
        {
            this.getFocused().mouseReleased(pMouseX, p_93492_, pMouseY);
        }

        return false;
    }

    public boolean mouseDragged(double pMouseX, double p_93425_, int pMouseY, double p_93427_, double pButton)
    {
        if (super.mouseDragged(pMouseX, p_93425_, pMouseY, p_93427_, pButton))
        {
            return true;
        }
        else if (pMouseY == 0 && this.scrolling)
        {
            if (p_93425_ < (double)this.y0)
            {
                this.setScrollAmount(0.0D);
            }
            else if (p_93425_ > (double)this.y1)
            {
                this.setScrollAmount((double)this.getMaxScroll());
            }
            else
            {
                double d0 = (double)Math.max(1, this.getMaxScroll());
                int i = this.y1 - this.y0;
                int j = Mth.clamp((int)((float)(i * i) / (float)this.getMaxPosition()), 32, i - 8);
                double d1 = Math.max(1.0D, d0 / (double)(i - j));
                this.setScrollAmount(this.getScrollAmount() + pButton * d1);
            }

            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean mouseScrolled(double pMouseX, double p_93417_, double pMouseY)
    {
        this.setScrollAmount(this.getScrollAmount() - pMouseY * (double)this.itemHeight / 2.0D);
        return true;
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (super.keyPressed(pKeyCode, pScanCode, pModifiers))
        {
            return true;
        }
        else if (pKeyCode == 264)
        {
            this.moveSelection(AbstractSelectionList.SelectionDirection.DOWN);
            return true;
        }
        else if (pKeyCode == 265)
        {
            this.moveSelection(AbstractSelectionList.SelectionDirection.UP);
            return true;
        }
        else
        {
            return false;
        }
    }

    protected void moveSelection(AbstractSelectionList.SelectionDirection pOrdering)
    {
        this.moveSelection(pOrdering, (p_93510_) ->
        {
            return true;
        });
    }

    protected void refreshSelection()
    {
        E e = this.getSelected();

        if (e != null)
        {
            this.setSelected(e);
            this.ensureVisible(e);
        }
    }

    protected boolean moveSelection(AbstractSelectionList.SelectionDirection pOrdering, Predicate<E> pFilter)
    {
        int i = pOrdering == AbstractSelectionList.SelectionDirection.UP ? -1 : 1;

        if (!this.children().isEmpty())
        {
            int j = this.children().indexOf(this.getSelected());

            while (true)
            {
                int k = Mth.clamp(j + i, 0, this.getItemCount() - 1);

                if (j == k)
                {
                    break;
                }

                E e = this.children().get(k);

                if (pFilter.test(e))
                {
                    this.setSelected(e);
                    this.ensureVisible(e);
                    return true;
                }

                j = k;
            }
        }

        return false;
    }

    public boolean isMouseOver(double pMouseX, double p_93480_)
    {
        return p_93480_ >= (double)this.y0 && p_93480_ <= (double)this.y1 && pMouseX >= (double)this.x0 && pMouseX <= (double)this.x1;
    }

    protected void renderList(PoseStack p_239228_, int p_239229_, int p_239230_, float p_239231_)
    {
        int i = this.getRowLeft();
        int j = this.getRowWidth();
        int k = this.itemHeight - 4;
        int l = this.getItemCount();

        for (int i1 = 0; i1 < l; ++i1)
        {
            int j1 = this.getRowTop(i1);
            int k1 = this.getRowBottom(i1);

            if (k1 >= this.y0 && j1 <= this.y1)
            {
                this.renderItem(p_239228_, p_239229_, p_239230_, p_239231_, i1, i, j1, j, k);
            }
        }
    }

    protected void renderItem(PoseStack p_238965_, int p_238966_, int p_238967_, float p_238968_, int p_238969_, int p_238970_, int p_238971_, int p_238972_, int p_238973_)
    {
        E e = this.getEntry(p_238969_);

        if (this.renderSelection && this.isSelectedItem(p_238969_))
        {
            int i = this.isFocused() ? -1 : -8355712;
            this.renderSelection(p_238965_, p_238971_, p_238972_, p_238973_, i, -16777216);
        }

        e.render(p_238965_, p_238969_, p_238971_, p_238970_, p_238972_, p_238973_, p_238966_, p_238967_, Objects.equals(this.hovered, e), p_238968_);
    }

    protected void renderSelection(PoseStack p_240141_, int p_240142_, int p_240143_, int p_240144_, int p_240145_, int p_240146_)
    {
        int i = this.x0 + (this.width - p_240143_) / 2;
        int j = this.x0 + (this.width + p_240143_) / 2;
        fill(p_240141_, i, p_240142_ - 2, j, p_240142_ + p_240144_ + 2, p_240145_);
        fill(p_240141_, i + 1, p_240142_ - 1, j - 1, p_240142_ + p_240144_ + 1, p_240146_);
    }

    public int getRowLeft()
    {
        return this.x0 + this.width / 2 - this.getRowWidth() / 2 + 2;
    }

    public int getRowRight()
    {
        return this.getRowLeft() + this.getRowWidth();
    }

    protected int getRowTop(int pIndex)
    {
        return this.y0 + 4 - (int)this.getScrollAmount() + pIndex * this.itemHeight + this.headerHeight;
    }

    private int getRowBottom(int pIndex)
    {
        return this.getRowTop(pIndex) + this.itemHeight;
    }

    protected boolean isFocused()
    {
        return false;
    }

    public NarratableEntry.NarrationPriority narrationPriority()
    {
        if (this.isFocused())
        {
            return NarratableEntry.NarrationPriority.FOCUSED;
        }
        else
        {
            return this.hovered != null ? NarratableEntry.NarrationPriority.HOVERED : NarratableEntry.NarrationPriority.NONE;
        }
    }

    @Nullable
    protected E remove(int pIndex)
    {
        E e = this.children.get(pIndex);
        return (E)(this.removeEntry(this.children.get(pIndex)) ? e : null);
    }

    protected boolean removeEntry(E pEntry)
    {
        boolean flag = this.children.remove(pEntry);

        if (flag && pEntry == this.getSelected())
        {
            this.setSelected((E)null);
        }

        return flag;
    }

    @Nullable
    protected E getHovered()
    {
        return this.hovered;
    }

    void bindEntryToSelf(AbstractSelectionList.Entry<E> pEntry)
    {
        pEntry.list = this;
    }

    protected void narrateListElementPosition(NarrationElementOutput p_168791_, E p_168792_)
    {
        List<E> list = this.children();

        if (list.size() > 1)
        {
            int i = list.indexOf(p_168792_);

            if (i != -1)
            {
                p_168791_.add(NarratedElementType.POSITION, Component.a("narrator.position.list", i + 1, list.size()));
            }
        }
    }

    public abstract static class Entry<E extends AbstractSelectionList.Entry<E>> implements GuiEventListener
    {
        @Deprecated
        AbstractSelectionList<E> list;

        public abstract void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick);

        public boolean isMouseOver(double pMouseX, double p_93538_)
        {
            return Objects.equals(this.list.getEntryAtPosition(pMouseX, p_93538_), this);
        }
    }

    protected static enum SelectionDirection
    {
        UP,
        DOWN;
    }

    class TrackedList extends AbstractList<E>
    {
        private final List<E> delegate = Lists.newArrayList();

        public E get(int p_93557_)
        {
            return this.delegate.get(p_93557_);
        }

        public int size()
        {
            return this.delegate.size();
        }

        public E set(int p_93559_, E p_93560_)
        {
            E e = this.delegate.set(p_93559_, p_93560_);
            AbstractSelectionList.this.bindEntryToSelf(p_93560_);
            return e;
        }

        public void add(int p_93567_, E p_93568_)
        {
            this.delegate.add(p_93567_, p_93568_);
            AbstractSelectionList.this.bindEntryToSelf(p_93568_);
        }

        public E remove(int p_93565_)
        {
            return this.delegate.remove(p_93565_);
        }
    }
}
