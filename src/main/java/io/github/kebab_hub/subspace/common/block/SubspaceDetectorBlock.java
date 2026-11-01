package io.github.kebab_hub.subspace.common.block;

import io.github.kebab_hub.subspace.common.detection.BackgroundActivity;
import io.github.kebab_hub.subspace.common.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Locale;

/** Momentary manual scans and continuous redstone activation need only block state. */
@SuppressWarnings("deprecation") // Vanilla requires these block overrides; callers use BlockState wrappers.
public final class SubspaceDetectorBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty SCANNING = BooleanProperty.create("scanning");
    public static final int SCAN_TICKS = BackgroundActivity.SAMPLE_INTERVAL_TICKS;
    private static final int ACTIVE_LIGHT = 7;

    public SubspaceDetectorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false).setValue(SCANNING, false));
    }

    public static boolean isActive(BlockState state) {
        return state.getValue(POWERED) || state.getValue(SCANNING);
    }

    public static int lightLevel(BlockState state) {
        return isActive(state) ? ACTIVE_LIGHT : 0;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, SCANNING);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean moving) {
        if (!oldState.is(this)) {
            updatePower(state, level, pos);
            // Piston/structure placement can carry SCANNING without moving the original scheduled tick.
            if (!level.isClientSide && state.getValue(SCANNING)) level.scheduleTick(pos, this, SCAN_TICKS);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor,
                                BlockPos neighborPos, boolean moving) {
        updatePower(state, level, pos);
    }

    private void updatePower(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide) return;
        boolean powered = level.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            level.setBlock(pos, state.setValue(POWERED, powered), Block.UPDATE_ALL);
            if (powered && !isActive(state)) activationSound(level, pos);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        // Sneaking leaves normal adjacent-block placement available.
        if (player.isShiftKeyDown() || hand != InteractionHand.MAIN_HAND) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            if (!isActive(state)) {
                level.setBlock(pos, state.setValue(SCANNING, true), Block.UPDATE_ALL);
                level.scheduleTick(pos, this, SCAN_TICKS);
                activationSound(level, pos);
            }
            int reading = BackgroundActivity.sample(server.getSeed(), server.dimension().location().toString(),
                    pos.getX(), pos.getY(), pos.getZ(), server.getGameTime());
            Component band = Component.translatable("message.subspace.detector."
                    + BackgroundActivity.band(reading).name().toLowerCase(Locale.ROOT));
            player.displayClientMessage(Component.translatable("message.subspace.detector.reading", reading, band)
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(SCANNING)) {
            level.setBlock(pos, state.setValue(SCANNING, false), Block.UPDATE_ALL);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!isActive(state) || random.nextInt(3) != 0) return;
        Direction facing = state.getValue(FACING);
        level.addParticle(ModParticles.SUBSPACE_SPARK.get(),
                pos.getX() + 0.5 + facing.getStepX() * 0.52,
                pos.getY() + 0.5 + random.nextDouble() * 0.12,
                pos.getZ() + 0.5 + facing.getStepZ() * 0.52,
                facing.getStepX() * 0.015, 0.01, facing.getStepZ() * 0.015);
    }

    private static void activationSound(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35F, 1.2F);
    }
}
