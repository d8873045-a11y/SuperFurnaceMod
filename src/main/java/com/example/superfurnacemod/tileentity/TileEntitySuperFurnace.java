package com.example.superfurnacemod.tileentity;

import com.example.superfurnacemod.block.BlockSuperFurnace;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * TileEntity for the Super Furnace.
 *
 * Smelts items 3× faster than a vanilla furnace:
 *   Vanilla = 200 ticks/item → Super = 66 ticks/item  (200 / 3 ≈ 66)
 */
public class TileEntitySuperFurnace extends TileEntityLockable implements ITickable {

    // Slot indices (match vanilla furnace layout)
    public static final int SLOT_INPUT  = 0;
    public static final int SLOT_FUEL   = 1;
    public static final int SLOT_OUTPUT = 2;

    /** Ticks needed to smelt one item (vanilla = 200, ours = 200/3 ≈ 66). */
    public static final int COOK_TIME = 66;

    private NonNullList<ItemStack> inventory =
        NonNullList.withSize(3, ItemStack.EMPTY);

    private int burnTime;           // ticks of fuel remaining
    private int currentItemBurnTime;// total burn time of the current fuel item
    private int cookTime;           // ticks spent cooking the current item
    private int totalCookTime;      // ticks needed for current item (always COOK_TIME)

    private String customName;

    @Nullable private IItemHandler itemHandler;

    // =========================================================================
    // Name
    // =========================================================================

    public void setCustomName(String name) { this.customName = name; }

    @Override public String getName() {
        return hasCustomName() ? customName : "container.superfurnacemod.super_furnace";
    }

    @Override public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    // =========================================================================
    // IInventory
    // =========================================================================

