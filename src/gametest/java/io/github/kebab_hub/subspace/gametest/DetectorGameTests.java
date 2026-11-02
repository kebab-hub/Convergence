package io.github.kebab_hub.subspace.gametest;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.block.SubspaceDetectorBlock;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Compiled by build; run only when an operator explicitly launches the GameTest server. */
@GameTestHolder(Subspace.MOD_ID)
@PrefixGameTestTemplate(false)
public final class DetectorGameTests {
    private static final BlockPos DETECTOR = new BlockPos(2, 1, 2);
    private static final BlockPos POWER = new BlockPos(3, 1, 2);

    @GameTest(template = "empty", timeoutTicks = 130)
    public static void manualScanExpiresDespiteRepeatedReads(GameTestHelper helper) {
        helper.setBlock(DETECTOR, ModRegistries.SUBSPACE_DETECTOR.get());
        read(helper);
        helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.SCANNING, true);
        helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.POWERED, false);
        helper.runAtTickTime(20, () -> read(helper));
        helper.runAtTickTime(110, () -> {
            helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.SCANNING, false);
            helper.assertBlockState(DETECTOR, state -> !SubspaceDetectorBlock.isActive(state),
                    () -> "Manual scan must expire without redstone");
            helper.succeed();
        });
    }

    @GameTest(template = "empty", timeoutTicks = 130)
    public static void redstoneAndManualScanRemainIndependent(GameTestHelper helper) {
        helper.setBlock(DETECTOR, ModRegistries.SUBSPACE_DETECTOR.get());
        read(helper);
        helper.runAtTickTime(10, () -> helper.setBlock(POWER, Blocks.REDSTONE_BLOCK));
        helper.runAtTickTime(20, () -> {
            helper.setBlock(POWER, Blocks.AIR);
            helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.POWERED, false);
            helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.SCANNING, true);
        });
        helper.runAtTickTime(30, () -> helper.setBlock(POWER, Blocks.REDSTONE_BLOCK));
        helper.runAtTickTime(110, () -> {
            helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.SCANNING, false);
            helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.POWERED, true);
            helper.assertBlockState(DETECTOR, SubspaceDetectorBlock::isActive,
                    () -> "Redstone must hold activation after the manual timer ends");
            helper.setBlock(POWER, Blocks.AIR);
            helper.assertBlockState(DETECTOR, state -> !SubspaceDetectorBlock.isActive(state),
                    () -> "Removing redstone must deactivate an expired detector");
            helper.succeed();
        });
    }

    @GameTest(template = "empty")
    public static void placementSeesExistingRedstone(GameTestHelper helper) {
        helper.setBlock(POWER, Blocks.REDSTONE_BLOCK);
        helper.setBlock(DETECTOR, ModRegistries.SUBSPACE_DETECTOR.get());
        helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.POWERED, true);
        helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.SCANNING, false);
        helper.setBlock(POWER, Blocks.AIR);
        helper.assertBlockProperty(DETECTOR, SubspaceDetectorBlock.POWERED, false);
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 150)
    public static void relocatedScanStateGetsAnExpiry(GameTestHelper helper) {
        BlockPos destination = new BlockPos(0, 1, 2);
        helper.setBlock(DETECTOR, ModRegistries.SUBSPACE_DETECTOR.get());
        read(helper);
        helper.runAtTickTime(20, () -> {
            // Moving/structure placement carries state, but not the old position's scheduled tick.
            helper.setBlock(destination, helper.getLevel().getBlockState(helper.absolutePos(DETECTOR)));
            helper.setBlock(DETECTOR, Blocks.AIR);
            helper.assertBlockProperty(destination, SubspaceDetectorBlock.SCANNING, true);
        });
        helper.runAtTickTime(130, () -> {
            helper.assertBlockProperty(destination, SubspaceDetectorBlock.SCANNING, false);
            helper.succeed();
        });
    }

    private static void read(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(DETECTOR);
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), helper.makeMockPlayer(),
                InteractionHand.MAIN_HAND, new BlockHitResult(Vec3.atCenterOf(pos), Direction.NORTH, pos, false));
    }
}
