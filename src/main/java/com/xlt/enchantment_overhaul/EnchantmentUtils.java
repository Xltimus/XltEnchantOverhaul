package com.xlt.enchantment_overhaul;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantmentUtils {
    public static void printEnchantments(ItemStack item) {
        Map<Enchantment, Integer> enchantments = getValidEnchantments(item);

        if (!enchantments.isEmpty()) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                System.out.println("Valid Enchantment: " + entry.getKey().getFullname(entry.getValue()).getString());
            }
        } else {
            System.out.println("No valid enchantments for this item.");
        }
    }

    // Returns a list of enchantments that can be applied
    public static Map<Enchantment, Integer> getValidEnchantments(ItemStack item) {
        Map<Enchantment, Integer> validEnchantments = new HashMap<>();
        Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(item);

        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            if (enchantment.canEnchant(item) && isEnchantmentValid(enchantment, existingEnchantments)) {
                // Get the existing level of the enchantment, or 0 if not found
                int existingLevel = existingEnchantments.getOrDefault(enchantment, 0);
                // Calculate the new level (existing level + 1, but no higher than max level)
                int newLevel = Math.min(existingLevel + 1, enchantment.getMaxLevel());

                validEnchantments.put(enchantment, newLevel);
            }
        }
        return validEnchantments;
    }

    // Checks if an enchantment is valid to apply
    private static boolean isEnchantmentValid(Enchantment newEnchant, Map<Enchantment, Integer> existingEnchantments) {
        for (Map.Entry<Enchantment, Integer> entry : existingEnchantments.entrySet()) {
            Enchantment existingEnchant = entry.getKey();
            int existingLevel = entry.getValue();

            if (existingEnchant == newEnchant) {
                // If the enchantment is already at max level, exclude it
                return existingLevel < newEnchant.getMaxLevel();
            }

            if (!existingEnchant.isCompatibleWith(newEnchant)) {
                return false; // Prevent conflicting enchantments
            }
        }
        return true;
    }

    // Returns a list of enchantments that can be applied to the item, including levels
    public static List<String> getEnchantmentsWithLevels(ItemStack item) {
        List<String> enchantmentsWithLevels = new ArrayList<>();
        Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(item);

        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            // Check if the enchantment can apply to the item
            if (enchantment.canEnchant(item)) {
                // Get the existing level of the enchantment, or 0 if not found
                int existingLevel = existingEnchantments.getOrDefault(enchantment, 0);
                // Calculate the new level (existing level + 1, but no higher than max level)
                int newLevel = Math.min(existingLevel + 1, enchantment.getMaxLevel());

                // Add to the list of enchantments with their levels
                enchantmentsWithLevels.add(enchantment.getFullname(newLevel).getString());
            }
        }
        return enchantmentsWithLevels;
    }
}

