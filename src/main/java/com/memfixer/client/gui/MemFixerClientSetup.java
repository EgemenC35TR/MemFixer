package com.memfixer.client.gui;

import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Client setup helper for registering GUI extensions.
 * Isolated from the main mod class to prevent client Screen classes from leaking into dedicated server bytecode.
 */
public final class MemFixerClientSetup {

    private MemFixerClientSetup() {}

    public static void registerConfigScreen(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class,
                (container, parent) -> MemFixerClothConfigScreen.create(parent));
    }
}
