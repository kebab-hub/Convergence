package io.github.kebab_hub.subspace.common.registry;

import io.github.kebab_hub.subspace.Subspace;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class ModRegistries {
    private static final ResourceLocation TAB_TEXTURE = Subspace.id("textures/gui/creative_tabs.png");

    // Forge 1.19.2 registers tabs through their constructor. The entrypoint initializes
    // this class even while the content registries are empty.
    public static final CreativeModeTab TAB = new CreativeModeTab(Subspace.MOD_ID) {
        @Override
        public ItemStack makeIcon() {
            // The original emblem is baked into the tab atlas; no placeholder item is needed.
            return ItemStack.EMPTY;
        }

        @Override
        public ResourceLocation getTabsImage() {
            return TAB_TEXTURE;
        }
    };

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, Subspace.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Subspace.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Subspace.MOD_ID);
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, Subspace.MOD_ID);

    private ModRegistries() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
    }
}
