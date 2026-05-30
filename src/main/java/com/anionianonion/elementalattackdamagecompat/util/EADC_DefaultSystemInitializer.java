package com.anionianonion.elementalattackdamagecompat.util;

import com.anionianonion.elementalattackdamagecompat.Config;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentStackingMode;
import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffect;
import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffectBuilder;
import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.*;
import com.anionianonion.elementalattackdamagecompat.api.API;
import com.anionianonion.elementalattackdamagecompat.enchants.ModEnchantments;
import com.anionianonion.elementalattackdamagecompat.pseudo_enchants.PseudoEnchantmentHandler.*;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EADC_DefaultSystemInitializer {

    public static void initialize() {
        API api = new API();

        /// Registering Elements
        //api.createElement("physical"); //throws bug, because there is no physical school in ISS
        api.createElement("fire");
        api.createElement("ice");
        api.createElement("lightning");
        api.createElement("holy");
        api.createElement("ender");
        api.createElement("blood");
        api.createElement("nature");
        api.createElement("evocation");
        api.createElement("eldritch");
        api.createElement("sound");
        api.createElement("geo");
        api.createElement("aqua");
        api.createElement("technomancy");
        api.createElement("abyssal");

        /// Registering & Assigning Ailments
        api.createDamagingAilment("ignite");
        api.addDefaultAilmentToElement("fire", "ignite");
        api.addDamagingAilmentEffect("ignite", new IgniteEffect());

        api.createNonDamagingAilment("chill");
        api.addDefaultAilmentToElement("ice", "chill");
        api.addNonDamagingAilmentEffect("chill", new ChillEffect());

        api.createNonDamagingAilment("freeze");
        api.addDefaultAilmentToElement("ice", "freeze");
        api.addNonDamagingAilmentEffect("freeze", new FreezeEffect());

        api.createNonDamagingAilment("shock");
        api.addDefaultAilmentToElement("lightning", "shock");
        api.addNonDamagingAilmentEffect("shock", new ShockEffect());

        api.createDamagingAilment("poison");
        api.addDefaultAilmentToElement("nature", "poison");
        api.addDamagingAilmentEffect("poison", new PoisonEffect());

        api.createDamagingAilment("bleed");
        //api.addDefaultAilmentToElement("physical", "bleed");
        api.addDamagingAilmentEffect("bleed", new BleedEffect());

        /// Non-default Ailments (to be used as alternatives to Ignite/Freeze/Shock)
        api.createNonDamagingAilment("scorch");
        api.addNonDamagingAilmentEffect("scorch", new ScorchEffect());

        api.createNonDamagingAilment("brittle");
        api.addNonDamagingAilmentEffect("brittle", new BrittleEffect());

        api.createNonDamagingAilment("sap");
        api.addNonDamagingAilmentEffect("sap", new SapEffect());

        /// Ailment Builder system
        api.createNonDamagingAilment("viral");
        api.addDefaultAilmentToElement("blood", "viral");
        api.addNonDamagingAilmentEffect("viral", new NonDamagingAilmentEffectBuilder()
                .setNamespace(ElementalAttackDamageCompatMod.MOD_ID)
                .setId("viral")
                .doesVaryEffectDuration(false)
                .doesVaryEffectDuration(false)
                .setBaseDurationInSeconds(3)
                .setEffectStrength(0.5f)
                .stackingMode(AilmentStackingMode.STACKING_THEN_STRONGEST_INTENSITY)
                .setMaxStacks(6)
                .createStackPayload((defender, instance) -> {
                    var attr = defender.getAttribute(Attributes.MAX_HEALTH);
                    if (attr == null) return null;

                    UUID uuid = UUID.randomUUID();

                    AttributeModifier mod = new AttributeModifier(
                            uuid,
                            "viral_debuff",
                            -0.5,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    );

                    attr.addPermanentModifier(mod);
                    return uuid; // payload
                })
                .onStackExpire((defender, instance, stack, payload) -> {
                    if (payload instanceof UUID uuid) {
                        var attr = defender.getAttribute(Attributes.MAX_HEALTH);
                        if (attr != null) attr.removeModifier(uuid);

                        if (defender.getHealth() > defender.getMaxHealth()) {
                            defender.setHealth(defender.getMaxHealth());
                        }
                    }
                })
                .build());

        //todo: doesn't work 'til there are 50 stacks
        NonDamagingAilmentEffect OATH_OF_SPRING_SHOCK =
                new NonDamagingAilmentEffectBuilder()
                        .setNamespace(ElementalAttackDamageCompatMod.MOD_ID)
                        .setId("oath_of_spring_shock")
                        .doesVaryEffectStrength(true)
                        .doesVaryEffectDuration(false)
                        .setIfDiscardIfBelowMinimumEffectStrength(true)
                        .setEffectStrengthCoefficient(1f)
                        .setMinEffectStrengthToKeep(0.05f)
                        .setBaseDurationInSeconds(40)
                        .stackingMode(AilmentStackingMode.STACKING_THEN_STRONGEST_INTENSITY)
                        .setMaxStacks(50)
                        .createStackPayload((defender, instance) -> {

                            return null;
                        })
                        .build();

        api.createNonDamagingAilment("oath_of_spring_shock");
        api.addNonDamagingAilmentEffect("oath_of_spring_shock", OATH_OF_SPRING_SHOCK);
    }

    public static void delayedInitialization() {
        API api = new API();

        //Fire
        float fireAttackDamagePerFireAspectLevel = (float) (Config.useGlobalConstantForAddingDamageFromElementalAspects ? Config.globalConstantForAddingDamageFromElementalAspects : Config.fireAttackDamagePerFireAspectLevel);
        float igniteChancePerFireAspectLevel = (float) Config.igniteChancePerFireAspectLevel;

        List<PseudoAttribute> fireAspectAttributes = new ArrayList<>();
        fireAspectAttributes.add(new PseudoAttribute(ModAttributes.getAttribute(String.format("%s:fire_attack_damage", ElementalAttackDamageCompatMod.MOD_ID)), AttributeModifier.Operation.ADDITION, fireAttackDamagePerFireAspectLevel));
        if(Config.enableIgniteChanceOnFireAspect) fireAspectAttributes.add(new PseudoAttribute(ModAttributes.getAttribute(String.format("%s:chance_to_inflict_ignite", ElementalAttackDamageCompatMod.MOD_ID)), AttributeModifier.Operation.ADDITION, igniteChancePerFireAspectLevel));
        api.attributizeEnchantment(Enchantments.FIRE_ASPECT, "fire_aspect", fireAspectAttributes);

        //Ice
        float iceAttackDamagePerIceAspectLevel = (float) (Config.useGlobalConstantForAddingDamageFromElementalAspects ? Config.globalConstantForAddingDamageFromElementalAspects : Config.iceAttackDamagePerIceAspectLevel);
        float freezeChancePerIceAspectLevel = (float) Config.freezeChancePerIceAspectLevel;

        List<PseudoAttribute> iceAspectAttributes = new ArrayList<>();
        iceAspectAttributes.add(new PseudoAttribute(ModAttributes.getAttribute(String.format("%s:ice_attack_damage", ElementalAttackDamageCompatMod.MOD_ID)), AttributeModifier.Operation.ADDITION, iceAttackDamagePerIceAspectLevel));
        if(Config.enableFreezeChanceOnIceAspect) iceAspectAttributes.add(new PseudoAttribute(ModAttributes.getAttribute(String.format("%s:chance_to_inflict_freeze", ElementalAttackDamageCompatMod.MOD_ID)), AttributeModifier.Operation.ADDITION, freezeChancePerIceAspectLevel));
        api.attributizeEnchantment(ModEnchantments.getEnchantment(String.format("%s:ice_aspect", ElementalAttackDamageCompatMod.MOD_ID)), "ice_aspect", iceAspectAttributes);

        //Lightning
        float lightningAttackDamagePerLightningAspectLevel = (float) (Config.useGlobalConstantForAddingDamageFromElementalAspects ? Config.globalConstantForAddingDamageFromElementalAspects : Config.lightningAttackDamagePerLightningAspectLevel);
        float shockChancePerLightningAspectLevel = (float) Config.shockChancePerLightningAspectLevel;

        List<PseudoAttribute> lightningAspectAttributes = new ArrayList<>();
        lightningAspectAttributes.add(new PseudoAttribute(ModAttributes.getAttribute(String.format("%s:lightning_attack_damage", ElementalAttackDamageCompatMod.MOD_ID)), AttributeModifier.Operation.ADDITION, lightningAttackDamagePerLightningAspectLevel));
        if(Config.enableShockChanceOnLightningAspect) lightningAspectAttributes.add(new PseudoAttribute(ModAttributes.getAttribute(String.format("%s:chance_to_inflict_shock", ElementalAttackDamageCompatMod.MOD_ID)), AttributeModifier.Operation.ADDITION, shockChancePerLightningAspectLevel));
        api.attributizeEnchantment(ModEnchantments.getEnchantment(String.format("%s:lightning_aspect", ElementalAttackDamageCompatMod.MOD_ID)), "lightning_aspect", lightningAspectAttributes);

        //Handling Apotheosis gems ourselves. Gems buff levels of enchantments, but we must take into account enchantments that we have Attributized.
        //Each gem has different effects depending on the weapon or armor, so we must specify the one we need.
        api.registerNewApotheosisGemEnchantmentBuff("apotheosis:core/ruby", LootCategory.SWORD, Enchantments.FIRE_ASPECT, rarity -> switch (rarity) {
            case "apotheosis:epic", "apotheosis:mythic" -> 1;
            case "apotheosis:ancient" -> 2;
            default -> 0;
        });
    }
}
