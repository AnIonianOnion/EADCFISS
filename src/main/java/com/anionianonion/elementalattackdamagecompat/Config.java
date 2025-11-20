package com.anionianonion.elementalattackdamagecompat;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ElementalAttackDamageCompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS;

    private static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_FALLING_MELEE_CRIT;
    private static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_FULLY_CHARGED_BOW_CRIT;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_CRIT_CHANCE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_CRIT_DAMAGE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_SPELL_CRIT_CHANCE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_SPELL_CRIT_DAMAGE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.DoubleValue MOD_COMPAT_CRIT_DAMAGE_OFFSET;
    private static final ForgeConfigSpec.BooleanValue APPLY_CRIT_ATTRIBUTES_GLOBALLY;


    static {
        COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS = BUILDER
                .comment("In vanilla, when adding a new attribute to a weapon with only base attributes (such as attack damage and attack speed), it will overwrite them. " +
                        "\nIf you attempt to add 10 fire damage to a diamond sword that deals 7 damage, you would expect 17 damage. " +
                        "\nBut what happens is the +6 damage the sword gives is overwritten, so it only deals 11 damage. " +
                        "\nIf this setting is true, weapons' base damage and attack speed will be kept, when adding a new attribute." +
                        "\nDefault: true")
                .define("copyWeaponsDefaultAttributesToNewWeapons", true);

        BUILDER.push("Crit");
            DISABLE_VANILLA_FALLING_MELEE_CRIT = BUILDER
                    .comment("Disable vanilla's forced 100% crit chance that happens when a player melee attacks as they are falling." +
                            "\nDefault: false")
                    .define("disableVanillaFallingCrit", false);
            DISABLE_VANILLA_FULLY_CHARGED_BOW_CRIT = BUILDER
                    .comment("Disables vanilla's forced 100% crit chance that happens when a bow is fully charged, or if an arrow is shot out of a crossbow." +
                            "\nDefault: false")
                    .define("disableVanillaFullyChargedBowCrits", false);
            MOD_COMPAT_CRIT_CHANCE_ATTRIBUTE_ID = BUILDER
                    .comment("This is an id of an attribute from another mod that provides attributes (like Apothic Attributes aka. attributeslib). " +
                            "Specifically, this option will use that mod's crit chance attribute as its own for attacks." +
                            "\nDefault: \"attributeslib:crit_chance\"")
                    .define("critChanceAttributeId", "attributeslib:crit_chance");
            MOD_COMPAT_CRIT_DAMAGE_ATTRIBUTE_ID = BUILDER
                    .comment("Same as the last one, but for crit damage instead." +
                            "\nDefault: \"attributeslib:crit_damage\"")
                    .define("critDamageAttributeId", "attributeslib:crit_damage");
            APPLY_CRIT_ATTRIBUTES_GLOBALLY = BUILDER
                    .comment("Whether the above crit attributes will also apply to spells, such as from Iron's Spells and Spellbooks. " +
                            "If false, it will only apply to attacks." +
                            "\nDefault: true")
                    .define("applyCritAttributesGlobally", true);
            MOD_COMPAT_SPELL_CRIT_CHANCE_ATTRIBUTE_ID = BUILDER
                    .comment("This is an id of an attribute that provides crit chance, but specifically for spells. " +
                            "\nIf applyCritAttributesGlobally is true, crit chance & crit damage from the above attributes will be added to these values when calculating damage." +
                            "\nDefault: \"elementalattackdamagecompat:spell_crit_chance\"")
                    .define("spellCritChanceAttributeId", "elementalattackdamagecompat:spell_crit_chance");
            MOD_COMPAT_SPELL_CRIT_DAMAGE_ATTRIBUTE_ID = BUILDER
                    .comment("Same as the last one, but for crit damage instead." +
                            "\nDefault: \"elementalattackdamagecompat:spell_crit_damage\"")
                    .define("spellCritDamageAttributeId", "elementalattackdamagecompat:spell_crit_damage");
            MOD_COMPAT_CRIT_DAMAGE_OFFSET = BUILDER
                    .comment("if 'applyCritAttributesGlobally' is true: crit damage formula is the sum of the attribute values from 'spellCritDamageAttributeId' + 'critDamageAttributeId', + this value. " +
                            "\nDefault: -1")
                    .defineInRange("modCompatCritDamageOffset", -1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            BUILDER.pop();
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean copyWeaponsDefaultAttributesToNewWeapons;

    public static boolean disableVanillaFallingCrit, disableVanillaFullyChargedBowCrit;
    public static String attackCritChanceAttributeId, attackCritDamageAttributeId;
    public static boolean applyCritAttributesGlobally;
    public static String spellCritChanceAttributeId, spellCritDamageAttributeId;
    public static double modCompatCritDamageOffset;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        copyWeaponsDefaultAttributesToNewWeapons = COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS.get();

        disableVanillaFallingCrit = DISABLE_VANILLA_FALLING_MELEE_CRIT.get();
        disableVanillaFullyChargedBowCrit = DISABLE_VANILLA_FULLY_CHARGED_BOW_CRIT.get();
        attackCritChanceAttributeId = MOD_COMPAT_CRIT_CHANCE_ATTRIBUTE_ID.get();
        attackCritDamageAttributeId = MOD_COMPAT_CRIT_DAMAGE_ATTRIBUTE_ID.get();
        applyCritAttributesGlobally = APPLY_CRIT_ATTRIBUTES_GLOBALLY.get();
        spellCritChanceAttributeId = MOD_COMPAT_SPELL_CRIT_CHANCE_ATTRIBUTE_ID.get();
        spellCritDamageAttributeId = MOD_COMPAT_SPELL_CRIT_DAMAGE_ATTRIBUTE_ID.get();
        modCompatCritDamageOffset = MOD_COMPAT_CRIT_DAMAGE_OFFSET.get();
    }
}
