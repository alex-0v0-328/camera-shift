package net.alex.camerashift;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * The client config, {@code config/camera_shift-client.toml}.
 *
 * <p>{@link #TRANSITION_SECONDS} is read each time Epic Fight's mode changes, so an edit applies from the next
 * switch on, with no restart.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

public final class ClientConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.DoubleValue TRANSITION_SECONDS = BUILDER
            .comment("Seconds the camera glides between first and third person when the Epic Fight mode changes; "
                    + "0 switches instantly")
            .defineInRange("transitionSeconds", 0.4, 0.0, 2.0);
    public static final ModConfigSpec SPEC = BUILDER.build();

    private ClientConfig() {}
}
