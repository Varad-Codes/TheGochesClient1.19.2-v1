package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.client.GuiMessage;
import net.minecraft.client.GuiMessageTag;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.chat.ChatListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.ChatVisiblity;
import org.slf4j.Logger;

public class ChatComponent extends GuiComponent
{
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_CHAT_HISTORY = 100;
    private static final int MESSAGE_NOT_FOUND = -1;
    private static final int MESSAGE_INDENT = 4;
    private static final int MESSAGE_TAG_MARGIN_LEFT = 4;
    private final Minecraft minecraft;
    private final List<String> recentChat = Lists.newArrayList();
    private final List<GuiMessage> allMessages = Lists.newArrayList();
    private final List<GuiMessage.Line> trimmedMessages = Lists.newArrayList();
    private int chatScrollbarPos;
    private boolean newMessageSinceScroll;
    private int lastChatWidth = 0;

    public ChatComponent(Minecraft pMinecraft)
    {
        this.minecraft = pMinecraft;
    }

    public void render(PoseStack pPoseStack, int pTickCount)
    {
        int i = this.getWidth();

        if (this.lastChatWidth != i)
        {
            this.lastChatWidth = i;
            this.rescaleChat();
        }

        if (!this.isChatHidden())
        {
            int j = this.getLinesPerPage();
            int k = this.trimmedMessages.size();

            if (k > 0)
            {
                boolean flag = this.isChatFocused();
                float f = (float)this.getScale();
                int l = Mth.ceil((float)this.getWidth() / f);
                pPoseStack.pushPose();
                pPoseStack.translate(4.0D, 8.0D, 0.0D);
                pPoseStack.scale(f, f, 1.0F);
                double d0 = this.minecraft.options.chatOpacity().get() * (double)0.9F + (double)0.1F;
                double d1 = this.minecraft.options.textBackgroundOpacity().get();
                double d2 = this.minecraft.options.chatLineSpacing().get();
                int i1 = this.getLineHeight();
                double d3 = -8.0D * (d2 + 1.0D) + 4.0D * d2;
                int j1 = 0;

                for (int k1 = 0; k1 + this.chatScrollbarPos < this.trimmedMessages.size() && k1 < j; ++k1)
                {
                    GuiMessage.Line guimessage$line = (GuiMessage.Line)this.trimmedMessages.get(k1 + this.chatScrollbarPos);

                    if (guimessage$line != null)
                    {
                        int l1 = pTickCount - guimessage$line.addedTime();

                        if (l1 < 200 || flag)
                        {
                            double d4 = flag ? 1.0D : getTimeFactor(l1);
                            int j2 = (int)(255.0D * d4 * d0);
                            int k2 = (int)(255.0D * d4 * d1);
                            ++j1;

                            if (j2 > 3)
                            {
                                int l2 = 0;
                                int i3 = -k1 * i1;
                                int j3 = (int)((double)i3 + d3);
                                pPoseStack.pushPose();
                                pPoseStack.translate(0.0D, 0.0D, 50.0D);

                                if (this.minecraft.options.ofChatBackground == 5)
                                {
                                    l = this.minecraft.font.width(guimessage$line.content()) - 2;
                                }

                                if (this.minecraft.options.ofChatBackground != 3)
                                {
                                    fill(pPoseStack, -4, i3 - i1, 0 + l + 4 + 4, i3, k2 << 24);
                                }

                                GuiMessageTag guimessagetag = guimessage$line.tag();

                                if (guimessagetag != null)
                                {
                                    int k3 = guimessagetag.indicatorColor() | j2 << 24;
                                    fill(pPoseStack, -4, i3 - i1, -2, i3, k3);

                                    if (flag && guimessage$line.endOfEntry() && guimessagetag.icon() != null)
                                    {
                                        int l3 = this.getTagIconLeft(guimessage$line);
                                        int i4 = j3 + 9;
                                        this.drawTagIcon(pPoseStack, l3, i4, guimessagetag.icon());
                                    }
                                }

                                RenderSystem.enableBlend();
                                pPoseStack.translate(0.0D, 0.0D, 50.0D);

                                if (!this.minecraft.options.ofChatShadow)
                                {
                                    this.minecraft.font.draw(pPoseStack, guimessage$line.content(), 0.0F, (float)j3, 16777215 + (i3 << 24));
                                }
                                else
                                {
                                    this.minecraft.font.drawShadow(pPoseStack, guimessage$line.content(), 0.0F, (float)j3, 16777215 + (j2 << 24));
                                }

                                RenderSystem.disableBlend();
                                pPoseStack.popPose();
                            }
                        }
                    }
                }

                long j4 = this.minecraft.getChatListener().queueSize();

                if (j4 > 0L)
                {
                    int k4 = (int)(128.0D * d0);
                    int i5 = (int)(255.0D * d1);
                    pPoseStack.pushPose();
                    pPoseStack.translate(0.0D, 0.0D, 50.0D);
                    fill(pPoseStack, -2, 0, l + 4, 9, i5 << 24);
                    RenderSystem.enableBlend();
                    pPoseStack.translate(0.0D, 0.0D, 50.0D);
                    this.minecraft.font.drawShadow(pPoseStack, Component.a("chat.queue", j4), 0.0F, 1.0F, 16777215 + (k4 << 24));
                    pPoseStack.popPose();
                    RenderSystem.disableBlend();
                }

                if (flag)
                {
                    int l4 = this.getLineHeight();
                    int j5 = k * l4;
                    int i2 = j1 * l4;
                    int k5 = this.chatScrollbarPos * i2 / k;
                    int l5 = i2 * i2 / j5;

                    if (j5 != i2)
                    {
                        int i6 = k5 > 0 ? 170 : 96;
                        int j6 = this.newMessageSinceScroll ? 13382451 : 3355562;
                        int k6 = l + 4;
                        fill(pPoseStack, k6, -k5, k6 + 2, -k5 - l5, j6 + (i6 << 24));
                        fill(pPoseStack, k6 + 2, -k5, k6 + 1, -k5 - l5, 13421772 + (i6 << 24));
                    }
                }

                pPoseStack.popPose();
            }
        }
    }

