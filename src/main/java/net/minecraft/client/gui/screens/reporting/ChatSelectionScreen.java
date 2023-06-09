package net.minecraft.client.gui.screens.reporting;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.report.AbuseReportLimits;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatTrustLevel;
import net.minecraft.client.multiplayer.chat.LoggedChatMessage;
import net.minecraft.client.multiplayer.chat.report.ChatReportBuilder;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;

public class ChatSelectionScreen extends Screen
{
    private static final Component TITLE = Component.translatable("gui.chatSelection.title");
    private static final Component CONTEXT_INFO = Component.translatable("gui.chatSelection.context").withStyle(ChatFormatting.GRAY);
    @Nullable
    private final Screen lastScreen;
    private final ReportingContext reportingContext;
    private Button confirmSelectedButton;
    private MultiLineLabel contextInfoLabel;
    @Nullable
    private ChatSelectionScreen.ChatSelectionList chatSelectionList;
    final ChatReportBuilder report;
    private final Consumer<ChatReportBuilder> onSelected;
    private ChatSelectionLogFiller<LoggedChatMessage.Player> chatLogFiller;
    @Nullable
    private List<FormattedCharSequence> tooltip;

    public ChatSelectionScreen(@Nullable Screen p_239090_, ReportingContext p_239091_, ChatReportBuilder p_239092_, Consumer<ChatReportBuilder> p_239093_)
    {
        super(TITLE);
        this.lastScreen = p_239090_;
        this.reportingContext = p_239091_;
        this.report = p_239092_.copy();
        this.onSelected = p_239093_;
    }

    protected void init()
    {
        this.chatLogFiller = new ChatSelectionLogFiller<>(this.reportingContext.chatLog(), this::canReport, LoggedChatMessage.Player.class);
        this.contextInfoLabel = MultiLineLabel.create(this.font, CONTEXT_INFO, this.width - 16);
        this.chatSelectionList = new ChatSelectionScreen.ChatSelectionList(this.minecraft, (this.contextInfoLabel.getLineCount() + 1) * 9);
        this.chatSelectionList.setRenderBackground(false);
        this.addWidget(this.chatSelectionList);
        this.addRenderableWidget(new Button(this.width / 2 - 155, this.height - 32, 150, 20, CommonComponents.GUI_BACK, (p_239860_) ->
        {
            this.onClose();
        }));
        this.confirmSelectedButton = this.addRenderableWidget(new Button(this.width / 2 - 155 + 160, this.height - 32, 150, 20, CommonComponents.GUI_DONE, (p_239591_) ->
        {
            this.onSelected.accept(this.report);
            this.onClose();
        }));
        this.updateConfirmSelectedButton();
        this.extendLog();
        this.chatSelectionList.setScrollAmount((double)this.chatSelectionList.getMaxScroll());
    }

    private boolean canReport(LoggedChatMessage p_242240_)
    {
        return p_242240_.canReport(this.report.reportedProfileId());
    }

    private void extendLog()
    {
        int i = this.chatSelectionList.getMaxVisibleEntries();
        this.chatLogFiller.fillNextPage(i, this.chatSelectionList);
    }

    void onReachedScrollTop()
    {
        this.extendLog();
    }

    void updateConfirmSelectedButton()
    {
        this.confirmSelectedButton.active = !this.report.reportedMessages().isEmpty();
    }

    public void render(PoseStack p_239286_, int p_239287_, int p_239288_, float p_239289_)
    {
        this.renderBackground(p_239286_);
        this.chatSelectionList.render(p_239286_, p_239287_, p_239288_, p_239289_);
        drawCenteredString(p_239286_, this.font, this.title, this.width / 2, 16, 16777215);
        AbuseReportLimits abusereportlimits = this.reportingContext.sender().reportLimits();
        int i = this.report.reportedMessages().size();
        int j = abusereportlimits.maxReportedMessageCount();
        Component component = Component.a("gui.chatSelection.selected", i, j);
        drawCenteredString(p_239286_, this.font, component, this.width / 2, 16 + 9 * 3 / 2, 10526880);
        this.contextInfoLabel.renderCentered(p_239286_, this.width / 2, this.chatSelectionList.getFooterTop());
        super.render(p_239286_, p_239287_, p_239288_, p_239289_);

        if (this.tooltip != null)
        {
            this.renderTooltip(p_239286_, this.tooltip, p_239287_, p_239288_);
            this.tooltip = null;
        }
    }

