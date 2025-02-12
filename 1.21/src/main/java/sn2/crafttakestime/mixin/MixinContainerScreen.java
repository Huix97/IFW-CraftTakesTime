package sn2.crafttakestime.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import sn2.crafttakestime.ITimeCraftGuiContainer;
import sn2.crafttakestime.common.config.ContainerConfig;
import sn2.crafttakestime.common.core.CraftManager;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinContainerScreen<T extends AbstractContainerMenu> extends Screen implements ITimeCraftGuiContainer {
    @Unique
    private static final Logger ifw_log = LogManager.getLogger(MixinContainerScreen.class);
    @Unique
    private final AbstractContainerScreen<T> ifw_self = (AbstractContainerScreen<T>) (Object) this;
    @Unique
    private boolean ifw_finished = false;
    @Unique
    private Inventory ifw_playerInventory;
    @Inject(at = @At("RETURN"), method = "<init>")
    private void initInv(AbstractContainerMenu menu, Inventory playerInventory, Component title, CallbackInfo ci) {
        this.ifw_playerInventory = playerInventory;
    }

    @Unique
    @Override
    public void handleCraftFinished(Slot slotIn, int slotId) {
        this.ifw_finished = true;
        this.slotClicked(slotIn, slotId, 0, ClickType.PICKUP);
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void crafttakestime$render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        try {
            CraftManager manager = CraftManager.getInstance();
            ContainerConfig containerConfig = manager.getCraftContainerConfig();

            // Skip if the properties are not set for this container
            if (!containerConfig.isEnabled()) return;

            // Skip if the player is not crafting
            if (!manager.isCrafting()) return;

            // Draw the crafting overlay
            if (containerConfig.isDrawCraftingOverlay() && manager.getCraftPeriod() > 0) {
                ResourceLocation craftOverlay = ResourceLocation.bySeparator(containerConfig.getOverlayTexture(), ':');
                float percentage = manager.getCurrentCraftTime() / manager.getCraftPeriod();
                int progressWidth = (int) (percentage * containerConfig.getOverlayWidth());
                guiGraphics.blit(
                        craftOverlay,
                        this.leftPos + containerConfig.getOverlayX(),
                        this.topPos + containerConfig.getOverlayY(),
                        0.0F, 0.0F,
                        progressWidth,
                        containerConfig.getOverlayHeight(),
                        containerConfig.getOverlayWidth(),
                        containerConfig.getOverlayHeight());
            }
        } catch (Exception e) {
            ifw_log.error("Failed to draw the crafting overlay", e);
        }
    }

    @Inject(method = "slotClicked", at = @At("HEAD"), cancellable = true)
    public void crafttakestime$handleMouseClick(Slot slot, int slotId, int clickData, ClickType actionType, CallbackInfo info) {
        // Skip if the player is just finished crafting, so that the player can pick up the result item
        if (this.ifw_finished) {
            this.ifw_finished = false;
            return;
        }
        try {
            if (CraftManager.getInstance().initCraft(this.ifw_self, slotId))
                info.cancel();
        } catch (Exception e) {
            ifw_log.error("Failed to handle the mouse click", e);
        }
    }

    @Inject(method = "onClose", at = @At("TAIL"))
    public void crafttakestime$onClose(CallbackInfo ci) {
        CraftManager.getInstance().unsetGuiContainer();
    }

    @Shadow
    protected int leftPos;
    @Shadow
    protected int topPos;
    protected MixinContainerScreen(Component component) {
        super(component);
    }
    @Shadow
    protected abstract void slotClicked(Slot slotIn, int slotId, int mouseButton, ClickType type);
}
