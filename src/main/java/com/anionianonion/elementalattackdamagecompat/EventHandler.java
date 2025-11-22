package com.anionianonion.elementalattackdamagecompat;

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.UUID;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

@Mod.EventBusSubscriber(modid = ElementalAttackDamageCompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    protected static final ThreadLocal<Boolean> PROCESSING_CUSTOM_DAMAGE = ThreadLocal.withInitial(() -> false);
    protected static Float cc = 0.1f;
    protected static Float cd = 1.5f;

    @SubscribeEvent
    public static void attacks(LivingHurtEvent e) {

        DamageSource damageSource = e.getSource();
        if(DamageManager.hasFailedInitialChecks(damageSource)) return;

        LivingEntity livingAttacker = (LivingEntity) damageSource.getEntity();
        var directEntity = damageSource.getDirectEntity();
        float baseTotalElementalDamage = DamageManager.calculateBaseTotalElementalDamageFromAttacksPostElementalResistances(livingAttacker, e.getEntity());

        //attacker
            //melee & non-bow projectiles gets flat added damage (and 'increases', and 'more' multipliers), nothing else
            //bow gets added damage (and 'increases', and 'more' multipliers), multiplied by speed in blocks/s
        //----RANGED----: BOWS & CROSSBOWS SPECIFICALLY
        if(directEntity instanceof Arrow arrow) {
            DamageManager.manageArrowShot(livingAttacker, arrow, baseTotalElementalDamage, e);
        }
        //---MELEE----     AND  ---RANGED---: OTHER PROJECTILES
        else if(livingAttacker == directEntity || directEntity instanceof AbstractArrow) {
            DamageManager.manageMeleeAndOtherProjectiles(livingAttacker, directEntity, damageSource, baseTotalElementalDamage, e);
        }
    }

    @SubscribeEvent
    public static void spells(SpellDamageEvent e) {
        if(ElementalAttackDamageCompatMod.IS_RANDOM_DAMAGE_MOD_ENABLED) return;

        SpellDamageSource spellDamageSource = e.getSpellDamageSource();
        LivingEntity caster = (LivingEntity) spellDamageSource.get().getEntity();
        var originalTotalDamage = e.getOriginalAmount();
        float baseTotalElementalDamage = DamageManager.calculateBaseTotalElementalDamageFromSpellsPostElementalResistances(spellDamageSource, e.getEntity(), originalTotalDamage);
        float critAdjustedDamage = DamageManager.simpleCritRoll(caster, true, baseTotalElementalDamage);

        int roundedDamage = Math.round(critAdjustedDamage);
        e.setAmount(roundedDamage);
    }


    //Goal: fix damage on weapons being overwritten when adding a nwe attribute if the weapon didn't have one before.
    //For example, a diamond adds 6 extra attack damage, so you deal 7 damage total without crit.
    //But when adding an attribute to it from any source, the original +6 bonus disappears. As a result, you only deal 1 damage + whatever elemental damage bonus from this mod.
    //armor not affected by this problem.
    @SubscribeEvent
    public static void copyDefaultItemAttributes(ItemAttributeModifierEvent e) {
        //todo: enchantments stored safely on new item?? yes

        //the setting may be disabled to offer more flexibility to other mods. if it is, we do nothing.
        if(!Config.copyWeaponsDefaultAttributesToNewWeapons) return;

        var itemStack = e.getItemStack(); //get the item being modified
        if(!itemStack.hasTag()) return; //if the item isn't armor or a weapon/tool, do nothing

        CompoundTag nbt = itemStack.getTag();
        if(nbt.contains("AttributeModifiers", Tag.TAG_LIST)) return; //if an item already has attributes, do nothing.
        if(LivingEntity.getEquipmentSlotForItem(itemStack) != EquipmentSlot.MAINHAND) return; //lastly, if the item isn't meant to be used in the main hand, do nothing.
        var originalModifiers = e.getOriginalModifiers();

        var atkDamageAttributeModifiers = originalModifiers.get(Attributes.ATTACK_DAMAGE);
        for(var atkDamageAttributeModifier : atkDamageAttributeModifiers) {
            itemStack.addAttributeModifier(
                Attributes.ATTACK_DAMAGE,
                new AttributeModifier(
                    //items who have an attribute with a specific UUID won't have that attribute added again when attempting to add an attribute with the same UUID
                    //also check out Attribute Tooltip Fix, as this uses the same UUID from there
                    UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"),
                    "Weapon's Base Damage",
                    atkDamageAttributeModifier.getAmount(),
                    ADDITION
                ),
                EquipmentSlot.MAINHAND
            );
        }

        var atkSpeedAttributeModifiers = originalModifiers.get(Attributes.ATTACK_SPEED);
        for(var atkSpeedAttributeModifier : atkSpeedAttributeModifiers) {
            itemStack.addAttributeModifier(
                Attributes.ATTACK_SPEED,
                new AttributeModifier(
                    UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"),
                    "Weapon's Base Attack Speed",
                    atkSpeedAttributeModifier.getAmount(),
                    ADDITION
                ),
                EquipmentSlot.MAINHAND
            );
        }
    }

    //Without this, increases and more modifiers from attack crit chance and crit damage, when set to global in config, won't be applied to spells.
    //if item has attack cc/cd increases/mores and doesn't have corresponding spell cc/cd increaes/mores, we modify item.
    //if it does have corresponding matches:
        //if global cc/cd from attack cc/cd is disabled, we remove spell cc/cd
        //else do nothing
    @SubscribeEvent
    public static void copyGlobalCritAttributesForSpells(ItemAttributeModifierEvent e) {
        ItemStack itemStack = e.getItemStack();
        var attributeModifiers = e.getModifiers();

        Attribute attackCritChance = ModAttributes.getAttribute(Config.attackCritChanceAttributeId);
        Attribute attackCritDamage = ModAttributes.getAttribute(Config.attackCritDamageAttributeId);
        Attribute spellCritChance = ModAttributes.getAttribute(Config.spellCritChanceAttributeId);
        Attribute spellCritDamage = ModAttributes.getAttribute(Config.spellCritDamageAttributeId);

        //get our attack attributes' increases/more modifiers for crit
        var attackCritChanceModifiers = attributeModifiers.get(attackCritChance)
                .stream()
                .filter(mod -> mod.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE
                        || mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
                .toList();

        var attackCritDamageModifiers = attributeModifiers.get(attackCritDamage)
                .stream()
                .filter(mod -> mod.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE
                        || mod.getOperation() == AttributeModifier.Operation.MULTIPLY_TOTAL)
                .toList();
        //

        //get our corresponding crit for spells that have already been added
        var spellCritChanceModifiers = attributeModifiers.get(spellCritChance)
                .stream()
                .filter(mod -> "Global Crit Chance".equals(mod.getName()))
                .toList();

        var spellCritDamageModifiers = attributeModifiers.get(spellCritDamage)
                .stream()
                .filter(mod -> "Global Crit Damage".equals(mod.getName()))
                .toList();
        //

        if (Config.applyAttackCritAttributesGlobally) {
            //if we have attack crit attributes that can be added
            if (!attackCritChanceModifiers.isEmpty() || !attackCritDamageModifiers.isEmpty()) {
                //only add spell attributes if counts don't match (meaning we need to sync)
                if (attackCritChanceModifiers.size() != spellCritChanceModifiers.size() || attackCritDamageModifiers.size() != spellCritDamageModifiers.size()) {

                    CritModifierHelpers.addStackingGlobalCritModifiers(itemStack, e.getSlotType(), attackCritChanceModifiers, attackCritDamageModifiers, spellCritChance, spellCritDamage);
                }
            }
        } else {
            //if global cc/cd is disabled and item has spell cc/cd, remove them
            if (!spellCritChanceModifiers.isEmpty() || !spellCritDamageModifiers.isEmpty()) {
                CritModifierHelpers.removeGlobalCritModifiers(attributeModifiers,
                    spellCritChanceModifiers, spellCritDamageModifiers,
                    spellCritChance, spellCritDamage);
            }
        }
    }




}