    private void drawTagIcon(PoseStack p_240586_, int p_240593_, int p_240610_, GuiMessageTag.Icon p_240605_)
    {
        int i = p_240610_ - p_240605_.height - 1;
        p_240605_.draw(p_240586_, p_240593_, i);
    }

    private int getTagIconLeft(GuiMessage.Line p_240622_)
    {
        return this.minecraft.font.width(p_240622_.content()) + 4;
    }

    private boolean isChatHidden()
    {
        return this.minecraft.options.chatVisibility().get() == ChatVisiblity.HIDDEN;
    }

    private static double getTimeFactor(int pCounter)
    {
        double d0 = (double)pCounter / 200.0D;
        d0 = 1.0D - d0;
        d0 *= 10.0D;
        d0 = Mth.clamp(d0, 0.0D, 1.0D);
        return d0 * d0;
    }

    public void clearMessages(boolean pClearSentMsgHistory)
    {
        this.minecraft.getChatListener().clearQueue();
        this.trimmedMessages.clear();
        this.allMessages.clear();

        if (pClearSentMsgHistory)
        {
            this.recentChat.clear();
        }
    }

    public void addMessage(Component pChatComponent)
    {
        this.addMessage(pChatComponent, (MessageSignature)null, GuiMessageTag.system());
    }

    public void addMessage(Component p_241484_, @Nullable MessageSignature p_241323_, @Nullable GuiMessageTag p_241297_)
    {
        this.addMessage(p_241484_, p_241323_, this.minecraft.gui.getGuiTicks(), p_241297_, false);
    }

    private void logChatMessage(Component p_242919_, @Nullable GuiMessageTag p_242840_)
    {
        String s = p_242919_.getString().replaceAll("\r", "\\\\r").replaceAll("\n", "\\\\n");
        String s1 = Util.mapNullable(p_242840_, GuiMessageTag::logTag);

        if (s1 != null)
        {
            LOGGER.info("[{}] [CHAT] {}", s1, s);
        }
        else
        {
            LOGGER.info("[CHAT] {}", (Object)s);
        }
    }

