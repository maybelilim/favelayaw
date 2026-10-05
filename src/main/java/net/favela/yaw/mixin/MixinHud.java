package net.favela.yaw.mixin;

import net.favela.yaw.impl.gui.hud.Hud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.gui.Hud.class)
public class MixinHud {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void favelayaw$renderHud(GuiGraphicsExtractor context, DeltaTracker deltaTracker, CallbackInfo ci) {
        Hud.renderAll(context);
    }
}