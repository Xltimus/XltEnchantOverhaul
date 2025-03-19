package com.xlt.enchantment_overhaul.mixin;

import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilMenu.class)
public class AnvilMenuMixin {

    // Redirect getBaseRepairCost() to always return 0
    @Redirect(method = "createResult", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getBaseRepairCost()I"))
    private int removeBaseRepairCost(ItemStack stack) {
        return 0; // Prevent the base repair cost from affecting the calculation
    }

    // Bypass any enchantment cost that might have been calculated
    @Inject(method = "createResult", at = @At("HEAD"))
    private void bypassEnchantmentCost(CallbackInfo ci) {
        // Ensure the XP cost remains 0 even if enchantments are present
        ((AnvilMenuAccessor) this).getCost().set(0);
    }

    @Inject(method = "createResult", at = @At("TAIL"))
    private void forceZeroCost(CallbackInfo ci) {
        // Forces XP cost to 0 in the anvil
        ((AnvilMenuAccessor) this).getCost().set(0);
        System.out.println("Anvil XP cost set to 0!");

        AnvilMenu menu = (AnvilMenu) (Object) this;
        ItemStack right = menu.getSlot(1).getItem();

        if (right.getItem() instanceof EnchantedBookItem) {
            menu.getSlot(2).set(ItemStack.EMPTY);
        }
    }

    @Inject(method = "mayPickup", at = @At("RETURN"), cancellable = true)
    private void alwaysAllowPickup(CallbackInfoReturnable<Boolean> cir) {
        // Force the return value to true, allowing players to always pick up the item
        cir.setReturnValue(true);
    }
}
