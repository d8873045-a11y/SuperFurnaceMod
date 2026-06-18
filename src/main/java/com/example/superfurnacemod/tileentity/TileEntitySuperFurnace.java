package com.example.superfurnacemod.tileentity;

import com.example.superfurnacemod.SuperFurnaceMod;
import com.example.superfurnacemod.block.BlockSuperFurnace;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerFurnace;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityLockable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
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
 * TileEntity for Super Furnace.
 * Smelts items 3x faster than a vanilla furnace (cook time divided by 3).
 * Vanilla furnace: 200 ticks per item. Super Furnace: ~67 ticks per item.
 */
public class TileEntitySuperFurnace extends TileEntityLockable implements ITickable {

    // Vanilla furnace cook time is 200 ticks. We divide by 3 → ~67 ticks.
    private static final int VANILLA_COOK_TIME = 200;
    public static final int SUPER_COOK_TIME = VANILLA_COOK_TIME / 3; // 66 ticks

    // Slot indices
    public static final int SLOT_INPUT  = 0;
    public static final int SLOT_FUEL   = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT  = 3;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int furnaceBurnTime;      // ticks remaining on current fuel
    private int currentItemBurnTime;  // total burn time of current fuel item
    private int cookTime;             // ticks spent cooking current item
    private int totalCookTime;        // total ticks needed to cook current item

    private String customName;

    private IItemHandler itemHandler;

    // -------------------------------------------------------------------------
    // Name / unlock helpers
    // -------------------------------------------------------------------------

    public void setCustomInventoryName(String name) {
        this.customName = name;
    }

    @Override
    public String getName() {
        return hasCustomName() ? customName : "container.superfurnacemod.super_furnace";
    }

    @Override
    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    // -------------------------------------------------------------------------
    // IInventory
    // -------------------------------------------------------------------------

