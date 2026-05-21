package com.anionianonion.elementalattackdamagecompat;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffectRegistry;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentsRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public interface ModAttributes {

    DeferredRegister<Attribute> ATTRIBUTES_REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ElementalAttackDamageCompatMod.MOD_ID);
    List<String> ELEMENTAL_ATTRIBUTE_NAMES = AilmentsRegistry.getAll().keySet().stream().toList();
    Set<String> AILMENT_NAMES = AilmentEffectRegistry.getAllAilments();
    Set<String> DAMAGING_AILMENT_NAMES = AilmentEffectRegistry.getDamagingAilments();
    Set<String> NON_DAMAGING_AILMENT_NAMES = AilmentEffectRegistry.getNonDamagingAilments();

    int numTypes = 5;

    Map<String, String> customSchoolToResistAttributeKey = new HashMap<>();
    Map<String, Integer> resistAttributeKeyToResistOffset = new HashMap<>();

    //based on https://www.youtube.com/watch?v=0gVO99YtaxE&t=5m48s&ab_channel=Kapitencraft
    //Changed -1 to null for more control, in case attribute values are negative.
    static Double getAttributeValue(LivingEntity le, Attribute a) {
        AttributeInstance instance = le.getAttribute(a);
        if(instance == null) return null;
        else return instance.getValue();
    }

    /**
     For a given living entity, try to get find an attribute and return its value based on a given key. Returns null if not found.
     */
    static Float getAttributeValue(LivingEntity le, String key) {

        Attribute attribute = getAttribute(key);
        Double value = getAttributeValue(le, attribute);

        if(value == null) return null;
        return value.floatValue();
    }

    static Attribute getAttribute(String key) {
        ResourceLocation attributeKey = ResourceLocation.tryParse(key);
        if (attributeKey == null) return null;

        return ForgeRegistries.ATTRIBUTES.getValue(attributeKey);
    }


    static void register(IEventBus eventBus) {
        //RangedAttribute arguments: name, default value, min, max
        for (String elementName : ELEMENTAL_ATTRIBUTE_NAMES) {
            ATTRIBUTES_REGISTRY.register(String.format("%s_attack_damage", elementName),
                    () -> new RangedAttribute(String.format("attribute.attack_damage.%s", elementName), 0, 0, Double.POSITIVE_INFINITY));
            ATTRIBUTES_REGISTRY.register(String.format("%s_spell_damage", elementName),
                    () -> new RangedAttribute(String.format("attribute.spell_damage.%s", elementName), 0, 0, Double.POSITIVE_INFINITY));
            ATTRIBUTES_REGISTRY.register(String.format("%s_damage", elementName),
                    () -> new RangedAttribute(String.format("attribute.attack_and_spell_damage.%s", elementName), 0, 0, Double.POSITIVE_INFINITY));
            ATTRIBUTES_REGISTRY.register(String.format("%s_max_resistance", elementName),
                    () -> new RangedAttribute(String.format("attribute.max_resistance.%s", elementName), 0.75, 0.5, 0.9));
            ATTRIBUTES_REGISTRY.register(String.format("duration_of_%s_ailments", elementName),
                    () -> new RangedAttribute(String.format("attribute.ailment.duration_of_%s_ailments", elementName), 0, 0, Double.POSITIVE_INFINITY));
        }
        ATTRIBUTES_REGISTRY.register("all_elemental_resistances", () -> new RangedAttribute("attribute.bonus_all_elemental_resistances", 0, -1, 1));

        ATTRIBUTES_REGISTRY.register("spell_damage_multiplier",
                () -> new RangedAttribute("attribute.multipliers.spell_damage", 1, 0, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("attack_damage_multiplier",
                () -> new RangedAttribute("attribute.multipliers.attack_damage", 1, 0, Double.POSITIVE_INFINITY));

        ATTRIBUTES_REGISTRY.register("attack_crit_chance",
                () -> new RangedAttribute("attribute.attack.crit_chance", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("attack_crit_damage",
                () -> new RangedAttribute("attribute.attack.crit_damage", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));

        ATTRIBUTES_REGISTRY.register("spell_crit_chance",
                () -> new RangedAttribute("attribute.spell.crit_chance", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("spell_crit_damage",
                () -> new RangedAttribute("attribute.spell.crit_damage", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));

        ATTRIBUTES_REGISTRY.register("global_crit_chance",
                () -> new RangedAttribute("attribute.global.crit_chance", 0.05, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("global_crit_damage",
                () -> new RangedAttribute("attribute.global.crit_damage", 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));

        for(int i = 1; i <= numTypes; i++) {
            int finalI = i;
            ATTRIBUTES_REGISTRY.register(String.format("type_%s_damage_multiplier", i),
                    () -> new RangedAttribute(String.format("attribute.multipliers.type_%s_damage", finalI), 1, 0, Double.POSITIVE_INFINITY));
        }

        ATTRIBUTES_REGISTRY.register("spell_suppression_chance",
                () -> new RangedAttribute("attribute.spell_suppression.chance", 0, 0, 1));
        ATTRIBUTES_REGISTRY.register("spell_suppression_prevented",
                () -> new RangedAttribute("attribute.spell_suppression.prevented", 0.5, 0, 1));
        ATTRIBUTES_REGISTRY.register("spell_dodge_chance",
                () -> new RangedAttribute("attribute.spell_dodge.chance", 0, 0, 1));

        for(String ailmentName : AILMENT_NAMES) {
            ATTRIBUTES_REGISTRY.register(String.format("chance_to_inflict_%s", ailmentName),
                    () -> new RangedAttribute(String.format("attribute.ailment.chance_to_inflict_%s", ailmentName), 0, 0, 1));
            ATTRIBUTES_REGISTRY.register(String.format("%s_duration", ailmentName),
                    () -> new RangedAttribute(String.format("attribute.ailment.%s_duration", ailmentName), 0, 0, 1));
        }
        for(String ailmentName : NON_DAMAGING_AILMENT_NAMES) {
            ATTRIBUTES_REGISTRY.register(String.format("%s_effect", ailmentName),
                    () -> new RangedAttribute(String.format("attribute.ailment.%s_effect", ailmentName), 0, 0, Double.POSITIVE_INFINITY));
        }

        ATTRIBUTES_REGISTRY.register("max_scorch_effect", () -> new RangedAttribute("attribute.ailment.max_scorch_effect", 0.3f, 0, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("max_shock_effect", () -> new RangedAttribute("attribute.ailment.max_shock_effect", 0.5f, 0, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("max_oath_of_spring_shock_effect", () -> new RangedAttribute("attribute.ailment.max_oath_of_spring_shock_effect", 0.02f, 0, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("max_brittle_effect", () -> new RangedAttribute("attribute.ailment.max_brittle_effect", 0.06f, 0, Double.POSITIVE_INFINITY));

        ATTRIBUTES_REGISTRY.register(eventBus);

        customSchoolToResistAttributeKey.put("sound", "alshanex_familiars:sound_magic_resist");
        customSchoolToResistAttributeKey.put("geo", "gtbcs_geomancy_plus:geo_magic_resist");
        customSchoolToResistAttributeKey.put("aqua", "traveloptics:aqua_magic_resist");
        customSchoolToResistAttributeKey.put("technomancy", "cataclysm_spellbooks:technomancy_magic_resist");
        customSchoolToResistAttributeKey.put("abyssal", "cataclysm_spellbooks:abyssal_magic_resist");
    }
}
