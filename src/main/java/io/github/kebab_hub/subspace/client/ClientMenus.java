package io.github.kebab_hub.subspace.client;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.client.screen.AnchorScreen;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Subspace.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientMenus {
    private ClientMenus() { }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> MenuScreens.register(ModRegistries.ANCHOR_MENU.get(), AnchorScreen::new));
    }
}
