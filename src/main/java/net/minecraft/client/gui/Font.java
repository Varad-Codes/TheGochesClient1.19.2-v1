package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.ibm.icu.text.ArabicShaping;
import com.ibm.icu.text.ArabicShapingException;
import com.ibm.icu.text.Bidi;
import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nullable;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.font.FontSet;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import net.minecraft.client.gui.font.glyphs.EmptyGlyph;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringDecomposer;
import net.optifine.util.GuiPoint;

public class Font
{
    private static final float EFFECT_DEPTH = 0.01F;
    private static final Vector3f SHADOW_OFFSET = new Vector3f(0.0F, 0.0F, 0.03F);
    public static final int ALPHA_CUTOFF = 8;
    public final int lineHeight = 9;
    public final RandomSource random = RandomSource.create();
    private final Function<ResourceLocation, FontSet> fonts;
    final boolean filterFishyGlyphs;
    private final StringSplitter splitter;

    public Font(Function<ResourceLocation, FontSet> p_243253_, boolean p_243245_)
    {
        this.fonts = p_243253_;
        this.filterFishyGlyphs = p_243245_;
        this.splitter = new StringSplitter((p_243025_1_, p_243025_2_) ->
        {
            return this.getFontSet(p_243025_2_.getFont()).getGlyphInfo(p_243025_1_, this.filterFishyGlyphs).getAdvance(p_243025_2_.isBold());
        });
    }

    FontSet getFontSet(ResourceLocation pFontLocation)
    {
        return this.fonts.apply(pFontLocation);
    }

