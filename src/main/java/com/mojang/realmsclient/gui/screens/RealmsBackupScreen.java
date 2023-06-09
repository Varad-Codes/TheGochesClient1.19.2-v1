package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.client.RealmsClient;
import com.mojang.realmsclient.dto.Backup;
import com.mojang.realmsclient.dto.RealmsServer;
import com.mojang.realmsclient.exception.RealmsServiceException;
import com.mojang.realmsclient.util.RealmsUtil;
import com.mojang.realmsclient.util.task.DownloadTask;
import com.mojang.realmsclient.util.task.RestoreTask;
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsObjectSelectionList;
import net.minecraft.realms.RealmsScreen;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

public class RealmsBackupScreen extends RealmsScreen
{
    static final Logger LOGGER = LogUtils.getLogger();
    static final ResourceLocation PLUS_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/plus_icon.png");
    static final ResourceLocation RESTORE_ICON_LOCATION = new ResourceLocation("realms", "textures/gui/realms/restore_icon.png");
    static final Component RESTORE_TOOLTIP = Component.translatable("mco.backup.button.restore");
    static final Component HAS_CHANGES_TOOLTIP = Component.translatable("mco.backup.changes.tooltip");
    private static final Component TITLE = Component.translatable("mco.configure.world.backup");
    private static final Component NO_BACKUPS_LABEL = Component.translatable("mco.backup.nobackups");
    static int lastScrollPosition = -1;
    private final RealmsConfigureWorldScreen lastScreen;
    List<Backup> backups = Collections.emptyList();
    @Nullable
    Component toolTip;
    RealmsBackupScreen.BackupObjectSelectionList backupObjectSelectionList;
    int selectedBackup = -1;
    private final int slotId;
    private Button downloadButton;
    private Button restoreButton;
    private Button changesButton;
    Boolean noBackups = false;
    final RealmsServer serverData;
    private static final String UPLOADED_KEY = "Uploaded";

    public RealmsBackupScreen(RealmsConfigureWorldScreen pLastScreen, RealmsServer pServerData, int pSlotId)
    {
        super(Component.translatable("mco.configure.world.backup"));
        this.lastScreen = pLastScreen;
        this.serverData = pServerData;
        this.slotId = pSlotId;
    }

    public void init()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.backupObjectSelectionList = new RealmsBackupScreen.BackupObjectSelectionList();

        if (lastScrollPosition != -1)
        {
            this.backupObjectSelectionList.setScrollAmount((double)lastScrollPosition);
        }

