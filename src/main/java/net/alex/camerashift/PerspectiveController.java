package net.alex.camerashift;

import net.minecraft.Util;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CalculateDetachedCameraDistanceEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch.PlayerMode;

/**
 * Follows Epic Fight's player mode: mining (vanilla) mode ends in first person, battle mode in the
 * third-person back view, and the camera glides along its third-person distance in between.
 *
 * <p>{@link #onRenderFrame} polls the mode per frame rather than per tick: Epic Fight applies its mode key inside
 * {@code Minecraft.tick()}, which runs before this event in the same frame, so the glide starts on the very frame
 * the key press is handled. The first mode seen in a world only aligns the perspective, since a glide on join would
 * look like a glitch; leaving the world resets it, so the next world starts fresh. {@link #onCameraDistance} runs
 * at {@code LOWEST}, so the glide scales whatever distance other camera mods settled on, from
 * {@link #NEAR_DISTANCE} (in blocks before entity scaling, which keeps the camera outside the head) to that full
 * distance.
 *
 * <p>A glide toward the eye stays in third person until it arrives; {@link #advanceGlide} then drops into first
 * person. Pressing F5 mid-glide ends the glide, since the player's choice wins. With Epic Fight's TPS camera set to
 * always on, every switch snaps: that camera replaces the vanilla third-person setup, so a glide would never show.
 * Epic Fight's own Auto Perspective Switching snaps instantly and would cut every glide short, so
 * {@link #disableEpicFightAutoSwitch} turns it off in Epic Fight's config whenever it finds it on.
 *
 * <p>For other camera mods: {@link #isGliding} is true while a glide runs, and a mod that reacts to a close
 * third-person camera must hold off meanwhile. {@link #consumeSettledInFirstPerson} tells, once per landing,
 * whether mining mode landed in first person since the last call ({@link #settledInFirstPerson}), so a mod can
 * drop a third-person view it was waiting to restore.
 *
 * <p>⚠ The Epic Fight members used are internals, not API. A {@link LinkageError} means a newer Epic Fight renamed
 * something this build links against: {@link #disabled} then keeps the controller off with one logged error,
 * instead of crashing every frame, until the mod is rebuilt against that version.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

@EventBusSubscriber(modid = CameraShift.MOD_ID, value = Dist.CLIENT)
public final class PerspectiveController {

    private static final float NEAR_DISTANCE = 0.5F;
    private static final CameraGlide GLIDE = new CameraGlide();
    @Nullable
    private static PlayerMode lastMode;
    private static boolean gliding;
    private static boolean settledInFirstPerson;
    private static boolean disabled;

    private PerspectiveController() {}

    public static boolean isGliding() {
        return gliding;
    }

    public static boolean consumeSettledInFirstPerson() {
        boolean settled = settledInFirstPerson;
        settledInFirstPerson = false;
        return settled;
    }

    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        if (disabled) {
            return;
        }
        try {
            followMode();
        } catch (LinkageError e) {
            disabled = true;
            gliding = false;
            CameraShift.LOGGER.error(
                    "Epic Fight's internals changed; Camera Shift stays off until rebuilt against this version", e);
        }
    }

    private static void followMode() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            lastMode = null;
            gliding = false;
            return;
        }
        disableEpicFightAutoSwitch();
        LocalPlayerPatch patch = EpicFightCapabilities.getLocalPlayerPatch(player);
        if (patch == null) {
            return;
        }
        long now = Util.getMillis();
        PlayerMode mode = patch.getPlayerMode();
        if (mode != lastMode) {
            boolean animate = lastMode != null;
            lastMode = mode;
            applyMode(minecraft.options, mode == PlayerMode.EPICFIGHT, animate, now);
        }
        if (gliding) {
            advanceGlide(minecraft.options, now);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onCameraDistance(CalculateDetachedCameraDistanceEvent event) {
        if (!gliding || event.getCamera().getEntity() != Minecraft.getInstance().player) {
            return;
        }
        float full = event.getDistance();
        float near = Math.min(NEAR_DISTANCE, full);
        event.setDistance(Mth.lerp((float) GLIDE.easedProgress(Util.getMillis()), near, full));
    }

    private static void applyMode(Options options, boolean battle, boolean animate, long now) {
        CameraType current = options.getCameraType();
        long duration = Math.round(CameraShiftConfig.TRANSITION_SECONDS.get() * 1000.0);
        boolean glide = animate && duration > 0
                && ClientConfig.getTpsActivationType() != ClientConfig.TPSActivationType.ALWAYS;
        if (!gliding) {
            GLIDE.snapTo(current.isFirstPerson() ? 0.0 : 1.0);
        }
        if (battle) {
            options.setCameraType(CameraType.THIRD_PERSON_BACK);
            if (glide && (current.isFirstPerson() || gliding)) {
                GLIDE.glideTo(1.0, now, duration);
                gliding = true;
            } else {
                GLIDE.snapTo(1.0);
                gliding = false;
            }
        } else if (current.isFirstPerson()) {
            gliding = false;
            settledInFirstPerson = true;
        } else if (glide && current == CameraType.THIRD_PERSON_BACK) {
            GLIDE.glideTo(0.0, now, duration);
            gliding = true;
        } else {
            options.setCameraType(CameraType.FIRST_PERSON);
            gliding = false;
            settledInFirstPerson = true;
        }
    }

    private static void advanceGlide(Options options, long now) {
        if (options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            gliding = false;
            return;
        }
        if (GLIDE.isFinished(now)) {
            gliding = false;
            if (GLIDE.target() == 0.0) {
                options.setCameraType(CameraType.FIRST_PERSON);
                settledInFirstPerson = true;
            }
        }
    }

    private static void disableEpicFightAutoSwitch() {
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
