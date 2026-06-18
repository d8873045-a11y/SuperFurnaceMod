package com.example.superfurnacemod;

import com.example.superfurnacemod.init.ModBlocks;
import com.example.superfurnacemod.proxy.CommonProxy;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import com.example.superfurnacemod.gui.GuiHandler;
import org.apache.logging.log4j.Logger;

@Mod(
    modid = SuperFurnaceMod.MODID,
    name = SuperFurnaceMod.NAME,
    version = SuperFurnaceMod.VERSION,
    acceptedMinecraftVersions = "[1.12.2]"
)
public class SuperFurnaceMod {

    public static final String MODID = "superfurnacemod";
    public static final String NAME = "Super Furnace Mod";
    public static final String VERSION = "1.0.0";

    @Instance(MODID)
    public static SuperFurnaceMod instance;

    @SidedProxy(
        clientSide = "com.example.superfurnacemod.proxy.ClientProxy",
        serverSide = "com.example.superfurnacemod.proxy.CommonProxy"
    )
    public static CommonProxy proxy;

    public static Logger logger;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        logger.info("SuperFurnaceMod preInit started");

        ModBlocks.registerBlocks();

        proxy.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("SuperFurnaceMod init started");

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());

        proxy.init(event);
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("SuperFurnaceMod postInit started");
        proxy.postInit(event);
    }
}
