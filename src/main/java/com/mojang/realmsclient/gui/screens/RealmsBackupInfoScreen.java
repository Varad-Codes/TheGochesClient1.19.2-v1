package com.mojang.realmsclient.gui.screens;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.realmsclient.dto.Backup;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.realms.RealmsScreen;

public class RealmsBackupInfoScreen extends RealmsScreen
{
    private static final Component TEXT_UNKNOWN = Component.literal("UNKNOWN");
    private final Screen lastScreen;
    final Backup backup;
    private RealmsBackupInfoScreen.BackupInfoList backupInfoList;

    public RealmsBackupInfoScreen(Screen pLastScreen, Backup pBackup)
    {
        super(Component.literal("Changes from last backup"));
        this.lastScreen = pLastScreen;
        this.backup = pBackup;
    }

    public void tick()
    {
    }

    public void init()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(true);
        this.addRenderableWidget(new Button(this.width / 2 - 100, this.height / 4 + 120 + 24, 200, 20, CommonComponents.GUI_BACK, (p_88066_) ->
        {
            this.minecraft.setScreen(this.lastScreen);
        }));
        this.backupInfoList = new RealmsBackupInfoScreen.BackupInfoList(this.minecraft);
        this.addWidget(this.backupInfoList);
        this.magicalSpecialHackyFocus(this.backupInfoList);
    }

    public void removed()
    {
        this.minecraft.keyboardHandler.setSendRepeatsToGui(false);
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

    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick)
    {
        this.renderBackground(pPoseStack);
        this.backupInfoList.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        drawCenteredString(pPoseStack, this.font, this.title, this.width / 2, 10, 16777215);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
    }

    Component checkForSpecificMetadata(String p_88068_, String p_88069_)
    {
        String s = p_88068_.toLowerCase(Locale.ROOT);

        if (s.contains("game") && s.contains("mode"))
        {
            return this.gameModeMetadata(p_88069_);
        }
        else
        {
            return (Component)(s.contains("game") && s.contains("difficulty") ? this.gameDifficultyMetadata(p_88069_) : Component.literal(p_88069_));
        }
    }

    private Component gameDifficultyMetadata(String p_88074_)
    {
        try
        {
            return RealmsSlotOptionsScreen.DIFFICULTIES.get(Integer.parseInt(p_88074_)).getDisplayName();
        }
        catch (Exception exception)
        {
            return TEXT_UNKNOWN;
        }
    }

    private Component gameModeMetadata(String p_88076_)
    {
        try
        {
            return RealmsSlotOptionsScreen.GAME_MODES.get(Integer.parseInt(p_88076_)).getShortDisplayName();
        }
        catch (Exception exception)
        {
            return TEXT_UNKNOWN;
        }
    }

    class BackupInfoList extends ObjectSelectionList<RealmsBackupInfoScreen.BackupInfoListEntry>
    {
        public BackupInfoList(Minecraft p_88082_)
        {
            super(p_88082_, RealmsBackupInfoScreen.this.width, RealmsBackupInfoScreen.this.height, 32, RealmsBackupInfoScreen.this.height - 64, 36);
            this.setRenderSelection(false);

            if (RealmsBackupInfoScreen.this.backup.changeList != null)
            {
                RealmsBackupInfoScreen.this.backup.changeList.forEach((p_88084_, p_88085_) ->
                {
                    this.addEntry(RealmsBackupInfoScreen.this.new BackupInfoListEntry(p_88084_, p_88085_));
                });
            }
        }
    }

    class BackupInfoListEntry extends ObjectSelectionList.Entry<RealmsBackupInfoScreen.BackupInfoListEntry>
    {
        private final String key;
        private final String value;

        public BackupInfoListEntry(String p_88091_, String p_88092_)
        {
            this.key = p_88091_;
            this.value = p_88092_;
        }

        public void render(PoseStack pPoseStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick)
        {
            Font font = RealmsBackupInfoScreen.this.minecraft.font;
            GuiComponent.drawString(pPoseStack, font, this.key, pLeft, pTop, 10526880);
            GuiComponent.drawString(pPoseStack, font, RealmsBackupInfoScreen.this.checkForSpecificMetadata(this.key, this.value), pLeft, pTop + 12, 16777215);
        }

        public Component getNarration()
        {
            return Component.a("narrator.select", this.key + " " + this.value);
        }
    }
}
