package net.alex.camerashift;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * Entry point of Camera Shift, a client-only add-on for Epic Fight: mining mode ends in first person, battle mode
 * in the third-person back view, and the camera glides between them ({@link PerspectiveController}).
 *
 * <p>Annotated {@code @Mod(dist = Dist.CLIENT)}, so nothing loads on a dedicated server. Holds the {@code MOD_ID}
 * constant and the shared {@code LOGGER}, and registers the client config {@link ClientConfig}.
 *
 * @author Alex
 * @version 1.0.0
 * @since 1.0.0
 */

@Mod(value = CameraShift.MOD_ID, dist = Dist.CLIENT)
public class CameraShift {

    public static final String MOD_ID = "camera_shift";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CameraShift(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
    }
}
