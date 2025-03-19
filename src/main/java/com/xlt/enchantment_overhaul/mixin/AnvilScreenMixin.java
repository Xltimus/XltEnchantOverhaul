package com.xlt.enchantment_overhaul.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreen.class)
public class AnvilScreenMixin {
    // Injecting into renderLabels to skip rendering the "Costs: X" text
    @Inject(method = "renderLabels", at = @At("HEAD"), cancellable = true)
    private void skipCostLabel(GuiGraphics guiGraphics, int mouseX, int mouseY, CallbackInfo ci) {
        // Simply cancel the label rendering (specifically the "Costs: X" text)
        ci.cancel();
    }

}
