package com.example.superfurnacemod.container;

import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Container for the Super Furnace — mirrors vanilla ContainerFurnace exactly.
 */
public class ContainerSuperFurnace extends Container {

    private final IInventory te;

    // Cached field values for change-detection
    private int cachedBurn;
    private int cachedBurnMax;
    private int cachedCook;
    private int cachedCookTotal;

    public ContainerSuperFurnace(InventoryPlayer playerInv, IInventory furnaceInv) {
        this.te = furnaceInv;

        // Furnace slots
        addSlotToContainer(new Slot(furnaceInv, TileEntitySuperFurnace.SLOT_INPUT,  56, 17));
        addSlotToContainer(new SlotFurnaceFuel(furnaceInv, TileEntitySuperFurnace.SLOT_FUEL, 56, 53));
        addSlotToContainer(new SlotFurnaceOutput(playerInv.player, furnaceInv,
                                                  TileEntitySuperFurnace.SLOT_OUTPUT, 116, 35));

        // Player inventory (rows 0-2)
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 9; col++)
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9,
                                            8 + col * 18, 84 + row * 18));

        // Hotbar
        for (int col = 0; col < 9; col++)
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        listener.sendAllWindowProperties(this, te);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (IContainerListener l : listeners) {
            if (cachedBurn      != te.getField(0)) l.sendWindowProperty(this, 0, te.getField(0));
            if (cachedBurnMax   != te.getField(1)) l.sendWindowProperty(this, 1, te.getField(1));
            if (cachedCook      != te.getField(2)) l.sendWindowProperty(this, 2, te.getField(2));
            if (cachedCookTotal != te.getField(3)) l.sendWindowProperty(this, 3, te.getField(3));
        }
        cachedBurn      = te.getField(0);
        cachedBurnMax   = te.getField(1);
        cachedCook      = te.getField(2);
        cachedCookTotal = te.getField(3);
    }

    @Override @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) { te.setField(id, data); }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return te.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) return copy;

        ItemStack stack = slot.getStack();
        copy = stack.copy();

        if (index == TileEntitySuperFurnace.SLOT_OUTPUT) {
            if (!mergeItemStack(stack, 3, 39, true)) return ItemStack.EMPTY;
            slot.onSlotChange(stack, copy);
        } else if (index != TileEntitySuperFurnace.SLOT_FUEL
                && index != TileEntitySuperFurnace.SLOT_INPUT) {
            if (!FurnaceRecipes.instance().getSmeltingResult(stack).isEmpty()) {
                if (!mergeItemStack(stack, SLOT_INPUT(), SLOT_INPUT() + 1, false))
                    return ItemStack.EMPTY;
            } else if (SlotFurnaceFuel.isBucket(stack)
                    || net.minecraft.tileentity.TileEntityFurnace.isItemFuel(stack)) {
                if (!mergeItemStack(stack, SLOT_FUEL(), SLOT_FUEL() + 1, false))
                    return ItemStack.EMPTY;
            } else if (index >= 3 && index < 30) {
                if (!mergeItemStack(stack, 30, 39, false)) return ItemStack.EMPTY;
            } else if (index >= 30 && index < 39) {
                if (!mergeItemStack(stack, 3, 30, false)) return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(stack, 3, 39, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty())               slot.putStack(ItemStack.EMPTY);
        else                               slot.onSlotChanged();
        if (stack.getCount() == copy.getCount()) return ItemStack.EMPTY;
        slot.onTake(player, stack);
        return copy;
    }

    private static int SLOT_INPUT() { return TileEntitySuperFurnace.SLOT_INPUT; }
    private static int SLOT_FUEL()  { return TileEntitySuperFurnace.SLOT_FUEL; }

    // ---- Client-side progress helpers ----

    @SideOnly(Side.CLIENT)
    public int getCookProgressScaled(int pixels) {
        int total = te.getField(3);
        if (total == 0) total = TileEntitySuperFurnace.COOK_TIME;
        return te.getField(2) * pixels / total;
    }

    @SideOnly(Side.CLIENT)
    public int getBurnLeftScaled(int pixels) {
        int max = te.getField(1);
        if (max == 0) max = 200;
        return te.getField(0) * pixels / max;
    }

    @SideOnly(Side.CLIENT)
    public boolean isBurning() { return te.getField(0) > 0; }
}
