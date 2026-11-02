package io.github.kebab_hub.subspace.gametest;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.block.SubspaceInterfaceBlock;
import io.github.kebab_hub.subspace.common.config.SubspaceConfig;
import io.github.kebab_hub.subspace.common.interfaceblock.InterfaceInteraction;
import io.github.kebab_hub.subspace.common.interfaceblock.SubspaceInterfaceBlockEntity;
import io.github.kebab_hub.subspace.common.link.ConnectionState;
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
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;

import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Compiled by build; owner may run with the default 64-item server limit in a separate test world. */
@GameTestHolder(Subspace.MOD_ID)
@PrefixGameTestTemplate(false)
public final class InterfaceGameTests {
    private static final BlockPos A = new BlockPos(1,2,1);
    private static final BlockPos B = new BlockPos(3,2,1);
    private static final BlockPos CHEST_A = A.below();
    private static final BlockPos CHEST_B = B.below();

    private static Endpoint endpoint(GameTestHelper helper, BlockPos relative, Direction face) {
        return new Endpoint(helper.getLevel().dimension().location(), helper.absolutePos(relative), face);
    }
    private static SubspaceInterfaceBlockEntity host(GameTestHelper helper, BlockPos pos) {
        return (SubspaceInterfaceBlockEntity) helper.getBlockEntity(pos);
    }
    private static ChestBlockEntity chest(GameTestHelper helper, BlockPos pos) {
        return (ChestBlockEntity) helper.getBlockEntity(pos);
    }
    private static void setup(GameTestHelper helper) {
        helper.setBlock(A, ModRegistries.SUBSPACE_INTERFACE.get());
        helper.setBlock(B, ModRegistries.SUBSPACE_INTERFACE.get());
        helper.setBlock(CHEST_A, Blocks.CHEST);
        helper.setBlock(CHEST_B, Blocks.CHEST);
        check(host(helper,A).configure(endpoint(helper,B,Direction.DOWN)), "A config failed");
        check(host(helper,B).configure(endpoint(helper,A,Direction.DOWN)), "B config failed");
        host(helper,A).refresh();
        check(SubspaceConfig.ITEMS_PER_TICK.get() == 64, "Run fixtures with the default 64 item limit");
    }
    private static IItemHandler items(SubspaceInterfaceBlockEntity host, Direction face) {
        return host.getCapability(ForgeCapabilities.ITEM_HANDLER, face).orElseThrow(() -> new AssertionError("No item view"));
    }

