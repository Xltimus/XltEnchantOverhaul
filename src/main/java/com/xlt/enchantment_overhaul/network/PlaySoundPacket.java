package com.xlt.enchantment_overhaul.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlaySoundPacket {
    private final ResourceLocation soundEvent;
    private final float volume;
    private final float pitch;

    // Constructor
    public PlaySoundPacket(ResourceLocation soundEvent, float volume, float pitch) {
        this.soundEvent = soundEvent;
        this.volume = volume;
        this.pitch = pitch;
    }

    // Encode method: Convert the packet's data into bytes for transmission
    public static void encode(PlaySoundPacket msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.soundEvent);  // Write the sound event's ResourceLocation
        buf.writeFloat(msg.volume);  // Write the volume
        buf.writeFloat(msg.pitch);  // Write the pitch
    }

    // Decode method: Convert the received bytes back into packet data
    public static PlaySoundPacket decode(FriendlyByteBuf buf) {
        ResourceLocation soundEvent = buf.readResourceLocation();  // Read the sound event's ResourceLocation
        float volume = buf.readFloat();  // Read the volume
        float pitch = buf.readFloat();  // Read the pitch
        return new PlaySoundPacket(soundEvent, volume, pitch);  // Return the decoded packet
    }

    // Handle method to play sound on the client
    public static void handle(PlaySoundPacket msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            ClientLevel clientLevel = Minecraft.getInstance().level;
            if (clientLevel != null) {
                // Convert the ResourceLocation to a SoundEvent
                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(msg.soundEvent);

                // Play the sound at the player's location if the SoundEvent is found
                if (soundEvent != null) {
                    Minecraft.getInstance().player.playSound(soundEvent, msg.volume, msg.pitch);
                }
            }
        });
        contextSupplier.get().setPacketHandled(true);
    }

}
