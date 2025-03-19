package com.xlt.enchantment_overhaul;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

public class PlayerEnchantData {
    private static final String ENCHANTMENT_DATA_TAG = "LearnedEnchantments"; // Key for enchantments in player data

    /**
     * Retrieves the player's stored enchantments from persistent data.
     * @param player The player whose data is being accessed.
     * @return A map of enchantment names (e.g., "minecraft:sharpness") and their levels.
     */
    public static Map<String, Integer> get(Player player) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag enchantTag = playerData.getCompound(ENCHANTMENT_DATA_TAG);

        return enchantTag.getAllKeys().stream()
                .collect(java.util.stream.Collectors.toMap(key -> key, enchantTag::getInt));
    }

    /**
     * Stores the updated enchantments in the player's persistent data.
     * @param player The player whose data is being modified.
     * @param enchantments A map of enchantment names (e.g., "minecraft:sharpness") and their levels.
     */
    public static void set(Player player, Map<String, Integer> enchantments) {
        CompoundTag playerData = player.getPersistentData();
        CompoundTag enchantTag = new CompoundTag();

        for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
            enchantTag.putInt(entry.getKey(), entry.getValue());
        }

        playerData.put(ENCHANTMENT_DATA_TAG, enchantTag);
    }

    public static void addEnchantment(Player player, Enchantment enchantment, int level) {
        Map<String, Integer> enchantments = get(player);
        ResourceLocation enchantmentKey = EnchantmentHelper.getEnchantmentId(enchantment);

        if (enchantmentKey != null) {
            String key = enchantmentKey.toString(); // Ensures proper format like "minecraft:sharpness"
            int currentLevel = enchantments.getOrDefault(key, 0);

            // Only update if the new level is higher
            if (level > currentLevel) {
                enchantments.put(key, level);
                set(player, enchantments);
            }
        }
    }

    public static void remove(Player player, String enchantment) {
        Map<String, Integer> enchantments = get(player);
        if (enchantments.containsKey(enchantment)) {
            enchantments.remove(enchantment);
            set(player, enchantments);
        }
    }
}