    @Override public int  getSizeInventory()          { return inventory.size(); }
    @Override public boolean isEmpty() {
        for (ItemStack s : inventory) if (!s.isEmpty()) return false;
        return true;
    }
    @Override public ItemStack getStackInSlot(int i)  { return inventory.get(i); }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        return ItemStackHelper.getAndSplit(inventory, index, count);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return ItemStackHelper.getAndRemove(inventory, index);
    }

    @Override
    public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
        boolean same = !stack.isEmpty()
            && stack.isItemEqual(inventory.get(index))
            && ItemStack.areItemStackTagsEqual(stack, inventory.get(index));

        inventory.set(index, stack);
        if (stack.getCount() > getInventoryStackLimit())
            stack.setCount(getInventoryStackLimit());

        if (index == SLOT_INPUT && !same) {
            totalCookTime = COOK_TIME;
            cookTime = 0;
            markDirty();
        }
    }

    @Override public int     getInventoryStackLimit()                  { return 64; }
    @Override public void    openInventory(EntityPlayer p)             {}
    @Override public void    closeInventory(EntityPlayer p)            {}

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this
            && player.getDistanceSq(pos.getX() + 0.5,
                                    pos.getY() + 0.5,
                                    pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_OUTPUT) return false;
        if (index == SLOT_FUEL)   return TileEntityFurnace.isItemFuel(stack);
        return true;
    }

    // IInventory fields (used by GUI / container sync)
    @Override public int getField(int id) {
        switch (id) {
            case 0: return burnTime;
            case 1: return currentItemBurnTime;
            case 2: return cookTime;
            case 3: return totalCookTime;
            default: return 0;
        }
    }
    @Override public void setField(int id, int val) {
        switch (id) {
            case 0: burnTime            = val; break;
            case 1: currentItemBurnTime = val; break;
            case 2: cookTime            = val; break;
            case 3: totalCookTime       = val; break;
        }
    }
    @Override public int getFieldCount() { return 4; }
    @Override public void clear()        { inventory.clear(); }

    // =========================================================================
    // GUI / Container — reuse vanilla furnace container
    // =========================================================================

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerFurnace(playerInventory, this);
    }

    @Override public String getGuiID() { return "minecraft:furnace"; }

    // =========================================================================
    // NBT
    // =========================================================================

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(tag, inventory);
        burnTime            = tag.getInteger("BurnTime");
        cookTime            = tag.getInteger("CookTime");
        totalCookTime       = tag.getInteger("CookTimeTotal");
        currentItemBurnTime = TileEntityFurnace.getItemBurnTime(inventory.get(SLOT_FUEL));
        if (tag.hasKey("CustomName", 8))
            customName = tag.getString("CustomName");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setInteger("BurnTime",      burnTime);
        tag.setInteger("CookTime",      cookTime);
        tag.setInteger("CookTimeTotal", totalCookTime);
        ItemStackHelper.saveAllItems(tag, inventory);
        if (hasCustomName()) tag.setString("CustomName", customName);
        return tag;
    }

    // =========================================================================
    // Tick — core smelting logic (3× faster)
    // =========================================================================

    @Override
    public void update() {
        boolean wasBurning = isBurning();
        boolean dirty      = false;

        if (isBurning()) {
            burnTime--;
        }

        if (!world.isRemote) {
            ItemStack fuel  = inventory.get(SLOT_FUEL);
            ItemStack input = inventory.get(SLOT_INPUT);

            if (isBurning() || (!fuel.isEmpty() && !input.isEmpty())) {
                // Need to light / refuel?
                if (!isBurning() && canSmelt()) {
                    burnTime            = TileEntityFurnace.getItemBurnTime(fuel);
                    currentItemBurnTime = burnTime;

                    if (isBurning()) {
                        dirty = true;
                        if (!fuel.isEmpty()) {
                            net.minecraft.item.Item fuelItem = fuel.getItem();
                            fuel.shrink(1);
                            if (fuel.isEmpty()) {
                                inventory.set(SLOT_FUEL,
                                    fuelItem.getContainerItem(fuel));
                            }
                        }
                    }
                }

                // Cook
                if (isBurning() && canSmelt()) {
                    cookTime++;
                    if (cookTime >= totalCookTime) {
                        cookTime      = 0;
                        totalCookTime = COOK_TIME;
                        doSmelt();
                        dirty = true;
                    }
                } else {
                    cookTime = 0;
                }
            } else if (!isBurning() && cookTime > 0) {
                cookTime = MathHelper.clamp(cookTime - 2, 0, totalCookTime);
            }

            // Toggle block state if burn state changed
            if (wasBurning != isBurning()) {
                dirty = true;
                BlockSuperFurnace.setState(isBurning(), world, pos);
            }
        }

        if (dirty) markDirty();
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    public boolean isBurning() { return burnTime > 0; }

    private boolean canSmelt() {
        if (inventory.get(SLOT_INPUT).isEmpty()) return false;

        ItemStack result = FurnaceRecipes.instance()
                                        .getSmeltingResult(inventory.get(SLOT_INPUT));
        if (result.isEmpty()) return false;

        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) return true;
        if (!output.isItemEqual(result)) return false;

        int combined = output.getCount() + result.getCount();
        return combined <= getInventoryStackLimit()
            && combined <= output.getMaxStackSize();
    }

    private void doSmelt() {
        if (!canSmelt()) return;

        ItemStack result = FurnaceRecipes.instance()
                                        .getSmeltingResult(inventory.get(SLOT_INPUT));
        ItemStack output = inventory.get(SLOT_OUTPUT);

        if (output.isEmpty()) {
            inventory.set(SLOT_OUTPUT, result.copy());
        } else if (output.getItem() == result.getItem()) {
            output.grow(result.getCount());
        }

        inventory.get(SLOT_INPUT).shrink(1);
    }

    /** Drop all contained items into the world when the block is broken. */
    public void dropInventory(World world, BlockPos pos) {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                EntityItem drop = new EntityItem(
                    world,
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    stack
                );
                drop.motionX = world.rand.nextGaussian() * 0.05;
                drop.motionY = world.rand.nextGaussian() * 0.05 + 0.2;
                drop.motionZ = world.rand.nextGaussian() * 0.05;
                world.spawnEntity(drop);
            }
        }
    }

    // =========================================================================
    // Capabilities
    // =========================================================================

    @Override
    public boolean hasCapability(@Nonnull Capability<?> cap, @Nullable EnumFacing side) {
        return cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || super.hasCapability(cap, side);
    }

    @Override @Nullable
    public <T> T getCapability(@Nonnull Capability<T> cap, @Nullable EnumFacing side) {
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (itemHandler == null) itemHandler = new InvWrapper(this);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return super.getCapability(cap, side);
    }
}
