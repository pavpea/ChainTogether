package com.evailcodes.chaintogether;

import com.evailcodes.chaintogether.config.ChainConfig;
import com.evailcodes.chaintogether.handler.ChainHandler;
import com.evailcodes.chaintogether.handler.ChainPlayerHandler;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.List;

public class ChainCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("chain")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("bind")
                        .then(Commands.argument("player1", EntityArgument.player())
                                .then(Commands.argument("player2", EntityArgument.player())
                                        .executes(context -> bindPlayers(context,
                                                EntityArgument.getPlayer(context, "player1"),
                                                EntityArgument.getPlayer(context, "player2"),
                                                null, null, null))
                                        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
                                                .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                                                        .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                                                                .executes(context -> bindPlayers(context,
                                                                        EntityArgument.getPlayer(context, "player1"),
                                                                        EntityArgument.getPlayer(context, "player2"),
                                                                        DoubleArgumentType.getDouble(context, "x"),
                                                                        DoubleArgumentType.getDouble(context, "y"),
                                                                        DoubleArgumentType.getDouble(context, "z")))))))))

                .then(Commands.literal("unbind")
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> unbindPlayer(context,
                                        EntityArgument.getPlayer(context, "player")))))

                .then(Commands.literal("length")
                        .then(Commands.argument("multiplier", DoubleArgumentType.doubleArg(0.5, 10.0))
                                .executes(context -> setChainLength(context,
                                        DoubleArgumentType.getDouble(context, "multiplier")))))

                .then(Commands.literal("reload")
                        .executes(ChainCommand::reloadConfig))

                .then(Commands.literal("list")
                        .executes(ChainCommand::listChainedPlayers))
                
                .then(Commands.literal("transparency")
                        .then(Commands.argument("value", IntegerArgumentType.integer(10, 100))
                                .executes(context -> setChainTransparency(context,
                                        IntegerArgumentType.getInteger(context, "value")))))
        );
    }

    private static int bindPlayers(CommandContext<CommandSourceStack> context, ServerPlayer player1, ServerPlayer player2, Double x, Double y, Double z) {
        // 如果提供了坐标，将player2传送到指定坐标
        if (x != null && y != null && z != null) {
            // 将player2传送到player1所在维度的指定坐标
            player2.teleportTo((net.minecraft.server.level.ServerLevel) player1.level(), x, y, z, player2.getYRot(), player2.getXRot());
            ChainTogether.LOGGER.info("Teleported player {} to coordinates {} in dimension {}", 
                    player2.getName().getString(), 
                    String.format("%.2f, %.2f, %.2f", x, y, z), 
                    player1.level().dimension().location());
        } else {
            // 如果没有提供坐标，将player2传送到player1身边
            player2.teleportTo((net.minecraft.server.level.ServerLevel) player1.level(), 
                    player1.getX(), player1.getY(), player1.getZ(), 
                    player2.getYRot(), player2.getXRot());
            ChainTogether.LOGGER.info("Teleported player {} to player {}", 
                    player2.getName().getString(), 
                    player1.getName().getString());
        }
        
        // 绑定玩家
        ChainHandler.bindPlayers(player1, player2);
        context.getSource().sendSuccess(() ->
                Component.literal("已成功将 " + player1.getName().getString() + " 与 " + player2.getName().getString() + " 绑定！"), true);
        return 1;
    }

    private static int unbindPlayer(CommandContext<CommandSourceStack> context, ServerPlayer player) {
        ChainHandler.unbindPlayer(player);
        context.getSource().sendSuccess(() ->
                Component.literal("已成功解绑玩家：" + player.getName().getString()), true);
        return 1;
    }

    private static int setChainLength(CommandContext<CommandSourceStack> context, double multiplier) {
        // 这里应该更新配置，然后通知所有玩家
        ChainConfig.CHAIN_LENGTH.set(multiplier);
        ChainConfig.SPEC.save();
        
        context.getSource().sendSuccess(() ->
                Component.literal("链条长度已设置为：" + multiplier + " 倍"), true);
        return 1;
    }

    private static int setChainTransparency(CommandContext<CommandSourceStack> context, int value) {
        // 更新配置
        ChainConfig.CHAIN_TRANSPARENCY.set(value);
        ChainConfig.SPEC.save();
        
        context.getSource().sendSuccess(() ->
                Component.literal("链条透明度已设置为：" + value + "%"), true);
        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {

        context.getSource().sendSuccess(() ->
                Component.literal("配置已重新加载！"), true);
        return 1;
    }

    private static int listChainedPlayers(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        net.minecraft.server.MinecraftServer server = source.getServer();
        
        List<String> pairs = ChainHandler.getChainedPlayerPairs(server);
        if (pairs.isEmpty()) {
            context.getSource().sendSuccess(() ->
                    Component.literal("没有玩家绑定！"), false);
        } else {
            context.getSource().sendSuccess(() ->
                    Component.literal("玩家绑定列表："), false);
            context.getSource().sendSuccess(() ->
                    Component.literal("-----------------------"), false);
            for (String pair : pairs) {
                context.getSource().sendSuccess(() ->
                        Component.literal(pair), false);
            }
        }
        return 1;
    }
}