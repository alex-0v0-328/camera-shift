package net.alex.camerashift;

import net.minecraft.client.player.LocalPlayer;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch.PlayerMode;

/**
 * Every Epic Fight member Camera Shift reads or writes, in one class, so a newer Epic Fight that renames one touches
 * only this file. {@code ClientConfig} here is Epic Fight's ({@link yesman.epicfight.config.ClientConfig}), not
 * Camera Shift's own.
 *
 * <p>{@link #getPlayerMode} is null until Epic Fight has attached its player patch. {@link #isTpsCameraAlways} tells
 * whether Epic Fight's TPS camera is set to always on. {@link #disableAutoPerspectiveSwitching} turns Epic Fight's
 * Auto Perspective Switching off in its config, and logs it, whenever it finds it on.
 *
 * <p>⚠ These members are internals, not API. Every call runs inside {@link PerspectiveController#onRenderFrame}, which
 * turns a {@link LinkageError} from here into one logged error and keeps the controller off.
 *
 * @author Alex
 * @version 1.0.0
 * @see PerspectiveController
 * @since 1.0.0
 */

final class EpicFightBridge {

    private EpicFightBridge() {}

    @Nullable
    static PlayerMode getPlayerMode(LocalPlayer player) {
        LocalPlayerPatch playerPatch = EpicFightCapabilities.getLocalPlayerPatch(player);
        return playerPatch == null ? null : playerPatch.getPlayerMode();
    }

    static boolean isTpsCameraAlways() {
        return ClientConfig.getTpsActivationType() == ClientConfig.TPSActivationType.ALWAYS;
    }

    static void disableAutoPerspectiveSwitching() {
        if (!ClientConfig.autoPerspectiveSwithing) {
            return;
        }
        ClientConfig.autoPerspectiveSwithing = false;
        ClientConfig.AUTO_PERSPECTIVE_SWITCHING.set(false);
        ClientConfig.AUTO_PERSPECTIVE_SWITCHING.save();
        CameraShift.LOGGER.info(
                "Turned off Epic Fight's Auto Perspective Switching; Camera Shift drives the perspective now");
    }
}
