package io.github.kebab_hub.subspace.gametest;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.block.SubspaceInterfaceBlock;
import io.github.kebab_hub.subspace.common.interfaceblock.InterfaceInteraction;
import io.github.kebab_hub.subspace.common.interfaceblock.SubspaceInterfaceBlockEntity;
import io.github.kebab_hub.subspace.common.link.CapabilityChannels;
import io.github.kebab_hub.subspace.common.link.ConnectionState;
import io.github.kebab_hub.subspace.common.link.DirectLinkConfig;
import io.github.kebab_hub.subspace.common.link.DirectLinkInspector;
import io.github.kebab_hub.subspace.common.link.Endpoint;
import io.github.kebab_hub.subspace.common.link.LinkerSelection;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Build compiles these tests; executing them requires an explicitly authorized GameTest server. */
@GameTestHolder(Subspace.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InterfaceGameTests {
    private static final BlockPos HOST = new BlockPos(1,1,1);
    private static final BlockPos TARGET = new BlockPos(3,1,1);

    @GameTest(template = "empty")
    public static void directLinkPersistsWithoutTransportOrProxy(GameTestHelper helper) {
        helper.setBlock(HOST, ModRegistries.SUBSPACE_INTERFACE.get());
        helper.setBlock(TARGET, Blocks.CHEST);
        var host = (SubspaceInterfaceBlockEntity) helper.getBlockEntity(HOST);
        var chest = (ChestBlockEntity) helper.getBlockEntity(TARGET);
        chest.setItem(0, new ItemStack(Items.IRON_INGOT, 7));
        Endpoint target = new Endpoint(helper.getLevel().dimension().location(), helper.absolutePos(TARGET), Direction.UP);
        check(host.configure(target), "Could not configure chest endpoint");
        check(host.snapshot().state() == ConnectionState.READY && host.snapshot().available().contains(CapabilityChannels.ITEMS), "Chest capability not detected");
        helper.assertBlockProperty(HOST, SubspaceInterfaceBlock.CONNECTED, true);
        var saved = host.saveWithoutMetadata();
        host.load(saved);
        host.refresh();
        check(target.equals(host.linkConfig().target()), "Reload lost the configured target");
        for (Direction side : Direction.values()) {
            check(!host.getCapability(ForgeCapabilities.ITEM_HANDLER, side).isPresent(), "Interface exposes transport prematurely");
            check(!host.getCapability(ForgeCapabilities.FLUID_HANDLER, side).isPresent(), "Interface exposes fluid handler");
            check(!host.getCapability(ForgeCapabilities.ENERGY, side).isPresent(), "Interface exposes energy handler");
        }
        host.refresh();
        check(chest.getItem(0).getCount() == 7, "Diagnostic query moved resources");
        helper.setBlock(TARGET, Blocks.AIR);
        host.refresh();
        check(host.snapshot().state() == ConnectionState.BLOCK_MISSING && target.equals(host.linkConfig().target()), "Removed target not retained/unavailable");
        helper.setBlock(TARGET, Blocks.CHEST);
        host.refresh();
        check(host.snapshot().state() == ConnectionState.READY, "Replacement capability not reacquired");
        check(!host.configure(new Endpoint(target.dimension(), helper.absolutePos(HOST), Direction.DOWN)), "Self link accepted");
        helper.setBlock(TARGET, ModRegistries.SUBSPACE_INTERFACE.get());
        host.refresh();
        check(host.snapshot().state() == ConnectionState.CHAIN_UNSUPPORTED, "Interface chain followed");
        check(!host.configure(target), "Interface chain configured");
        host.clearLink();
        check(host.snapshot().state() == ConnectionState.UNBOUND, "Clear did not disconnect");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unavailableTargetsNeverRequestChunks(GameTestHelper helper) {
        helper.setBlock(HOST, ModRegistries.SUBSPACE_INTERFACE.get());
        var world = helper.getLevel();
        var host = (SubspaceInterfaceBlockEntity) helper.getBlockEntity(HOST);
        BlockPos far = new BlockPos(12_000_000, 64, 12_000_000);
        Endpoint target = new Endpoint(world.dimension().location(), far, Direction.NORTH);
        check(world.getChunkSource().getChunkNow(far.getX()>>4, far.getZ()>>4) == null, "Test chunk already loaded");
        check(host.configure(target), "Unavailable target address rejected");
        check(host.snapshot().state() == ConnectionState.UNLOADED, "Unloaded target not reported");
        check(world.getChunkSource().getChunkNow(far.getX()>>4, far.getZ()>>4) == null, "Inspection loaded a chunk");
        Endpoint missing = new Endpoint(ResourceLocation.fromNamespaceAndPath("subspace", "missing_test_dimension"), far, Direction.UP);
        check(DirectLinkInspector.inspect(world, helper.absolutePos(HOST), DirectLinkConfig.to(missing)).state()
                == ConnectionState.DIMENSION_UNAVAILABLE, "Missing dimension accepted");
        Endpoint outside = new Endpoint(world.dimension().location(), new BlockPos(0, world.getMaxBuildHeight(), 0), Direction.UP);
        check(DirectLinkInspector.inspect(world, helper.absolutePos(HOST), DirectLinkConfig.to(outside)).state()
                == ConnectionState.OUT_OF_BOUNDS, "Invalid build height accepted");
        helper.setBlock(TARGET, Blocks.STONE);
        Endpoint stone = new Endpoint(world.dimension().location(), helper.absolutePos(TARGET), Direction.UP);
        check(DirectLinkInspector.inspect(world, helper.absolutePos(HOST), DirectLinkConfig.to(stone)).state()
                == ConnectionState.NO_BLOCK_ENTITY, "Plain block incorrectly reports capabilities");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void linkerApplicationPreservesSelectionAndRejectsDistantUse(GameTestHelper helper) {
        helper.setBlock(HOST, ModRegistries.SUBSPACE_INTERFACE.get());
        helper.setBlock(TARGET, Blocks.CHEST);
        var world = helper.getLevel();
        var host = (SubspaceInterfaceBlockEntity) helper.getBlockEntity(HOST);
        var player = helper.makeMockPlayer();
        var pos = helper.absolutePos(HOST);
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        player.getAbilities().mayBuild = true;
        Endpoint target = new Endpoint(world.dimension().location(), helper.absolutePos(TARGET), Direction.WEST);
        ItemStack linker = new ItemStack(ModRegistries.PHASE_LINKER.get());
        new LinkerSelection(target, null).writeTo(linker.getOrCreateTag());
        var original = linker.getTag().copy();
        check(InterfaceInteraction.applyLinker(world, pos, player, linker) == InteractionResult.CONSUME, "Linker did not configure target");
        check(linker.getTag().equals(original) && host.linkConfig().target().equals(target), "Application changed the tool or target side");
        player.setPos(pos.getX()+20, pos.getY(), pos.getZ());
        check(InterfaceInteraction.applyLinker(world, pos, player, linker) == InteractionResult.FAIL, "Distant configuration accepted");
        helper.succeed();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
