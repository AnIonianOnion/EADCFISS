package com.anionianonion.elementalattackdamagecompat;

import com.anionianonion.elementalattackdamagecompat.ailments.Ailment;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffectRegistry;
import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.NonDamagingAilmentEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

public interface ModAttributes {

    DeferredRegister<Attribute> ATTRIBUTES_REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ElementalAttackDamageCompatMod.MOD_ID);
    List<String> ELEMENTAL_ATTRIBUTE_NAMES = Arrays.stream(Element.values()).map(e -> e.name().toLowerCase()).toList();
    List<String> AILMENT_NAMES = Arrays.stream(Ailment.values()).map(a -> a.name().toLowerCase()).toList();
    List<String> NON_DAMAGING_AILMENT_NAMES = AILMENT_NAMES.stream().filter(a -> AilmentEffectRegistry.get(Ailment.valueOf(a.toUpperCase())) instanceof NonDamagingAilmentEffect).toList();
    int numTypes = 5;

    Map<String, String> customSchoolToResistAttributeKey = new HashMap<>();

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
                    () -> new RangedAttribute(String.format("attack_damage.%s", elementName), 0, 0, Double.POSITIVE_INFINITY));
            ATTRIBUTES_REGISTRY.register(String.format("%s_spell_damage", elementName),
                    () -> new RangedAttribute(String.format("spell_damage.%s", elementName), 0, 0, Double.POSITIVE_INFINITY));
            ATTRIBUTES_REGISTRY.register(String.format("%s_damage", elementName),
                    () -> new RangedAttribute(String.format("attack_and_spell_damage.%s", elementName), 0, 0, Double.POSITIVE_INFINITY));
            ATTRIBUTES_REGISTRY.register(String.format("%s_max_resistance", elementName),
                    () -> new RangedAttribute(String.format("max_resistance.%s", elementName), 0.75, 0.5, 0.9));
            ATTRIBUTES_REGISTRY.register(String.format("duration_of_%s_ailments", elementName),
                    () -> new RangedAttribute(String.format("ailment.%s_ailments_duration", elementName), 0, 0, Double.POSITIVE_INFINITY));
        }

        ATTRIBUTES_REGISTRY.register("spell_damage_multiplier",
                () -> new RangedAttribute("multipliers.spell_damage", 1, 0, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("attack_damage_multiplier",
                () -> new RangedAttribute("multipliers.attack_damage", 1, 0, Double.POSITIVE_INFINITY));

        ATTRIBUTES_REGISTRY.register("attack_crit_chance",
                () -> new RangedAttribute("attack.crit_chance", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("attack_crit_damage",
                () -> new RangedAttribute("attack.crit_damage", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));

        ATTRIBUTES_REGISTRY.register("spell_crit_chance",
                () -> new RangedAttribute("spell.crit_chance", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("spell_crit_damage",
                () -> new RangedAttribute("spell.crit_damage", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));

        ATTRIBUTES_REGISTRY.register("global_crit_chance",
                () -> new RangedAttribute("global.crit_chance", 0.05, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("global_crit_damage",
                () -> new RangedAttribute("global.crit_damage", 1.5, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY));

        for(int i = 1; i <= numTypes; i++) {
            int finalI = i;
            ATTRIBUTES_REGISTRY.register(String.format("type_%s_damage_multiplier", i),
                    () -> new RangedAttribute(String.format("multipliers.type_%s_damage", finalI), 1, 0, Double.POSITIVE_INFINITY));
        }

        ATTRIBUTES_REGISTRY.register("spell_suppression_chance",
                () -> new RangedAttribute("spell_suppression.chance", 0, 0, 1));
        ATTRIBUTES_REGISTRY.register("spell_suppression_prevented",
                () -> new RangedAttribute("spell_suppression.prevented", 0.5, 0, 1));
        ATTRIBUTES_REGISTRY.register("spell_dodge_chance",
                () -> new RangedAttribute("spell_dodge.chance", 0, 0, 1));


        ATTRIBUTES_REGISTRY.register("all_elemental_resistances", () -> new RangedAttribute("bonus_all_elemental_resistances", 0, -1, 1));
        ATTRIBUTES_REGISTRY.register("max_scorch_effect", () -> new RangedAttribute("effect.scorch.max", 0.3f, 0, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("max_shock_effect", () -> new RangedAttribute("effect.shock.max", 0.5f, 0, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("max_brittle_effect", () -> new RangedAttribute("effect.shock.max", 0.06f, 0, Double.POSITIVE_INFINITY));

        for(String ailmentName : AILMENT_NAMES) {
            ATTRIBUTES_REGISTRY.register(String.format("chance_to_%s", ailmentName),
                    () -> new RangedAttribute(String.format("ailment.%s_chance", ailmentName), 0, 0, 1));
            ATTRIBUTES_REGISTRY.register(String.format("%s_duration", ailmentName),
                    () -> new RangedAttribute(String.format("ailment.%s_duration", ailmentName), 0, 0, 1));
        }
        for(String ailmentName : NON_DAMAGING_AILMENT_NAMES) {
            ATTRIBUTES_REGISTRY.register(String.format("%s_effect", ailmentName),
                    () -> new RangedAttribute(String.format("ailment.%s_effect", ailmentName), 0, 0, Double.POSITIVE_INFINITY));
        }

        ATTRIBUTES_REGISTRY.register(eventBus);

        customSchoolToResistAttributeKey.put("sound", "alshanex_familiars:sound_magic_resist");
        customSchoolToResistAttributeKey.put("geo", "gtbcs_geomancy_plus:geo_magic_resist");
        customSchoolToResistAttributeKey.put("aqua", "traveloptics:aqua_magic_resist");
        customSchoolToResistAttributeKey.put("technomancy", "cataclysm_spellbooks:technomancy_magic_resist");
        customSchoolToResistAttributeKey.put("abyssal", "cataclysm_spellbooks:abyssal_magic_resist");
    }
}
