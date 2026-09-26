package net.alex.camerashift;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(value = CameraShift.MODID, dist = Dist.CLIENT)
public class CameraShift {
    public static final String MODID = "camerashift";
    public static final Logger LOGGER = LogUtils.getLogger();

    public CameraShift(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, CameraShiftConfig.SPEC);
    }
}
