package com.xlt.enchantment_overhaul.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import java.util.Map;
import java.util.function.Supplier;

public class ApplyEnchantmentPacket {
    private final ResourceLocation enchantmentId;
    private final int level;

    public ApplyEnchantmentPacket(ResourceLocation enchantmentId, int level) {
        this.enchantmentId = enchantmentId;
        this.level = level;
    }

    public static void encode(ApplyEnchantmentPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.enchantmentId);
        buf.writeInt(msg.level);
    }

    public static ApplyEnchantmentPacket decode(FriendlyByteBuf buf) {
        return new ApplyEnchantmentPacket(buf.readResourceLocation(), buf.readInt());
    }

    public static void handle(ApplyEnchantmentPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ServerPlayer player = contextSupplier.get().getSender();
            if (player != null) {
                Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.get(msg.enchantmentId);
                if (enchantment != null) {
                    // Access the item in slot 0 of the container (enchanting table)
                    ItemStack stack = player.containerMenu.getSlot(0).getItem();
                    if (!stack.isEmpty()) {
                        System.out.println("Applying enchantment to item in SLOT 0: " + stack.getHoverName().getString());

                        // Get the existing enchantments on the item
                        Map<Enchantment, Integer> existingEnchantments = EnchantmentHelper.getEnchantments(stack);

                        // Check if the enchantment already exists
                        if (existingEnchantments.containsKey(enchantment)) {
                            // If the enchantment exists, upgrade it only if the new level is higher
                            int currentLevel = existingEnchantments.get(enchantment);
                            int newLevel = Math.max(currentLevel, msg.level); // Keep the higher level
                            existingEnchantments.put(enchantment, newLevel);  // Update the enchantment level
                        } else {
                            // If the enchantment doesn't exist, add it to the map
                            existingEnchantments.put(enchantment, msg.level); // Add new enchantment
                        }

                        // Apply the updated enchantments to the item
                        EnchantmentHelper.setEnchantments(existingEnchantments, stack);

                        // Force the inventory to sync
                        player.getInventory().setChanged();
                        player.containerMenu.broadcastChanges();  // Sync changes

                        // Send updated stack to client to prevent desync
                        player.inventoryMenu.slotsChanged(player.getInventory());

                        // 🟢 Play the enchanting sound effect for this player

                        EnchantmentOverhaulNetwork.CHANNEL.sendTo(
                                new PlaySoundPacket(SoundEvents.ENCHANTMENT_TABLE_USE.getLocation(), 1.0F, 1.0F),
                                player.connection.connection, NetworkDirection.PLAY_TO_CLIENT
                        );

                    }
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }


}
