package io.github.kebab_hub.subspace.common.item;

import io.github.kebab_hub.subspace.common.link.Endpoint;
import io.github.kebab_hub.subspace.common.interfaceblock.InterfaceInteraction;
import io.github.kebab_hub.subspace.common.link.EndpointAvailability;
import io.github.kebab_hub.subspace.common.link.LinkerSelection;
import io.github.kebab_hub.subspace.common.registry.ModParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public final class PhaseLinkerItem extends Item {
    private static final int INPUT_COOLDOWN_TICKS = 4;

    public PhaseLinkerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isSpectator() || player.getCooldowns().isOnCooldown(this)) {
            return InteractionResult.FAIL;
        }
        if (!(context.getLevel() instanceof ServerLevel world)) return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            InteractionResult configured = InterfaceInteraction.applyLinker(world, context.getClickedPos(), player, stack);
            if (configured != null) return configured;
            clear(stack, player);
            return InteractionResult.CONSUME;
        }
        LinkerSelection.ReadResult saved = LinkerSelection.read(stack.getTag());
        if (saved.malformed()) return fail(player, "malformed");
        BlockPos pos = context.getClickedPos();
        Endpoint target = new Endpoint(world.dimension().location(), pos, context.getClickedFace());
        EndpointAvailability.Status status = EndpointAvailability.check(world.getServer(), target);
        if (status != EndpointAvailability.Status.AVAILABLE) {
            player.displayClientMessage(Component.translatable("message.subspace.phase_linker.rejected", statusText(status))
                    .withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }
        if (!world.mayInteract(player, pos)) return fail(player, "forbidden");
        LinkerSelection old = saved.selection();
        if (!old.complete() && target.equals(old.a())) return fail(player, "duplicate");
        LinkerSelection next = old.select(target);
        next.writeTo(stack.getOrCreateTag());
        player.getCooldowns().addCooldown(this, INPUT_COOLDOWN_TICKS);
        if (next.complete()) {
            // An unavailable A is retained; cross-dimension selection must not require loading it.
            player.displayClientMessage(Component.translatable("message.subspace.phase_linker.selected_b",
                    statusText(EndpointAvailability.check(world.getServer(), next.a())))
                    .withStyle(ChatFormatting.AQUA), true);
        } else {
            player.displayClientMessage(Component.translatable("message.subspace.phase_linker.selected_a",
                    pos.getX(), pos.getY(), pos.getZ(), faceText(target.face()))
                    .withStyle(ChatFormatting.LIGHT_PURPLE), true);
        }
        feedback(world, target, next.complete());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSpectator() || player.getCooldowns().isOnCooldown(this)) return InteractionResultHolder.fail(stack);
        if (level instanceof ServerLevel world) {
            if (player.isShiftKeyDown()) clear(stack, player);
            else showStatus(stack, world, player);
            player.getCooldowns().addCooldown(this, INPUT_COOLDOWN_TICKS);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        LinkerSelection.ReadResult saved = LinkerSelection.read(stack.getTag());
        if (saved.malformed()) {
            tooltip.add(Component.translatable("message.subspace.phase_linker.malformed").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(address("A", saved.selection().a()).withStyle(ChatFormatting.LIGHT_PURPLE));
            tooltip.add(address("B", saved.selection().b()).withStyle(ChatFormatting.AQUA));
            tooltip.add(nextStep(saved.selection()).withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.translatable("tooltip.subspace.phase_linker.select").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.subspace.phase_linker.status").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.subspace.phase_linker.clear").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.subspace.phase_linker.interface").withStyle(ChatFormatting.GRAY));
    }

    private void clear(ItemStack stack, Player player) {
        LinkerSelection.clear(stack.getTag());
        player.getCooldowns().addCooldown(this, INPUT_COOLDOWN_TICKS);
        player.displayClientMessage(Component.translatable("message.subspace.phase_linker.cleared"), true);
    }

    private static void showStatus(ItemStack stack, ServerLevel world, Player player) {
        LinkerSelection.ReadResult saved = LinkerSelection.read(stack.getTag());
        if (saved.malformed()) {
            fail(player, "malformed");
            return;
        }
        LinkerSelection selection = saved.selection();
        if (selection.a() != null) report(player, world, "A", selection.a());
        if (selection.b() != null) report(player, world, "B", selection.b());
        player.displayClientMessage(nextStep(selection), true);
    }

    private static void report(Player player, ServerLevel world, String slot, Endpoint endpoint) {
        player.sendSystemMessage(Component.translatable("message.subspace.phase_linker.status",
                address(slot, endpoint), statusText(EndpointAvailability.check(world.getServer(), endpoint))));
    }

    private static MutableComponent address(String slot, @Nullable Endpoint endpoint) {
        if (endpoint == null) return Component.translatable("tooltip.subspace.phase_linker.empty", slot);
        return Component.translatable("tooltip.subspace.phase_linker.address", slot, endpoint.dimension().toString(),
                endpoint.pos().getX(), endpoint.pos().getY(), endpoint.pos().getZ(), faceText(endpoint.face()));
    }

    private static Component statusText(EndpointAvailability.Status status) {
        return Component.translatable("status.subspace.phase_linker." + status.name().toLowerCase(Locale.ROOT));
    }

    private static Component faceText(Direction face) {
        return Component.translatable("face.subspace.phase_linker." + face.getName());
    }

    private static MutableComponent nextStep(LinkerSelection selection) {
        return Component.translatable("tooltip.subspace.phase_linker."
                + (selection.complete() ? "restart" : selection.a() == null ? "next_a" : "next_b"));
    }

    private static InteractionResult fail(Player player, String message) {
        player.displayClientMessage(Component.translatable("message.subspace.phase_linker." + message)
                .withStyle(ChatFormatting.RED), true);
        return InteractionResult.FAIL;
    }

    private static void feedback(ServerLevel world, Endpoint endpoint, boolean complete) {
        Direction face = endpoint.face();
        double x = endpoint.pos().getX() + 0.5 + face.getStepX() * 0.55;
        double y = endpoint.pos().getY() + 0.5 + face.getStepY() * 0.55;
        double z = endpoint.pos().getZ() + 0.5 + face.getStepZ() * 0.55;
        world.sendParticles(ModParticles.SUBSPACE_SPARK.get(), x, y, z, complete ? 12 : 8, 0.08, 0.08, 0.08, 0.015);
        world.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS,
                0.4F, complete ? 1.4F : 1.0F);
    }
}
