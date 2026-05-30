package com.anionianonion.elementalattackdamagecompat.enchants;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ModEnchantments {

    private static final DeferredRegister<Enchantment> ENCHANTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ElementalAttackDamageCompatMod.MOD_ID);

    public static void register(IEventBus eventBus) {

        Set<String> elementsToRegisterElementalAspectsFor = new HashSet<>(ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES);
        elementsToRegisterElementalAspectsFor.remove("fire");

        HashMap<String, String> elementToBowEnchantmentName = new HashMap<>();
        //fire bow dmg enchant already exists, it's Flame.
        elementToBowEnchantmentName.put("ice", "frost");
        elementToBowEnchantmentName.put("lightning", "storm");
        elementToBowEnchantmentName.put("holy", "judgement");
        elementToBowEnchantmentName.put("ender", "rift");
        elementToBowEnchantmentName.put("blood", "puncture");
        elementToBowEnchantmentName.put("nature", "bloom");
        elementToBowEnchantmentName.put("evocation", "arcane");
        elementToBowEnchantmentName.put("eldritch", "horror");
        elementToBowEnchantmentName.put("sound", "pulse");
        elementToBowEnchantmentName.put("geo", "quake");
        elementToBowEnchantmentName.put("aqua", "splash");
        elementToBowEnchantmentName.put("technomancy", "drive");
        elementToBowEnchantmentName.put("abyssal", "void");

        for(String elementName : elementsToRegisterElementalAspectsFor) {
            //EnchantmentCategory.WEAPON, if you CTRL-Click Weapon, says it only applies to swords, which is exactly what we want similar to Fire Aspect, which also only applies to swords.
            ENCHANTS.register(String.format("%s_aspect", elementName), () -> new Enchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {

                @Override
                public float getDamageBonus(int level, MobType mobType, ItemStack enchantedItem) {
                    return 1 + (0.5f * level);
                }

                @Override
                public int getMaxLevel() {
                    return 2;
                }

                @Override
                public boolean isTradeable() {
                    return false;
                }

                @Override
                public int getMinCost(int p_44679_) {
                    return super.getMinCost(p_44679_);
                }

                @Override
                public int getMaxCost(int p_44691_) {
                    return super.getMaxCost(p_44691_);
                }
            });

            String enchantmentName = elementToBowEnchantmentName.get(elementName);

            if(elementToBowEnchantmentName.containsKey(elementName) &&
                    enchantmentName != null &&
                    !enchantmentName.isEmpty()) {

                ENCHANTS.register(enchantmentName, () -> new Enchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.BOW, new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {

                    @Override
                    public float getDamageBonus(int level, MobType mobType, ItemStack enchantedItem) {
                        return 1 + (0.5f * level);
                    }

                    @Override
                    public boolean isTradeable() {
                        return false;
                    }
                });
            }
        }

        ENCHANTS.register(eventBus);
    }

    public static Enchantment getEnchantment(String key) {
        ResourceLocation enchantmentKey = ResourceLocation.tryParse(key);
        if (enchantmentKey == null) return null;

        return ForgeRegistries.ENCHANTMENTS.getValue(enchantmentKey);
    }
}
