package io.github.kebab_hub.subspace.gametest;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.link.Endpoint;
import io.github.kebab_hub.subspace.common.link.EndpointAvailability;
import io.github.kebab_hub.subspace.common.link.LinkerSelection;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Compiled by build; execution requires an explicitly requested GameTest server run. */
@GameTestHolder(Subspace.MOD_ID)
@PrefixGameTestTemplate(false)
public final class PhaseLinkerGameTests {
    private static final BlockPos TARGET = new BlockPos(2, 1, 2);

    @GameTest(template = "empty")
    public static void selectionRetainsFacesRejectsBadClicksAndPersists(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.STONE);
        Player player = helper.makeMockPlayer();
        ItemStack stack = new ItemStack(ModRegistries.PHASE_LINKER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos pos = helper.absolutePos(TARGET);
        check(select(player, stack, pos, Direction.UP) == InteractionResult.CONSUME, "A selection failed");
        CompoundTag first = stack.getTag().copy();
        check(select(player, stack, pos, Direction.UP) == InteractionResult.FAIL, "Duplicate endpoint accepted");
        check(first.equals(stack.getTag()), "Rejected click changed A");
        check(select(player, stack, pos, Direction.DOWN) == InteractionResult.CONSUME, "Different face rejected");
        LinkerSelection pair = LinkerSelection.read(stack.getTag()).selection();
        check(pair.a().face() == Direction.UP && pair.b().face() == Direction.DOWN, "Face selection lost");
        ItemStack reloaded = ItemStack.of(stack.save(new CompoundTag()));
        check(LinkerSelection.read(reloaded.getTag()).selection().equals(pair), "Stack save/load lost selection");
        helper.setBlock(TARGET, Blocks.AIR);
        check(select(player, stack, pos, Direction.NORTH) == InteractionResult.FAIL, "Removed block accepted");
        check(LinkerSelection.read(stack.getTag()).selection().equals(pair), "Invalid third click discarded pair");
        helper.setBlock(TARGET, Blocks.STONE);
        select(player, stack, pos, Direction.NORTH);
        check(LinkerSelection.read(stack.getTag()).selection().b() == null, "Valid third click did not start fresh A");
        stack.getOrCreateTag().putString("unrelated", "keep");
        player.setShiftKeyDown(true);
        select(player, stack, pos, Direction.NORTH);
        check(LinkerSelection.read(stack.getTag()).selection().equals(LinkerSelection.EMPTY), "Sneak clear failed");
        check("keep".equals(stack.getTag().getString("unrelated")), "Clear removed unrelated data");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void unavailableAddressesNeverLoadChunks(GameTestHelper helper) {
        var world = helper.getLevel();
        BlockPos far = new BlockPos(12_000_000, world.getMinBuildHeight() + 1, 12_000_000);
        Endpoint unloaded = new Endpoint(world.dimension().location(), far, Direction.NORTH);
        check(world.getChunkSource().getChunkNow(far.getX() >> 4, far.getZ() >> 4) == null, "Test chunk already loaded");
        check(EndpointAvailability.check(world.getServer(), unloaded) == EndpointAvailability.Status.UNLOADED,
                "Unloaded address was not identified");
        check(world.getChunkSource().getChunkNow(far.getX() >> 4, far.getZ() >> 4) == null, "Status loaded a chunk");
        Endpoint outside = new Endpoint(world.dimension().location(), new BlockPos(0, world.getMaxBuildHeight(), 0), Direction.UP);
        check(EndpointAvailability.check(world.getServer(), outside) == EndpointAvailability.Status.OUT_OF_BOUNDS,
                "Invalid height accepted");
        Endpoint missing = new Endpoint(ResourceLocation.fromNamespaceAndPath("subspace", "missing_test_dimension"), far, Direction.UP);
        check(EndpointAvailability.check(world.getServer(), missing) == EndpointAvailability.Status.DIMENSION_UNAVAILABLE,
                "Missing dimension accepted");
        helper.setBlock(TARGET, Blocks.STONE);
        Player player = helper.makeMockPlayer();
        ItemStack stack = new ItemStack(ModRegistries.PHASE_LINKER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        new LinkerSelection(unloaded, null).writeTo(stack.getOrCreateTag());
        check(select(player, stack, helper.absolutePos(TARGET), Direction.SOUTH) == InteractionResult.CONSUME,
                "Unloaded A prevented B selection");
        check(LinkerSelection.read(stack.getTag()).selection().a().equals(unloaded), "Unloaded A was discarded");
        check(world.getChunkSource().getChunkNow(far.getX() >> 4, far.getZ() >> 4) == null, "B selection loaded A's chunk");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void malformedSelectionRemainsUntilExplicitClear(GameTestHelper helper) {
        helper.setBlock(TARGET, Blocks.STONE);
        Player player = helper.makeMockPlayer();
        ItemStack stack = new ItemStack(ModRegistries.PHASE_LINKER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        stack.getOrCreateTag().putString(LinkerSelection.TAG_KEY, "malformed");
        CompoundTag original = stack.getTag().copy();
        check(select(player, stack, helper.absolutePos(TARGET), Direction.UP) == InteractionResult.FAIL, "Malformed state overwritten");
        check(stack.getTag().equals(original), "Malformed data was silently changed");
        player.setShiftKeyDown(true);
        stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        check(!stack.getTag().contains(LinkerSelection.TAG_KEY), "Air sneak use failed to clear malformed state");
        helper.succeed();
    }

    private static InteractionResult select(Player player, ItemStack stack, BlockPos pos, Direction face) {
        player.getCooldowns().removeCooldown(stack.getItem());
        UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false));
        return stack.getItem().onItemUseFirst(stack, context);
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
