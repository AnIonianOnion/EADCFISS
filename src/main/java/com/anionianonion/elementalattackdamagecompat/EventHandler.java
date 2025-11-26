package com.anionianonion.elementalattackdamagecompat;

import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
import java.util.UUID;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

@Mod.EventBusSubscriber(modid = ElementalAttackDamageCompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    protected static final ThreadLocal<Boolean> PROCESSING_CUSTOM_DAMAGE = ThreadLocal.withInitial(() -> false);
    protected static Float cc = 0.05f;
    protected static Float cd = 1.5f;

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void attacks(LivingHurtEvent e) {
        LivingEntity defender = e.getEntity();
        DamageSource damageSource = e.getSource();
        LivingEntity livingAttacker = (LivingEntity) damageSource.getEntity();
        var directEntity = damageSource.getDirectEntity();

        if(DamageManager.hasFailedInitialChecks(damageSource)) return;
        //attacker
        //melee & non-bow projectiles gets flat added damage (and 'increases', and 'more' multipliers), nothing else
        //bow gets added damage (and 'increases', and 'more' multipliers), multiplied by speed in blocks/s
        //----RANGED----: BOWS & CROSSBOWS SPECIFICALLY
        if(directEntity instanceof Arrow arrow) {
            DamageManager.manageArrowShot(livingAttacker, arrow, e); //I need e in order to get the event to set the damage within the function
        }
        //---MELEE----     AND  ---RANGED---: OTHER PROJECTILES
        else if(livingAttacker == directEntity || directEntity instanceof AbstractArrow) {
            DamageManager.manageMeleeAndOtherProjectiles(livingAttacker, directEntity, damageSource, e); //ignore this warning, living attacker null check is in hasFailedInitialCheck(damageSource)
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void attackDamage(LivingDamageEvent e) {


    }


    @SubscribeEvent
    public static void spells(SpellDamageEvent e) {
        if(ElementalAttackDamageCompatMod.IS_RANDOM_DAMAGE_MOD_ENABLED) return;

        SpellDamageSource spellDamageSource = e.getSpellDamageSource();
        LivingEntity caster = (LivingEntity) spellDamageSource.get().getEntity();
        var originalTotalDamage = e.getOriginalAmount();
        var spellSchool = spellDamageSource.spell().getSchoolType().getId().getPath();

        float baseTotalElementalDamage = DamageManager.sumOfDamages(AttributeHelpers.getAllElementalData(caster, e.getEntity(), true, Map.entry(spellSchool, originalTotalDamage)));
        float critAdjustedDamage = DamageManager.calculatePostCritDamage(caster, true, baseTotalElementalDamage);

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


}