    public int drawShadow(PoseStack pPoseStack, String pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText, pX, pY, pColor, pPoseStack.last().pose(), true, this.isBidirectional());
    }

    public int drawShadow(PoseStack pPoseStack, String pText, float pX, float pY, int pColor, boolean pTransparent)
    {
        return this.drawInternal(pText, pX, pY, pColor, pPoseStack.last().pose(), true, pTransparent);
    }

    public int draw(PoseStack pPoseStack, String pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText, pX, pY, pColor, pPoseStack.last().pose(), false, this.isBidirectional());
    }

    public int drawShadow(PoseStack pPoseStack, FormattedCharSequence pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText, pX, pY, pColor, pPoseStack.last().pose(), true);
    }

    public int drawShadow(PoseStack pPoseStack, Component pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText.getVisualOrderText(), pX, pY, pColor, pPoseStack.last().pose(), true);
    }

    public int draw(PoseStack pPoseStack, FormattedCharSequence pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText, pX, pY, pColor, pPoseStack.last().pose(), false);
    }

    public int draw(PoseStack pPoseStack, Component pText, float pX, float pY, int pColor)
    {
        return this.drawInternal(pText.getVisualOrderText(), pX, pY, pColor, pPoseStack.last().pose(), false);
    }

    public String bidirectionalShaping(String pText)
    {
        try
        {
            Bidi bidi = new Bidi((new ArabicShaping(8)).shape(pText), 127);
            bidi.setReorderingMode(0);
            return bidi.writeReordered(2);
        }
        catch (ArabicShapingException arabicshapingexception1)
        {
            return pText;
        }
    }

    private int drawInternal(String pText, float pX, float pY, int pColor, Matrix4f pMatrix, boolean pDropShadow, boolean pTransparent)
    {
        if (pText == null)
        {
            return 0;
        }
        else
        {
            MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            int i = this.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, multibuffersource$buffersource, false, 0, 15728880, pTransparent);
            multibuffersource$buffersource.endBatch();
            return i;
        }
    }

    public void renderStrings(List<String> texts, GuiPoint[] points, int color, Matrix4f matrix, boolean dropShadow, boolean bidiIn)
    {
        MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        for (int i = 0; i < texts.size(); ++i)
        {
            String s = texts.get(i);

            if (s != null && !s.isEmpty())
            {
                GuiPoint guipoint = points[i];

                if (guipoint != null)
                {
                    float f = (float)guipoint.getX();
                    float f1 = (float)guipoint.getY();
                    this.drawInBatch(s, f, f1, color, dropShadow, matrix, multibuffersource$buffersource, false, 0, 15728880, bidiIn);
                }
            }
        }

        multibuffersource$buffersource.endBatch();
    }

    private int drawInternal(FormattedCharSequence pReorderingProcessor, float pX, float pY, int pColor, Matrix4f pMatrix, boolean pDrawShadow)
    {
        MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        int i = this.drawInBatch(pReorderingProcessor, pX, pY, pColor, pDrawShadow, pMatrix, multibuffersource$buffersource, false, 0, 15728880);
        multibuffersource$buffersource.endBatch();
        return i;
    }

    public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pBackgroundColor, int pPackedLight)
    {
        return this.drawInBatch(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pBackgroundColor, pPackedLight, this.isBidirectional());
    }

    public int drawInBatch(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pBackgroundColor, int pPackedLight, boolean pBidiFlag)
    {
        return this.drawInternal(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pBackgroundColor, pPackedLight, pBidiFlag);
    }

    public int drawInBatch(Component pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pBackgroundColor, int pPackedLight)
    {
        return this.drawInBatch(pText.getVisualOrderText(), pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pBackgroundColor, pPackedLight);
    }

    public int drawInBatch(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pBackgroundColor, int pPackedLight)
    {
        return this.drawInternal(pText, pX, pY, pColor, pDropShadow, pMatrix, pBuffer, pTransparent, pBackgroundColor, pPackedLight);
    }

    public void drawInBatch8xOutline(FormattedCharSequence pText, float pX, float pY, int pColor, int pBackgroundColor, Matrix4f pMatrix, MultiBufferSource pBuffer, int pPackedLightCoords)
    {
        int i = adjustColor(pBackgroundColor);
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(pBuffer, 0.0F, 0.0F, i, false, pMatrix, Font.DisplayMode.NORMAL, pPackedLightCoords);

        for (int j = -1; j <= 1; ++j)
        {
            for (int k = -1; k <= 1; ++k)
            {
                if (j != 0 || k != 0)
                {
                    float[] afloat = new float[] {pX};
                    int l = j;
                    int i1 = k;
                    pText.accept((p_168654_7_, p_168654_8_, p_168654_9_) ->
                    {
                        boolean flag = p_168654_8_.isBold();
                        FontSet fontset = this.getFontSet(p_168654_8_.getFont());
                        GlyphInfo glyphinfo = fontset.getGlyphInfo(p_168654_9_, this.filterFishyGlyphs);
                        font$stringrenderoutput.x = afloat[0] + (float)l * glyphinfo.getShadowOffset();
                        font$stringrenderoutput.y = pY + (float)i1 * glyphinfo.getShadowOffset();
                        afloat[0] += glyphinfo.getAdvance(flag);
                        return font$stringrenderoutput.accept(p_168654_7_, p_168654_8_.withColor(i), p_168654_9_);
                    });
                }
            }
        }

        Font.StringRenderOutput font$stringrenderoutput1 = new Font.StringRenderOutput(pBuffer, pX, pY, adjustColor(pColor), false, pMatrix, Font.DisplayMode.POLYGON_OFFSET, pPackedLightCoords);
        pText.accept(font$stringrenderoutput1);
        font$stringrenderoutput1.finish(0, pX);
    }

    private static int adjustColor(int pColor)
    {
        return (pColor & -67108864) == 0 ? pColor | -16777216 : pColor;
    }

    private int drawInternal(String pText, float pX, float pY, int pColor, boolean pDropShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight, boolean pBidiFlag)
    {
        if (pBidiFlag)
        {
            pText = this.bidirectionalShaping(pText);
        }

        pColor = adjustColor(pColor);
        Matrix4f matrix4f = pMatrix.copy();

        if (pDropShadow)
        {
            this.renderText(pText, pX, pY, pColor, true, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight);
            matrix4f.translate(SHADOW_OFFSET);
        }

        pX = this.renderText(pText, pX, pY, pColor, false, matrix4f, pBuffer, pTransparent, pColorBackground, pPackedLight);
        return (int)pX + (pDropShadow ? 1 : 0);
    }

    private int drawInternal(FormattedCharSequence pProcessor, float pX, float pY, int pColor, boolean pDrawShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pTransparent, int pColorBackground, int pPackedLight)
    {
        pColor = adjustColor(pColor);
        Matrix4f matrix4f = pMatrix.copy();

        if (pDrawShadow)
        {
            this.renderText(pProcessor, pX, pY, pColor, true, pMatrix, pBuffer, pTransparent, pColorBackground, pPackedLight);
            matrix4f.translate(SHADOW_OFFSET);
        }

        pX = this.renderText(pProcessor, pX, pY, pColor, false, matrix4f, pBuffer, pTransparent, pColorBackground, pPackedLight);
        return (int)pX + (pDrawShadow ? 1 : 0);
    }

    private float renderText(String pText, float pX, float pY, int pColor, boolean pIsShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pIsTransparent, int pColorBackground, int pPackedLight)
    {
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(pBuffer, pX, pY, pColor, pIsShadow, pMatrix, pIsTransparent, pPackedLight);
        StringDecomposer.iterateFormatted(pText, Style.EMPTY, font$stringrenderoutput);
        return font$stringrenderoutput.finish(pColorBackground, pX);
    }

    private float renderText(FormattedCharSequence pText, float pX, float pY, int pColor, boolean pIsShadow, Matrix4f pMatrix, MultiBufferSource pBuffer, boolean pIsTransparent, int pColorBackground, int pPackedLight)
    {
        Font.StringRenderOutput font$stringrenderoutput = new Font.StringRenderOutput(pBuffer, pX, pY, pColor, pIsShadow, pMatrix, pIsTransparent, pPackedLight);
        pText.accept(font$stringrenderoutput);
        return font$stringrenderoutput.finish(pColorBackground, pX);
    }

    void renderChar(BakedGlyph pGlyph, boolean pBold, boolean pItalic, float pBoldOffset, float pX, float pY, Matrix4f pMatrix, VertexConsumer pBuffer, float pRed, float pGreen, float pBlue, float pAlpha, int pPackedLight)
    {
        pGlyph.render(pItalic, pX, pY, pMatrix, pBuffer, pRed, pGreen, pBlue, pAlpha, pPackedLight);

        if (pBold)
        {
            pGlyph.render(pItalic, pX + pBoldOffset, pY, pMatrix, pBuffer, pRed, pGreen, pBlue, pAlpha, pPackedLight);
        }
    }

    public int width(String pText)
    {
        return Mth.ceil(this.splitter.stringWidth(pText));
    }

    public int width(FormattedText pText)
    {
        return Mth.ceil(this.splitter.stringWidth(pText));
    }

    public int width(FormattedCharSequence pText)
    {
        return Mth.ceil(this.splitter.stringWidth(pText));
    }

    public String plainSubstrByWidth(String p_92838_, int p_92839_, boolean p_92840_)
    {
        return p_92840_ ? this.splitter.plainTailByWidth(p_92838_, p_92839_, Style.EMPTY) : this.splitter.plainHeadByWidth(p_92838_, p_92839_, Style.EMPTY);
    }

    public String plainSubstrByWidth(String pText, int pMaxWidth)
    {
        return this.splitter.plainHeadByWidth(pText, pMaxWidth, Style.EMPTY);
    }

    public FormattedText substrByWidth(FormattedText pText, int pMaxWidth)
    {
        return this.splitter.headByWidth(pText, pMaxWidth, Style.EMPTY);
    }

    public void drawWordWrap(FormattedText pText, int pX, int pY, int pMaxWidth, int pColor)
    {
        Matrix4f matrix4f = Transformation.identity().getMatrix();

        for (FormattedCharSequence formattedcharsequence : this.split(pText, pMaxWidth))
        {
            this.drawInternal(formattedcharsequence, (float)pX, (float)pY, pColor, matrix4f, false);
            pY += 9;
        }
    }

    public int wordWrapHeight(String pStr, int pMaxWidth)
    {
        return 9 * this.splitter.splitLines(pStr, pMaxWidth, Style.EMPTY).size();
    }

    public int wordWrapHeight(FormattedText pStr, int pMaxWidth)
    {
        return 9 * this.splitter.splitLines(pStr, pMaxWidth, Style.EMPTY).size();
    }

    public List<FormattedCharSequence> split(FormattedText pText, int pMaxWidth)
    {
        return Language.getInstance().getVisualOrder(this.splitter.splitLines(pText, pMaxWidth, Style.EMPTY));
    }

    public boolean isBidirectional()
    {
        return Language.getInstance().isDefaultRightToLeft();
    }

    public StringSplitter getSplitter()
    {
        return this.splitter;
    }

    public static enum DisplayMode
    {
        NORMAL,
        SEE_THROUGH,
        POLYGON_OFFSET;
    }

    class StringRenderOutput implements FormattedCharSink
    {
        final MultiBufferSource bufferSource;
        private final boolean dropShadow;
        private final float dimFactor;
        private final float r;
        private final float g;
        private final float b;
        private final float a;
        private final Matrix4f pose;
        private final Font.DisplayMode mode;
        private final int packedLightCoords;
        float x;
        float y;
        @Nullable
        private List<BakedGlyph.Effect> effects;
        private Style lastStyle;
        private FontSet lastStyleFont;

        private void addEffect(BakedGlyph.Effect pEffect)
        {
            if (this.effects == null)
            {
                this.effects = Lists.newArrayList();
            }

            this.effects.add(pEffect);
        }

        public StringRenderOutput(MultiBufferSource p_92953_, float p_92954_, float p_92955_, int p_92956_, boolean p_92957_, Matrix4f p_92958_, boolean p_92959_, int p_92960_)
        {
            this(p_92953_, p_92954_, p_92955_, p_92956_, p_92957_, p_92958_, p_92959_ ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, p_92960_);
        }

        public StringRenderOutput(MultiBufferSource p_181365_, float p_181366_, float p_181367_, int p_181368_, boolean p_181369_, Matrix4f p_181370_, Font.DisplayMode p_181371_, int p_181372_)
        {
            this.bufferSource = p_181365_;
            this.x = p_181366_;
            this.y = p_181367_;
            this.dropShadow = p_181369_;
            this.dimFactor = p_181369_ ? 0.25F : 1.0F;
            this.r = (float)(p_181368_ >> 16 & 255) / 255.0F * this.dimFactor;
            this.g = (float)(p_181368_ >> 8 & 255) / 255.0F * this.dimFactor;
            this.b = (float)(p_181368_ & 255) / 255.0F * this.dimFactor;
            this.a = (float)(p_181368_ >> 24 & 255) / 255.0F;
            this.pose = p_181370_.isIdentity() ? BakedGlyph.MATRIX_IDENTITY : p_181370_;
            this.mode = p_181371_;
            this.packedLightCoords = p_181372_;
        }

        public boolean accept(int p_92967_, Style p_92968_, int p_92969_)
        {
            FontSet fontset = this.getFont(p_92968_);
            GlyphInfo glyphinfo = fontset.getGlyphInfo(p_92969_, Font.this.filterFishyGlyphs);
            BakedGlyph bakedglyph = p_92968_.isObfuscated() && p_92969_ != 32 ? fontset.getRandomGlyph(glyphinfo) : fontset.getGlyph(p_92969_);
            boolean flag = p_92968_.isBold();
            float f = this.a;
            TextColor textcolor = p_92968_.getColor();
            float f1;
            float f2;
            float f3;

            if (textcolor != null)
            {
                int i = textcolor.getValue();
                f1 = (float)(i >> 16 & 255) / 255.0F * this.dimFactor;
                f2 = (float)(i >> 8 & 255) / 255.0F * this.dimFactor;
                f3 = (float)(i & 255) / 255.0F * this.dimFactor;
            }
            else
            {
                f1 = this.r;
                f2 = this.g;
                f3 = this.b;
            }

            if (!(bakedglyph instanceof EmptyGlyph))
            {
                float f5 = flag ? glyphinfo.getBoldOffset() : 0.0F;
                float f4 = this.dropShadow ? glyphinfo.getShadowOffset() : 0.0F;
                VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));
                Font.this.renderChar(bakedglyph, flag, p_92968_.isItalic(), f5, this.x + f4, this.y + f4, this.pose, vertexconsumer, f1, f2, f3, f, this.packedLightCoords);
            }

            float f6 = glyphinfo.getAdvance(flag);
            float f7 = this.dropShadow ? 1.0F : 0.0F;

            if (p_92968_.isStrikethrough())
            {
                this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 4.5F, this.x + f7 + f6, this.y + f7 + 4.5F - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (p_92968_.isUnderlined())
            {
                this.addEffect(new BakedGlyph.Effect(this.x + f7 - 1.0F, this.y + f7 + 9.0F, this.x + f7 + f6, this.y + f7 + 9.0F - 1.0F, 0.01F, f1, f2, f3, f));
            }

            this.x += f6;
            return true;
        }

        public float finish(int p_92962_, float p_92963_)
        {
            if (p_92962_ != 0)
            {
                float f = (float)(p_92962_ >> 24 & 255) / 255.0F;
                float f1 = (float)(p_92962_ >> 16 & 255) / 255.0F;
                float f2 = (float)(p_92962_ >> 8 & 255) / 255.0F;
                float f3 = (float)(p_92962_ & 255) / 255.0F;
                this.addEffect(new BakedGlyph.Effect(p_92963_ - 1.0F, this.y + 9.0F, this.x + 1.0F, this.y - 1.0F, 0.01F, f1, f2, f3, f));
            }

            if (this.effects != null)
            {
                BakedGlyph bakedglyph = Font.this.getFontSet(Style.DEFAULT_FONT).whiteGlyph();
                VertexConsumer vertexconsumer = this.bufferSource.getBuffer(bakedglyph.renderType(this.mode));

                for (BakedGlyph.Effect bakedglyph$effect : this.effects)
                {
                    bakedglyph.renderEffect(bakedglyph$effect, this.pose, vertexconsumer, this.packedLightCoords);
                }
            }

            return this.x;
        }

        private FontSet getFont(Style styleIn)
        {
            if (styleIn == this.lastStyle)
            {
                return this.lastStyleFont;
            }
            else
            {
                this.lastStyle = styleIn;
                this.lastStyleFont = Font.this.getFontSet(styleIn.getFont());
                return this.lastStyleFont;
            }
        }
    }
}