    @GameTest(template = "empty")
    public static void pairConservesItemsAndSharesBudgetAcrossBothEnds(GameTestHelper helper) {
        setup(helper);
        var a = host(helper,A);
        var b = host(helper,B);
        var remote = chest(helper,CHEST_B);
        var local = chest(helper,CHEST_A);
        var view = items(a, Direction.UP);
        ItemStack input = new ItemStack(Items.IRON_INGOT, 64);
        input.getOrCreateTag().putString("subspace_test", "retain");
        remote.setItem(0, input.copy());
        remote.getItem(0).setCount(60);
        ItemStack simulated = view.insertItem(0, input, true);
        check(simulated.getCount() == 60 && remote.getItem(0).getCount() == 60 && a.itemBudget().used() == 0, "Simulation committed");
        remote.getItem(0).setCount(62); // Simulation is advisory.
        ItemStack left = view.insertItem(0, input, false);
        check(left.getCount() == 62 && remote.getItem(0).getCount() == 64 && input.getCount() == 64, "Partial insertion lost/duplicated items");
        check(left.getTag().equals(input.getTag()), "Remainder lost metadata");
        ItemStack read = view.getStackInSlot(0);
        read.setCount(1);
        check(remote.getItem(0).getCount() == 64, "Mutable inventory alias leaked");
        check(view.extractItem(0, 10, true).getCount() == 10 && remote.getItem(0).getCount() == 64, "Extraction simulation mutated");
        ItemStack pulled = view.extractItem(0, 10, false);
        check(pulled.getCount() == 10 && remote.getItem(0).getCount() == 54, "Extraction conservation failed");
        local.setItem(0, new ItemStack(Items.IRON_INGOT, 64));
        check(items(b,Direction.NORTH).extractItem(0, 64, false).getCount() == 52, "Opposite direction bypassed budget");
        check(items(a,Direction.EAST).insertItem(1, input, false).getCount() == 64, "New face bypassed budget");
        check(a.itemBudget().used() == 64 && b.itemBudget().used() == 64, "Pair accounting disagrees");
        var saved = a.saveWithoutMetadata();
        var old = view;
        a.load(saved);
        check(old.getSlots() == 0, "Old raw reference survived load");
        check(items(a,Direction.UP).extractItem(0, 1, false).isEmpty(), "Reload reset same-tick budget");
        for (Direction face : Direction.values()) {
            check(!a.getCapability(ForgeCapabilities.FLUID_HANDLER,face).isPresent(), "Fluid forwarding added");
            check(!a.getCapability(ForgeCapabilities.ENERGY,face).isPresent(), "Energy forwarding added");
        }
        check(!a.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.DOWN).isPresent(), "Local inventory port exposed remote inventory");
        check(!a.getCapability(ForgeCapabilities.ITEM_HANDLER,null).isPresent(), "Unsided access bypassed port policy");
        helper.assertBlockProperty(A, SubspaceInterfaceBlock.CONNECTED, true);
        helper.runAfterDelay(1, () -> {
            check(items(a,Direction.UP).extractItem(0, 1, false).getCount() == 1, "Next tick failed to replenish");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void invalidationReplacementAndClearRevokeStaleViews(GameTestHelper helper) {
        setup(helper);
        var a = host(helper,A);
        var b = host(helper,B);
        var cap = a.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP);
        var view = items(a,Direction.UP);
        var oldChest = chest(helper,CHEST_B);
        oldChest.setItem(0, new ItemStack(Items.GOLD_INGOT, 8));
        // Invalidate the advertised handle without asking the chest to create its replacement yet.
        oldChest.getCapability(ForgeCapabilities.ITEM_HANDLER,Direction.UP).invalidate();
        check(view.extractItem(0, 4, false).isEmpty(), "Invalidated destination remained accessible");
        oldChest.invalidateCaps();
        oldChest.reviveCaps();
        check(view.extractItem(0, 1, true).getCount() == 1, "Revived provider not reacquired");
        helper.setBlock(CHEST_B,Blocks.AIR);
        check(view.extractItem(0,1,false).isEmpty() && view.insertItem(0,new ItemStack(Items.GOLD_INGOT,4),false).getCount() == 4, "Missing destination lost items");
        helper.setBlock(CHEST_B,Blocks.CHEST);
        check(view.getSlots() > 0 && view.getStackInSlot(0).isEmpty(), "Replacement retained old handler");
        b.invalidateCaps();
        check(view.getSlots() == 0, "Unloading peer remained accessible");
        b.reviveCaps();
        check(view.getSlots() > 0, "Peer revival did not recover");
        a.clearLink();
        check(!cap.isPresent() && view.getSlots() == 0, "Clear did not revoke optional and raw handler");
        check(view.insertItem(0,new ItemStack(Items.IRON_INGOT,3),false).getCount() == 3, "Revoked handler deleted items");
        a.configure(endpoint(helper,B,Direction.DOWN));
        check(view.getSlots() == 0 && items(a,Direction.UP).getSlots() > 0, "Retarget revived obsolete view");
        var beforeUnload = items(a,Direction.UP);
        a.onChunkUnloaded();
        check(beforeUnload.getSlots() == 0, "Unloaded source retained access");
        a.onLoad();
        check(beforeUnload.getSlots() == 0 && items(a,Direction.UP).getSlots() > 0, "Reused block entity did not revive safely");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void sidedFurnaceAndInterfaceLoops(GameTestHelper helper) {
        setup(helper);
        var a = host(helper,A);
        var b = host(helper,B);
        helper.setBlock(CHEST_B,Blocks.FURNACE);
        var view = items(a,Direction.UP); // B's DOWN port reaches the furnace's UP face.
        check(view.insertItem(0,new ItemStack(Items.IRON_ORE),false).isEmpty(), "Furnace top rejected smelting input");
        helper.setBlock(CHEST_B,ModRegistries.SUBSPACE_INTERFACE.get());
        a.refresh();
        check(a.snapshot().state() == ConnectionState.CHAIN_UNSUPPORTED && view.getSlots() == 0, "Third interface chain followed");
        b.configure(endpoint(helper,CHEST_B,Direction.UP));
        a.refresh();
        check(a.snapshot().state() == ConnectionState.UNPAIRED, "Nonreciprocal peer accepted");
        check(!a.configure(endpoint(helper,A,Direction.DOWN)), "Self link accepted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void allSixPortsUseOppositeInventoryFaces(GameTestHelper helper) {
        helper.setBlock(A,ModRegistries.SUBSPACE_INTERFACE.get());
        BlockPos center = new BlockPos(3,2,3);
        helper.setBlock(center,ModRegistries.SUBSPACE_INTERFACE.get());
        var a = host(helper,A);
        var b = host(helper,center);
        b.configure(endpoint(helper,A,Direction.DOWN));
        for (Direction face : Direction.values()) {
            BlockPos destination = center.relative(face);
            helper.setBlock(destination,Blocks.FURNACE);
            a.configure(endpoint(helper,center,face));
            IItemHandler view = items(a,Direction.UP);
            check(view.getSlots() == (face == Direction.UP ? 2 : 1), "Wrong sided furnace slots for " + face);
            if (face.getAxis().isHorizontal()) {
                check(view.insertItem(0,new ItemStack(Items.IRON_ORE),false).getCount() == 1, "Furnace side accepted nonfuel");
                check(view.insertItem(0,new ItemStack(Items.COAL),false).isEmpty(), "Furnace side rejected fuel");
            }
            helper.setBlock(destination,Blocks.AIR);
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unavailableTargetsNeverRequestChunks(GameTestHelper helper) {
        setup(helper);
        var world = helper.getLevel();
        var a = host(helper,A);
        BlockPos far = new BlockPos(12_000_000,64,12_000_000);
        check(world.getChunkSource().getChunkNow(far.getX()>>4,far.getZ()>>4) == null,"Fixture chunk already loaded");
        a.configure(new Endpoint(world.dimension().location(),far,Direction.NORTH));
        check(a.snapshot().state() == ConnectionState.UNLOADED, "Unloaded peer not reported");
        var view = items(a,Direction.UP);
        check(view.getSlots() == 0 && view.extractItem(0,64,false).isEmpty(), "Unloaded peer served items");
        check(view.insertItem(0,new ItemStack(Items.IRON_INGOT,17),false).getCount() == 17,"Unloaded insertion lost items");
        check(world.getChunkSource().getChunkNow(far.getX()>>4,far.getZ()>>4) == null,"Operation loaded destination chunk");
        a.configure(new Endpoint(ResourceLocation.fromNamespaceAndPath("subspace","missing_test_dimension"),far,Direction.UP));
        check(a.snapshot().state() == ConnectionState.DIMENSION_UNAVAILABLE,"Missing dimension accepted");
        a.configure(new Endpoint(world.dimension().location(),new BlockPos(0,world.getMaxBuildHeight(),0),Direction.UP));
        check(a.snapshot().state() == ConnectionState.OUT_OF_BOUNDS,"Outside build bounds accepted");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void linkerRequiresLocalConfirmationAtBothEnds(GameTestHelper helper) {
        setup(helper);
        var a = host(helper,A);
        var b = host(helper,B);
        a.clearLink();
        b.clearLink();
        var player = helper.makeMockPlayer();
        player.getAbilities().mayBuild = true;
        ItemStack linker = new ItemStack(ModRegistries.PHASE_LINKER.get());
        new LinkerSelection(endpoint(helper,A,Direction.DOWN),endpoint(helper,B,Direction.DOWN)).writeTo(linker.getOrCreateTag());
        var original = linker.getTag().copy();
        BlockPos posA = helper.absolutePos(A);
        BlockPos posB = helper.absolutePos(B);
        player.setPos(posA.getX(),posA.getY(),posA.getZ());
        check(InterfaceInteraction.applyLinker(helper.getLevel(),posA,player,linker) == InteractionResult.CONSUME,"A confirmation failed");
        check(a.snapshot().state() == ConnectionState.UNPAIRED && b.linkConfig().target() == null,"One visit silently configured remote end");
        player.setPos(posB.getX(),posB.getY(),posB.getZ());
        check(InterfaceInteraction.applyLinker(helper.getLevel(),posB,player,linker) == InteractionResult.CONSUME,"B confirmation failed");
        a.refresh();
        check(a.snapshot().state() == ConnectionState.READY && linker.getTag().equals(original),"Pair/tool configuration incorrect");
        player.setPos(posB.getX()+20,posB.getY(),posB.getZ());
        check(InterfaceInteraction.applyLinker(helper.getLevel(),posB,player,linker) == InteractionResult.FAIL,"Distant confirmation accepted");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 160)
    public static void hopperInsertsThroughPair(GameTestHelper helper) {
        setup(helper);
        BlockPos hopperPos = A.above();
        helper.setBlock(hopperPos,Blocks.HOPPER.defaultBlockState().setValue(HopperBlock.FACING,Direction.DOWN));
        var hopper = (HopperBlockEntity) helper.getBlockEntity(hopperPos);
        hopper.setItem(0,new ItemStack(Items.IRON_INGOT,8));
        helper.succeedWhen(() -> {
            int held = 0;
            for (int slot=0;slot<hopper.getContainerSize();slot++) held += hopper.getItem(slot).getCount();
            int received = 0;
            var remote = chest(helper,CHEST_B);
            for (int slot=0;slot<remote.getContainerSize();slot++) received += remote.getItem(slot).getCount();
            check(held + received == 8,"Hopper transfer did not conserve items");
            if (received != 8) helper.fail("Hopper has not delivered all items yet");
        });
    }
    private static void check(boolean condition,String message) { if (!condition) throw new AssertionError(message); }
}
