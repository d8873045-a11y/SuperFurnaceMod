package com.example.superfurnacemod.proxy;

import com.example.superfurnacemod.init.ModBlocks;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        registerBlockModels();
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    private void registerBlockModels() {
        // Register item model for super_furnace (the "off" variant)
        Item item = Item.getItemFromBlock(ModBlocks.SUPER_FURNACE);
        ModelLoader.setCustomModelResourceLocation(
                item, 0,
                new ModelResourceLocation("superfurnacemod:super_furnace", "inventory")
        );
    }
}
