package dev.manny.createmobgrinding.client;

import dev.manny.createmobgrinding.menu.SpawnerChunkBagMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class SpawnerChunkBagScreen extends AbstractContainerScreen<SpawnerChunkBagMenu> {
    // Same layout as a vanilla single chest
    private static final ResourceLocation TEXTURE = ResourceLocation.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int HINT_COLOR = 0x5B9BD5;

    public SpawnerChunkBagScreen(SpawnerChunkBagMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageHeight = 114 + SpawnerChunkBagMenu.ROWS * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        if (menu.isSwapMode()) {
            Component hint = Component.translatable("container.createmobgrinding.spawner_chunk_bag.swap_hint");
            guiGraphics.drawString(font, hint, imageWidth - 8 - font.width(hint), inventoryLabelY, HINT_COLOR, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int topHeight = SpawnerChunkBagMenu.ROWS * 18 + 17;
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, topHeight);
        guiGraphics.blit(TEXTURE, x, y + topHeight, 0, 126, imageWidth, 96);
    }
}
