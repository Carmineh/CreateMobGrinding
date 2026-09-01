package dev.manny.createmobgrinding.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.manny.createmobgrinding.config.ModConfigs;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

public class ModCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("createmobgrinding")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("blacklist")
                        .then(Commands.literal("add")
                                .then(Commands.argument("mob", ResourceArgument.resource(buildContext, Registries.ENTITY_TYPE))
                                        .executes(ModCommands::addBlacklist)))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("mob", ResourceArgument.resource(buildContext, Registries.ENTITY_TYPE))
                                        .executes(ModCommands::removeBlacklist)))
                )
                .then(Commands.literal("tier")
                        .then(Commands.argument("mob", ResourceArgument.resource(buildContext, Registries.ENTITY_TYPE))
                                .then(Commands.argument("tier", com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, 5))
                                        .executes(ModCommands::setCustomTier)))
                )
        );
    }

    private static int addBlacklist(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var holder = ResourceArgument.getResource(context, "mob", Registries.ENTITY_TYPE);
        ResourceLocation mobId = holder.key().location();
        String mobStr = mobId.toString();

        List<String> current = new ArrayList<>(ModConfigs.COMMON.spawnerBlacklist.get());
        if (current.contains(mobStr)) {
            context.getSource().sendFailure(Component.literal(mobStr + " is already in the blacklist."));
            return 0;
        }

        current.add(mobStr);
        ModConfigs.COMMON.spawnerBlacklist.set(current);
        context.getSource().sendSuccess(() -> Component.literal("Added " + mobStr + " to the spawner blacklist."), true);
        return 1;
    }

    private static int removeBlacklist(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var holder = ResourceArgument.getResource(context, "mob", Registries.ENTITY_TYPE);
        ResourceLocation mobId = holder.key().location();
        String mobStr = mobId.toString();

        List<String> current = new ArrayList<>(ModConfigs.COMMON.spawnerBlacklist.get());
        if (!current.contains(mobStr)) {
            context.getSource().sendFailure(Component.literal(mobStr + " is not in the blacklist."));
            return 0;
        }

        current.remove(mobStr);
        ModConfigs.COMMON.spawnerBlacklist.set(current);
        context.getSource().sendSuccess(() -> Component.literal("Removed " + mobStr + " from the spawner blacklist."), true);
        return 1;
    }

    private static int setCustomTier(CommandContext<CommandSourceStack> context) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        var holder = ResourceArgument.getResource(context, "mob", Registries.ENTITY_TYPE);
        ResourceLocation mobId = holder.key().location();
        String mobStr = mobId.toString();
        int tier = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "tier");

        List<String> current = new ArrayList<>(ModConfigs.COMMON.customTiers.get());
        
        // Remove existing entry for this mob if it exists
        current.removeIf(entry -> entry.startsWith(mobStr + "="));
        
        // Add new entry
        current.add(mobStr + "=" + tier);
        
        ModConfigs.COMMON.customTiers.set(current);
        context.getSource().sendSuccess(() -> Component.literal("Set tier for " + mobStr + " to " + tier), true);
        
        return 1;
    }
}
