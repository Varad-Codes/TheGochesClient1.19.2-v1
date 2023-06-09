package com.mojang.realmsclient.gui.screens;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.dto.WorldTemplate;
import com.mojang.realmsclient.dto.WorldTemplatePaginatedList;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsTextureManager;
import com.mojang.realmsclient.util.TextRenderingUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class RealmsSelectWorldTemplateScreen extends RealmsScreen
{
    static final Logger LOGGER = LogUtils.getLogger();
    static final ResourceLocation LINK_ICON = new ResourceLocation("realms", "textures/gui/realms/link_icons.png");
    static final ResourceLocation TRAILER_ICON = new ResourceLocation("realms", "textures/gui/realms/trailer_icons.png");
    static final ResourceLocation SLOT_FRAME_LOCATION = new ResourceLocation("realms", "textures/gui/realms/slot_frame.png");
    static final Component PUBLISHER_LINK_TOOLTIP = Component.translatable("mco.template.info.tooltip");
    static final Component TRAILER_LINK_TOOLTIP = Component.translatable("mco.template.trailer.tooltip");
    private final Consumer<WorldTemplate> callback;
    RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList worldTemplateObjectSelectionList;
    int selectedTemplate = -1;
    private Button selectButton;
    private Button trailerButton;
    private Button publisherButton;
    @Nullable
    Component toolTip;
    @Nullable
    String currentLink;
    private final RealmsServer.WorldType worldType;
    int clicks;
    @Nullable
    private Component[] warning;
    private String warningURL;
    boolean displayWarning;
    private boolean hoverWarning;
    @Nullable
    List<TextRenderingUtils.Line> noTemplatesMessage;

    public RealmsSelectWorldTemplateScreen(Component pTitle, Consumer<WorldTemplate> pCallback, RealmsServer.WorldType pWorldType)
    {
        this(pTitle, pCallback, pWorldType, (WorldTemplatePaginatedList)null);
    }

    public RealmsSelectWorldTemplateScreen(Component pTitle, Consumer<WorldTemplate> pCallback, RealmsServer.WorldType pWorldType, @Nullable WorldTemplatePaginatedList pWorldTemplatePaginatedList)
    {
        super(pTitle);
        this.callback = pCallback;
        this.worldType = pWorldType;

        if (pWorldTemplatePaginatedList == null)
        {
            this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList();
            this.fetchTemplatesAsync(new WorldTemplatePaginatedList(10));
        }
        else
        {
            this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(Lists.newArrayList(pWorldTemplatePaginatedList.templates));
            this.fetchTemplatesAsync(pWorldTemplatePaginatedList);
        }
    }

    public void a(Component... p_89683_)
    {
        this.warning = p_89683_;
        this.displayWarning = true;
    }

    public boolean mouseClicked(double pMouseX, double p_89630_, int pMouseY)
    {
        if (this.hoverWarning && this.warningURL != null)
        {
            Util.getPlatform().openUri("https://www.minecraft.net/realms/adventure-maps-in-1-9");
            return true;
        }
        else
        {
            return super.mouseClicked(pMouseX, p_89630_, pMouseY);
        }
    }

    public void init()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.worldTemplateObjectSelectionList = new RealmsSelectWorldTemplateScreen.WorldTemplateObjectSelectionList(this.worldTemplateObjectSelectionList.getTemplates());
        this.trailerButton = this.addRenderableWidget(new Button(this.width / 2 - 206, this.height - 32, 100, 20, Component.translatable("mco.template.button.trailer"), (p_89701_) ->
        {
            this.onTrailer();
        }));
        this.selectButton = this.addRenderableWidget(new Button(this.width / 2 - 100, this.height - 32, 100, 20, Component.translatable("mco.template.button.select"), (p_89696_) ->
        {
            this.selectTemplate();
        }));
        Component component = this.worldType == RealmsServer.WorldType.MINIGAME ? CommonComponents.GUI_CANCEL : CommonComponents.GUI_BACK;
        Button button = new Button(this.width / 2 + 6, this.height - 32, 100, 20, component, (p_89691_) ->
        {
            this.onClose();
        });
        this.addRenderableWidget(button);
        this.publisherButton = this.addRenderableWidget(new Button(this.width / 2 + 112, this.height - 32, 100, 20, Component.translatable("mco.template.button.publisher"), (p_89679_) ->
        {
            this.onPublish();
        }));
        this.selectButton.active = false;
        this.trailerButton.visible = false;
        this.publisherButton.visible = false;
        this.addWidget(this.worldTemplateObjectSelectionList);
        this.magicalSpecialHackyFocus(this.worldTemplateObjectSelectionList);
    }

    public Component getNarrationMessage()
    {
        List<Component> list = Lists.newArrayListWithCapacity(2);

        if (this.title != null)
        {
            list.add(this.title);
        }

        if (this.warning != null)
        {
            list.addAll(Arrays.asList(this.warning));
        }

        return CommonComponents.joinLines(list);
    }

    void updateButtonStates()
    {
        this.publisherButton.visible = this.shouldPublisherBeVisible();
        this.trailerButton.visible = this.shouldTrailerBeVisible();
        this.selectButton.active = this.shouldSelectButtonBeActive();
    }

    private boolean shouldSelectButtonBeActive()
    {
        return this.selectedTemplate != -1;
    }

    private boolean shouldPublisherBeVisible()
    {
        return this.selectedTemplate != -1 && !this.getSelectedTemplate().link.isEmpty();
    }

    private WorldTemplate getSelectedTemplate()
    {
        return this.worldTemplateObjectSelectionList.get(this.selectedTemplate);
    }

    private boolean shouldTrailerBeVisible()
    {
        return this.selectedTemplate != -1 && !this.getSelectedTemplate().trailer.isEmpty();
    }

    public void tick()
    {
        super.tick();
        --this.clicks;

        if (this.clicks < 0)
        {
            this.clicks = 0;
        }
    }

    public void onClose()
    {
        this.callback.accept((WorldTemplate)null);
    }

    void selectTemplate()
    {
        if (this.hasValidTemplate())
        {
            this.callback.accept(this.getSelectedTemplate());
        }
    }

    private boolean hasValidTemplate()
    {
        return this.selectedTemplate >= 0 && this.selectedTemplate < this.worldTemplateObjectSelectionList.getItemCount();
    }

    private void onTrailer()
    {
        if (this.hasValidTemplate())
        {
            WorldTemplate worldtemplate = this.getSelectedTemplate();

            if (!"".equals(worldtemplate.trailer))
            {
                Util.getPlatform().openUri(worldtemplate.trailer);
            }
        }
    }

    private void onPublish()
    {
        if (this.hasValidTemplate())
        {
            WorldTemplate worldtemplate = this.getSelectedTemplate();

            if (!"".equals(worldtemplate.link))
            {
                Util.getPlatform().openUri(worldtemplate.link);
            }
        }
    }

    private void fetchTemplatesAsync(final WorldTemplatePaginatedList p_89654_)
    {
        (new Thread("realms-template-fetcher")
        {
            public void run()
            {
                WorldTemplatePaginatedList worldtemplatepaginatedlist = p_89654_;
                RealmsClient realmsclient = RealmsClient.create();

                while (worldtemplatepaginatedlist != null)
                {
                    Either<WorldTemplatePaginatedList, String> either = RealmsSelectWorldTemplateScreen.this.fetchTemplates(worldtemplatepaginatedlist, realmsclient);
                    worldtemplatepaginatedlist = RealmsSelectWorldTemplateScreen.this.minecraft.submit(() ->
                    {
                        if (either.right().isPresent())
                        {
                            RealmsSelectWorldTemplateScreen.LOGGER.error("Couldn't fetch templates: {}", either.right().get());

                            if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty())
                            {
                                RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.a(I18n.a("mco.template.select.failure"));
                            }

                            return null;
                        }
                        else {
                            WorldTemplatePaginatedList worldtemplatepaginatedlist1 = either.left().get();

                            for (WorldTemplate worldtemplate : worldtemplatepaginatedlist1.templates)
                            {
                                RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.addEntry(worldtemplate);
                            }

                            if (worldtemplatepaginatedlist1.templates.isEmpty())
                            {
                                if (RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.isEmpty())
                                {
                                    String s = I18n.a("mco.template.select.none", "%link");
                                    TextRenderingUtils.LineSegment textrenderingutils$linesegment = TextRenderingUtils.LineSegment.link(I18n.a("mco.template.select.none.linkTitle"), "https://aka.ms/MinecraftRealmsContentCreator");
                                    RealmsSelectWorldTemplateScreen.this.noTemplatesMessage = TextRenderingUtils.a(s, textrenderingutils$linesegment);
                                }

                                return null;
                            }
                            else {
                                return worldtemplatepaginatedlist1;
                            }
                        }
                    }).join();
                }
            }
        }).start();
    }

    Either<WorldTemplatePaginatedList, String> fetchTemplates(WorldTemplatePaginatedList p_89656_, RealmsClient p_89657_)
    {
        try
        {
            return Either.left(p_89657_.fetchWorldTemplates(p_89656_.page + 1, p_89656_.size, this.worldType));
        }
        catch (RealmsServiceException realmsserviceexception)
        {
            return Either.right(realmsserviceexception.getMessage());
        }
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        this.toolTip = null;
        this.currentLink = null;
        this.hoverWarning = false;
        this.renderBackground(pPoseStack);
        this.worldTemplateObjectSelectionList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (this.noTemplatesMessage != null)
        {
            this.renderMultilineMessage(pPoseStack, pMouseX, pMouseY, this.noTemplatesMessage);
        }

        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 13, 16777215);

        if (this.displayWarning)
        {
            Component[] acomponent = this.warning;

            for (int i = 0; i < acomponent.length; ++i)
            {
                int j = this.font.width(acomponent[i]);
                int k = this.width / 2 - j / 2;
                int l = row(-1 + i);

                if (pMouseX >= k && pMouseX <= k + j && pMouseY >= l && pMouseY <= l + 9)
                {
                    this.hoverWarning = true;
                }
            }

            for (int i1 = 0; i1 < acomponent.length; ++i1)
            {
                Component component = acomponent[i1];
                int j1 = 10526880;

                if (this.warningURL != null)
                {
                    if (this.hoverWarning)
                    {
                        j1 = 7107012;
                        component = component.copy().withStyle(ChatFormatting.STRIKETHROUGH);
                    }
                    else
                    {
                        j1 = 3368635;
                    }
                }

                drawCenteredString(pPoseStack, this.font, component, this.width / 2, row(-1 + i1), j1);
            }
        }

        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        this.renderMousehoverTooltip(pPoseStack, this.toolTip, pMouseX, pMouseY);
    }

    private void renderMultilineMessage(PoseStack p_89644_, int p_89645_, int p_89646_, List<TextRenderingUtils.Line> p_89647_)
    {
        for (int i = 0; i < p_89647_.size(); ++i)
        {
            TextRenderingUtils.Line textrenderingutils$line = p_89647_.get(i);
            int j = row(4 + i);
            int k = textrenderingutils$line.segments.stream().mapToInt((p_89677_) ->
            {
                return this.font.width(p_89677_.renderedText());
            }).sum();
            int l = this.width / 2 - k / 2;

            for (TextRenderingUtils.LineSegment textrenderingutils$linesegment : textrenderingutils$line.segments)
            {
                int i1 = textrenderingutils$linesegment.isLink() ? 3368635 : 16777215;
                int j1 = this.font.drawShadow(p_89644_, textrenderingutils$linesegment.renderedText(), (float)l, (float)j, i1);

                if (textrenderingutils$linesegment.isLink() && p_89645_ > l && p_89645_ < j1 && p_89646_ > j - 3 && p_89646_ < j + 8)
                {
                    this.toolTip = Component.literal(textrenderingutils$linesegment.getLinkUrl());
                    this.currentLink = textrenderingutils$linesegment.getLinkUrl();
                }

                l = j1;
            }
        }
    }

    protected void renderMousehoverTooltip(PoseStack p_89649_, @Nullable Component p_89650_, int p_89651_, int p_89652_)
    {
        if (p_89650_ != null)
        {
            int i = p_89651_ + 12;
            int j = p_89652_ - 12;
            int k = this.font.width(p_89650_);
            this.fillGradient(p_89649_, i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(p_89649_, p_89650_, (float)i, (float)j, 16777215);
        }
    }

    class Entry extends ObjectSelectionList.Entry<RealmsSelectWorldTemplateScreen.Entry>
    {
        final WorldTemplate template;

        public Entry(WorldTemplate p_89753_)
        {
            this.template = p_89753_;
        }

        public void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick)
        {
            this.renderWorldTemplateItem(pPoseStack, this.template, pLeft, pTop, pMouseX, pMouseY);
        }

        private void renderWorldTemplateItem(PoseStack p_89782_, WorldTemplate p_89783_, int p_89784_, int p_89785_, int p_89786_, int p_89787_)
        {
            int i = p_89784_ + 45 + 20;
            RealmsSelectWorldTemplateScreen.this.font.draw(p_89782_, p_89783_.name, (float)i, (float)(p_89785_ + 2), 16777215);
            RealmsSelectWorldTemplateScreen.this.font.draw(p_89782_, p_89783_.author, (float)i, (float)(p_89785_ + 15), 7105644);
            RealmsSelectWorldTemplateScreen.this.font.draw(p_89782_, p_89783_.version, (float)(i + 227 - RealmsSelectWorldTemplateScreen.this.font.width(p_89783_.version)), (float)(p_89785_ + 1), 7105644);

            if (!"".equals(p_89783_.link) || !"".equals(p_89783_.trailer) || !"".equals(p_89783_.recommendedPlayers))
            {
                this.drawIcons(p_89782_, i - 1, p_89785_ + 25, p_89786_, p_89787_, p_89783_.link, p_89783_.trailer, p_89783_.recommendedPlayers);
            }

            this.drawImage(p_89782_, p_89784_, p_89785_ + 1, p_89786_, p_89787_, p_89783_);
        }

        private void drawImage(PoseStack p_89766_, int p_89767_, int p_89768_, int p_89769_, int p_89770_, WorldTemplate p_89771_)
        {
            RealmsTextureManager.bindWorldTemplate(p_89771_.id, p_89771_.image);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(p_89766_, p_89767_ + 1, p_89768_ + 1, 0.0F, 0.0F, 38, 38, 38, 38);
            RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.SLOT_FRAME_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            GuiComponent.blit(p_89766_, p_89767_, p_89768_, 0.0F, 0.0F, 40, 40, 40, 40);
        }

        private void drawIcons(PoseStack p_89773_, int p_89774_, int p_89775_, int p_89776_, int p_89777_, String p_89778_, String p_89779_, String p_89780_)
        {
            if (!"".equals(p_89780_))
            {
                RealmsSelectWorldTemplateScreen.this.font.draw(p_89773_, p_89780_, (float)p_89774_, (float)(p_89775_ + 4), 5000268);
            }

            int i = "".equals(p_89780_) ? 0 : RealmsSelectWorldTemplateScreen.this.font.width(p_89780_) + 2;
            boolean flag = false;
            boolean flag1 = false;
            boolean flag2 = "".equals(p_89778_);

            if (p_89776_ >= p_89774_ + i && p_89776_ <= p_89774_ + i + 32 && p_89777_ >= p_89775_ && p_89777_ <= p_89775_ + 15 && p_89777_ < RealmsSelectWorldTemplateScreen.this.height - 15 && p_89777_ > 32)
            {
                if (p_89776_ <= p_89774_ + 15 + i && p_89776_ > i)
                {
                    if (flag2)
                    {
                        flag1 = true;
                    }
                    else
                    {
                        flag = true;
                    }
                }
                else if (!flag2)
                {
                    flag1 = true;
                }
            }

            if (!flag2)
            {
                RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.LINK_ICON);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                float f = flag ? 15.0F : 0.0F;
                GuiComponent.blit(p_89773_, p_89774_ + i, p_89775_, f, 0.0F, 15, 15, 30, 15);
            }

            if (!"".equals(p_89779_))
            {
                RenderSystem.setShaderTexture(0, RealmsSelectWorldTemplateScreen.TRAILER_ICON);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                int j = p_89774_ + i + (flag2 ? 0 : 17);
                float f1 = flag1 ? 15.0F : 0.0F;
                GuiComponent.blit(p_89773_, j, p_89775_, f1, 0.0F, 15, 15, 30, 15);
            }

            if (flag)
            {
                RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.PUBLISHER_LINK_TOOLTIP;
                RealmsSelectWorldTemplateScreen.this.currentLink = p_89778_;
            }
            else if (flag1 && !"".equals(p_89779_))
            {
                RealmsSelectWorldTemplateScreen.this.toolTip = RealmsSelectWorldTemplateScreen.TRAILER_LINK_TOOLTIP;
                RealmsSelectWorldTemplateScreen.this.currentLink = p_89779_;
            }
        }

        public Component getNarration()
        {
            Component component = CommonComponents.a(Component.literal(this.template.name), Component.a("mco.template.select.narrate.authors", this.template.author), Component.literal(this.template.recommendedPlayers), Component.a("mco.template.select.narrate.version", this.template.version));
            return Component.a("narrator.select", component);
        }
    }

    class WorldTemplateObjectSelectionList extends RealmsObjectSelectionList<RealmsSelectWorldTemplateScreen.Entry>
    {
        public WorldTemplateObjectSelectionList()
        {
            this(Collections.emptyList());
        }

        public WorldTemplateObjectSelectionList(Iterable<WorldTemplate> p_89795_)
        {
            super(RealmsSelectWorldTemplateScreen.this.width, RealmsSelectWorldTemplateScreen.this.height, RealmsSelectWorldTemplateScreen.this.displayWarning ? RealmsSelectWorldTemplateScreen.row(1) : 32, RealmsSelectWorldTemplateScreen.this.height - 40, 46);
            p_89795_.forEach(this::addEntry);
        }

        public void addEntry(WorldTemplate p_89805_)
        {
            this.addEntry(RealmsSelectWorldTemplateScreen.this.new Entry(p_89805_));
        }

        public boolean mouseClicked(double p_89797_, double p_89798_, int p_89799_)
        {
            if (p_89799_ == 0 && p_89798_ >= (double)this.y0 && p_89798_ <= (double)this.y1)
            {
                int i = this.width / 2 - 150;

                if (RealmsSelectWorldTemplateScreen.this.currentLink != null)
                {
                    Util.getPlatform().openUri(RealmsSelectWorldTemplateScreen.this.currentLink);
                }

                int j = (int)Math.floor(p_89798_ - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount() - 4;
                int k = j / this.itemHeight;

                if (p_89797_ >= (double)i && p_89797_ < (double)this.getScrollbarPosition() && k >= 0 && j >= 0 && k < this.getItemCount())
                {
                    this.selectItem(k);
                    this.itemClicked(j, k, p_89797_, p_89798_, this.width);

                    if (k >= RealmsSelectWorldTemplateScreen.this.worldTemplateObjectSelectionList.getItemCount())
                    {
                        return super.mouseClicked(p_89797_, p_89798_, p_89799_);
                    }

                    RealmsSelectWorldTemplateScreen.this.clicks += 7;

                    if (RealmsSelectWorldTemplateScreen.this.clicks >= 10)
                    {
                        RealmsSelectWorldTemplateScreen.this.selectTemplate();
                    }

                    return true;
                }
            }

            return super.mouseClicked(p_89797_, p_89798_, p_89799_);
        }

        public void setSelected(@Nullable RealmsSelectWorldTemplateScreen.Entry p_89807_)
        {
            super.setSelected(p_89807_);
            RealmsSelectWorldTemplateScreen.this.selectedTemplate = this.children().indexOf(p_89807_);
            RealmsSelectWorldTemplateScreen.this.updateButtonStates();
        }

        public int getMaxPosition()
        {
            return this.getItemCount() * 46;
        }

        public int getRowWidth()
        {
            return 300;
        }

        public void renderBackground(PoseStack p_89803_)
        {
            RealmsSelectWorldTemplateScreen.this.renderBackground(p_89803_);
        }

        public boolean isFocused()
        {
            return RealmsSelectWorldTemplateScreen.this.getFocused() == this;
        }

        public boolean isEmpty()
        {
            return this.getItemCount() == 0;
        }

        public WorldTemplate get(int p_89812_)
        {
            return (this.children().get(p_89812_)).template;
        }

        public List<WorldTemplate> getTemplates()
        {
            return this.children().stream().map((p_89814_) ->
            {
                return p_89814_.template;
            }).collect(Collectors.toList());
        }
    }
}
