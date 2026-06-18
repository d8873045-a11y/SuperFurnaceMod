package com.example.superfurnacemod.init;

import com.example.superfurnacemod.SuperFurnaceMod;
import com.example.superfurnacemod.block.BlockSuperFurnace;
import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class ModBlocks {

    public static BlockSuperFurnace SUPER_FURNACE;

    public static void registerBlocks() {
        SUPER_FURNACE = registerBlock(new BlockSuperFurnace(false), "super_furnace");

        // Register the "on" (active) variant — not obtainable in inventory,
        // placed automatically by TileEntity when smelting
        registerBlock(new BlockSuperFurnace(true), "super_furnace_active");

        // Register TileEntity
        GameRegistry.registerTileEntity(
            TileEntitySuperFurnace.class,
            SuperFurnaceMod.MODID + ":super_furnace"
        );
    }

    private static <T extends Block> T registerBlock(T block, String name) {
        block.setRegistryName(SuperFurnaceMod.MODID, name);
        block.setUnlocalizedName(SuperFurnaceMod.MODID + "." + name);
        ForgeRegistries.BLOCKS.register(block);

        // Register ItemBlock only for the "off" version
        if (!name.endsWith("_active")) {
            ItemBlock itemBlock = new ItemBlock(block);
            itemBlock.setRegistryName(SuperFurnaceMod.MODID, name);
            ForgeRegistries.ITEMS.register(itemBlock);
        }

        return block;
    }
}
