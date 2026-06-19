package com.example.superfurnacemod.gui;

import com.example.superfurnacemod.container.ContainerSuperFurnace;
import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

import javax.annotation.Nullable;

public class GuiHandler implements IGuiHandler {

    public static final int GUI_ID = 0;

    @Nullable @Override
    public Object getServerGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z) {
        if (ID != GUI_ID) return null;
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof TileEntitySuperFurnace)
            return new ContainerSuperFurnace(player.inventory, (TileEntitySuperFurnace) te);
        return null;
    }

    @Nullable @Override
    public Object getClientGuiElement(int ID, EntityPlayer player,
                                      World world, int x, int y, int z) {
        if (ID != GUI_ID) return null;
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof TileEntitySuperFurnace)
            return new GuiSuperFurnace(player.inventory, (TileEntitySuperFurnace) te);
        return null;
    }
}
