package io.github.kebab_hub.subspace.common.registry;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.block.FacingDecorativeBlock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

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

    private static final int CASING_LIGHT_LEVEL = 7;
    public static final RegistryObject<Block> SUBSPACE_TEST_CASING = BLOCKS.register("subspace_test_casing",
            () -> new FacingDecorativeBlock(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(3.5F).sound(SoundType.METAL).requiresCorrectToolForDrops()
                    .lightLevel(state -> CASING_LIGHT_LEVEL)));
    public static final RegistryObject<Item> SUBSPACE_TEST_CASING_ITEM = ITEMS.register("subspace_test_casing",
            () -> new BlockItem(SUBSPACE_TEST_CASING.get(), new Item.Properties().tab(TAB)));

    private ModRegistries() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        ModParticles.register(modBus);
    }
}
