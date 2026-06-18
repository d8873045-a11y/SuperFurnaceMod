package com.example.superfurnacemod.gui;

import com.example.superfurnacemod.container.ContainerSuperFurnace;
import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client-side GUI for the Super Furnace.
 * Re-uses the vanilla furnace texture (no custom texture needed).
 */
@SideOnly(Side.CLIENT)
public class GuiSuperFurnace extends GuiContainer {

    // Use the vanilla furnace GUI texture
    private static final ResourceLocation FURNACE_GUI_TEXTURE =
            new ResourceLocation("textures/gui/container/furnace.png");

    private final TileEntitySuperFurnace tileEntity;
    private final ContainerSuperFurnace container;

    public GuiSuperFurnace(InventoryPlayer playerInventory, TileEntitySuperFurnace tileEntity) {
        super(new ContainerSuperFurnace(playerInventory, tileEntity));
        this.tileEntity = tileEntity;
        this.container  = (ContainerSuperFurnace) this.inventorySlots;
        this.xSize = 176;
        this.ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String title = tileEntity.hasCustomName()
                ? tileEntity.getName()
                : net.minecraft.client.resources.I18n.format(tileEntity.getName());
        this.fontRenderer.drawString(title, this.xSize / 2 - this.fontRenderer.getStringWidth(title) / 2, 6, 0x404040);
        this.fontRenderer.drawString(
                net.minecraft.client.resources.I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(FURNACE_GUI_TEXTURE);
        int x = (this.width  - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(x, y, 0, 0, this.xSize, this.ySize);

        // Flame (burn indicator) — located at gui x=56, y=36 offset (14×14 pixels)
        if (container.isBurning()) {
            int flame = container.getBurnLeftScaled(13);
            this.drawTexturedModalRect(x + 56, y + 36 + 12 - flame, 176, 12 - flame, 14, flame + 1);
        }

        // Cook progress arrow — located at gui x=79, y=34 offset (24×16 pixels)
        int progress = container.getCookProgressScaled(24);
        this.drawTexturedModalRect(x + 79, y + 34, 176, 14, progress + 1, 16);
    }
}
