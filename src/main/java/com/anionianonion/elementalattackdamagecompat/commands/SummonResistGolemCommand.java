package com.anionianonion.elementalattackdamagecompat.commands;

import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentsRegistry;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.core.BlockPos;

import java.util.Locale;
import java.util.Set;

public class SummonResistGolemCommand {

    private static final Set<String> VALID_ELEMENTS = AilmentsRegistry.getAll().keySet();

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {

        dispatcher.register(
                Commands.literal("summon_resistant_golem")
                        .requires(src -> src.hasPermission(0))

                        // /summon_resistant_golem <element>
                        .then(Commands.argument("element", StringArgumentType.string())
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    String element = StringArgumentType.getString(ctx, "element")
                                            .toLowerCase(Locale.ROOT)
                                            .trim();

                                    return summonGolem(src, element);
                                })
                        )
        );
    }

    private static int summonGolem(CommandSourceStack src, String element) {

        if (!VALID_ELEMENTS.contains(element)) {
            src.sendFailure(Component.literal("Invalid element: " + element));
            return 0;
        }

        ServerLevel level = src.getLevel();
        BlockPos pos = BlockPos.containing(src.getPosition());

        IronGolem golem = EntityType.IRON_GOLEM.create(level);
        if (golem == null) {
            src.sendFailure(Component.literal("Failed to create Iron Golem entity."));
            return 0;
        }

        golem.moveTo(pos, 0, 0);

        // Set health
        AttributeInstance maxHealth = golem.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null) {
            maxHealth.setBaseValue(1000.0);
        }
        golem.setHealth(1000.0f);

        // Set elemental resistance
        ResourceLocation resistId = ResourceLocation.fromNamespaceAndPath("irons_spellbooks", element + "_magic_resist");
        AttributeInstance resistAttr = golem.getAttribute(
                level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE)
                        .get(resistId)
        );

        if(resistAttr == null) {
            resistId = ResourceLocation.tryParse(ModAttributes.customSchoolToResistAttributeKey.get(element));
            resistAttr = golem.getAttribute(
                    level.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.ATTRIBUTE)
                            .get(resistId)
            );
        }

        if (resistAttr != null) {
            resistAttr.setBaseValue(1.75);
        } else {
            src.sendFailure(Component.literal("Attribute not found: " + resistId));
            return 0;
        }

        level.addFreshEntity(golem);

        src.sendSuccess(
                () -> Component.literal("Summoned Iron Golem with 1000 HP and 1.75 " + element + " resistance."),
                false
        );

        return 1;
    }
}
