package com.memfixer.mixin.core;

import com.memfixer.modules.reclaimer.MemoryReclaimer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin intercepting Minecraft.disconnect to execute an asynchronous memory sweep
 * when leaving a singleplayer or multiplayer world, releasing chunk and entity caches.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftDisconnectMixin {

    @Inject(
            method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V",
            at = @At("RETURN")
    )
    private void onDisconnectPost(Screen nextScreen, boolean transferring, CallbackInfo ci) {
        if (!transferring) {
            MemoryReclaimer.onLevelUnload();
        }
    }
}
