package com.example.superfurnacemod.block;

import com.example.superfurnacemod.SuperFurnaceMod;
import com.example.superfurnacemod.init.ModBlocks;
import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockSuperFurnace extends BlockFurnace {

    private final boolean isActive;

    public BlockSuperFurnace(boolean isActive) {
        super(isActive);
        this.isActive = isActive;
        this.setCreativeTab(null);
        this.setSoundType(SoundType.STONE);
        this.setHardness(3.5F);
        this.setResistance(17.5F);

        if (!isActive) {
            this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySuperFurnace();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
                                    EntityPlayer playerIn, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            FMLNetworkHandler.openGui(playerIn, SuperFurnaceMod.instance,
                    GuiIds.SUPER_FURNACE, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        if (stack.hasDisplayName()) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof TileEntitySuperFurnace) {
                ((TileEntitySuperFurnace) tileentity).setCustomInventoryName(stack.getDisplayName());
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tileentity = worldIn.getTileEntity(pos);
        if (tileentity instanceof TileEntitySuperFurnace) {
            ((TileEntitySuperFurnace) tileentity).dropInventory(worldIn, pos);
        }
        super.breakBlock(worldIn, pos, state);
    }

    /**
     * Called when this block is activated (active/inactive state toggle).
     * Updates the block in world from inactive to active variant and vice versa.
     */
    public static void setState(boolean active, World worldIn, BlockPos pos) {
        IBlockState iblockstate = worldIn.getBlockState(pos);
        TileEntity tileentity = worldIn.getTileEntity(pos);

        keepInventory = true;

        if (active) {
            worldIn.setBlockState(pos, ModBlocks.SUPER_FURNACE.getDefaultState()
                    .withProperty(FACING, iblockstate.getValue(FACING)), 3);
            // Use active block directly from registry
            worldIn.setBlockState(pos,
                    net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS
                            .getValue(new net.minecraft.util.ResourceLocation(
                                    SuperFurnaceMod.MODID, "super_furnace_active"))
                            .getDefaultState()
                            .withProperty(FACING, iblockstate.getValue(FACING)), 3);
        } else {
            worldIn.setBlockState(pos, ModBlocks.SUPER_FURNACE.getDefaultState()
                    .withProperty(FACING, iblockstate.getValue(FACING)), 3);
        }

        keepInventory = false;

        if (tileentity != null) {
            tileentity.validate();
            worldIn.setTileEntity(pos, tileentity);
        }
    }

    public boolean isActive() {
        return isActive;
    }

    // GUI ID constant inner reference
    public static final class GuiIds {
        public static final int SUPER_FURNACE = 0;
    }
}