    private void addMessage(Component p_240562_, @Nullable MessageSignature p_241566_, int p_240583_, @Nullable GuiMessageTag p_240624_, boolean p_240558_)
    {
        if (!p_240558_)
        {
            this.logChatMessage(p_240562_, p_240624_);
        }

        int i = Mth.floor((double)this.getWidth() / this.getScale());

        if (p_240624_ != null && p_240624_.icon() != null)
        {
            i -= p_240624_.icon().width + 4 + 2;
        }

        List<FormattedCharSequence> list = ComponentRenderUtils.wrapComponents(p_240562_, i, this.minecraft.font);
        boolean flag = this.isChatFocused();

        for (int j = 0; j < list.size(); ++j)
        {
            FormattedCharSequence formattedcharsequence = list.get(j);

            if (flag && this.chatScrollbarPos > 0)
            {
                this.newMessageSinceScroll = true;
                this.scrollChat(1);
            }

            boolean flag1 = j == list.size() - 1;
            this.trimmedMessages.add(0, new GuiMessage.Line(p_240583_, formattedcharsequence, p_240624_, flag1));
        }

        while (this.trimmedMessages.size() > 100)
        {
            this.trimmedMessages.remove(this.trimmedMessages.size() - 1);
        }

        if (!p_240558_)
        {
            this.allMessages.add(0, new GuiMessage(p_240583_, p_240562_, p_241566_, p_240624_));

            while (this.allMessages.size() > 100)
            {
                this.allMessages.remove(this.allMessages.size() - 1);
            }
        }
    }

    public void deleteMessage(MessageSignature p_241324_)
    {
        Iterator<GuiMessage> iterator = this.allMessages.iterator();

        while (iterator.hasNext())
        {
            MessageSignature messagesignature = ((GuiMessage)iterator.next()).headerSignature();

            if (messagesignature != null && messagesignature.equals(p_241324_))
            {
                iterator.remove();
                break;
            }
        }

        this.refreshTrimmedMessage();
    }

    public void rescaleChat()
    {
        this.resetChatScroll();
        this.refreshTrimmedMessage();
    }

    private void refreshTrimmedMessage()
    {
        this.trimmedMessages.clear();

        for (int i = this.allMessages.size() - 1; i >= 0; --i)
        {
            GuiMessage guimessage = (GuiMessage)this.allMessages.get(i);
            this.addMessage(guimessage.content(), guimessage.headerSignature(), guimessage.addedTime(), guimessage.tag(), true);
        }
    }

    public List<String> getRecentChat()
    {
        return this.recentChat;
    }

    public void addRecentChat(String pMessage)
    {
        if (this.recentChat.isEmpty() || !this.recentChat.get(this.recentChat.size() - 1).equals(pMessage))
        {
            this.recentChat.add(pMessage);
        }
    }

    public void resetChatScroll()
    {
        this.chatScrollbarPos = 0;
        this.newMessageSinceScroll = false;
    }

    public void scrollChat(int pPosInc)
    {
        this.chatScrollbarPos += pPosInc;
        int i = this.trimmedMessages.size();

        if (this.chatScrollbarPos > i - this.getLinesPerPage())
        {
            this.chatScrollbarPos = i - this.getLinesPerPage();
        }

        if (this.chatScrollbarPos <= 0)
        {
            this.chatScrollbarPos = 0;
            this.newMessageSinceScroll = false;
        }
    }

