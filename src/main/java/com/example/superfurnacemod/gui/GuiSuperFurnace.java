package com.example.superfurnacemod.gui;

import com.example.superfurnacemod.container.ContainerSuperFurnace;
import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Client-side GUI — reuses the vanilla furnace texture (no custom artwork needed).
 */
@SideOnly(Side.CLIENT)
public class GuiSuperFurnace extends GuiContainer {

    private static final ResourceLocation FURNACE_TEXTURE =
        new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    private final ContainerSuperFurnace container;
    private final TileEntitySuperFurnace tileEntity;

    public GuiSuperFurnace(InventoryPlayer playerInv, TileEntitySuperFurnace te) {
        super(new ContainerSuperFurnace(playerInv, te));
        this.tileEntity = te;
        this.container  = (ContainerSuperFurnace) inventorySlots;
        xSize = 176;
        ySize = 166;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        String title = tileEntity.hasCustomName()
            ? tileEntity.getName()
            : I18n.format(tileEntity.getName());
        fontRenderer.drawString(
            title,
            xSize / 2 - fontRenderer.getStringWidth(title) / 2,
            6, 0x404040
        );
        fontRenderer.drawString(
            I18n.format("container.inventory"),
            8, ySize - 96 + 2, 0x404040
        );
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partial, int mx, int my) {
        GlStateManager.color(1f, 1f, 1f, 1f);
        mc.getTextureManager().bindTexture(FURNACE_TEXTURE);

        int x = (width  - xSize) / 2;
        int y = (height - ySize) / 2;
        drawTexturedModalRect(x, y, 0, 0, xSize, ySize);

        // Flame (fuel level)
        if (container.isBurning()) {
            int flame = container.getBurnLeftScaled(13);
            drawTexturedModalRect(x + 56, y + 36 + 12 - flame, 176, 12 - flame, 14, flame + 1);
        }

        // Cook-progress arrow
        int progress = container.getCookProgressScaled(24);
        drawTexturedModalRect(x + 79, y + 34, 176, 14, progress + 1, 16);
    }
}
