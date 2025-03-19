package com.xlt.enchantment_overhaul.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.xlt.enchantment_overhaul.menu.OverhaulEnchantingMenu;

@Mixin(EnchantmentTableBlock.class)
public class EnchantmentTableBlockMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void redirectEnchantmentMenu(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MenuProvider provider = new SimpleMenuProvider(
                    (id, playerInventory, playerEntity) -> new OverhaulEnchantingMenu(id, playerInventory), state.getBlock().getName());

            serverPlayer.openMenu(provider);
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }
}
