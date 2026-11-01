package io.github.kebab_hub.subspace.common.registry;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.anchor.SubspaceAnchorBlockEntity;
import io.github.kebab_hub.subspace.common.block.SubspaceAnchorBlock;
import io.github.kebab_hub.subspace.common.menu.AnchorMenu;
import io.github.kebab_hub.subspace.common.item.PhaseLinkerItem;
import io.github.kebab_hub.subspace.common.block.FacingDecorativeBlock;
import io.github.kebab_hub.subspace.common.block.SubspaceDetectorBlock;
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
import net.minecraftforge.common.extensions.IForgeMenuType;
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

    public static final RegistryObject<Item> PHASE_CRYSTAL = component("phase_crystal");
    public static final RegistryObject<Item> STABILIZED_PHASE_CRYSTAL = component("stabilized_phase_crystal");
    public static final RegistryObject<Item> RESONANCE_COIL = component("resonance_coil");
    public static final RegistryObject<Item> PHASE_ALLOY_INGOT = component("phase_alloy_ingot");
    public static final RegistryObject<Item> PHASE_ALLOY_PLATE = component("phase_alloy_plate");
    public static final RegistryObject<Item> PHASE_LINKER = ITEMS.register("phase_linker",
            () -> new PhaseLinkerItem(new Item.Properties().tab(TAB)));

    public static final RegistryObject<Block> SUBSPACE_DETECTOR = BLOCKS.register("subspace_detector",
            () -> new SubspaceDetectorBlock(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(3.5F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()
                    .lightLevel(SubspaceDetectorBlock::lightLevel)));
    public static final RegistryObject<Item> SUBSPACE_DETECTOR_ITEM = ITEMS.register("subspace_detector",
            () -> new BlockItem(SUBSPACE_DETECTOR.get(), new Item.Properties().tab(TAB)));

    public static final RegistryObject<Block> SUBSPACE_ANCHOR = BLOCKS.register("subspace_anchor",
            () -> new SubspaceAnchorBlock(BlockBehaviour.Properties.of(Material.METAL)
                    .strength(4.5F).sound(SoundType.METAL).requiresCorrectToolForDrops().noOcclusion()
                    .lightLevel(state -> state.getValue(SubspaceAnchorBlock.ACTIVE) ? 10 : 0)));
    public static final RegistryObject<Item> SUBSPACE_ANCHOR_ITEM = ITEMS.register("subspace_anchor",
            () -> new BlockItem(SUBSPACE_ANCHOR.get(), new Item.Properties().tab(TAB)));
    public static final RegistryObject<BlockEntityType<SubspaceAnchorBlockEntity>> SUBSPACE_ANCHOR_ENTITY =
            BLOCK_ENTITIES.register("subspace_anchor", () -> BlockEntityType.Builder.of(
                    SubspaceAnchorBlockEntity::new, SUBSPACE_ANCHOR.get()).build(null));
    public static final RegistryObject<MenuType<AnchorMenu>> ANCHOR_MENU =
            MENUS.register("subspace_anchor", () -> IForgeMenuType.create(AnchorMenu::new));

    private ModRegistries() {
    }

    private static RegistryObject<Item> component(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties().tab(TAB)));
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
        MENUS.register(modBus);
        ModParticles.register(modBus);
    }
}
