package com.xlt.enchantment_overhaul;

import com.xlt.enchantment_overhaul.menu.OverhaulEnchantingMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, EnchantmentOverhaulMod.MODID);

    public static final RegistryObject<MenuType<OverhaulEnchantingMenu>> OVERHAUL_ENCHANTING_MENU = MENUS.register(EnchantmentOverhaulMod.MODID, () -> new MenuType<>(OverhaulEnchantingMenu::new, FeatureFlags.DEFAULT_FLAGS));

}
