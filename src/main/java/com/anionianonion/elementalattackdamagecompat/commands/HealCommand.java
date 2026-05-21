package com.anionianonion.elementalattackdamagecompat.commands;

import com.anionianonion.elementalattackdamagecompat.ailments.IAilmentData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class HealCommand {

    // Cooldown storage (per-player)
    private static final Map<UUID, Long> COOLDOWNS = new HashMap<>();
    private static final long COOLDOWN_MS = 5000; // 5 seconds

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("heal")
                        .requires(source -> source.hasPermission(0))

                        // /heal (self)
                        .executes(ctx -> {
                            CommandSourceStack src = ctx.getSource();

                            if (!(src.getEntity() instanceof ServerPlayer player)) {
                                src.sendFailure(Component.literal("A player must be specified."));
                                return 0;
                            }

                            if (!checkCooldown(src, player)) return 0;

                            healPlayer(player);
                            src.sendSuccess(() -> Component.literal("You have been healed."), false);
                            return 1;
                        })

                        // /heal <target>
                        .then(Commands.argument("target", EntityArgument.players())
                                .executes(ctx -> {
                                    Collection<ServerPlayer> targets =
                                            EntityArgument.getPlayers(ctx, "target");

                                    for (ServerPlayer p : targets) {
                                        healPlayer(p);
                                    }

                                    ctx.getSource().sendSuccess(
                                            () -> Component.literal("Healed " + targets.size() + " player(s)."),
                                            false
                                    );
                                    return 1;
                                })
                        )

                        // /heal radius <r>
                        .then(Commands.literal("radius")
                                .then(Commands.argument("r", IntegerArgumentType.integer(1, 256))
                                        .executes(ctx -> {
                                            int r = IntegerArgumentType.getInteger(ctx, "r");
                                            CommandSourceStack src = ctx.getSource();

                                            Vec3 pos = src.getPosition();
                                            AABB box = new AABB(
                                                    pos.x - r, pos.y - r, pos.z - r,
                                                    pos.x + r, pos.y + r, pos.z + r
                                            );

                                            List<ServerPlayer> players =
                                                    src.getLevel().getEntitiesOfClass(ServerPlayer.class, box);

                                            for (ServerPlayer p : players) {
                                                healPlayer(p);
                                            }

                                            src.sendSuccess(
                                                    () -> Component.literal("Healed " + players.size() + " player(s) in radius " + r),
                                                    false
                                            );
                                            return 1;
                                        })
                                )
                        )
        );
    }

    private static boolean checkCooldown(CommandSourceStack src, ServerPlayer player) {
        long now = System.currentTimeMillis();
        long last = COOLDOWNS.getOrDefault(player.getUUID(), 0L);

        if (now - last < COOLDOWN_MS) {
            long remaining = (COOLDOWN_MS - (now - last)) / 1000;
            src.sendFailure(Component.literal("You must wait " + remaining + "s before using /heal again."));
            return false;
        }

        COOLDOWNS.put(player.getUUID(), now);
        return true;
    }

    private static void healPlayer(ServerPlayer player) {
        float max = player.getMaxHealth();
        player.setHealth(max);

        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20f);
        player.clearFire();

        // Clear your custom ailments
        if (player instanceof IAilmentData iAilmentDataHolder) {
            iAilmentDataHolder.getAilmentsOnEntity().clear();
        }
    }
}
