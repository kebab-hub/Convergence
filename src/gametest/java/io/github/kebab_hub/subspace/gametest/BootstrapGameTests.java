package io.github.kebab_hub.subspace.gametest;

import io.github.kebab_hub.subspace.Subspace;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Subspace.MOD_ID)
@PrefixGameTestTemplate(false)
public final class BootstrapGameTests {
    @GameTest(template = "empty")
    public static void entrypointLoadsOnDedicatedServer(GameTestHelper helper) {
        if (FMLEnvironment.dist != Dist.DEDICATED_SERVER) {
            helper.fail("Bootstrap smoke test must run on a dedicated server");
        }
        if (ModList.get().getModContainerById(Subspace.MOD_ID)
                        .filter(container -> container.getMod() instanceof Subspace)
                        .isEmpty()) {
            helper.fail("Subspace mod container must hold the initialized entrypoint");
        }
        helper.succeed();
    }
}
