package net.alex.camerashift;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class CameraShiftConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue TRANSITION_SECONDS = BUILDER
            .comment("Seconds the camera glides between first and third person when the Epic Fight mode changes; 0 switches instantly")
            .defineInRange("transitionSeconds", 0.4, 0.0, 2.0);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private CameraShiftConfig() {
    }
}