    @Override
    public int getSizeInventory() {
        return inventory.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.get(index);
    }

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
        boolean sameItem = !stack.isEmpty() && stack.isItemEqual(inventory.get(index))
                && ItemStack.areItemStackTagsEqual(stack, inventory.get(index));
        inventory.set(index, stack);
        if (stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
        if (index == SLOT_INPUT && !sameItem) {
            totalCookTime = getCookTime(stack);
            cookTime = 0;
            markDirty();
        }
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        if (world.getTileEntity(pos) != this) return false;
        return player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        if (index == SLOT_OUTPUT) return false;
        if (index == SLOT_FUEL)   return TileEntityFurnaceHelper.isItemFuel(stack);
        return true;
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0: return furnaceBurnTime;
            case 1: return currentItemBurnTime;
            case 2: return cookTime;
            case 3: return totalCookTime;
            default: return 0;
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0: furnaceBurnTime    = value; break;
            case 1: currentItemBurnTime = value; break;
            case 2: cookTime           = value; break;
            case 3: totalCookTime      = value; break;
        }
    }

    @Override
    public int getFieldCount() {
        return 4;
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    // -------------------------------------------------------------------------
    // Container / GUI
    // -------------------------------------------------------------------------

    @Override
    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn) {
        return new ContainerFurnace(playerInventory, this);
    }

    @Override
    public String getGuiID() {
        return "minecraft:furnace";
    }

    // -------------------------------------------------------------------------
    // NBT serialization
    // -------------------------------------------------------------------------

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        inventory = NonNullList.withSize(getSizeInventory(), ItemStack.EMPTY);
        ItemStackHelper.loadAllItems(compound, inventory);
        furnaceBurnTime     = compound.getInteger("BurnTime");
        cookTime            = compound.getInteger("CookTime");
        totalCookTime       = compound.getInteger("CookTimeTotal");
        currentItemBurnTime = getItemBurnTime(inventory.get(SLOT_FUEL));
        if (compound.hasKey("CustomName", 8)) {
            customName = compound.getString("CustomName");
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("BurnTime", furnaceBurnTime);
        compound.setInteger("CookTime", cookTime);
        compound.setInteger("CookTimeTotal", totalCookTime);
        ItemStackHelper.saveAllItems(compound, inventory);
        if (hasCustomName()) {
            compound.setString("CustomName", customName);
        }
        return compound;
    }

    // -------------------------------------------------------------------------
    // Tick logic
    // -------------------------------------------------------------------------

    @Override
    public void update() {
        boolean wasBurning = isBurning();
        boolean dirty = false;

        if (isBurning()) {
            furnaceBurnTime--;
        }

        if (!world.isRemote) {
            ItemStack fuelStack  = inventory.get(SLOT_FUEL);
            ItemStack inputStack = inventory.get(SLOT_INPUT);

            if (isBurning() || (!fuelStack.isEmpty() && !inputStack.isEmpty())) {
                if (!isBurning() && canSmelt()) {
                    furnaceBurnTime     = getItemBurnTime(fuelStack);
                    currentItemBurnTime = furnaceBurnTime;

                    if (isBurning()) {
                        dirty = true;
                        if (!fuelStack.isEmpty()) {
                            Item fuelItem = fuelStack.getItem();
                            fuelStack.shrink(1);
                            if (fuelStack.isEmpty()) {
                                ItemStack fuelContainerItem = fuelItem.getContainerItem(fuelStack);
                                inventory.set(SLOT_FUEL, fuelContainerItem);
                            }
                        }
                    }
                }

                if (isBurning() && canSmelt()) {
                    cookTime++;
                    if (cookTime == totalCookTime) {
                        cookTime      = 0;
                        totalCookTime = getCookTime(inputStack);
                        smeltItem();
                        dirty = true;
                    }
                } else {
                    cookTime = 0;
                }
            } else if (!isBurning() && cookTime > 0) {
                cookTime = MathHelper.clamp(cookTime - 2, 0, totalCookTime);
            }

            if (wasBurning != isBurning()) {
                dirty = true;
                BlockSuperFurnace.setState(isBurning(), world, pos);
            }
        }

        if (dirty) {
            markDirty();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    public boolean isBurning() {
        return furnaceBurnTime > 0;
    }

    private boolean canSmelt() {
        if (inventory.get(SLOT_INPUT).isEmpty()) return false;
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(inventory.get(SLOT_INPUT));
        if (result.isEmpty()) return false;

        ItemStack output = inventory.get(SLOT_OUTPUT);
        if (output.isEmpty()) return true;
        if (!output.isItemEqual(result)) return false;

        int combined = output.getCount() + result.getCount();
        return combined <= getInventoryStackLimit() && combined <= output.getMaxStackSize();
    }

    private void smeltItem() {
        if (!canSmelt()) return;
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(inventory.get(SLOT_INPUT));
        ItemStack output = inventory.get(SLOT_OUTPUT);

        if (output.isEmpty()) {
            inventory.set(SLOT_OUTPUT, result.copy());
        } else if (output.getItem() == result.getItem()) {
            output.grow(result.getCount());
        }

        inventory.get(SLOT_INPUT).shrink(1);
    }

    private int getCookTime(ItemStack stack) {
        // 3x faster than vanilla (66 ticks instead of 200)
        return SUPER_COOK_TIME;
    }

    private int getItemBurnTime(ItemStack stack) {
        return net.minecraft.tileentity.TileEntityFurnace.getItemBurnTime(stack);
    }

    /**
     * Drop all inventory items into the world when the block is broken.
     */
    public void dropInventory(World world, BlockPos pos) {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) {
                net.minecraft.entity.item.EntityItem entity = new net.minecraft.entity.item.EntityItem(
                        world,
                        pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        stack
                );
                entity.motionX = world.rand.nextGaussian() * 0.05;
                entity.motionY = world.rand.nextGaussian() * 0.05 + 0.2;
                entity.motionZ = world.rand.nextGaussian() * 0.05;
                world.spawnEntity(entity);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Capability support
    // -------------------------------------------------------------------------

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (itemHandler == null) itemHandler = new InvWrapper(this);
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return super.getCapability(capability, facing);
    }

    // -------------------------------------------------------------------------
    // Fuel helper
    // -------------------------------------------------------------------------

    static final class TileEntityFurnaceHelper {
        static boolean isItemFuel(ItemStack stack) {
            return net.minecraft.tileentity.TileEntityFurnace.isItemFuel(stack);
        }
    }
}
