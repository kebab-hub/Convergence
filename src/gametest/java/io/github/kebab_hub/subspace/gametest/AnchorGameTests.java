package io.github.kebab_hub.subspace.gametest;

import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.anchor.SubspaceAnchorBlockEntity;
import io.github.kebab_hub.subspace.common.block.SubspaceAnchorBlock;
import io.github.kebab_hub.subspace.common.menu.AnchorMenu;
import io.github.kebab_hub.subspace.common.registry.ModRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Compiled during build. A GameTest server is only run with explicit operator authorization. */
@GameTestHolder(Subspace.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AnchorGameTests {
    private static final BlockPos A = new BlockPos(1, 1, 1);
    private static final BlockPos B = new BlockPos(3, 1, 1);

    @GameTest(template = "empty")
    public static void addressPersistsAndCopiesReidentify(GameTestHelper helper) {
        helper.setBlock(A, ModRegistries.SUBSPACE_ANCHOR.get());
        helper.setBlock(B, ModRegistries.SUBSPACE_ANCHOR.get());
        var a = (SubspaceAnchorBlockEntity) helper.getBlockEntity(A);
        var b = (SubspaceAnchorBlockEntity) helper.getBlockEntity(B);
        var original = a.address();
        check(!original.equals(b.address()), "Distinct anchors share an address");
        var saved = a.saveWithoutMetadata();
        a.load(saved);
        check(original.equals(a.address()), "Save/load changed address");
        b.load(saved);
        check(!original.equals(b.address()), "Copied NBT duplicated an address");
        a.newPlacement();
        check(!original.equals(a.address()), "New placement retained old address");
        check(a.linkedEndpointCount() == 0, "An unlinked anchor reports links");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void menuToggleKeepsIdentityAndRejectsStaleTargets(GameTestHelper helper) {
        helper.setBlock(A, ModRegistries.SUBSPACE_ANCHOR.get());
        var anchor = (SubspaceAnchorBlockEntity) helper.getBlockEntity(A);
        var id = anchor.address();
        var player = helper.makeMockPlayer();
        var pos = helper.absolutePos(A);
        player.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        player.getAbilities().mayBuild = true;
        var menu = new AnchorMenu(1, player.getInventory(), anchor);
        check(menu.address().equals(id), "Menu address differs from saved identity");
        check(!menu.clickMenuButton(player, 100), "Unknown button accepted");
        check(menu.clickMenuButton(player, AnchorMenu.TOGGLE), "Toggle rejected nearby player");
        helper.assertBlockProperty(A, SubspaceAnchorBlock.ACTIVE, false);
        check(anchor.address().equals(id), "Activation changed address");
        var saved = anchor.saveWithoutMetadata();
        anchor.load(saved);
        check(anchor.address().equals(id) && !anchor.isActive(), "Reload lost inactive state/address");
        player.setPos(pos.getX() + 20, pos.getY(), pos.getZ());
        check(!menu.stillValid(player) && !menu.clickMenuButton(player, AnchorMenu.TOGGLE), "Distant control accepted");
        player.setPos(pos.getX(), pos.getY(), pos.getZ());
        helper.setBlock(A, Blocks.AIR);
        helper.setBlock(A, ModRegistries.SUBSPACE_ANCHOR.get());
        check(!menu.stillValid(player) && !menu.clickMenuButton(player, AnchorMenu.TOGGLE), "Stale screen controls replacement");
        helper.succeed();
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
