package io.github.kebab_hub.convergence.gametest;

import io.github.kebab_hub.convergence.Convergence;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Convergence.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BootstrapGameTests {
    @GameTest(template = "empty")
    public static void entrypointLoadsOnDedicatedServer(GameTestHelper helper) {
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            helper.fail("Bootstrap smoke test must run on a dedicated server");
        }
        if (ModList.get().getModContainerById(Convergence.MOD_ID)
                        .filter(container -> container.getMod() instanceof Convergence)
                        .isEmpty()) {
            helper.fail("Convergence mod container must hold the initialized entrypoint");
        }
        helper.succeed();
    }
}