    public void onClose()
    {
        this.minecraft.setScreen(this.lastScreen);
    }

    public Component getNarrationMessage()
    {
        return CommonComponents.joinForNarration(super.getNarrationMessage(), CONTEXT_INFO);
    }

    void setTooltip(@Nullable List<FormattedCharSequence> p_239306_)
    {
        this.tooltip = p_239306_;
    }

    public class ChatSelectionList extends ObjectSelectionList<ChatSelectionScreen.ChatSelectionList.Entry> implements ChatSelectionLogFiller.Output<LoggedChatMessage.Player>
    {
        @Nullable
        private ChatSelectionScreen.ChatSelectionList.Heading previousHeading;

        public ChatSelectionList(Minecraft p_239060_, int p_239061_)
        {
            super(p_239060_, ChatSelectionScreen.this.width, ChatSelectionScreen.this.height, 40, ChatSelectionScreen.this.height - 40 - p_239061_, 16);
        }

        public void setScrollAmount(double p_239021_)
        {
            double d0 = this.getScrollAmount();
            super.setScrollAmount(p_239021_);

            if ((float)this.getMaxScroll() > 1.0E-5F && p_239021_ <= (double)1.0E-5F && !Mth.equal(p_239021_, d0))
            {
                ChatSelectionScreen.this.onReachedScrollTop();
            }
        }

        public void acceptMessage(int p_242846_, LoggedChatMessage.Player p_242909_)
        {
            boolean flag = p_242909_.canReport(ChatSelectionScreen.this.report.reportedProfileId());
            ChatTrustLevel chattrustlevel = p_242909_.trustLevel();
            GuiMessageTag guimessagetag = chattrustlevel.createTag(p_242909_.message());
            ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = new ChatSelectionScreen.ChatSelectionList.MessageEntry(p_242846_, p_242909_.toContentComponent(), p_242909_.toNarrationComponent(), guimessagetag, flag, true);
            this.addEntryToTop(chatselectionscreen$chatselectionlist$entry);
            this.updateHeading(p_242909_, flag);
        }

        private void updateHeading(LoggedChatMessage.Player p_242229_, boolean p_240019_)
        {
            ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = new ChatSelectionScreen.ChatSelectionList.MessageHeadingEntry(p_242229_.profile(), p_242229_.toHeadingComponent(), p_240019_);
            this.addEntryToTop(chatselectionscreen$chatselectionlist$entry);
            ChatSelectionScreen.ChatSelectionList.Heading chatselectionscreen$chatselectionlist$heading = new ChatSelectionScreen.ChatSelectionList.Heading(p_242229_.profileId(), chatselectionscreen$chatselectionlist$entry);

            if (this.previousHeading != null && this.previousHeading.canCombine(chatselectionscreen$chatselectionlist$heading))
            {
                this.removeEntryFromTop(this.previousHeading.entry());
            }

            this.previousHeading = chatselectionscreen$chatselectionlist$heading;
        }

        public void acceptDivider(Component p_239876_)
        {
            this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
            this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.DividerEntry(p_239876_));
            this.addEntryToTop(new ChatSelectionScreen.ChatSelectionList.PaddingEntry());
            this.previousHeading = null;
        }

        protected int getScrollbarPosition()
        {
            return (this.width + this.getRowWidth()) / 2;
        }

        public int getRowWidth()
        {
            return Math.min(350, this.width - 50);
        }

        public int getMaxVisibleEntries()
        {
            return Mth.positiveCeilDiv(this.y1 - this.y0, this.itemHeight);
        }

