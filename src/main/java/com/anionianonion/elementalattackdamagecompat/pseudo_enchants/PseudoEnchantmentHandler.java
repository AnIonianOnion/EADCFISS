package com.anionianonion.elementalattackdamagecompat.pseudo_enchants;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.*;
import java.util.function.Function;

public class PseudoEnchantmentHandler {

    public static final String PSEUDO_ENCHANTS_TAG_ID = "pseudo_enchants";
    //this map should always the same keys as the previous
    public static final String GEMS_ENCHANTMENTS_LEVEL_BUFFS = "gems_enchantment_level_buffs";


    /**
    Stores a map that converts from Minecraft Enchantments to keys as Strings.
     */
    private static final Map<Enchantment, String> ENCHANTMENTS_TO_STRINGS = new HashMap<>();

    /**
     * Reverse lookup: pseudo key → real enchantment
     */
    public static Enchantment getRealEnchantmentFromKey(String key) {
        for (var entry : ENCHANTMENTS_TO_STRINGS.entrySet()) {
            if (entry.getValue().equals(key)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static Map<Enchantment, String> getEnchantmentsToStringsMap() {
        return ENCHANTMENTS_TO_STRINGS;
    }

    public static void addEnchantmentAndKey(Enchantment enchantment, String key) {
        ENCHANTMENTS_TO_STRINGS.putIfAbsent(enchantment, key);
    }
    /**
    Stores a map that converts from Strings as keys to a list of PseudoAttributes, which contains the attribute id, modifier operation, and an amount per level of enchantment.
     */
    private static final Map<String, List<PseudoAttribute>> ENCHANTMENT_STRINGS_TO_PSEUDO_ATTRIBUTES = new HashMap<>();

    /**
     @return A list of attribute modifiers to apply for each level per enchantment, given a key, and null if key doesn't exist.
     */
    public static List<PseudoAttribute> getPseudoAttributes(String enchantmentKey) {
        return ENCHANTMENT_STRINGS_TO_PSEUDO_ATTRIBUTES.get(enchantmentKey);
    }

    public static void addKeyAndPseudoAttributes(String enchantmentKey, List<PseudoAttribute> pseudoAttributes) {
        ENCHANTMENT_STRINGS_TO_PSEUDO_ATTRIBUTES.putIfAbsent(enchantmentKey, pseudoAttributes);
    }

    private static final Map<EnchantmentCategory, Set<EquipmentSlot>> CATEGORY_SLOTS = Map.of(
            EnchantmentCategory.WEAPON, Set.of(EquipmentSlot.MAINHAND),
            EnchantmentCategory.DIGGER, Set.of(EquipmentSlot.MAINHAND),
            EnchantmentCategory.ARMOR, Set.of(
                    EquipmentSlot.HEAD,
                    EquipmentSlot.CHEST,
                    EquipmentSlot.LEGS,
                    EquipmentSlot.FEET
            ),
            EnchantmentCategory.ARMOR_HEAD, Set.of(EquipmentSlot.HEAD),
            EnchantmentCategory.ARMOR_CHEST, Set.of(EquipmentSlot.CHEST),
            EnchantmentCategory.ARMOR_LEGS, Set.of(EquipmentSlot.LEGS),
            EnchantmentCategory.ARMOR_FEET, Set.of(EquipmentSlot.FEET)
    );

    public record PseudoAttribute(
            Attribute attribute,
            AttributeModifier.Operation op,
            float amountPerLevel
    ) {}

    // Convert real enchants → pseudo enchants
    private static void convertToPseudo(ItemStack stack) {
        Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
        if (enchants.isEmpty()) return;

        CompoundTag tag = stack.getOrCreateTag();
        //no detect necessary, because we are creating a new one, and getCompound automatically does so when it doesn't exist.
        CompoundTag pseudo = tag.getCompound(PSEUDO_ENCHANTS_TAG_ID);

        boolean changed = false;

        for (var entry : enchants.entrySet()) {
            Enchantment ench = entry.getKey();
            int level = entry.getValue();

            if (ENCHANTMENTS_TO_STRINGS.containsKey(ench)) {
                String key = ENCHANTMENTS_TO_STRINGS.get(ench);

                pseudo.putShort(key, (short) level);
                changed = true;
            }
        }

        if (changed) {
            //put our new tag/data onto the item itself.
            tag.put(PSEUDO_ENCHANTS_TAG_ID, pseudo);
            //loops through keyset, and removes enchantments for the ones we created pseudo-enchantments for
            enchants.keySet().removeIf(ENCHANTMENTS_TO_STRINGS::containsKey);
            //overrides the previous map of enchantments
            EnchantmentHelper.setEnchantments(enchants, stack);
        }
    }

    // Lazy conversion: fix items when accessed
    public static void fixItemOnLoad(ItemStack stack) {
        convertToPseudo(stack);
        sanitizePseudoEnchantNBT(stack);
        writeGemEnchantmentLevelBuffsToPseudoEnchants(stack);
    }

    /**
    Gets level of pseudo-enchantment from compound tag.
     */
    public static short getPseudoLevel(ItemStack stack, Enchantment ench) {
        String key = ENCHANTMENTS_TO_STRINGS.get(ench);
        if (key == null) return 0;

        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag pseudo = tag.getCompound(PSEUDO_ENCHANTS_TAG_ID);

        return pseudo.getShort(key);
    }

    public static Set<EquipmentSlot> getSlotsForCategory(EnchantmentCategory category) {
        return CATEGORY_SLOTS.getOrDefault(category, Set.of());
    }

    public static Map<String, Short> getPseudoEnchantsMapFromItem(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        //creates new CompoundTag without keys if it doesn't exist
        CompoundTag pseudo = tag.getCompound(PSEUDO_ENCHANTS_TAG_ID);

        Map<String, Short> map = new HashMap<>();
        for (String key : pseudo.getAllKeys()) {
            map.put(key, pseudo.getShort(key));
        }
        return map;
    }

    public static Map<String, Short> mergePseudoEnchants(ItemStack leftItem, ItemStack rightItem) {
        // Read pseudo enchants
        var left = PseudoEnchantmentHandler.getPseudoEnchantsMapFromItem(leftItem);
        var right = PseudoEnchantmentHandler.getPseudoEnchantsMapFromItem(rightItem);

        left.entrySet().removeIf(e -> getRealEnchantmentFromKey(e.getKey()) == null);
        right.entrySet().removeIf(e -> getRealEnchantmentFromKey(e.getKey()) == null);


        //starts with left pseudo-enchantments, because we are using the left item as our base; the same as in an anvil.
        Map<String, Short> mergedPseudo = new HashMap<>(left);

        //adds Pseudo enchants
        for (var entry : right.entrySet()) {

            String enchantmentKey = entry.getKey();
            Enchantment ench = getRealEnchantmentFromKey(enchantmentKey);
            if(ench == null) continue;

            short maxLevel = (short) ench.getMaxLevel();
            short rightLevel = entry.getValue();

            if (!mergedPseudo.containsKey(enchantmentKey)) {
                mergedPseudo.put(enchantmentKey, rightLevel);
                continue;
            }

            short leftLevel = mergedPseudo.get(enchantmentKey);

            if (leftLevel == rightLevel && leftLevel != maxLevel) {
                mergedPseudo.put(enchantmentKey, (short) (leftLevel + 1));
            } else {
                mergedPseudo.put(enchantmentKey, (short) Math.max(leftLevel, rightLevel));
            }
        }

        return mergedPseudo;
    }
    public static Map<Enchantment, Integer> mergeRealEnchants(ItemStack leftItem, ItemStack rightItem) {
        //Read REAL enchants (Sharpness, Unbreaking, Mending, etc.)
        Map<Enchantment, Integer> realEnchantmentsFromLeftItem = EnchantmentHelper.getEnchantments(leftItem);
        Map<Enchantment, Integer> realEnchantmentsFromRightItem = EnchantmentHelper.getEnchantments(rightItem);

        //start with left-side enchantments, because we are starting with the left item as our base, the same as how it works in an anvil.
        Map<Enchantment, Integer> mergedReal = new HashMap<>(realEnchantmentsFromLeftItem);

        //adds enchants from the second item
        for (var entry : realEnchantmentsFromRightItem.entrySet()) {
            Enchantment enchantmentFromRightItem = entry.getKey();
            short maxEnchantmentLevel = (short) enchantmentFromRightItem.getMaxLevel();

            int rightLevel = entry.getValue();

            //skip enchantment if it's incompatible with the left item type.
            if(!enchantmentFromRightItem.canEnchant(leftItem)) continue;

            //assumes right enchantment is compatible before checking left-item's enchantments
            boolean compatible = true;
            //if there's one incompatible enchantment that already exists, set compatible flag to false...
            for (Enchantment existing : mergedReal.keySet()) {
                if (!enchantmentFromRightItem.isCompatibleWith(existing)) {
                    compatible = false;
                    break;
                }
            }
            //...and skip enchantment.
            if (!compatible) continue;


            //mergedReal is our left-item's enchantments.
            //Because we are in the for-loop that loops through the right-item's enchantments,
            // we use the enchantment key from the right-item to check the enchantment level on the left-item, and if the item
            // doesn't contain the enchantment, we can safely assume the enchantment level is 0.
            int leftLevel = mergedReal.getOrDefault(enchantmentFromRightItem, 0);

            //this case shouldn't be run if levels are 0, since both levels should never be 0.
            if (leftLevel == rightLevel && leftLevel != maxEnchantmentLevel) {
                //both levels are the same so increase existing/left-item's enchantment level for that enchantment by 1,
                //as long as both enchantments aren't already at max level.
                mergedReal.put(enchantmentFromRightItem, leftLevel + 1);
            } else {
                //both levels are different, so put the enchantment of the one that has the higher level.
                //leftLevel is 0 if enchantment doesn't exist.
                mergedReal.put(enchantmentFromRightItem, Math.max(leftLevel, rightLevel));
            }
        }

        return mergedReal;
    }

    public static void writePseudoEnchantsMap(Map<String, Short> map, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag pseudo = new CompoundTag();

        for (var entry : map.entrySet()) {
            if (entry.getKey() == null) continue;

            pseudo.putShort(entry.getKey(), entry.getValue());
        }

        tag.put(PSEUDO_ENCHANTS_TAG_ID, pseudo);
    }
    public static void writeGemLevelBuffsMap(Map<String, Short> map, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag gemsEnchantmentLevelBuffs = new CompoundTag();

        for(var entry : map.entrySet()) {
            gemsEnchantmentLevelBuffs.putShort(entry.getKey(), entry.getValue());
        }

        tag.put(GEMS_ENCHANTMENTS_LEVEL_BUFFS, gemsEnchantmentLevelBuffs);
    }

    public static short getEnderSurgeLevel(ItemStack stack) {
        return getGemBonusEnchantmentLevel(stack, "apotheosis:the_end/endersurge", rarity -> switch (rarity) {
            case "apotheosis:epic", "apotheosis:mythic" -> 1;
            case "apotheosis:ancient" -> 2;
            default -> 0;
        });
    }
    public static short getGemBonusEnchantmentLevel(ItemStack stack, String targetGemId, Function<String, Short> getLevelFromRarity) {
        CompoundTag stackTag = stack.getOrCreateTag();
        if(!stackTag.contains("affix_data")) return 0;

        CompoundTag stackAffixData = stackTag.getCompound("affix_data");
        if(!stackAffixData.contains("gems")) return 0;

        short totalLevel = 0;
        ListTag gems = stackAffixData.getList("gems", Tag.TAG_COMPOUND);
        for(int i = 0; i < gems.size(); i++) {
            CompoundTag gem = gems.getCompound(i);

            if(!gem.contains("tag")) continue;
            CompoundTag gemTag = gem.getCompound("tag");

            //what we need.
            if(!gemTag.contains("gem")) continue;
            String gemId = gemTag.getString("gem");

            if(!gemTag.contains("affix_data")) continue;
            CompoundTag gemAffixData = gemTag.getCompound("affix_data");

            //also what we need.
            if(!gemAffixData.contains("rarity")) continue;
            String rarity = gemAffixData.getString("rarity");

            if(gemId.equals(targetGemId)) {
                totalLevel += getLevelFromRarity.apply(rarity);
            }
        }
        return totalLevel;
    }

    private static void writeGemEnchantmentLevelBuffsToPseudoEnchants(ItemStack stack) {
        LootCategory lc = LootCategory.forItem(stack);

        var pseudoEnchants = PseudoEnchantmentHandler.getPseudoEnchantsMapFromItem(stack);
        Map<String, Short> copy = new HashMap<>();

        for (var entry : pseudoEnchants.entrySet()) {
            var enchantmentKey = entry.getKey();
            Enchantment enchantment = PseudoEnchantmentHandler.getRealEnchantmentFromKey(enchantmentKey);
            if(enchantment == null) continue;

            var search = GemBuffRegistry.filterByEnchantmentAndLootCategory(enchantment, lc);
            if (search.isEmpty()) continue;

            var result = search.get(0);

            var bonusLevels = PseudoEnchantmentHandler.getGemBonusEnchantmentLevel(
                    stack,
                    result.gemId(),
                    result.levelsToBuffFunctionBasedOnRarity()
            );

            if (bonusLevels > 0)
                copy.put(enchantmentKey, bonusLevels);
        }

        writeGemLevelBuffsMap(copy, stack);
    }

    public static void sanitizePseudoEnchantNBT(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(PSEUDO_ENCHANTS_TAG_ID)) return;

        CompoundTag pseudo = tag.getCompound(PSEUDO_ENCHANTS_TAG_ID);

        // Remove invalid keys
        pseudo.getAllKeys().removeIf(k -> getRealEnchantmentFromKey(k) == null);

        tag.put(PSEUDO_ENCHANTS_TAG_ID, pseudo);
    }

}
