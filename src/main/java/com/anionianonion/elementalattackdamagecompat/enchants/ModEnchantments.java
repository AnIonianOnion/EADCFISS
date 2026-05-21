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

import java.util.HashSet;
import java.util.Set;

public class ModEnchantments {

    private static final DeferredRegister<Enchantment> ENCHANTS = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, ElementalAttackDamageCompatMod.MOD_ID);

    public static void register(IEventBus eventBus) {

        Set<String> elementsToRegisterElementalAspectsFor = new HashSet<>(ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES);
        elementsToRegisterElementalAspectsFor.remove("fire");

        for(String elementName : elementsToRegisterElementalAspectsFor) {
            //EnchantmentCategory.WEAPON, if you CTRL-Click Weapon, says it only applies to swords, which is exactly what we want similar to Fire Aspect, which also only applies to swords.
            ENCHANTS.register(String.format("%s_aspect", elementName), () -> new Enchantment(Enchantment.Rarity.UNCOMMON, EnchantmentCategory.WEAPON, new EquipmentSlot[]{EquipmentSlot.MAINHAND}) {
                @Override
                public void doPostAttack(LivingEntity attacker, Entity target, int level) {
                    super.doPostAttack(attacker, target, level);
                }

                @Override
                public void doPostHurt(LivingEntity defender, Entity attacker, int p_44694_) {
                    super.doPostHurt(defender, attacker, p_44694_);
                }

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
                public boolean isTreasureOnly() {
                    return true;
                }
            });
        }

        ENCHANTS.register(eventBus);
    }

    public static Enchantment getEnchantment(String key) {
        ResourceLocation enchantmentKey = ResourceLocation.tryParse(key);
        if (enchantmentKey == null) return null;

        return ForgeRegistries.ENCHANTMENTS.getValue(enchantmentKey);
    }
}
