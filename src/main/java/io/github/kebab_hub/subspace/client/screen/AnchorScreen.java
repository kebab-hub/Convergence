package io.github.kebab_hub.subspace.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.kebab_hub.subspace.Subspace;
import io.github.kebab_hub.subspace.common.menu.AnchorMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class AnchorScreen extends AbstractContainerScreen<AnchorMenu> {
    private static final ResourceLocation TEXTURE = Subspace.id("textures/gui/anchor.png");
    private Button toggle;

    public AnchorScreen(AnchorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 236;
        imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        toggle = addRenderableWidget(new Button(leftPos + 48, topPos + 128, 140, 20,
                Component.empty(), button -> {
                    if (minecraft != null && minecraft.gameMode != null) {
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId, AnchorMenu.TOGGLE);
                    }
                }));
        updateButton();
    }

    private void updateButton() {
        toggle.active = menu.synced() && minecraft.player != null && !minecraft.player.isSpectator() && minecraft.player.mayBuild();
        toggle.setMessage(Component.translatable("screen.subspace.anchor." + (menu.active() ? "deactivate" : "activate")));
    }

    @Override
    public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
        updateButton();
        renderBackground(pose);
        super.render(pose, mouseX, mouseY, partialTick);
        renderTooltip(pose, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(pose, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
        font.draw(pose, title, 16, 13, 0xFFE19B);
        font.draw(pose, Component.translatable("screen.subspace.anchor.address"), 16, 37, 0x8D9CB5);
        if (menu.synced()) {
            String address = menu.address().toString();
            font.draw(pose, address.substring(0, 18), 20, 52, 0xEACDFF);
            font.draw(pose, address.substring(18), 20, 63, 0xEACDFF);
        } else {
            font.draw(pose, Component.translatable("screen.subspace.anchor.syncing"), 20, 52, 0x8D9CB5);
        }
        Component status = Component.translatable("screen.subspace.anchor."
                + (!menu.synced() ? "syncing" : menu.active() ? "active" : "inactive"));
        font.draw(pose, Component.translatable("screen.subspace.anchor.status", status), 16, 87,
                menu.active() ? 0xA4F8FF : 0x8D9CB5);
        font.draw(pose, Component.translatable("screen.subspace.anchor.links", menu.linkedEndpointCount()), 16, 105, 0xEACDFF);
    }
}