    public boolean handleChatQueueClicked(double p_93773_, double p_93774_)
    {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden())
        {
            ChatListener chatlistener = this.minecraft.getChatListener();

            if (chatlistener.queueSize() == 0L)
            {
                return false;
            }
            else
            {
                double d0 = p_93773_ - 2.0D;
                double d1 = (double)this.minecraft.getWindow().getGuiScaledHeight() - p_93774_ - 40.0D;

                if (d0 <= (double)Mth.floor((double)this.getWidth() / this.getScale()) && d1 < 0.0D && d1 > (double)Mth.floor(-9.0D * this.getScale()))
                {
                    chatlistener.acceptNextDelayedMessage();
                    return true;
                }
                else
                {
                    return false;
                }
            }
        }
        else
        {
            return false;
        }
    }

    @Nullable
    public Style getClickedComponentStyleAt(double p_93801_, double p_93802_)
    {
        double d0 = this.screenToChatX(p_93801_);

        if (!(d0 < 0.0D) && !(d0 > (double)Mth.floor((double)this.getWidth() / this.getScale())))
        {
            double d1 = this.screenToChatY(p_93802_);
            int i = this.getMessageIndexAt(d1);

            if (i >= 0 && i < this.trimmedMessages.size())
            {
                GuiMessage.Line guimessage$line = (GuiMessage.Line)this.trimmedMessages.get(i);
                return this.minecraft.font.getSplitter().componentStyleAtWidth(guimessage$line.content(), Mth.floor(d0));
            }
            else
            {
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    @Nullable
    public GuiMessageTag getMessageTagAt(double p_240576_, double p_240554_)
    {
        double d0 = this.screenToChatX(p_240576_);
        double d1 = this.screenToChatY(p_240554_);
        int i = this.getMessageIndexAt(d1);

        if (i >= 0 && i < this.trimmedMessages.size())
        {
            GuiMessage.Line guimessage$line = (GuiMessage.Line)this.trimmedMessages.get(i);
            GuiMessageTag guimessagetag = guimessage$line.tag();

            if (guimessagetag != null && this.hasSelectedMessageTag(d0, guimessage$line, guimessagetag))
            {
                return guimessagetag;
            }
        }

        return null;
    }

    private boolean hasSelectedMessageTag(double p_240619_, GuiMessage.Line p_240547_, GuiMessageTag p_240637_)
    {
        if (p_240619_ < 0.0D)
        {
            return true;
        }
        else
        {
            GuiMessageTag.Icon guimessagetag$icon = p_240637_.icon();

            if (guimessagetag$icon == null)
            {
                return false;
            }
            else
            {
                int i = this.getTagIconLeft(p_240547_);
                int j = i + guimessagetag$icon.width;
                return p_240619_ >= (double)i && p_240619_ <= (double)j;
            }
        }
    }

    private double screenToChatX(double p_240580_)
    {
        return (p_240580_ - 4.0D) / this.getScale();
    }

    private double screenToChatY(double p_240548_)
    {
        double d0 = (double)this.minecraft.getWindow().getGuiScaledHeight() - p_240548_ - 40.0D;
        return d0 / (this.getScale() * (this.minecraft.options.chatLineSpacing().get() + 1.0D));
    }

    private int getMessageIndexAt(double p_240641_)
    {
        if (this.isChatFocused() && !this.minecraft.options.hideGui && !this.isChatHidden())
        {
            int i = Math.min(this.getLinesPerPage(), this.trimmedMessages.size());

            if (p_240641_ >= 0.0D && p_240641_ < (double)(9 * i + i))
            {
                int j = Mth.floor(p_240641_ / 9.0D + (double)this.chatScrollbarPos);

                if (j >= 0 && j < this.trimmedMessages.size())
                {
                    return j;
                }
            }

            return -1;
        }
        else
        {
            return -1;
        }
    }

    @Nullable
    public ChatScreen getFocusedChat()
    {
        Screen screen = this.minecraft.screen;
        return screen instanceof ChatScreen ? (ChatScreen)screen : null;
    }

    private boolean isChatFocused()
    {
        return this.getFocusedChat() != null;
    }

    public int getWidth()
    {
        int i = getWidth(this.minecraft.options.chatWidth().get());
        Window window = Minecraft.getInstance().getWindow();
        int j = (int)((double)(window.getWidth() - 3) / window.getGuiScale());
        return Mth.clamp(i, 0, j);
    }

    public int getHeight()
    {
        return getHeight(this.isChatFocused() ? this.minecraft.options.chatHeightFocused().get() : this.minecraft.options.chatHeightUnfocused().get());
    }

    public double getScale()
    {
        return this.minecraft.options.chatScale().get();
    }

    public static int getWidth(double p_93799_)
    {
        int i = 320;
        int j = 40;
        return Mth.floor(p_93799_ * 280.0D + 40.0D);
    }

    public static int getHeight(double p_93812_)
    {
        int i = 180;
        int j = 20;
        return Mth.floor(p_93812_ * 160.0D + 20.0D);
    }

    public static double defaultUnfocusedPct()
    {
        int i = 180;
        int j = 20;
        return 70.0D / (double)(getHeight(1.0D) - 20);
    }

    public int getLinesPerPage()
    {
        return this.getHeight() / this.getLineHeight();
    }

    private int getLineHeight()
    {
        return (int)(9.0D * (this.minecraft.options.chatLineSpacing().get() + 1.0D));
    }
}
