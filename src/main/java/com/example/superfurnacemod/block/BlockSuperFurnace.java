package com.example.superfurnacemod.block;

import com.example.superfurnacemod.SuperFurnaceMod;
import com.example.superfurnacemod.init.ModBlocks;
import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import javax.annotation.Nullable;

/**
 * Super Furnace block.
 * Two registry variants: super_furnace (off) and super_furnace_lit (on).
 * Uses vanilla furnace textures via JSON models.
 */
public class BlockSuperFurnace extends BlockHorizontal implements ITileEntityProvider {

    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    /** true  = this block is the "lit" (active/smelting) variant */
    private final boolean isLit;

    public BlockSuperFurnace(boolean isLit) {
        super(Material.ROCK);
        this.isLit = isLit;

        this.setHardness(3.5F);
        this.setResistance(17.5F);
        this.setSoundType(SoundType.STONE);
        this.setLightLevel(isLit ? 0.875F : 0.0F);

        if (!isLit) {
            this.setCreativeTab(net.minecraft.creativetab.CreativeTabs.tabDecorations);
        }

        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
        );
    }

    // -------------------------------------------------------------------------
    // State helpers
    // -------------------------------------------------------------------------

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.getHorizontal(meta);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos,
            EnumFacing facing, float hitX, float hitY, float hitZ,
            int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState()
                   .withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    // -------------------------------------------------------------------------
    // TileEntity
    // -------------------------------------------------------------------------

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntitySuperFurnace();
    }

    // -------------------------------------------------------------------------
    // Player interaction — opens GUI
    // -------------------------------------------------------------------------

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
            EntityPlayer playerIn, EnumHand hand,
            EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!worldIn.isRemote) {
            FMLNetworkHandler.openGui(
                playerIn, SuperFurnaceMod.instance,
                0 /* GuiHandler.GUI_ID */,
                worldIn, pos.getX(), pos.getY(), pos.getZ()
            );
        }
        return true;
    }

    // -------------------------------------------------------------------------
    // Placement / removal
    // -------------------------------------------------------------------------

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state,
                                EntityLivingBase placer, ItemStack stack) {
        if (stack.hasDisplayName()) {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntitySuperFurnace) {
                ((TileEntitySuperFurnace) te).setCustomName(stack.getDisplayName());
            }
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof TileEntitySuperFurnace) {
            ((TileEntitySuperFurnace) te).dropInventory(worldIn, pos);
        }
        super.breakBlock(worldIn, pos, state);
    }

    // -------------------------------------------------------------------------
    // Switch between lit / unlit variants
    // -------------------------------------------------------------------------

    /**
     * Called by TileEntitySuperFurnace to toggle the lit state.
     * Preserves the TileEntity across the block swap.
     */
    public static void setState(boolean lit, World world, BlockPos pos) {
        IBlockState old = world.getBlockState(pos);
        TileEntity  te  = world.getTileEntity(pos);

        BlockSuperFurnace target = lit
            ? ModBlocks.SUPER_FURNACE_LIT
            : ModBlocks.SUPER_FURNACE;

        // keepInventory is a static flag in BlockContainer — we set it
        // via reflection-free hack: just swap block + restore TE
        world.setBlockState(pos,
            target.getDefaultState().withProperty(FACING, old.getValue(FACING)),
            3);

        if (te != null) {
            te.validate();
            world.setTileEntity(pos, te);
        }
    }

    public boolean isLit() {
        return isLit;
    }
}
