package com.memfixer;

import com.memfixer.client.gui.MemFixerClientSetup;
import com.memfixer.command.MemFixerCommand;
import com.memfixer.config.MemFixerConfig;
import com.memfixer.modules.reclaimer.MemoryReclaimer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Main entry point for MemFixer mod.
 * Coordinates modular lifecycle hooks and delegates execution to specialized modules.
 */
@Mod(MemFixer.MOD_ID)
public class MemFixer {
    public static final String MOD_ID = "memfixer";
    public static final Logger LOGGER = LogManager.getLogger("MemFixer");

    public MemFixer(IEventBus modEventBus, ModContainer modContainer) {
        MemFixerConfig.load();

        if (MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer] Initializing modular ultra-low footprint memory engine...");
        }

        if (FMLEnvironment.dist.isClient()) {
            if (net.neoforged.fml.ModList.get().isLoaded("cloth_config")) {
                MemFixerClientSetup.registerConfigScreen(modContainer);
            }
            modEventBus.addListener(this::onClientSetup);
        }

        modEventBus.addListener(this::onCommonSetup);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        MemFixerCommand.register(event.getDispatcher());
    }

    private void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            if (MemFixerConfig.ENABLE_LOGGING) {
                LOGGER.info("[MemFixer] Common setup initialized. Active memory constraints engaged.");
            }
        });
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (MemFixerConfig.ENABLE_LOGGING) {
                LOGGER.info("[MemFixer] Client setup complete. Arming post-launch memory reclaimer...");
            }
            MemoryReclaimer.armClientReclamation();
        });
    }
}
