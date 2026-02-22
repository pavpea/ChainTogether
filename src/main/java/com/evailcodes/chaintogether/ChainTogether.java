package com.evailcodes.chaintogether;

import com.evailcodes.chaintogether.config.ChainConfig;
import com.evailcodes.chaintogether.handler.ChainHandler;
import com.evailcodes.chaintogether.network.ChainPacketHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ChainTogether.MODID)
public class ChainTogether {
    public static final String MODID = "chaintogether";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public ChainTogether(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ChainConfig.SPEC, "chaintogether-common.toml");

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ChainHandler());
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ChainPacketHandler.register();
        LOGGER.info("ChainTogether initialized!");
    }
}