package net.favela.yaw.mixin;

import net.favela.yaw.impl.modules.categories.movement.Step;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "maxUpStep", at = @At("HEAD"), cancellable = true)
    private void yaw$applyStep(CallbackInfoReturnable<Float> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self != Minecraft.getInstance().player) return;
        Step step = Step.getInstance();
        if (step == null || !step.isVanillaMode()) return;
        cir.setReturnValue(step.getStepHeightValue());
    }
}
