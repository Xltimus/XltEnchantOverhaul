package com.xlt.enchantment_overhaul;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Random;

public class ModEventHandler {

    private static final Random RANDOM = new Random();

    // Registering the event with the Event Bus
    public static void register(IEventBus modEventBus) {
        //modEventBus.addListener(ModEventHandler::onItemUse);  // Register the item use event listener
        modEventBus.addListener(ModEventHandler::onAnvilRepair);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onAnvilRepair(net.minecraftforge.event.entity.player.AnvilRepairEvent event) {
        ItemStack output = event.getOutput();
        Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(output);
        Player player = event.getEntity();

        // Iterate through all enchantments and randomly degrade them
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            int currentLevel = entry.getValue();

            // 50% chance to lower enchantment level by 1 (Adjust probability as needed)
            if (RANDOM.nextInt(100) <= 15){
                player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, 0.8f, 0.8f);
                if (currentLevel > 1){
                    enchantments.put(entry.getKey(), currentLevel - 1);
                }
                else {
                    enchantments.remove(entry.getKey());
                }


            }
        }

        // Apply the modified enchantments back to the item
        EnchantmentHelper.setEnchantments(enchantments, output);
    }
}
