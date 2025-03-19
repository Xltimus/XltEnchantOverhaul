package com.xlt.enchantment_overhaul.mixin;

import com.xlt.enchantment_overhaul.PlayerEnchantData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(Item.class)
public class ItemMixin {

    private boolean isCharging = false;  // Local flag to track charging state (per item usage)

    @Inject(method = "getUseAnimation", at = @At("HEAD"), cancellable = true)
    private void modifyUseAnimation(CallbackInfoReturnable<UseAnim> cir) {
        if ((Item) (Object) this instanceof EnchantedBookItem){
            cir.setReturnValue(UseAnim.BOW);
        }
    }

    @Inject(method = "getUseDuration", at = @At("HEAD"), cancellable = true)
    private void modifyUseDuration(CallbackInfoReturnable<Integer> cir) {
        if ((Item) (Object) this instanceof EnchantedBookItem) {
            cir.setReturnValue(40); // Adjust charge time like a bow (ticks)
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void modifyUse(Level world, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof EnchantedBookItem) {
            player.startUsingItem(hand); // Forces the bow-style charge animation
            cir.setReturnValue(InteractionResultHolder.consume(stack)); // Prevents instant usage and enables charging
        }
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void onBookUseComplete(ItemStack stack, Level world, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (world.isClientSide) return; // Prevents duplicate execution on the client

        if ((stack.getItem() instanceof EnchantedBookItem) && (entity instanceof Player player)) {
            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);

            if (!enchantments.isEmpty()) {
                boolean learnedSomethingNew = false; // Track if anything was learned

                for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                    Enchantment enchantment = entry.getKey();
                    int level = entry.getValue();
                    ResourceLocation enchantmentKey = EnchantmentHelper.getEnchantmentId(enchantment);

                    if (enchantmentKey != null) {
                        String key = enchantmentKey.toString(); // "minecraft:sharpness" format
                        int currentLevel = PlayerEnchantData.get(player).getOrDefault(key, 0);

                        // Only update if the new enchantment level is higher
                        if (level > currentLevel) {
                            PlayerEnchantData.addEnchantment(player, enchantment, level);
                            learnedSomethingNew = true;

                            // Format enchantment name for message
                            String formattedEnchantment = enchantment.getFullname(level).getString();

                            // Send chat message
                            player.sendSystemMessage(Component.literal("You have learned: " + formattedEnchantment)
                                    .withStyle(ChatFormatting.YELLOW));
                        }
                    }
                }

                // Play success sound if something was learned, otherwise fail sound
                if (learnedSomethingNew) {
                    world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                } else {
                    player.sendSystemMessage(Component.literal("You already know all enchantments at this level or higher.")
                            .withStyle(ChatFormatting.RED));
                    world.playSound(null, player.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.8f, 0.8f);
                }
            }
        }
    }
}