        protected void renderItem(PoseStack p_239774_, int p_239775_, int p_239776_, float p_239777_, int p_239778_, int p_239779_, int p_239780_, int p_239781_, int p_239782_)
        {
            ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = this.getEntry(p_239778_);

            if (this.shouldHighlightEntry(chatselectionscreen$chatselectionlist$entry))
            {
                boolean flag = this.getSelected() == chatselectionscreen$chatselectionlist$entry;
                int i = this.isFocused() && flag ? -1 : -8355712;
                this.renderSelection(p_239774_, p_239780_, p_239781_, p_239782_, i, -16777216);
            }

            chatselectionscreen$chatselectionlist$entry.render(p_239774_, p_239778_, p_239780_, p_239779_, p_239781_, p_239782_, p_239775_, p_239776_, this.getHovered() == chatselectionscreen$chatselectionlist$entry, p_239777_);
        }

        private boolean shouldHighlightEntry(ChatSelectionScreen.ChatSelectionList.Entry p_240327_)
        {
            if (p_240327_.canSelect())
            {
                boolean flag = this.getSelected() == p_240327_;
                boolean flag1 = this.getSelected() == null;
                boolean flag2 = this.getHovered() == p_240327_;
                return flag || flag1 && flag2 && p_240327_.canReport();
            }
            else
            {
                return false;
            }
        }

        protected void moveSelection(AbstractSelectionList.SelectionDirection p_239561_)
        {
            if (!this.moveSelectableSelection(p_239561_) && p_239561_ == AbstractSelectionList.SelectionDirection.UP)
            {
                ChatSelectionScreen.this.onReachedScrollTop();
                this.moveSelectableSelection(p_239561_);
            }
        }

        private boolean moveSelectableSelection(AbstractSelectionList.SelectionDirection p_239917_)
        {
            return this.moveSelection(p_239917_, ChatSelectionScreen.ChatSelectionList.Entry::canSelect);
        }

        public boolean keyPressed(int p_239322_, int p_239323_, int p_239324_)
        {
            ChatSelectionScreen.ChatSelectionList.Entry chatselectionscreen$chatselectionlist$entry = this.getSelected();

            if (chatselectionscreen$chatselectionlist$entry != null && chatselectionscreen$chatselectionlist$entry.keyPressed(p_239322_, p_239323_, p_239324_))
            {
                return true;
            }
            else
            {
                this.setFocused((GuiEventListener)null);
                return super.keyPressed(p_239322_, p_239323_, p_239324_);
            }
        }

        public int getFooterTop()
        {
            return this.y1 + 9;
        }

        protected boolean isFocused()
        {
            return ChatSelectionScreen.this.getFocused() == this;
        }

        public class DividerEntry extends ChatSelectionScreen.ChatSelectionList.Entry
        {
            private static final int COLOR = -6250336;
            private final Component text;

            public DividerEntry(Component p_239672_)
            {
                this.text = p_239672_;
            }

            public void render(PoseStack p_239814_, int p_239815_, int p_239816_, int p_239817_, int p_239818_, int p_239819_, int p_239820_, int p_239821_, boolean p_239822_, float p_239823_)
            {
                int i = p_239816_ + p_239819_ / 2;
                int j = p_239817_ + p_239818_ - 8;
                int k = ChatSelectionScreen.this.font.width(this.text);
                int l = (p_239817_ + j - k) / 2;
                int i1 = i - 9 / 2;
                GuiComponent.drawString(p_239814_, ChatSelectionScreen.this.font, this.text, l, i1, -6250336);
            }

            public Component getNarration()
            {
                return this.text;
            }
        }

        public abstract class Entry extends ObjectSelectionList.Entry<ChatSelectionScreen.ChatSelectionList.Entry>
        {
            public Component getNarration()
            {
                return CommonComponents.EMPTY;
            }

            public boolean isSelected()
            {
                return false;
            }

