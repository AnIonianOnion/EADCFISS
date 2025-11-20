package com.anionianonion.elementalattackdamagecompat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public interface ModAttributes {

    DeferredRegister<Attribute> ATTRIBUTES_REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, ElementalAttackDamageCompatMod.MOD_ID);
    List<String> ELEMENTAL_ATTRIBUTE_NAMES = List.of("fire", "ice", "lightning", "holy", "ender", "blood", "evocation", "nature", "eldritch");

    UUID GLOBAL_CRIT_CHANCE_UUID = UUID.fromString("d6c25898-7a2f-49cf-9cca-c523cb22823b");
    UUID GLOBAL_CRIT_DAMAGE_UUID = UUID.fromString("d07454b9-f775-4d67-a635-e2e1632a3ec7");

    //based on https://www.youtube.com/watch?v=0gVO99YtaxE&t=5m48s&ab_channel=Kapitencraft
    //Changed -1 to null for more control, in case attribute values are negative.
    static Double getAttributeValue(LivingEntity le, Attribute a) {
        AttributeInstance instance = le.getAttribute(a);
        if(instance == null) return null;
        else return instance.getValue();
    }

    /**
     For a given living entity, try to get find an attribute and return its value based on a given key. Returns a -1 if not found.
     */
    static Float getAttributeValue(LivingEntity le, String key) {

        Attribute attribute = getAttribute(key);
        Double value = getAttributeValue(le, attribute);

        if(value == null) return null;
        else return value.floatValue();
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
        }
        ATTRIBUTES_REGISTRY.register("spell_crit_chance",
                () -> new RangedAttribute("spell.crit_chance", 0.1, 0, Double.POSITIVE_INFINITY));
        ATTRIBUTES_REGISTRY.register("spell_crit_damage",
                () -> new RangedAttribute("spell.crit_damage", 1.5, 0, Double.POSITIVE_INFINITY));

        ATTRIBUTES_REGISTRY.register(eventBus);
    }
}
