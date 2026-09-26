package net.alex.camerashift.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.alex.camerashift.PerspectiveController;
import net.minecraft.client.CameraType;

/**
 * Better Lock On switches to first person whenever the third-person camera comes within its switch distance of the
 * eyes, and back to third person once there is room. A glide passes through that range on purpose, so the check
 * sits out while one runs.
 *
 * <p>{@code @Pseudo} plus {@code require = 0} and the non-required mixin config keep this optional: without Better
 * Lock On, or after it renames these members, the mixin simply does not apply.
 */
@Pseudo
@Mixin(targets = "net.shelmarow.betterlockon.client.control.LockOnControl")
public abstract class LockOnControlMixin {
    @Shadow
    private static CameraType lastCameraType;

    @Inject(method = "handleCamera", at = @At("HEAD"), cancellable = true, require = 0)
    private static void camerashift$yieldToGlide(CallbackInfo ci) {
        if (PerspectiveController.isGliding()) {
            ci.cancel();
            return;
        }
        if (PerspectiveController.consumeSettledInFirstPerson()) {
            // Mining mode owns first person now; a third-person view Better Lock On meant to restore is stale.
            lastCameraType = CameraType.FIRST_PERSON;
        }
    }
}
