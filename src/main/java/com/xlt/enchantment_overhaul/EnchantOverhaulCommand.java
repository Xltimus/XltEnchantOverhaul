package com.xlt.enchantment_overhaul;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber
public class EnchantOverhaulCommand {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("enchantoverhaul")
                .requires(source -> source.hasPermission(2)) // OP permission level 2
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.literal("add")
                                .then(Commands.argument("enchant", ResourceLocationArgument.id())
                                        .suggests(EnchantOverhaulCommand::suggestEnchantments) // Autocomplete for enchantments
                                        .then(Commands.argument("level", IntegerArgumentType.integer(1, 10))
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    ResourceLocation enchantId = ResourceLocationArgument.getId(ctx, "enchant");
                                                    int level = IntegerArgumentType.getInteger(ctx, "level");

                                                    return addEnchantment(player, enchantId.toString(), level, ctx.getSource());
                                                })
                                        )
                                )
                        )
                        .then(Commands.literal("remove")
                                .then(Commands.argument("enchant", ResourceLocationArgument.id())
                                        .suggests(EnchantOverhaulCommand::suggestEnchantments) // Autocomplete for enchantments
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                            ResourceLocation enchantId = ResourceLocationArgument.getId(ctx, "enchant");

                                            return removeEnchantment(player, enchantId.toString(), ctx.getSource());
                                        })
                                )
                        )
                        .then(Commands.literal("check")
                                .then(Commands.argument("enchant", ResourceLocationArgument.id())
                                        .suggests(EnchantOverhaulCommand::suggestEnchantments) // Autocomplete for enchantments
                                        .executes(ctx -> {
                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                            ResourceLocation enchantId = ResourceLocationArgument.getId(ctx, "enchant");

                                            return checkEnchantment(player, enchantId.toString(), ctx.getSource());
                                        })
                                )
                        )
                )
        );
    }

    private static CompletableFuture<Suggestions> suggestEnchantments(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        // Add enchantment suggestions
        for (Enchantment enchantment : BuiltInRegistries.ENCHANTMENT) {
            String enchantmentDescription = enchantment.getDescriptionId();
            String enchantmentKey = enchantmentDescription.replace("enchantment.", "");
            enchantmentKey = enchantmentKey.replace(".", ":"); //I'm getting that dang registry name one way or another
            suggestionsBuilder.suggest(enchantmentKey); // Adds "<modid>:<enchantment>" as suggestion
        }

        return suggestionsBuilder.buildFuture();
    }

    private static int addEnchantment(ServerPlayer player, String enchantName, int level, CommandSourceStack source) {
        ResourceLocation enchantmentKey = ResourceLocation.tryParse(enchantName);
        if (enchantmentKey == null || !BuiltInRegistries.ENCHANTMENT.containsKey(enchantmentKey)) {
            source.sendFailure(Component.literal("Invalid enchantment: " + enchantName));
            return Command.SINGLE_SUCCESS;
        }

        Enchantment enchantment = BuiltInRegistries.ENCHANTMENT.get(enchantmentKey);
        if (enchantment == null) {
            source.sendFailure(Component.literal("Enchantment not found: " + enchantName));
            return Command.SINGLE_SUCCESS;
        }

        // Fetch stored enchantments
        Map<String, Integer> storedEnchants = PlayerEnchantData.get(player);
        int currentLevel = storedEnchants.getOrDefault(enchantName, 0);

        if (level <= currentLevel) {
            source.sendFailure(Component.literal(player.getName().getString() + " already knows " + enchantName + " at level " + currentLevel + " or higher!"));
            return Command.SINGLE_SUCCESS;
        }

        // Add or upgrade the enchantment
        PlayerEnchantData.addEnchantment(player, enchantment, level);
        source.sendSuccess(() -> Component.literal("Successfully added " + enchantName + " level " + level + " to " + player.getName().getString()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int removeEnchantment(ServerPlayer player, String enchantName, CommandSourceStack source) {
        Map<String, Integer> storedEnchants = PlayerEnchantData.get(player);

        if (!storedEnchants.containsKey(enchantName)) {
            source.sendFailure(Component.literal(player.getName().getString() + " does not have " + enchantName));
            return Command.SINGLE_SUCCESS;
        }

        PlayerEnchantData.remove(player, enchantName);
        source.sendSuccess(() -> Component.literal("Removed " + enchantName + " from " + player.getName().getString()), true);
        return Command.SINGLE_SUCCESS;
    }

    private static int checkEnchantment(ServerPlayer player, String enchantName, CommandSourceStack source) {
        Map<String, Integer> storedEnchants = PlayerEnchantData.get(player);

        if (storedEnchants.containsKey(enchantName)) {
            int level = storedEnchants.get(enchantName);
            source.sendSuccess(() -> Component.literal(player.getName().getString() + " knows " + enchantName + " at level " + level), false);
        } else {
            source.sendFailure(Component.literal(player.getName().getString() + " does not know " + enchantName));
        }
        return Command.SINGLE_SUCCESS;
    }
}
