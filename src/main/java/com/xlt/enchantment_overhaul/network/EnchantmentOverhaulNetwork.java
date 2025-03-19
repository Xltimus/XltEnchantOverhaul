package com.xlt.enchantment_overhaul.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.simple.SimpleChannel;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.RegisterEvent;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class EnchantmentOverhaulNetwork {

    private static final String PROTOCOL_VERSION = "1.0";

    // Create the SimpleChannel
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("enchantmentoverhaul", "main_channel"),
            () -> PROTOCOL_VERSION,
            version -> version.equals(PROTOCOL_VERSION),
            version -> version.equals(PROTOCOL_VERSION)
    );

    // Register packets inside a static block
    static {
        CHANNEL.registerMessage(0, ApplyEnchantmentPacket.class, ApplyEnchantmentPacket::encode, ApplyEnchantmentPacket::decode, ApplyEnchantmentPacket::handle);
        CHANNEL.registerMessage(1, PlaySoundPacket.class, PlaySoundPacket::encode, PlaySoundPacket::decode, PlaySoundPacket::handle);
    }

    // Register the network channel in the mod event bus
    @SubscribeEvent
    public static void onRegisterEvent(RegisterEvent event) {
        // No additional setup needed as packets are registered in the static block
        System.out.println("[EnchantmentOverhaul] Network Packets Registered!");
    }
}
