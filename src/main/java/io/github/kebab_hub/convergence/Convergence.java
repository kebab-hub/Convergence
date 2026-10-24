package io.github.kebab_hub.convergence;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Convergence.MOD_ID)
public final class Convergence {
    public static final String MOD_ID = "convergence";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Convergence() {
        LOGGER.info("Initializing Convergence");
    }
}