            public boolean canSelect()
            {
                return false;
            }

            public boolean canReport()
            {
                return this.canSelect();
            }
        }

        static record Heading(UUID sender, ChatSelectionScreen.ChatSelectionList.Entry entry)
        {
            public boolean canCombine(ChatSelectionScreen.ChatSelectionList.Heading p_239748_)
            {
                return p_239748_.sender.equals(this.sender);
            }
        }

        public class MessageEntry extends ChatSelectionScreen.ChatSelectionList.Entry
        {
            private static final ResourceLocation CHECKMARK_TEXTURE = new ResourceLocation("realms", "textures/gui/realms/checkmark.png");
            private static final int CHECKMARK_WIDTH = 9;
            private static final int CHECKMARK_HEIGHT = 8;
            private static final int INDENT_AMOUNT = 11;
            private static final int TAG_MARGIN_LEFT = 4;
            private final int chatId;
            private final FormattedText text;
            private final Component narration;
            @Nullable
            private final List<FormattedCharSequence> hoverText;
            @Nullable
            private final GuiMessageTag.Icon tagIcon;
            @Nullable
            private final List<FormattedCharSequence> tagHoverText;
            private final boolean canReport;
            private final boolean playerMessage;

            public MessageEntry(int p_240650_, Component p_240525_, @Nullable Component p_240539_, GuiMessageTag p_240551_, boolean p_240596_, boolean p_240615_)
            {
                this.chatId = p_240650_;
                this.tagIcon = Util.mapNullable(p_240551_, GuiMessageTag::icon);
                this.tagHoverText = p_240551_ != null && p_240551_.text() != null ? ChatSelectionScreen.this.font.split(p_240551_.text(), ChatSelectionList.this.getRowWidth()) : null;
                this.canReport = p_240596_;
                this.playerMessage = p_240615_;
                FormattedText formattedtext = ChatSelectionScreen.this.font.substrByWidth(p_240525_, this.getMaximumTextWidth() - ChatSelectionScreen.this.font.width(CommonComponents.ELLIPSIS));

                if (p_240525_ != formattedtext)
                {
                    this.text = FormattedText.a(formattedtext, CommonComponents.ELLIPSIS);
                    this.hoverText = ChatSelectionScreen.this.font.split(p_240525_, ChatSelectionList.this.getRowWidth());
                }
                else
                {
                    this.text = p_240525_;
                    this.hoverText = null;
                }

                this.narration = p_240539_;
            }

            public void render(PoseStack p_239595_, int p_239596_, int p_239597_, int p_239598_, int p_239599_, int p_239600_, int p_239601_, int p_239602_, boolean p_239603_, float p_239604_)
            {
                if (this.isSelected() && this.canReport)
                {
                    this.renderSelectedCheckmark(p_239595_, p_239597_, p_239598_, p_239600_);
                }

                int i = p_239598_ + this.getTextIndent();
                int j = p_239597_ + 1 + (p_239600_ - 9) / 2;
                GuiComponent.drawString(p_239595_, ChatSelectionScreen.this.font, Language.getInstance().getVisualOrder(this.text), i, j, this.canReport ? -1 : -1593835521);

                if (this.hoverText != null && p_239603_)
                {
                    ChatSelectionScreen.this.setTooltip(this.hoverText);
                }

                int k = ChatSelectionScreen.this.font.width(this.text);
                this.renderTag(p_239595_, i + k + 4, p_239597_, p_239600_, p_239601_, p_239602_);
            }

            private void renderTag(PoseStack p_240603_, int p_240566_, int p_240565_, int p_240581_, int p_240614_, int p_240612_)
            {
                if (this.tagIcon != null)
                {
                    int i = p_240565_ + (p_240581_ - this.tagIcon.height) / 2;
                    this.tagIcon.draw(p_240603_, p_240566_, i);

                    if (this.tagHoverText != null && p_240614_ >= p_240566_ && p_240614_ <= p_240566_ + this.tagIcon.width && p_240612_ >= i && p_240612_ <= i + this.tagIcon.height)
                    {
                        ChatSelectionScreen.this.setTooltip(this.tagHoverText);
                    }
                }
            }

