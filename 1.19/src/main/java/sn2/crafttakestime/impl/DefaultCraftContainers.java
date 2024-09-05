package sn2.crafttakestime.impl;

import lombok.Data;
import sn2.crafttakestime.common.config.ContainerConfig;
import sn2.crafttakestime.common.slot.SlotRange;

import java.util.ArrayList;
import java.util.List;

@Data
public class DefaultCraftContainers {

    // Singleton
    private static DefaultCraftContainers INSTANCE;
    private final List<ContainerConfig> craftContainers = new ArrayList<>();

    private DefaultCraftContainers() {
        // minecraft inventory
        registerCraftContainer(ContainerConfig.builder()
                .containerName("minecraft:inventory")
                .guiContainerClassName("net.minecraft.client.gui.screens.inventory.InventoryScreen")
                .ingredientSlots(SlotRange.fromString("1-4"))
                .overlayTexture("crafttakestime:textures/gui/crafting_overlay_inventory.png")
                .overlayX(134)
                .overlayY(29)
                .overlayWidth(18)
                .overlayHeight(15)
                .build());
        // minecraft inventory with Curios API
        registerCraftContainer(ContainerConfig.builder()
                .containerName("minecraft:inventory")
                .guiContainerClassName("top.theillusivec4.curios.client.gui.CuriosScreen")
                .ingredientSlots(SlotRange.fromString("1-4"))
                .overlayTexture("crafttakestime:textures/gui/crafting_overlay_inventory.png")
                .overlayX(134)
                .overlayY(29)
                .overlayWidth(18)
                .overlayHeight(15)
                .build());
        // minecraft crafting table
        registerCraftContainer(ContainerConfig.builder()
                .containerName("minecraft:crafting_table")
                .guiContainerClassName("net.minecraft.client.gui.screens.inventory.CraftingScreen")
                .build());
        // minecraft smithing table
        registerCraftContainer(ContainerConfig.builder()
                .containerName("minecraft:smithing_table")
                .guiContainerClassName("net.minecraft.client.gui.screens.inventory.SmithingScreen")
                .ingredientSlots(SlotRange.fromString("0-1"))
                .outputSlot(2)
                .overlayX(101)
                .overlayY(48)
                .build());
        // minecraft anvil screen
        registerCraftContainer(ContainerConfig.builder()
                .containerName("minecraft:anvil")
                .guiContainerClassName("net.minecraft.client.gui.screens.inventory.AnvilScreen")
                .ingredientSlots(SlotRange.fromString("0-1"))
                .outputSlot(2)
                .overlayX(101)
                .overlayY(48)
                .build());
        // tinker's crafting station
        registerCraftContainer(ContainerConfig.builder()
                .containerName("tconstruct:crafting_station")
                .guiContainerClassName("slimeknights.tconstruct.tables.client.inventory.CraftingStationScreen")
                .ingredientSlots(SlotRange.fromString("0-8"))
                .outputSlot(9)
                .build());
    }

    public static DefaultCraftContainers getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DefaultCraftContainers();
        }
        return INSTANCE;
    }

    public void registerCraftContainer(ContainerConfig config) {
        craftContainers.add(config);
    }
}
