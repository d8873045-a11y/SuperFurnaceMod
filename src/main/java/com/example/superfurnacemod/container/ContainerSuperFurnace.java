package com.example.superfurnacemod.container;

import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnaceFuel;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Container for the Super Furnace GUI.
 * Mirrors the layout of vanilla ContainerFurnace.
 */
public class ContainerSuperFurnace extends Container {

    private final IInventory tileInventory;
    private final InventoryPlayer playerInventory;

    private int burnTime;
    private int currentItemBurnTime;
    private int cookTime;
    private int totalCookTime;

    public ContainerSuperFurnace(InventoryPlayer playerInventory, IInventory tileInventory) {
        this.tileInventory   = tileInventory;
        this.playerInventory = playerInventory;

        // Input slot
        this.addSlotToContainer(new Slot(tileInventory, TileEntitySuperFurnace.SLOT_INPUT, 56, 17));
        // Fuel slot
        this.addSlotToContainer(new SlotFurnaceFuel(tileInventory, TileEntitySuperFurnace.SLOT_FUEL, 56, 53));
        // Output slot
        this.addSlotToContainer(new SlotFurnaceOutput(playerInventory.player, tileInventory,
                TileEntitySuperFurnace.SLOT_OUTPUT, 116, 35));

        // Player inventory (9x3)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9,
                        8 + col * 18, 84 + row * 18));
            }
        }
        // Hotbar (9)
        for (int col = 0; col < 9; col++) {
            this.addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, tileInventory);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener listener : this.listeners) {
            if (burnTime != tileInventory.getField(0)) {
                listener.sendWindowProperty(this, 0, tileInventory.getField(0));
            }
            if (currentItemBurnTime != tileInventory.getField(1)) {
                listener.sendWindowProperty(this, 1, tileInventory.getField(1));
            }
            if (cookTime != tileInventory.getField(2)) {
                listener.sendWindowProperty(this, 2, tileInventory.getField(2));
            }
            if (totalCookTime != tileInventory.getField(3)) {
                listener.sendWindowProperty(this, 3, tileInventory.getField(3));
            }
        }
        burnTime            = tileInventory.getField(0);
        currentItemBurnTime = tileInventory.getField(1);
        cookTime            = tileInventory.getField(2);
        totalCookTime       = tileInventory.getField(3);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        tileInventory.setField(id, data);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tileInventory.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);

        if (slot != null && slot.getHasStack()) {
            ItemStack slotStack = slot.getStack();
            result = slotStack.copy();

            if (index == TileEntitySuperFurnace.SLOT_OUTPUT) {
                if (!this.mergeItemStack(slotStack, 3, 39, true)) return ItemStack.EMPTY;
                slot.onSlotChange(slotStack, result);
            } else if (index != TileEntitySuperFurnace.SLOT_FUEL
                    && index != TileEntitySuperFurnace.SLOT_INPUT) {
                // From player inventory
                if (!FurnaceRecipes.instance().getSmeltingResult(slotStack).isEmpty()) {
                    if (!this.mergeItemStack(slotStack, TileEntitySuperFurnace.SLOT_INPUT, 1, false))
                        return ItemStack.EMPTY;
                } else if (SlotFurnaceFuel.isBucket(slotStack)
                        || net.minecraft.tileentity.TileEntityFurnace.isItemFuel(slotStack)) {
                    if (!this.mergeItemStack(slotStack, TileEntitySuperFurnace.SLOT_FUEL, 2, false))
                        return ItemStack.EMPTY;
                } else if (index >= 3 && index < 30) {
                    if (!this.mergeItemStack(slotStack, 30, 39, false)) return ItemStack.EMPTY;
                } else if (index >= 30 && index < 39) {
                    if (!this.mergeItemStack(slotStack, 3, 30, false)) return ItemStack.EMPTY;
                }
            } else if (!this.mergeItemStack(slotStack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }

            if (slotStack.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(playerIn, slotStack);
        }
        return result;
    }

    // ----- Progress helpers for GUI -----

    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled(int pixels) {
        int total = tileInventory.getField(3);
        if (total == 0) total = TileEntitySuperFurnace.SUPER_COOK_TIME;
        return tileInventory.getField(2) * pixels / total;
    }

    @SideOnly(Side.CLIENT)
    public int getBurnLeftScaled(int pixels) {
        int max = tileInventory.getField(1);
        if (max == 0) max = 200;
        return tileInventory.getField(0) * pixels / max;
    }

    @SideOnly(Side.CLIENT)
    public boolean isBurning() {
        return tileInventory.getField(0) > 0;
    }
}
