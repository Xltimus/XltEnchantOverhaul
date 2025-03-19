package com.xlt.enchantment_overhaul;

import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AnvilEventHandler {
    @SubscribeEvent()
    public static void onAnvilRepair(AnvilRepairEvent event) {
        // Get the items involved in the anvil operation
        ItemStack rightItem = event.getRight();

        // Check if we're only renaming and not modifying the item
        if (rightItem.isEmpty()) {
            // If only renaming, set the break chance to 0
            event.setBreakChance(0);  // No damage to the anvil

        }
    }
}