        (new Thread("Realms-fetch-backups")
        {
            public void run()
            {
                RealmsClient realmsclient = RealmsClient.create();

                try
                {
                    List<Backup> list = realmsclient.backupsFor(RealmsBackupScreen.this.serverData.id).backups;
                    RealmsBackupScreen.this.minecraft.execute(() ->
                    {
                        RealmsBackupScreen.this.backups = list;
                        RealmsBackupScreen.this.noBackups = RealmsBackupScreen.this.backups.isEmpty();
                        RealmsBackupScreen.this.backupObjectSelectionList.clear();

                        for (Backup backup : RealmsBackupScreen.this.backups)
                        {
                            RealmsBackupScreen.this.backupObjectSelectionList.addEntry(backup);
                        }

                        RealmsBackupScreen.this.generateChangeList();
                    });
                }
                catch (RealmsServiceException realmsserviceexception)
                {
                    RealmsBackupScreen.LOGGER.error("Couldn't request backups", (Throwable)realmsserviceexception);
                }
            }
        }).start();
        this.downloadButton = this.addRenderableWidget(new Button(this.width - 135, row(1), 120, 20, Component.translatable("mco.backup.button.download"), (p_88185_) ->
        {
            this.downloadClicked();
        }));
        this.restoreButton = this.addRenderableWidget(new Button(this.width - 135, row(3), 120, 20, Component.translatable("mco.backup.button.restore"), (p_88179_) ->
        {
            this.restoreClicked(this.selectedBackup);
        }));
        this.changesButton = this.addRenderableWidget(new Button(this.width - 135, row(5), 120, 20, Component.translatable("mco.backup.changes.tooltip"), (p_88174_) ->
        {
            this.minecraft.setScreen(new RealmsBackupInfoScreen(this, this.backups.get(this.selectedBackup)));
            this.selectedBackup = -1;
        }));
        this.addRenderableWidget(new Button(this.width - 100, this.height - 35, 85, 20, CommonComponents.GUI_BACK, (p_88164_) ->
        {
            this.minecraft.setScreen(this.lastScreen);
        }));
        this.addWidget(this.backupObjectSelectionList);
        this.magicalSpecialHackyFocus(this.backupObjectSelectionList);
        this.updateButtonStates();
    }

    void generateChangeList()
    {
        if (this.backups.size() > 1)
        {
            for (int i = 0; i < this.backups.size() - 1; ++i)
            {
                Backup backup = this.backups.get(i);
                Backup backup1 = this.backups.get(i + 1);

                if (!backup.metadata.isEmpty() && !backup1.metadata.isEmpty())
                {
                    for (String s : backup.metadata.keySet())
                    {
                        if (!s.contains("Uploaded") && backup1.metadata.containsKey(s))
                        {
                            if (!backup.metadata.get(s).equals(backup1.metadata.get(s)))
                            {
                                this.addToChangeList(backup, s);
                            }
                        }
                        else
                        {
                            this.addToChangeList(backup, s);
                        }
                    }
                }
            }
        }
    }

    private void addToChangeList(Backup p_88147_, String p_88148_)
    {
        if (p_88148_.contains("Uploaded"))
        {
            String s = DateFormat.getDateTimeInstance(3, 3).format(p_88147_.lastModifiedDate);
            p_88147_.changeList.put(p_88148_, s);
            p_88147_.setUploadedVersion(true);
        }
        else
        {
            p_88147_.changeList.put(p_88148_, p_88147_.metadata.get(p_88148_));
        }
    }

    void updateButtonStates()
    {
        this.restoreButton.visible = this.shouldRestoreButtonBeVisible();
        this.changesButton.visible = this.shouldChangesButtonBeVisible();
    }

    private boolean shouldChangesButtonBeVisible()
    {
        if (this.selectedBackup == -1)
        {
            return false;
        }
        else
        {
            return !(this.backups.get(this.selectedBackup)).changeList.isEmpty();
        }
    }

    private boolean shouldRestoreButtonBeVisible()
    {
        if (this.selectedBackup == -1)
        {
            return false;
        }
        else
        {
            return !this.serverData.expired;
        }
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (pKeyCode == 256)
        {
            this.minecraft.setScreen(this.lastScreen);
            return true;
        }
        else
        {
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    void restoreClicked(int p_88167_)
    {
        if (p_88167_ >= 0 && p_88167_ < this.backups.size() && !this.serverData.expired)
        {
            this.selectedBackup = p_88167_;
            Date date = (this.backups.get(p_88167_)).lastModifiedDate;
            String s = DateFormat.getDateTimeInstance(3, 3).format(date);
            String s1 = RealmsUtil.convertToAgePresentationFromInstant(date);
            Component component = Component.a("mco.configure.world.restore.question.line1", s, s1);
            Component component1 = Component.translatable("mco.configure.world.restore.question.line2");
            this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_88187_) ->
            {
                if (p_88187_)
                {
                    this.restore();
                }
                else {
                    this.selectedBackup = -1;
                    this.minecraft.setScreen(this);
                }
            }, RealmsLongConfirmationScreen.Type.Warning, component, component1, true));
        }
    }

    private void downloadClicked()
    {
        Component component = Component.translatable("mco.configure.world.restore.download.question.line1");
        Component component1 = Component.translatable("mco.configure.world.restore.download.question.line2");
        this.minecraft.setScreen(new RealmsLongConfirmationScreen((p_88181_) ->
        {
            if (p_88181_)
            {
                this.downloadWorldData();
            }
            else {
                this.minecraft.setScreen(this);
            }
        }, RealmsLongConfirmationScreen.Type.Info, component, component1, true));
    }

    private void downloadWorldData()
    {
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), new DownloadTask(this.serverData.id, this.slotId, this.serverData.name + " (" + this.serverData.slots.get(this.serverData.activeSlot).getSlotName(this.serverData.activeSlot) + ")", this)));
    }

    private void restore()
    {
        Backup backup = this.backups.get(this.selectedBackup);
        this.selectedBackup = -1;
        this.minecraft.setScreen(new RealmsLongRunningMcoTaskScreen(this.lastScreen.getNewScreen(), new RestoreTask(backup, this.serverData.id, this.lastScreen)));
    }

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        this.toolTip = null;
        this.renderBackground(pPoseStack);
        this.backupObjectSelectionList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 12, 16777215);
        this.font.draw(pPoseStack, TITLE, (float)((this.width - 150) / 2 - 90), 20.0F, 10526880);

        if (this.noBackups)
        {
            this.font.draw(pPoseStack, NO_BACKUPS_LABEL, 20.0F, (float)(this.height / 2 - 10), 16777215);
        }

        this.downloadButton.active = !this.noBackups;
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);

        if (this.toolTip != null)
        {
            this.renderMousehoverTooltip(pPoseStack, this.toolTip, pMouseX, pMouseY);
        }
    }

    protected void renderMousehoverTooltip(PoseStack p_88142_, @Nullable Component p_88143_, int p_88144_, int p_88145_)
    {
        if (p_88143_ != null)
        {
            int i = p_88144_ + 12;
            int j = p_88145_ - 12;
            int k = this.font.width(p_88143_);
            this.fillGradient(p_88142_, i - 3, j - 3, i + k + 3, j + 8 + 3, -1073741824, -1073741824);
            this.font.drawShadow(p_88142_, p_88143_, (float)i, (float)j, 16777215);
        }
    }

    class BackupObjectSelectionList extends RealmsObjectSelectionList<RealmsBackupScreen.Entry>
    {
        public BackupObjectSelectionList()
        {
            super(RealmsBackupScreen.this.width - 150, RealmsBackupScreen.this.height, 32, RealmsBackupScreen.this.height - 15, 36);
        }

        public void addEntry(Backup pBackup)
        {
            this.addEntry(RealmsBackupScreen.this.new Entry(pBackup));
        }

        public int getRowWidth()
        {
            return (int)((double)this.width * 0.93D);
        }

        public boolean isFocused()
        {
            return RealmsBackupScreen.this.getFocused() == this;
        }

        public int getMaxPosition()
        {
            return this.getItemCount() * 36;
        }

        public void renderBackground(PoseStack pPoseStack)
        {
            RealmsBackupScreen.this.renderBackground(pPoseStack);
        }

        public boolean mouseClicked(double pMouseX, double p_88222_, int pMouseY)
        {
            if (pMouseY != 0)
            {
                return false;
            }
            else if (pMouseX < (double)this.getScrollbarPosition() && p_88222_ >= (double)this.y0 && p_88222_ <= (double)this.y1)
            {
                int i = this.width / 2 - 92;
                int j = this.width;
                int k = (int)Math.floor(p_88222_ - (double)this.y0) - this.headerHeight + (int)this.getScrollAmount();
                int l = k / this.itemHeight;

                if (pMouseX >= (double)i && pMouseX <= (double)j && l >= 0 && k >= 0 && l < this.getItemCount())
                {
                    this.selectItem(l);
                    this.itemClicked(k, l, pMouseX, p_88222_, this.width);
                }

                return true;
            }
            else
            {
                return false;
            }
        }

        public int getScrollbarPosition()
        {
            return this.width - 5;
        }

        public void itemClicked(int p_88227_, int p_88228_, double p_88229_, double p_88230_, int p_88231_)
        {
            int i = this.width - 35;
            int j = p_88228_ * this.itemHeight + 36 - (int)this.getScrollAmount();
            int k = i + 10;
            int l = j - 3;

            if (p_88229_ >= (double)i && p_88229_ <= (double)(i + 9) && p_88230_ >= (double)j && p_88230_ <= (double)(j + 9))
            {
                if (!(RealmsBackupScreen.this.backups.get(p_88228_)).changeList.isEmpty())
                {
                    RealmsBackupScreen.this.selectedBackup = -1;
                    RealmsBackupScreen.lastScrollPosition = (int)this.getScrollAmount();
                    this.minecraft.setScreen(new RealmsBackupInfoScreen(RealmsBackupScreen.this, RealmsBackupScreen.this.backups.get(p_88228_)));
                }
            }
            else if (p_88229_ >= (double)k && p_88229_ < (double)(k + 13) && p_88230_ >= (double)l && p_88230_ < (double)(l + 15))
            {
                RealmsBackupScreen.lastScrollPosition = (int)this.getScrollAmount();
                RealmsBackupScreen.this.restoreClicked(p_88228_);
            }
        }

        public void selectItem(int pIndex)
        {
            super.selectItem(pIndex);
            this.selectInviteListItem(pIndex);
        }

        public void selectInviteListItem(int pIndex)
        {
            RealmsBackupScreen.this.selectedBackup = pIndex;
            RealmsBackupScreen.this.updateButtonStates();
        }

        public void setSelected(@Nullable RealmsBackupScreen.Entry pSelected)
        {
            super.setSelected(pSelected);
            RealmsBackupScreen.this.selectedBackup = this.children().indexOf(pSelected);
            RealmsBackupScreen.this.updateButtonStates();
        }
    }

    class Entry extends ObjectSelectionList.Entry<RealmsBackupScreen.Entry>
    {
        private final Backup backup;

        public Entry(Backup p_88250_)
        {
            this.backup = p_88250_;
        }

        public void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick)
        {
            this.renderBackupItem(pPoseStack, this.backup, pLeft - 40, pTop, pMouseX, pMouseY);
        }

        private void renderBackupItem(PoseStack p_88269_, Backup p_88270_, int p_88271_, int p_88272_, int p_88273_, int p_88274_)
        {
            int i = p_88270_.isUploadedVersion() ? -8388737 : 16777215;
            RealmsBackupScreen.this.font.draw(p_88269_, "Backup (" + RealmsUtil.convertToAgePresentationFromInstant(p_88270_.lastModifiedDate) + ")", (float)(p_88271_ + 40), (float)(p_88272_ + 1), i);
            RealmsBackupScreen.this.font.draw(p_88269_, this.getMediumDatePresentation(p_88270_.lastModifiedDate), (float)(p_88271_ + 40), (float)(p_88272_ + 12), 5000268);
            int j = RealmsBackupScreen.this.width - 175;
            int k = -3;
            int l = j - 10;
            int i1 = 0;

            if (!RealmsBackupScreen.this.serverData.expired)
            {
                this.drawRestore(p_88269_, j, p_88272_ + -3, p_88273_, p_88274_);
            }

            if (!p_88270_.changeList.isEmpty())
            {
                this.drawInfo(p_88269_, l, p_88272_ + 0, p_88273_, p_88274_);
            }
        }

        private String getMediumDatePresentation(Date pDate)
        {
            return DateFormat.getDateTimeInstance(3, 3).format(pDate);
        }

        private void drawRestore(PoseStack p_88252_, int p_88253_, int p_88254_, int p_88255_, int p_88256_)
        {
            boolean flag = p_88255_ >= p_88253_ && p_88255_ <= p_88253_ + 12 && p_88256_ >= p_88254_ && p_88256_ <= p_88254_ + 14 && p_88256_ < RealmsBackupScreen.this.height - 15 && p_88256_ > 32;
            RenderSystem.setShaderTexture(0, RealmsBackupScreen.RESTORE_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            p_88252_.pushPose();
            p_88252_.scale(0.5F, 0.5F, 0.5F);
            float f = flag ? 28.0F : 0.0F;
            GuiComponent.blit(p_88252_, p_88253_ * 2, p_88254_ * 2, 0.0F, f, 23, 28, 23, 56);
            p_88252_.popPose();

            if (flag)
            {
                RealmsBackupScreen.this.toolTip = RealmsBackupScreen.RESTORE_TOOLTIP;
            }
        }

        private void drawInfo(PoseStack p_88278_, int p_88279_, int p_88280_, int p_88281_, int p_88282_)
        {
            boolean flag = p_88281_ >= p_88279_ && p_88281_ <= p_88279_ + 8 && p_88282_ >= p_88280_ && p_88282_ <= p_88280_ + 8 && p_88282_ < RealmsBackupScreen.this.height - 15 && p_88282_ > 32;
            RenderSystem.setShaderTexture(0, RealmsBackupScreen.PLUS_ICON_LOCATION);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            p_88278_.pushPose();
            p_88278_.scale(0.5F, 0.5F, 0.5F);
            float f = flag ? 15.0F : 0.0F;
            GuiComponent.blit(p_88278_, p_88279_ * 2, p_88280_ * 2, 0.0F, f, 15, 15, 15, 30);
            p_88278_.popPose();

            if (flag)
            {
                RealmsBackupScreen.this.toolTip = RealmsBackupScreen.HAS_CHANGES_TOOLTIP;
            }
        }

        public Component getNarration()
        {
            return Component.a("narrator.select", this.backup.lastModifiedDate.toString());
        }
    }
}
