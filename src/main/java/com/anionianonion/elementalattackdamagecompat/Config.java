package com.anionianonion.elementalattackdamagecompat;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ElementalAttackDamageCompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    private static final ForgeConfigSpec.BooleanValue COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS;

    private static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_FALLING_MELEE_CRIT;
    private static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_FULLY_CHARGED_BOW_CRIT;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_ATTACK_CRIT_CHANCE_ATTRIBUTE_ID, MOD_COMPAT_ATTACK_CRIT_DAMAGE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_GLOBAL_CRIT_CHANCE_ATTRIBUTE_ID, MOD_COMPAT_GLOBAL_CRIT_DAMAGE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_SPELL_CRIT_CHANCE_ATTRIBUTE_ID, MOD_COMPAT_SPELL_CRIT_DAMAGE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.DoubleValue MOD_COMPAT_CRIT_CHANCE_OFFSET, MOD_COMPAT_CRIT_DAMAGE_OFFSET;
    private static final ForgeConfigSpec.BooleanValue APPLY_ATTACK_CRIT_ATTRIBUTES_GLOBALLY;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TYPE_1_SCHOOLS, TYPE_2_SCHOOLS, TYPE_3_SCHOOLS;


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

            MOD_COMPAT_ATTACK_CRIT_CHANCE_ATTRIBUTE_ID = BUILDER
                    .comment("\nThis is an id of an attribute. (Can be from another mod that provides attributes like Apothic Attributes aka. attributeslib.) " +
                            "Specifically, this option will use that mod's crit chance attribute as its own for attacks." +
                            "\nDefault: \"elementalattackdamagecompat:attack_crit_chance\"")
                    .define("attackCritChanceAttributeId", "elementalattackdamagecompat:attack_crit_chance");
            MOD_COMPAT_ATTACK_CRIT_DAMAGE_ATTRIBUTE_ID = BUILDER
                    .comment("Same as the last one, but for crit damage instead." +
                            "\nDefault: \"elementalattackdamagecompat:attack_crit_damage\"")
                    .define("attackCritDamageAttributeId", "elementalattackdamagecompat:attack_crit_damage");
            APPLY_ATTACK_CRIT_ATTRIBUTES_GLOBALLY = BUILDER
                    .comment("If you already have a global crit attribute you want to use, this option modifiers whether the two above crit attributes will also apply to spells, such as from Iron's Spells and Spellbooks. " +
                            "If false, the crit attributes will only apply to attacks, and this mod will use its global attributes instead." +
                            "\nDefault: false")
                    .define("applyAttackCritAttributesGlobally", false);

            MOD_COMPAT_GLOBAL_CRIT_CHANCE_ATTRIBUTE_ID = BUILDER
                    .comment("\nIf applyAttackCritAttributesGlobally is true, then this and the next option won't be used. " +
                            "\nThis attribute id gets the attribute used, and it will affect the crit chance of both attacks and spells." +
                            "Default: \"elementalattackdamagecompat:global_crit_chance\"")
                    .define("globalCritChanceAttributeId", "elementalattackdamagecompat:global_crit_chance");
            MOD_COMPAT_GLOBAL_CRIT_DAMAGE_ATTRIBUTE_ID = BUILDER
                    .comment("Same as the last one, but for crit damage instead." +
                            "Default: \"elementalattackdamagecompat:global_crit_damage\"")
                    .define("globalCritDamageAttributeId", "elementalattackdamagecompat:global_crit_damage");

            MOD_COMPAT_SPELL_CRIT_CHANCE_ATTRIBUTE_ID = BUILDER
                    .comment("\nThis is the id of an attribute that provides crit chance, but specifically for spells. " +
                            "\nIf applyAttackCritAttributesGlobally is true, crit chance & crit damage from the above attributes will be added to these values when calculating damage." +
                            "\nDefault: \"elementalattackdamagecompat:spell_crit_chance\"")
                    .define("spellCritChanceAttributeId", "elementalattackdamagecompat:spell_crit_chance");
            MOD_COMPAT_SPELL_CRIT_DAMAGE_ATTRIBUTE_ID = BUILDER
                    .comment("Same as the last one, but for crit damage instead." +
                            "\nDefault: \"elementalattackdamagecompat:spell_crit_damage\"")
                    .define("spellCritDamageAttributeId", "elementalattackdamagecompat:spell_crit_damage");
            MOD_COMPAT_CRIT_CHANCE_OFFSET = BUILDER
                    .comment("if 'applyAttackCritAttributesGlobally' is true: " +
                            "\ntotal *spell* crit chance will be the sum of attack crit chance + spell crit chance + this value." +
                            "\ntotal *attack* crit chance will be the sum of attack crit chance + this value." +
                            "\nif false: " +
                            "\n\ttotal *spell* crit chance will be the sum of global crit chance + spell crit chance + this value, and " +
                            "\n\ttotal *attack* crit chance will be the sum of global crit chance + attack crit chance + this value.")
                    .defineInRange("modCompatCritChanceOffset", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            MOD_COMPAT_CRIT_DAMAGE_OFFSET = BUILDER
                    .comment("if 'applyAttackCritAttributesGlobally' is true: total *spell* crit damage is the sum of the sum of attack crit damage + spell crit damage + this value." +
                            "\ntotal *attack* crit damage will be the sum of attack crit damage + this value." +
                            "\nif false: " +
                            "\n\ttotal *spell* crit damage will be the sum of global crit damage + spell crit damage + this value, and " +
                            "\n\ttotal *attack* crit damage will be the sum of global crit damage + attack crit damage + this value." +
                            "\nDefault: -1 (minus 100% flat crit damage)")
                    .defineInRange("modCompatCritDamageOffset", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        BUILDER.pop();
        
        BUILDER.push("Classify Types");
            TYPE_1_SCHOOLS = BUILDER
                    .comment("Spells from schools in this list will be categorized into Type 1 and benefit from \"Type 1\" damage modifiers. Name customizable.")
                    .defineListAllowEmpty("type_1_schools", List.of("fire", "ice", "lightning"), Config::validateIsString);
            TYPE_2_SCHOOLS = BUILDER
                    .comment("Spells from schools in this list will be categorized into Type 2 and benefit from \"Type 2\" damage modifiers. Name customizable.")
                    .defineListAllowEmpty("type_2_schools", List.of("holy", "ender", "eldritch"), Config::validateIsString);
            TYPE_3_SCHOOLS = BUILDER
                    .comment("Spells from schools in this list will be categorized into Type 3 and benefit from \"Type 3\" damage modifiers. Name customizable.")
                    .defineListAllowEmpty("type_3_schools", List.of("blood", "nature", "evocation"), Config::validateIsString);

    }

    private static boolean validateIsString(Object object) {
        return object instanceof String;
    }

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean copyWeaponsDefaultAttributesToNewWeapons;

    public static boolean disableVanillaFallingCrit, disableVanillaFullyChargedBowCrit;
    public static String attackCritChanceAttributeId, attackCritDamageAttributeId;
    public static boolean applyAttackCritAttributesGlobally;
    public static String globalCritChanceAttributeId, globalCritDamageAttributeId;
    public static String spellCritChanceAttributeId, spellCritDamageAttributeId;
    public static double modCompatCritChanceOffset, modCompatCritDamageOffset;
    public static List<String> type1schools, type2schools, type3schools;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        copyWeaponsDefaultAttributesToNewWeapons = COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS.get();

        disableVanillaFallingCrit = DISABLE_VANILLA_FALLING_MELEE_CRIT.get();
        disableVanillaFullyChargedBowCrit = DISABLE_VANILLA_FULLY_CHARGED_BOW_CRIT.get();
        attackCritChanceAttributeId = MOD_COMPAT_ATTACK_CRIT_CHANCE_ATTRIBUTE_ID.get();
        attackCritDamageAttributeId = MOD_COMPAT_ATTACK_CRIT_DAMAGE_ATTRIBUTE_ID.get();
        applyAttackCritAttributesGlobally = APPLY_ATTACK_CRIT_ATTRIBUTES_GLOBALLY.get();
        globalCritChanceAttributeId = MOD_COMPAT_GLOBAL_CRIT_CHANCE_ATTRIBUTE_ID.get();
        globalCritDamageAttributeId = MOD_COMPAT_GLOBAL_CRIT_DAMAGE_ATTRIBUTE_ID.get();
        spellCritChanceAttributeId = MOD_COMPAT_SPELL_CRIT_CHANCE_ATTRIBUTE_ID.get();
        spellCritDamageAttributeId = MOD_COMPAT_SPELL_CRIT_DAMAGE_ATTRIBUTE_ID.get();
        modCompatCritChanceOffset = MOD_COMPAT_CRIT_CHANCE_OFFSET.get();
        modCompatCritDamageOffset = MOD_COMPAT_CRIT_DAMAGE_OFFSET.get();
        type1schools = new ArrayList<>(TYPE_1_SCHOOLS.get());
        type2schools = new ArrayList<>(TYPE_2_SCHOOLS.get());
        type3schools = new ArrayList<>(TYPE_3_SCHOOLS.get());

    }
}
