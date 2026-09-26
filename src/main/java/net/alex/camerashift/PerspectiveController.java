package net.alex.camerashift;

import org.jetbrains.annotations.Nullable;

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
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.config.ClientConfig;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch.PlayerMode;

/**
 * Follows Epic Fight's player mode: mining (vanilla) mode ends in first person, battle mode in the
 * third-person back view, and the camera glides along its third-person distance in between.
 */
@EventBusSubscriber(modid = CameraShift.MODID, value = Dist.CLIENT)
public final class PerspectiveController {
    /** Closest point of the glide, in blocks before entity scaling; keeps the camera outside the head. */
    private static final float NEAR_DISTANCE = 0.5F;
    private static final CameraGlide GLIDE = new CameraGlide();
    @Nullable
    private static PlayerMode lastMode;
    private static boolean gliding;
    /** Raised whenever mining mode lands in first person; read and cleared by {@link #consumeSettledInFirstPerson}. */
    private static boolean settledInFirstPerson;
    /** Set once Epic Fight's internals stop matching this build; the patch then stays out of the way. */
    private static boolean disabled;

    private PerspectiveController() {
    }

    /** True while a glide runs; camera mods that react to a close third-person camera must hold off meanwhile. */
    public static boolean isGliding() {
        return gliding;
    }

    /**
     * Whether mining mode landed in first person since the last call, so a camera mod can drop a third-person view
     * it was waiting to restore.
     */
    public static boolean consumeSettledInFirstPerson() {
        boolean settled = settledInFirstPerson;
        settledInFirstPerson = false;
        return settled;
    }

    // Polled per frame rather than per tick: Epic Fight applies its mode key inside Minecraft.tick(), which runs
    // before this event in the same frame, so the glide starts on the very frame the key press is handled.
    @SubscribeEvent
    public static void onRenderFrame(RenderFrameEvent.Pre event) {
        if (disabled) {
            return;
        }
        try {
            followMode();
        } catch (LinkageError e) {
            // A newer Epic Fight renamed something this build links against; stop instead of crashing every frame.
            disabled = true;
            gliding = false;
            CameraShift.LOGGER.error("Epic Fight's internals changed; Camera Shift stays off until rebuilt against this version", e);
        }
    }

    private static void followMode() {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            // Out of the world: the next one starts fresh.
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
            // The first mode seen in a world only aligns the perspective; a glide on join would look like a glitch.
            boolean animate = lastMode != null;
            lastMode = mode;
            applyMode(minecraft.options, mode == PlayerMode.EPICFIGHT, animate, now);
        }
        if (gliding) {
            advanceGlide(minecraft.options, now);
        }
    }

    // LOWEST so the glide scales whatever distance other camera mods settled on.
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
        // Epic Fight's always-on TPS camera replaces the vanilla third-person setup, so a glide would never show.
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
            // Stays in third person until the glide reaches the eye; advanceGlide then drops into first person.
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
            // The player pressed F5 mid-glide; their choice wins.
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

    /** Epic Fight's own switch snaps the perspective instantly and would cut every glide short. */
    private static void disableEpicFightAutoSwitch() {
        if (!ClientConfig.autoPerspectiveSwithing) {
            return;
        }
        ClientConfig.autoPerspectiveSwithing = false;
        ClientConfig.AUTO_PERSPECTIVE_SWITCHING.set(false);
        ClientConfig.AUTO_PERSPECTIVE_SWITCHING.save();
        CameraShift.LOGGER.info("Turned off Epic Fight's Auto Perspective Switching; Camera Shift drives the perspective now");
    }
}