            private void renderSelectedCheckmark(PoseStack p_240274_, int p_240275_, int p_240276_, int p_240277_)
            {
                int i = p_240275_ + (p_240277_ - 8) / 2;
                RenderSystem.setShaderTexture(0, CHECKMARK_TEXTURE);
                RenderSystem.enableBlend();
                GuiComponent.blit(p_240274_, p_240276_, i, 0.0F, 0.0F, 9, 8, 9, 8);
                RenderSystem.disableBlend();
            }

            private int getMaximumTextWidth()
            {
                int i = this.tagIcon != null ? this.tagIcon.width + 4 : 0;
                return ChatSelectionList.this.getRowWidth() - this.getTextIndent() - 4 - i;
            }

            private int getTextIndent()
            {
                return this.playerMessage ? 11 : 0;
            }

            public Component getNarration()
            {
                return (Component)(this.isSelected() ? Component.a("narrator.select", this.narration) : this.narration);
            }

            public boolean mouseClicked(double p_239729_, double p_239730_, int p_239731_)
            {
                if (p_239731_ == 0)
                {
                    ChatSelectionList.this.setSelected((ChatSelectionScreen.ChatSelectionList.Entry)null);
                    return this.toggleReport();
                }
                else
                {
                    return false;
                }
            }

            public boolean keyPressed(int p_239368_, int p_239369_, int p_239370_)
            {
                return p_239368_ != 257 && p_239368_ != 32 && p_239368_ != 335 ? false : this.toggleReport();
            }

            public boolean isSelected()
            {
                return ChatSelectionScreen.this.report.isReported(this.chatId);
            }

            public boolean canSelect()
            {
                return true;
            }

            public boolean canReport()
            {
                return this.canReport;
            }

            private boolean toggleReport()
            {
                if (this.canReport)
                {
                    ChatSelectionScreen.this.report.toggleReported(this.chatId);
                    ChatSelectionScreen.this.updateConfirmSelectedButton();
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }

        public class MessageHeadingEntry extends ChatSelectionScreen.ChatSelectionList.Entry
        {
            private static final int FACE_SIZE = 12;
            private final Component heading;
            private final ResourceLocation skin;
            private final boolean canReport;

            public MessageHeadingEntry(GameProfile p_240080_, Component p_240081_, boolean p_240082_)
            {
                this.heading = p_240081_;
                this.canReport = p_240082_;
                this.skin = ChatSelectionList.this.minecraft.getSkinManager().getInsecureSkinLocation(p_240080_);
            }

            public void render(PoseStack p_239156_, int p_239157_, int p_239158_, int p_239159_, int p_239160_, int p_239161_, int p_239162_, int p_239163_, boolean p_239164_, float p_239165_)
            {
                int i = p_239159_ - 12 - 4;
                int j = p_239158_ + (p_239161_ - 12) / 2;
                this.renderFace(p_239156_, i, j, this.skin);
                int k = p_239158_ + 1 + (p_239161_ - 9) / 2;
                GuiComponent.drawString(p_239156_, ChatSelectionScreen.this.font, this.heading, p_239159_, k, this.canReport ? -1 : -1593835521);
            }

            private void renderFace(PoseStack p_238956_, int p_238957_, int p_238958_, ResourceLocation p_238959_)
            {
                RenderSystem.setShaderTexture(0, p_238959_);
                PlayerFaceRenderer.draw(p_238956_, p_238957_, p_238958_, 12);
            }
        }

        public class PaddingEntry extends ChatSelectionScreen.ChatSelectionList.Entry
        {
            public void render(PoseStack p_240109_, int p_240110_, int p_240111_, int p_240112_, int p_240113_, int p_240114_, int p_240115_, int p_240116_, boolean p_240117_, float p_240118_)
            {
            }
        }
    }
}
