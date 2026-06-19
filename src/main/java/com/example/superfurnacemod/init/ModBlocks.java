package com.example.superfurnacemod.init;

import com.example.superfurnacemod.SuperFurnaceMod;
import com.example.superfurnacemod.block.BlockSuperFurnace;
import com.example.superfurnacemod.tileentity.TileEntitySuperFurnace;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

public final class ModBlocks {

    public static BlockSuperFurnace SUPER_FURNACE;
    public static BlockSuperFurnace SUPER_FURNACE_LIT;

    private ModBlocks() {}

    public static void register() {
        // Inactive (placeable) variant
        SUPER_FURNACE = registerBlock(
            new BlockSuperFurnace(false), "super_furnace", true);

        // Active (lit) variant — not in creative tab, no ItemBlock
        SUPER_FURNACE_LIT = registerBlock(
            new BlockSuperFurnace(true), "super_furnace_lit", false);

        // Register TileEntity
        GameRegistry.registerTileEntity(
            TileEntitySuperFurnace.class,
            SuperFurnaceMod.MODID + ":super_furnace"
        );
    }

    private static BlockSuperFurnace registerBlock(
            BlockSuperFurnace block, String name, boolean withItemBlock) {

        block.setRegistryName(SuperFurnaceMod.MODID, name);
        block.setUnlocalizedName(SuperFurnaceMod.MODID + "." + name);
        ForgeRegistries.BLOCKS.register(block);

        if (withItemBlock) {
            Item item = new ItemBlock(block);
            item.setRegistryName(SuperFurnaceMod.MODID, name);
            ForgeRegistries.ITEMS.register(item);
        }

        return block;
    }
}
