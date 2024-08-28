package sn2.crafttakestime.core;

import lombok.Data;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sn2.crafttakestime.ITimeCraftGuiContainer;
import sn2.crafttakestime.config.ContainerConfig;
import sn2.crafttakestime.config.CraftConfig;
import sn2.crafttakestime.sound.CraftingTickableSound;
import sn2.crafttakestime.sound.SoundEventRegistry;
import sn2.crafttakestime.util.CraftingSpeedHelper;
import sn2.crafttakestime.util.SlotRange;

import java.util.ArrayList;
import java.util.List;

@Data
public class CraftManager {

    private static final Logger log = LogManager.getLogger(CraftManager.class);
    private static final float BASE_CRAFTING_TIME_PER_ITEM = 20F;
    private static final ContainerConfig DISABLED_CONTAINER = ContainerConfig.builder().enabled(false).build();
    // Singleton
    private static CraftManager INSTANCE;
    private CraftConfig config;
    private AbstractContainerScreen currentGuiContainer;
    private boolean crafting = false;
    private int waitCounter = 0;
    private float currentCraftTime = 0;
    private float craftPeriod = 0;
    private ItemStack resultStack;

    private CraftManager() {
    }

    public static CraftManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CraftManager();
        }
        return INSTANCE;
    }

    public void unsetGuiContainer() {
        this.currentGuiContainer = null;
        this.stopCraft();
    }

    public ContainerConfig getCraftContainerConfig() {
        if (this.currentGuiContainer == null) {
            return DISABLED_CONTAINER;
        }
        String guiClassName = this.currentGuiContainer.getClass().getName();
        return config.getContainers().stream().filter(container ->
                        container.getGuiContainerClassName().equals(guiClassName))
                .findFirst().orElse(DISABLED_CONTAINER);
    }

    public boolean initCraft(AbstractContainerScreen gui, int invSlot) {
        this.setCurrentGuiContainer(gui);
        ContainerConfig containerConfig = this.getCraftContainerConfig();
        if (config.isDebug()) {
            log.info("Inv slot {}, gui class {}, containerConfig {}",
                    invSlot, this.getCurrentGuiContainer().getClass().getName(), containerConfig);
        }

        if (!containerConfig.isEnabled()) {
            return false;
        }

        // Check if clicking the result slot
        int outputSlot = containerConfig.getOutputSlot();
        if (invSlot != outputSlot) {
            stopCraft();
            return false;
        }

        // Check if the result slot is empty
        if (this.currentGuiContainer.getMenu().getSlot(outputSlot).getItem().isEmpty()) {
            return false;
        }

        // Check if the player is already crafting
        if (!isCrafting()) {
            craftPeriod = getCraftingTime(
                    this.currentGuiContainer.getMenu(), outputSlot, containerConfig.getIngredientSlots(), containerConfig);

            if (craftPeriod >= 10F && config.isEnableCraftingSound()) {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) {
                    Minecraft.getInstance().getSoundManager().play(
                            new CraftingTickableSound(player.getOnPos()));
                }
            }
            startCraft();
        }
        return true;
    }

    private void startCraft() {
        this.crafting = true;
        this.currentCraftTime = 0;
    }

    private void stopCraft() {
        this.crafting = false;
        this.currentCraftTime = 0;
    }

    public void tick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        ContainerConfig containerConfig = this.getCraftContainerConfig();
        if (!containerConfig.isEnabled()) {
            return;
        }

        if (this.isCrafting()) {
            int outputSlot = containerConfig.getOutputSlot();
            SlotRange ingredientSlots = containerConfig.getIngredientSlots();

            ItemStack resultStack = this.currentGuiContainer.getMenu()
                    .getSlot(outputSlot).getItem();

            // Stop crafting if the result slot is empty
            if (resultStack.isEmpty()) {
                if (waitCounter < 5) {
                    waitCounter++;
                } else {
                    waitCounter = 0;
                    this.stopCraft();
                }
                return;
            }

            ItemStack cursorStack = player.inventoryMenu.getCarried();
            if (cursorStack.getItem() != Items.AIR) {
                if (!cursorStack.sameItem(resultStack)
                        || cursorStack.getCount() + resultStack.getCount() > cursorStack.getMaxStackSize()) {
                    this.stopCraft();
                    return;
                }
            }
            if (this.getCurrentCraftTime() < this.getCraftPeriod()) {
                this.currentCraftTime += CraftingSpeedHelper.getCraftingSpeed(player);
            } else if (this.getCurrentCraftTime() >= this.getCraftPeriod()) {
                if (config.isEnableCraftingSound()) {
                    player.playSound(SoundEventRegistry.finishSound.get(), 0.1F, 1f);
                }

                // Record the old recipe before picking up the result item
                List<Item> oldRecipe = getIngredientItems(
                        this.getCurrentGuiContainer().getMenu(), ingredientSlots);

                ((ITimeCraftGuiContainer) this.currentGuiContainer).handleCraftFinished(
                        this.getCurrentGuiContainer().getMenu().getSlot(outputSlot), outputSlot);

                // Compare the old recipe with the new recipe
                List<Item> newRecipe = getIngredientItems(
                        this.getCurrentGuiContainer().getMenu(), ingredientSlots);
                if (!oldRecipe.equals(newRecipe)) {
                    this.stopCraft();
                } else {
                    waitCounter = 0;
                    this.startCraft();
                }
            }
        }
    }

    private List<Item> getIngredientItems(AbstractContainerMenu handler, SlotRange ingredientSlots) {
        List<Item> items = new ArrayList<>();
        for (int i : ingredientSlots) {
            items.add(handler.getSlot(i).getItem().getItem());
        }
        log.info("Ingredient items: {}", items);
        return items;
    }

    private Item getOutputItem(AbstractContainerMenu handler, int outputSlot) {
        return handler.getSlot(outputSlot).getItem().getItem();
    }

    private float getCraftingTime(AbstractContainerMenu handler,
                                  int outputSlot,
                                  SlotRange ingredientSlots,
                                  ContainerConfig containerConfig) {
        // Global multiplier
        float globalMultiplier = config.getGlobalCraftingTimeMultiplier();

        // Container multiplier
        float containerMultiplier = 1F;
        if (containerConfig != null) {
            containerMultiplier = containerConfig.getCraftingTimeMultiplier();
        }

        // Ingredient multiplier
        List<Item> ingredients = getIngredientItems(handler, ingredientSlots);
        float ingredientDifficulty = 0F;
        for (Item item : ingredients) {
            if (item == Items.AIR) {
                continue;
            }
            ResourceLocation registry = ForgeRegistries.ITEMS.getKey(item);
            if (registry == null) {
                continue;
            }
            float modMultiplier = config.getIngredientConfig().getModCraftingTimeMultipliers()
                    .getOrDefault(registry.getNamespace(), 1F);
            float itemMultiplier = config.getIngredientConfig().getItemCraftingTimeMultipliers()
                    .getOrDefault(registry.toString(), 1F);
            ingredientDifficulty += modMultiplier * itemMultiplier;
        }

        // Output multiplier
        Item outputItem = getOutputItem(handler, outputSlot);
        ResourceLocation outputRegistry = ForgeRegistries.ITEMS.getKey(outputItem);
        float outputMultiplier = 1F;
        if (outputRegistry != null) {
            float modMultiplier = config.getOutputConfig().getModCraftingTimeMultipliers()
                    .getOrDefault(outputRegistry.getNamespace(), 1F);
            float itemMultiplier = config.getOutputConfig().getItemCraftingTimeMultipliers()
                    .getOrDefault(outputRegistry.toString(), 1F);
            outputMultiplier = modMultiplier * itemMultiplier;
        }

        // Final crafting time
        return BASE_CRAFTING_TIME_PER_ITEM * ingredientDifficulty * outputMultiplier
                * containerMultiplier * globalMultiplier;
    }
}
