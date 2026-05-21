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
    private static final ForgeConfigSpec.BooleanValue ENABLE_DEBUG_MODE;
    private static final ForgeConfigSpec.BooleanValue COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS;
    private static final ForgeConfigSpec.BooleanValue ROUND_FINAL_DAMAGE;

    private static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_FALLING_MELEE_CRIT;
    private static final ForgeConfigSpec.BooleanValue DISABLE_VANILLA_FULLY_CHARGED_BOW_CRIT;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_ATTACK_CRIT_CHANCE_ATTRIBUTE_ID, MOD_COMPAT_ATTACK_CRIT_DAMAGE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_GLOBAL_CRIT_CHANCE_ATTRIBUTE_ID, MOD_COMPAT_GLOBAL_CRIT_DAMAGE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.ConfigValue<String> MOD_COMPAT_SPELL_CRIT_CHANCE_ATTRIBUTE_ID, MOD_COMPAT_SPELL_CRIT_DAMAGE_ATTRIBUTE_ID;
    private static final ForgeConfigSpec.DoubleValue MOD_COMPAT_CRIT_CHANCE_OFFSET, MOD_COMPAT_CRIT_DAMAGE_OFFSET;
    private static final ForgeConfigSpec.BooleanValue APPLY_ATTACK_CRIT_ATTRIBUTES_GLOBALLY;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> TYPE_1_SCHOOLS, TYPE_2_SCHOOLS, TYPE_3_SCHOOLS, TYPE_4_SCHOOLS, TYPE_5_SCHOOLS;

    private static final ForgeConfigSpec.BooleanValue USE_GLOBAL_CONSTANT_FOR_ADDING_DAMAGE_FROM_ELEMENTAL_ASPECTS, USE_GLOBAL_CONSTANT_FOR_ADDING_AILMENT_CHANCE_FROM_ELEMENTAL_ASPECTS;
    private static final ForgeConfigSpec.DoubleValue GLOBAL_CONSTANT_FOR_ADDING_DAMAGE_FROM_ELEMENTAL_ASPECTS, GLOBAL_CONSTANT_FOR_ADDING_AILMENT_CHANCE_FROM_ELEMENTAL_ASPECTS;

    private static final ForgeConfigSpec.DoubleValue FIRE_ATTACK_DAMAGE_PER_FIRE_ASPECT_LEVEL;
    private static final ForgeConfigSpec.BooleanValue ENABLE_IGNITE_CHANCE_ON_FIRE_ASPECT;
    private static final ForgeConfigSpec.DoubleValue IGNITE_CHANCE_PER_FIRE_ASPECT_LEVEL;

    private static final ForgeConfigSpec.DoubleValue ICE_ATTACK_DAMAGE_PER_ICE_ASPECT_LEVEL;
    private static final ForgeConfigSpec.BooleanValue ENABLE_FREEZE_CHANCE_ON_ICE_ASPECT;
    private static final ForgeConfigSpec.DoubleValue FREEZE_CHANCE_PER_ICE_ASPECT_LEVEL;

    private static final ForgeConfigSpec.DoubleValue LIGHTNING_ATTACK_DAMAGE_PER_LIGHTNING_ASPECT_LEVEL;
    private static final ForgeConfigSpec.BooleanValue ENABLE_SHOCK_CHANCE_ON_LIGHTNING_ASPECT;
    private static final ForgeConfigSpec.DoubleValue SHOCK_CHANCE_PER_LIGHTNING_ASPECT_LEVEL;

    private static final ForgeConfigSpec.DoubleValue HOLY_ATTACK_DAMAGE_PER_HOLY_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue ENDER_ATTACK_DAMAGE_PER_ENDER_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue BLOOD_ATTACK_DAMAGE_PER_BLOOD_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue NATURE_ATTACK_DAMAGE_PER_NATURE_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue EVOCATION_ATTACK_DAMAGE_PER_EVOCATION_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue ELDRITCH_ATTACK_DAMAGE_PER_ELDRITCH_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue SOUND_ATTACK_DAMAGE_PER_SOUND_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue GEO_ATTACK_DAMAGE_PER_GEO_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue AQUA_ATTACK_DAMAGE_PER_AQUA_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue TECHNOMANCY_ATTACK_DAMAGE_PER_TECHNOMANCY_ASPECT_LEVEL;
    private static final ForgeConfigSpec.DoubleValue ABYSSAL_ATTACK_DAMAGE_PER_ABYSSAL_ASPECT_LEVEL;

    static {
        COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS = BUILDER
                .comment("In vanilla, when adding a new attribute to a weapon with only base attributes (such as attack damage and attack speed), it will overwrite them. " +
                        "\nIf you attempt to add 10 fire damage to a diamond sword that deals 7 damage, you would expect 17 damage. " +
                        "\nBut what happens is the +6 damage the sword gives is overwritten, so it only deals 11 damage. " +
                        "\nIf this setting is true, weapons' base damage and attack speed will be kept, when adding a new attribute." +
                        "\nDefault: true")
                .define("copyWeaponsDefaultAttributesToNewWeapons", true);
        ENABLE_DEBUG_MODE = BUILDER.define("enableDebugMode", false);
        ROUND_FINAL_DAMAGE = BUILDER
                .comment("If true, elemental damage from ISS and this mod will be rounded to the nearest integer.")
                .define("roundFinalDamage", false);

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
                    .comment("This is an id of an attribute. (Can be from another mod that provides attributes like Apothic Attributes aka. attributeslib.) " +
                            "Specifically, this option will use that mod's crit chance attribute as its own for attacks." +
                            "\nDefault: \"elementalattackdamagecompat:attack_crit_chance\"")
                    .define("attackCritChanceAttributeId", "elementalattackdamagecompat:attack_crit_chance");
            MOD_COMPAT_ATTACK_CRIT_DAMAGE_ATTRIBUTE_ID = BUILDER
                    .comment("Same as the last one, but for crit damage instead." +
                            "\nDefault: \"elementalattackdamagecompat:attack_crit_damage\"")
                    .define("attackCritDamageAttributeId", "elementalattackdamagecompat:attack_crit_damage");
            APPLY_ATTACK_CRIT_ATTRIBUTES_GLOBALLY = BUILDER
                    .comment("If you already have a global crit attribute you want to use, this option modifiers whether the two above crit attributes will also apply to spells, " +
                            "\nsuch as from Iron's Spells and Spellbooks. " +
                            "If false, the crit attributes will only apply to attacks, and this mod will use its global attributes instead." +
                            "\nDefault: false")
                    .define("applyAttackCritAttributesGlobally", false);

            MOD_COMPAT_GLOBAL_CRIT_CHANCE_ATTRIBUTE_ID = BUILDER
                    .comment("If applyAttackCritAttributesGlobally is true, then this and the next option won't be used. " +
                            "\nThis attribute id gets the attribute used, and it will affect the crit chance of both attacks and spells." +
                            "\nDefault: \"elementalattackdamagecompat:global_crit_chance\"")
                    .define("globalCritChanceAttributeId", "elementalattackdamagecompat:global_crit_chance");
            MOD_COMPAT_GLOBAL_CRIT_DAMAGE_ATTRIBUTE_ID = BUILDER
                    .comment("Same as the last one, but for crit damage instead." +
                            "Default: \"elementalattackdamagecompat:global_crit_damage\"")
                    .define("globalCritDamageAttributeId", "elementalattackdamagecompat:global_crit_damage");

            MOD_COMPAT_SPELL_CRIT_CHANCE_ATTRIBUTE_ID = BUILDER
                    .comment("This is the id of an attribute that provides crit chance, but specifically for spells. " +
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
                            "\n\ttotal *attack* crit chance will be the sum of global crit chance + attack crit chance + this value." +
                            "\nDefault: 0 (a 1 means minus 100% flat crit chance)")
                    .defineInRange("modCompatCritChanceOffset", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            MOD_COMPAT_CRIT_DAMAGE_OFFSET = BUILDER
                    .comment("if 'applyAttackCritAttributesGlobally' is true: total *spell* crit damage is the sum of the sum of attack crit damage + spell crit damage + this value." +
                            "\ntotal *attack* crit damage will be the sum of attack crit damage + this value." +
                            "\nif false: " +
                            "\n\ttotal *spell* crit damage will be the sum of global crit damage + spell crit damage + this value, and " +
                            "\n\ttotal *attack* crit damage will be the sum of global crit damage + attack crit damage + this value." +
                            "\nDefault: 0 (a 1 means minus 100% flat crit damage)")
                    .defineInRange("modCompatCritDamageOffset", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        BUILDER.pop();
        
        BUILDER.push("Classify Types");
            BUILDER.comment("Spells from schools in these lists will be categorized into Type 1, Type 2, and Type 3, ... and benefit from \"Type 1\", \"Type 2\", \"Type 3\", ... damage modifiers respectively." +
                "\nCategory name is customizable by going to your lang file, and using 'multipliers.type_1_damage', 'multipliers.type_2_damage', and 'multipliers.type_3_damage'.");
            TYPE_1_SCHOOLS = BUILDER
                    .comment("Default list: [\"fire\", \"ice\", \"lightning\", \"geo\", \"aqua\"]")
                    .defineListAllowEmpty("type_1_schools", List.of("fire", "ice", "lightning", "geo", "aqua"), Config::validateIsString);
            TYPE_2_SCHOOLS = BUILDER
                    .comment("Default list: [\"holy\", \"ender\", \"eldritch\", \"abyssal\"]")
                    .defineListAllowEmpty("type_2_schools", List.of("holy", "ender", "eldritch", "abyssal"), Config::validateIsString);
            TYPE_3_SCHOOLS = BUILDER
                    .comment("Default list: [\"blood\", \"nature\", \"evocation\"]")
                    .defineListAllowEmpty("type_3_schools", List.of("blood", "nature", "evocation"), Config::validateIsString);
            TYPE_4_SCHOOLS = BUILDER
                    .comment("Default list: [\"sound\", \"technomancy\"]")
                    .defineListAllowEmpty("type_4_schools", List.of("sound", "technomancy"), Config::validateIsString);
            TYPE_5_SCHOOLS = BUILDER
                    .comment("Default list: []")
                    .defineListAllowEmpty("type_5_schools", List.of(), Config::validateIsString);
        BUILDER.pop();

        BUILDER.push("Enchantments");
            USE_GLOBAL_CONSTANT_FOR_ADDING_DAMAGE_FROM_ELEMENTAL_ASPECTS = BUILDER
                    .comment("If this is true, use a global constant for adding damage from elemental Aspect enchantments, instead of each element using their own value." +
                            "\nDefault: false")
                    .define("useGlobalConstantForAddingDamageFromElementalAspects", false);
            GLOBAL_CONSTANT_FOR_ADDING_DAMAGE_FROM_ELEMENTAL_ASPECTS = BUILDER
                    .comment("This value will be multiplied by the enchantment level to provide the final damage, if useGlobalDamage is true." +
                            "\nDefault: 0.75")
                    .defineInRange("globalConstantForAddingDamageFromElementalAspects", 0.75, 0, Double.POSITIVE_INFINITY);
            USE_GLOBAL_CONSTANT_FOR_ADDING_AILMENT_CHANCE_FROM_ELEMENTAL_ASPECTS = BUILDER
                    .comment("Similar to the above, but for ailment chance." +
                            "\nDefault: false")
                    .define("useGlobalConstantForAddingAilmentChanceFromElementalAspects", false);
            GLOBAL_CONSTANT_FOR_ADDING_AILMENT_CHANCE_FROM_ELEMENTAL_ASPECTS = BUILDER
                    .comment("Similar to the above, but for ailment chance (0.01 is 1% chance)." +
                            "\nDefault: 0.05")
                    .defineInRange("globalConstantForAddingAilmentChanceFromElementalAspects", 0.05, 0, 1);

            //BUILDER.comment("These next settings are used for attributizing enchantments in Minecraft 1.20");
            FIRE_ATTACK_DAMAGE_PER_FIRE_ASPECT_LEVEL = BUILDER
                    .defineInRange("fireDamagePerFireAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);
            ENABLE_IGNITE_CHANCE_ON_FIRE_ASPECT = BUILDER
                    .define("enableIgniteChanceOnFireAspect", true);
            IGNITE_CHANCE_PER_FIRE_ASPECT_LEVEL = BUILDER
                    .defineInRange("igniteChancePerFireAspectLevel", 0.08, 0, 1);

            ICE_ATTACK_DAMAGE_PER_ICE_ASPECT_LEVEL = BUILDER
                    .defineInRange("iceDamagePerIceAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);
            ENABLE_FREEZE_CHANCE_ON_ICE_ASPECT = BUILDER
                    .define("enableFreezeChanceOnIceAspect", true);
            FREEZE_CHANCE_PER_ICE_ASPECT_LEVEL = BUILDER
                    .defineInRange("freezeChancePerIceAspectLevel", 0.08, 0, 1);

            LIGHTNING_ATTACK_DAMAGE_PER_LIGHTNING_ASPECT_LEVEL = BUILDER
                    .defineInRange("lightningDamagePerIceAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);
            ENABLE_SHOCK_CHANCE_ON_LIGHTNING_ASPECT = BUILDER
                    .define("enableShockChanceOnLightningAspect", true);
            SHOCK_CHANCE_PER_LIGHTNING_ASPECT_LEVEL = BUILDER
                    .defineInRange("shockChancePerLightningAspectLevel", 0.08, 0, 1);

            HOLY_ATTACK_DAMAGE_PER_HOLY_ASPECT_LEVEL = BUILDER
                    .defineInRange("holyAttackDamagePerHolyAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            ENDER_ATTACK_DAMAGE_PER_ENDER_ASPECT_LEVEL = BUILDER
                    .defineInRange("enderAttackDamagePerEnderAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            BLOOD_ATTACK_DAMAGE_PER_BLOOD_ASPECT_LEVEL = BUILDER
                    .defineInRange("bloodAttackDamagePerBloodAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            NATURE_ATTACK_DAMAGE_PER_NATURE_ASPECT_LEVEL = BUILDER
                    .defineInRange("natureAttackDamagePerNatureAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            EVOCATION_ATTACK_DAMAGE_PER_EVOCATION_ASPECT_LEVEL = BUILDER
                    .defineInRange("evocationAttackDamagePerEvocationAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            ELDRITCH_ATTACK_DAMAGE_PER_ELDRITCH_ASPECT_LEVEL = BUILDER
                    .defineInRange("eldritchAttackDamagePerEldritchAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            SOUND_ATTACK_DAMAGE_PER_SOUND_ASPECT_LEVEL = BUILDER
                    .defineInRange("soundAttackDamagePerSoundAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            GEO_ATTACK_DAMAGE_PER_GEO_ASPECT_LEVEL = BUILDER
                    .defineInRange("geoAttackDamagePerGeoAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            AQUA_ATTACK_DAMAGE_PER_AQUA_ASPECT_LEVEL = BUILDER
                    .defineInRange("aquaAttackDamagePerAquaAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            TECHNOMANCY_ATTACK_DAMAGE_PER_TECHNOMANCY_ASPECT_LEVEL = BUILDER
                    .defineInRange("technomancyAttackDamagePerTechnomancyAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

            ABYSSAL_ATTACK_DAMAGE_PER_ABYSSAL_ASPECT_LEVEL = BUILDER
                    .defineInRange("abyssalAttackDamagePerAbyssalAspectLevel", 0.75, 0, Double.POSITIVE_INFINITY);

        BUILDER.pop();
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
    public static List<String> type1schools, type2schools, type3schools, type4schools, type5schools;
    public static boolean enableDebugMode, roundFinalDamage;

    public static boolean useGlobalConstantForAddingDamageFromElementalAspects, useGlobalConstantForAddingAilmentChanceFromElementalAspects;
    public static double globalConstantForAddingDamageFromElementalAspects, globalConstantForAddingAilmentChanceFromElementalAspects;

    public static double fireAttackDamagePerFireAspectLevel;
    public static boolean enableIgniteChanceOnFireAspect;
    public static double igniteChancePerFireAspectLevel;

    public static double iceAttackDamagePerIceAspectLevel;
    public static boolean enableFreezeChanceOnIceAspect;
    public static double freezeChancePerIceAspectLevel;

    public static double lightningAttackDamagePerLightningAspectLevel;
    public static boolean enableShockChanceOnLightningAspect;
    public static double shockChancePerLightningAspectLevel;

    public static double holyAttackDamagePerHolyAspectLevel;
    public static double enderAttackDamagePerEnderAspectLevel;
    public static double bloodAttackDamagePerBloodAspectLevel;
    public static double natureAttackDamagePerNatureAspectLevel;
    public static double evocationAttackDamagePerEvocationAspectLevel;
    public static double eldritchAttackDamagePerEldritchAspectLevel;
    public static double soundAttackDamagePerSoundAspectLevel;
    public static double geoAttackDamagePerGeoAspectLevel;
    public static double aquaAttackDamagePerAquaAspectLevel;
    public static double technomancyAttackDamagePerTechnomancyAspectLevel;
    public static double abyssalAttackDamagePerAbyssalAspectLevel;


    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        copyWeaponsDefaultAttributesToNewWeapons = COPY_WEAPONS_DEFAULT_ATTRIBUTES_TO_NEW_WEAPONS.get();
        enableDebugMode = ENABLE_DEBUG_MODE.get();

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
        type4schools = new ArrayList<>(TYPE_4_SCHOOLS.get());
        type5schools = new ArrayList<>(TYPE_5_SCHOOLS.get());
        roundFinalDamage = ROUND_FINAL_DAMAGE.get();

        useGlobalConstantForAddingDamageFromElementalAspects = USE_GLOBAL_CONSTANT_FOR_ADDING_DAMAGE_FROM_ELEMENTAL_ASPECTS.get();
        globalConstantForAddingDamageFromElementalAspects = GLOBAL_CONSTANT_FOR_ADDING_DAMAGE_FROM_ELEMENTAL_ASPECTS.get();
        useGlobalConstantForAddingAilmentChanceFromElementalAspects = USE_GLOBAL_CONSTANT_FOR_ADDING_AILMENT_CHANCE_FROM_ELEMENTAL_ASPECTS.get();
        globalConstantForAddingAilmentChanceFromElementalAspects = GLOBAL_CONSTANT_FOR_ADDING_AILMENT_CHANCE_FROM_ELEMENTAL_ASPECTS.get();

        fireAttackDamagePerFireAspectLevel = FIRE_ATTACK_DAMAGE_PER_FIRE_ASPECT_LEVEL.get();
        enableIgniteChanceOnFireAspect = ENABLE_IGNITE_CHANCE_ON_FIRE_ASPECT.get();
        igniteChancePerFireAspectLevel = IGNITE_CHANCE_PER_FIRE_ASPECT_LEVEL.get();

        iceAttackDamagePerIceAspectLevel = ICE_ATTACK_DAMAGE_PER_ICE_ASPECT_LEVEL.get();
        enableFreezeChanceOnIceAspect = ENABLE_FREEZE_CHANCE_ON_ICE_ASPECT.get();
        freezeChancePerIceAspectLevel = FREEZE_CHANCE_PER_ICE_ASPECT_LEVEL.get();

        lightningAttackDamagePerLightningAspectLevel = LIGHTNING_ATTACK_DAMAGE_PER_LIGHTNING_ASPECT_LEVEL.get();
        enableShockChanceOnLightningAspect = ENABLE_SHOCK_CHANCE_ON_LIGHTNING_ASPECT.get();
        shockChancePerLightningAspectLevel = SHOCK_CHANCE_PER_LIGHTNING_ASPECT_LEVEL.get();

        holyAttackDamagePerHolyAspectLevel = HOLY_ATTACK_DAMAGE_PER_HOLY_ASPECT_LEVEL.get();

        enderAttackDamagePerEnderAspectLevel = ENDER_ATTACK_DAMAGE_PER_ENDER_ASPECT_LEVEL.get();

        bloodAttackDamagePerBloodAspectLevel = BLOOD_ATTACK_DAMAGE_PER_BLOOD_ASPECT_LEVEL.get();

        natureAttackDamagePerNatureAspectLevel = NATURE_ATTACK_DAMAGE_PER_NATURE_ASPECT_LEVEL.get();

        evocationAttackDamagePerEvocationAspectLevel = EVOCATION_ATTACK_DAMAGE_PER_EVOCATION_ASPECT_LEVEL.get();

        eldritchAttackDamagePerEldritchAspectLevel = ELDRITCH_ATTACK_DAMAGE_PER_ELDRITCH_ASPECT_LEVEL.get();

        soundAttackDamagePerSoundAspectLevel = SOUND_ATTACK_DAMAGE_PER_SOUND_ASPECT_LEVEL.get();

        geoAttackDamagePerGeoAspectLevel = GEO_ATTACK_DAMAGE_PER_GEO_ASPECT_LEVEL.get();

        aquaAttackDamagePerAquaAspectLevel = AQUA_ATTACK_DAMAGE_PER_AQUA_ASPECT_LEVEL.get();

        technomancyAttackDamagePerTechnomancyAspectLevel = TECHNOMANCY_ATTACK_DAMAGE_PER_TECHNOMANCY_ASPECT_LEVEL.get();

        abyssalAttackDamagePerAbyssalAspectLevel = ABYSSAL_ATTACK_DAMAGE_PER_ABYSSAL_ASPECT_LEVEL.get();
    }
}
