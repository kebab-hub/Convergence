package io.github.kebab_hub.subspace.common.interfaceblock;

import io.github.kebab_hub.subspace.common.link.CapabilityChannels;
import io.github.kebab_hub.subspace.common.link.Endpoint;
import io.github.kebab_hub.subspace.common.link.LinkerSelection;
import io.github.kebab_hub.subspace.common.transport.DirectItemLink;
import io.github.kebab_hub.subspace.common.config.SubspaceConfig;
import net.minecraft.core.Direction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;
import java.util.Locale;

/** Tool integration and localized interaction text, separate from the reusable link services. */
public final class InterfaceInteraction {
    private InterfaceInteraction() { }

    @Nullable
    public static InteractionResult applyLinker(ServerLevel world, BlockPos pos, Player player, ItemStack linker) {
        LevelChunk chunk = world.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null || !(chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK)
                instanceof SubspaceInterfaceBlockEntity host)) return null;
        if (!canConfigure(player, world, pos)) return failure(player, "forbidden");
        var saved = LinkerSelection.read(linker.getTag());
        if (saved.malformed() || saved.selection().a() == null) return failure(player, "select_a");
        Endpoint target = saved.selection().a();
        Endpoint own = new Endpoint(world.dimension().location(), pos, Direction.UP);
        // Configure only the end the player is visiting; both ends require a permitted local interaction.
        if (saved.selection().b() != null) {
            if (DirectItemLink.sameLocation(own, saved.selection().a())) target = saved.selection().b();
            else if (!DirectItemLink.sameLocation(own, saved.selection().b())) return failure(player, "visit_pair");
        }
        if (!host.configure(target)) return failure(player, "invalid_link");
        player.getCooldowns().addCooldown(linker.getItem(), 4);
        world.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.35F, 1.1F);
        status(host, player);
        return InteractionResult.CONSUME;
    }

    public static boolean canConfigure(Player player, Level world, BlockPos pos) {
        return player.level == world && !player.isSpectator() && player.mayBuild() && world.mayInteract(player, pos)
                && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64;
    }

    public static void status(SubspaceInterfaceBlockEntity host, Player player) {
        host.refresh();
        Endpoint target = host.linkConfig().target();
        if (target != null) {
            player.sendSystemMessage(Component.translatable("message.subspace.interface.target", target.dimension().toString(),
                    target.pos().getX(), target.pos().getY(), target.pos().getZ(),
                    Component.translatable("face.subspace.phase_linker." + target.face().getName())));
        }
        player.displayClientMessage(Component.translatable("message.subspace.interface.state",
                Component.translatable("state.subspace.interface." + host.snapshot().state().name().toLowerCase(Locale.ROOT)))
                .withStyle(ChatFormatting.AQUA), true);
        for (CapabilityChannels.Channel<?> channel : java.util.List.of(CapabilityChannels.ITEMS)) {
            player.sendSystemMessage(Component.translatable("message.subspace.interface.channel",
                    Component.translatable("channel.subspace." + channel.id()),
                    Component.translatable(host.snapshot().available().contains(channel)
                            ? "message.subspace.interface.available" : "message.subspace.interface.unavailable")));
        }
        player.sendSystemMessage(Component.translatable("message.subspace.interface.limit", SubspaceConfig.ITEMS_PER_TICK.get()));
        player.sendSystemMessage(Component.translatable("message.subspace.interface.help").withStyle(ChatFormatting.GRAY));
    }

    private static InteractionResult failure(Player player, String key) {
        player.displayClientMessage(Component.translatable("message.subspace.interface." + key).withStyle(ChatFormatting.RED), true);
        return InteractionResult.FAIL;
    }
}
