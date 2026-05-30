package com.anionianonion.elementalattackdamagecompat.events;

import com.anionianonion.elementalattackdamagecompat.Config;
import com.anionianonion.elementalattackdamagecompat.DamageManager;
import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentDataHelper;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentModifierHelper;
import com.anionianonion.elementalattackdamagecompat.items.ScorchHelmetItem;
import com.anionianonion.elementalattackdamagecompat.pseudo_enchants.GemBuffRegistry;
import com.anionianonion.elementalattackdamagecompat.util.AttributeHelpers;
import com.anionianonion.elementalattackdamagecompat.pseudo_enchants.PseudoEnchantmentHandler;
import com.anionianonion.elementalattackdamagecompat.util.RomanNumeralHelper;
import dev.shadowsoffire.apotheosis.adventure.loot.LootCategory;
import io.redspace.ironsspellbooks.api.events.SpellDamageEvent;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION;

@Mod.EventBusSubscriber(modid = ElementalAttackDamageCompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    //--------------------DAMAGE EVENTS--------------------
    @SubscribeEvent
    public static void spellDodge(LivingAttackEvent e) {

        DamageSource damageSource = e.getSource();
        LivingEntity defender = e.getEntity();

        //do not cast before checking.
        Entity entity = damageSource.getEntity();
        if(!(entity instanceof LivingEntity)) return;

        if(damageSource instanceof SpellDamageSource) {

            Float spellDodgeChance = ModAttributes.getAttributeValue(defender, String.format("%s:spell_dodge_chance", ElementalAttackDamageCompatMod.MOD_ID));
            if(spellDodgeChance == null) spellDodgeChance = 0f;

            float roll = (float) Math.random();
            if(spellDodgeChance >= roll) e.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttackDamage(LivingDamageEvent e) {
        DamageSource damageSource = e.getSource();

        if(DamageManager.hasFailedInitialChecks(damageSource)) return;

        //cast is safe, because we check that entity is instance of LivingEntity before casting within hasFailedInitialChecks.
        LivingEntity livingAttacker = (LivingEntity) damageSource.getEntity();
        var directEntity = damageSource.getDirectEntity();

        //attacker
        //melee & non-bow projectiles gets flat added damage (and 'increases', and 'more' multipliers), nothing else
        //bow gets added damage (and 'increases', and 'more' multipliers), multiplied by speed in blocks/s
        //----RANGED----: BOWS & CROSSBOWS SPECIFICALLY
        if(directEntity instanceof Arrow) {
            DamageManager.manageArrowShot(e); //I need e in order to get the event to set the damage within the function
        }
        //---MELEE----     AND  ---RANGED---: OTHER PROJECTILES
        else if(livingAttacker == directEntity ||
                    (directEntity instanceof Projectile && !(directEntity instanceof ThrowableItemProjectile))
        ) {
                DamageManager.manageMeleeAndOtherProjectiles(e);
        }
        //custom spell handler
        else if(DamageManager.isCustomSpellDamageSource(damageSource)) {
                DamageManager.manageOtherSpells(e);
        }

        if(Config.enableDebugMode) {
            AilmentDataHelper.getOptional(e.getEntity()).ifPresent(cap -> {
                cap.getAilmentsOnEntity().forEach((ailment, inst) -> {
                    ElementalAttackDamageCompatMod.LOGGER.info(
                            "{}: effectStrength={}, strongestEffectStrength={}, totalStrength={}, duration={} ticks, maxStacks={}, stacks= {}",
                            ailment,
                            inst.getEffectStrength(),
                            inst.getStrongestEffectStrength(),
                            inst.getTotalEffectStrength(),
                            inst.getDuration(),
                            inst.getMaxStacks(),
                            inst.getStacks()
                    );
                });
            });
        }

        if(livingAttacker instanceof ServerPlayer serverPlayerAttacker) {
            serverPlayerAttacker.sendSystemMessage(Component.literal(
                    "You dealt " + e.getAmount() + " damage."
            ));
        }
        if(e.getEntity() instanceof ServerPlayer serverPlayerDefender) {
            serverPlayerDefender.sendSystemMessage(Component.literal(
                    "You took " + e.getAmount() + " damage!"
            ));
        }
    }

    @SubscribeEvent
    public static void onSpellDamage(SpellDamageEvent e) {
        if(ElementalAttackDamageCompatMod.IS_RANDOM_DAMAGE_MOD_ENABLED) return;

        SpellDamageSource spellDamageSource = e.getSpellDamageSource();
        DamageSource damageSource = spellDamageSource.get();
        LivingEntity caster = (LivingEntity) damageSource.getEntity();

        if(caster == null) return;

        LivingEntity livingDefender = e.getEntity();

        var originalTotalDamage = e.getOriginalAmount();
        var spellSchoolName = spellDamageSource.spell().getSchoolType().getId().getPath();
        var spellId = e.getSpellDamageSource().spell().getSpellId();

        var data = AttributeHelpers.getBasicElementalData(caster, livingDefender,true, Map.entry(spellSchoolName, originalTotalDamage));

        AttributeHelpers.multiplyWithEnemyResistances(data, livingDefender, true);
        AttributeHelpers.multiplyWithCritDamageIfCrit(data, caster, livingDefender,true);
        AttributeHelpers.multiplyWithSpellSuppressionIfSuppressed(data, livingDefender);

        float baseTotalElementalSpellDamage = DamageManager.sumOfDamages(data);

        float rounded = Math.round(baseTotalElementalSpellDamage);
        float finalDamage = Config.roundFinalDamage ? rounded : baseTotalElementalSpellDamage;

        if(Config.enableDebugMode) {
            ElementalAttackDamageCompatMod.LOGGER.info(e.getEntity().toString());
            if(caster instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("Spell School: " + spellSchoolName));
                serverPlayer.sendSystemMessage(Component.literal("Spell id: " + spellId));
                serverPlayer.sendSystemMessage(Component.literal("PreFinal damage: " + finalDamage));
            }
        }

        e.setAmount(finalDamage);
    }

    @SubscribeEvent
    public static void onHit(LivingHurtEvent e) {
        DamageSource damageSource = e.getSource();
        if(DamageManager.hasFailedInitialChecks(damageSource)) return;

        LivingEntity livingDamager = (LivingEntity) damageSource.getEntity();

        if(Config.enableDebugMode) ElementalAttackDamageCompatMod.LOGGER.info("secondary damage event fired");

        assert livingDamager != null;
        AilmentModifierHelper.getOptional(livingDamager).ifPresent(ailmentModifiers -> {

            if(Config.enableDebugMode) ElementalAttackDamageCompatMod.LOGGER.info("Ailment modifier capability fired from secondary damage event.");
            ItemStack helmet = livingDamager.getItemBySlot(EquipmentSlot.HEAD);
            if(helmet.getItem() instanceof ScorchHelmetItem) {
                if(Config.enableDebugMode) ElementalAttackDamageCompatMod.LOGGER.info("scorch has replaced ignite");
                ailmentModifiers.setReplacement("ignite", "scorch");
                ailmentModifiers.setExtraMaxStacks("scorch", 1);
            }
            else {
                if(Config.enableDebugMode) ElementalAttackDamageCompatMod.LOGGER.info("Ignite has went back to normal");
                ailmentModifiers.setReplacement("ignite", null);
                ailmentModifiers.setExtraMaxStacks("scorch", 0);
            }

            ItemStack mainHandItem = livingDamager.getItemInHand(InteractionHand.MAIN_HAND);
            if(mainHandItem.getItem() == Items.LIGHTNING_ROD) {
                if(Config.enableDebugMode) ElementalAttackDamageCompatMod.LOGGER.info("Oath of Spring Shock has replaced normal shock.");
                ailmentModifiers.setReplacement("shock", "oath_of_spring_shock");
            }
            else {
                if(Config.enableDebugMode) ElementalAttackDamageCompatMod.LOGGER.info("Shock has went back to normal");
                ailmentModifiers.setReplacement("shock", null);
            }
        });
    }


    //--------------------TICK EVENTS--------------------
    @SubscribeEvent
    public static void myOwnCustomWorkingLivingTick(TickEvent.ServerTickEvent event) {

        if(event.phase != TickEvent.Phase.START) return;

        Set<LivingEntity> entities = new HashSet<>();
        event.getServer().getPlayerList().getPlayers().forEach(serverPlayer -> {

            var surroundingEntities = serverPlayer.level().getEntitiesOfClass(LivingEntity.class,
                    serverPlayer.getBoundingBox().inflate(128f),
                    e -> e != serverPlayer && !entities.contains(e));
            entities.addAll(surroundingEntities);
        });

        for(LivingEntity livingEntity : entities) {

            if (!livingEntity.isOnFire()) {
                livingEntity.getPersistentData().remove("fire_aspect_ignite");
            }

            AilmentDataHelper.getOptional(livingEntity).ifPresent(cap -> {
                boolean hasIgnite = cap.getAilmentsOnEntity().containsKey("ignite");

                if (hasIgnite) {
                    livingEntity.setSharedFlagOnFire(true); // visual only
                } else {
                    // Only turn off if not actually burning
                    if (!livingEntity.isOnFire()) {
                        livingEntity.setSharedFlagOnFire(false);
                    }
                }

                cap.tick(livingEntity);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;

        var player = event.player;

        if(player.level().isClientSide()) return;

        AilmentModifierHelper.getOptional(player).ifPresent(cap -> {
            //cap.setReplacement("shock", "oath_of_spring_shock");
        });
    }

    //--------------------ITEM EVENTS--------------------

    //Goal: fix damage on weapons being overwritten when adding a nwe attribute if the weapon didn't have one before.
    //For example, a diamond adds 6 extra attack damage, so you deal 7 damage total without crit.
    //But when adding an attribute to it from any source, the original +6 bonus disappears. As a result, you only deal 1 damage + whatever elemental damage bonus from this mod.
    //armor not affected by this problem.
    @SubscribeEvent
    public static void copyDefaultItemAttributes(ItemAttributeModifierEvent e) {

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

    @SubscribeEvent
    public static void addAttributizedEnchantsTooltip(ItemTooltipEvent e) {
        ItemStack stack = e.getItemStack();
        List<Component> itemTooltips = e.getToolTip();

        var itemTag = stack.getOrCreateTag();
        if(!itemTag.contains(PseudoEnchantmentHandler.PSEUDO_ENCHANTS_TAG_ID)) return;
        //var pseudoTag = itemTag.getCompound(PseudoEnchantmentHandler.PSEUDO_ENCHANTS_TAG_ID);

        List<Component> pseudoEnchantTooltips = new ArrayList<>();

        PseudoEnchantmentHandler.sanitizePseudoEnchantNBT(stack);
        short enderSurgeLevel = ElementalAttackDamageCompatMod.IS_APOTHEOSIS_MOD_ENABLED ? PseudoEnchantmentHandler.getEnderSurgeLevel(stack) : 0;
        Object lootCategory = ElementalAttackDamageCompatMod.IS_APOTHEOSIS_MOD_ENABLED ? LootCategory.forItem(stack) : null;

        //itemTag.put(PseudoEnchantmentHandler.PSEUDO_ENCHANTS_TAG_ID, pseudoTag);

        for (var entry : PseudoEnchantmentHandler.getPseudoEnchantsMapFromItem(stack).entrySet()) {
            Enchantment ench = PseudoEnchantmentHandler.getRealEnchantmentFromKey(entry.getKey());
            if(ench == null) continue;

            short level = PseudoEnchantmentHandler.getPseudoLevel(stack, ench);
            if (level <= 0) continue;

            short bonusPseudoEnchantmentLevels = enderSurgeLevel;

            //takes into account Apotheosis's Gem Enchantment level buffs
            if(ElementalAttackDamageCompatMod.IS_APOTHEOSIS_MOD_ENABLED) {
                var search = GemBuffRegistry.filterByEnchantmentAndLootCategory(ench, (LootCategory) lootCategory);
                if(!search.isEmpty()) {
                    var result = search.get(0);
                    bonusPseudoEnchantmentLevels += PseudoEnchantmentHandler.getGemBonusEnchantmentLevel(stack, result.gemId(), result.levelsToBuffFunctionBasedOnRarity());
                }
            }
            short totalLevel = (short) (level + bonusPseudoEnchantmentLevels);

            Component enchantmentComponent = bonusPseudoEnchantmentLevels > 0 ? Component.translatable("tooltip.enchantment_level_bonus.format",
                    ench.getFullname(totalLevel),
                    Component.translatable("tooltip.enchantment_level_bonus.format.original_plus_bonus",
                            RomanNumeralHelper.toRoman(level),
                            RomanNumeralHelper.toRoman(bonusPseudoEnchantmentLevels)
                    ).withStyle(ChatFormatting.DARK_GRAY)
            )
            // Auto-translated enchantment name
            : ench.getFullname(level);

            pseudoEnchantTooltips.add(enchantmentComponent);
        }

        //index of line below the name of the item
        int insertIndex = 1;
        itemTooltips.addAll(insertIndex, pseudoEnchantTooltips);
    }

    /**
    Adds attributes to items based on their enchantments.
     */
    @SubscribeEvent
    public static void addPseudoEnchantmentAttributeModifiers(ItemAttributeModifierEvent e) {
        ItemStack stack = e.getItemStack();

        if(stack.is(Items.ENCHANTED_BOOK)) return;

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(PseudoEnchantmentHandler.PSEUDO_ENCHANTS_TAG_ID)) return;

        // Lazy conversion: convert real enchants → pseudo enchants
        PseudoEnchantmentHandler.fixItemOnLoad(stack);
        CompoundTag pseudoEnchantsTag = tag.getCompound(PseudoEnchantmentHandler.PSEUDO_ENCHANTS_TAG_ID);

        CompoundTag gemBonusEnchantmentLevelsTag = tag.getCompound(PseudoEnchantmentHandler.GEMS_ENCHANTMENTS_LEVEL_BUFFS);

        var slot = e.getSlotType();
        short enderSurgeLevel = PseudoEnchantmentHandler.getEnderSurgeLevel(stack);

        for (String key : pseudoEnchantsTag.getAllKeys()) {

            Enchantment ench = PseudoEnchantmentHandler.getRealEnchantmentFromKey(key);
            if (ench == null) continue;

            short enchantLevel = pseudoEnchantsTag.getShort(key);
            short bonusLevels = enderSurgeLevel;
            bonusLevels += gemBonusEnchantmentLevelsTag.getShort(key);

            var validSlots = PseudoEnchantmentHandler.getSlotsForCategory(ench.category);
            if(!validSlots.contains(slot)) continue;

            //get attributes based on key
            List<PseudoEnchantmentHandler.PseudoAttribute> pseudoAttributes =
                    PseudoEnchantmentHandler.getPseudoAttributes(key);

            if (pseudoAttributes == null) continue;

            for (var pseudoAttribute : pseudoAttributes) {
                double amount = pseudoAttribute.amountPerLevel() * (enchantLevel + bonusLevels);

                AttributeModifier modifier = new AttributeModifier(
                        UUID.nameUUIDFromBytes((key + slot).getBytes()),
                        key + "_modifier",
                        amount,
                        pseudoAttribute.op()
                );

                e.addModifier(pseudoAttribute.attribute(), modifier);
            }
        }
    }

    /**
    Pseudo-enchantments are regular enchantments stored in a separate item tag than the default. This lets us nullify the enchantment behavior, and apply our own.
     This function is needed to actually let Pseudo-enchantments in the second slot combine with Pseudo-enchantments in the first slot.
     Without this function, anvils can not be used with Pseudo-enchanted items in the second slot.
     */
    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent e) {
        ItemStack left = e.getLeft();
        ItemStack right = e.getRight();

        if (left.isEmpty() || right.isEmpty()) return;
        if(!isValidMerge(left, right)) return;

        // Merge them
        var mergedPseudoEnchants = PseudoEnchantmentHandler.mergePseudoEnchants(left, right);

        //because we are implementing the AnvilUpdateEvent, we need to preserve vanilla behavior, which is why we need to
        // implement our own version of determining which real enchantment can be merged onto the left item.
        var mergedRealEnchants = PseudoEnchantmentHandler.mergeRealEnchants(left, right);

        // Create output
        ItemStack output = left.copy();
        PseudoEnchantmentHandler.writePseudoEnchantsMap(mergedPseudoEnchants, output);
        EnchantmentHelper.setEnchantments(mergedRealEnchants, output);

        /*
        var name = e.getName();
        if(!name.isEmpty()) output.setHoverName(Component.literal(name));
         */
        e.setOutput(output);

        e.setCost(mergedPseudoEnchants.size() + mergedRealEnchants.size());
    }


    @SubscribeEvent
    public static void grindstoneRemoveAllEnchants(GrindstoneEvent.OnPlaceItem e) {
        ItemStack stack = e.getTopItem();

        ItemStack copy = stack.copy();
        CompoundTag tag = copy.getOrCreateTag();
        tag.remove(PseudoEnchantmentHandler.PSEUDO_ENCHANTS_TAG_ID);

        copy.getEnchantmentTags().clear();

        e.setOutput(copy);
    }

    private static boolean isValidMerge(ItemStack left, ItemStack right) {

        // 1. Right item must not be empty
        if (right.isEmpty()) return false;

        // 2. If right is not a book, it must be the same item type
        boolean rightIsBook = right.getItem() == Items.ENCHANTED_BOOK;
        boolean sameItemType = left.getItem() == right.getItem();

        if (!rightIsBook && !sameItemType) {
            return false;
        }

        // 3. Check enchantment compatibility
        Map<Enchantment, Integer> leftEnchants = EnchantmentHelper.getEnchantments(left);
        Map<Enchantment, Integer> rightEnchants = EnchantmentHelper.getEnchantments(right);

        for (var entry : rightEnchants.entrySet()) {
            Enchantment ench = entry.getKey();

            // 3a. Check if enchantment can apply to the left item
            if (!ench.canEnchant(left)) {
                return false;
            }

            // ⭐ NEW: enforce weapon/tool/armor category restrictions
            if (!ench.category.canEnchant(left.getItem())) {
                return false;
            }

            // 3b. Check compatibility with all existing enchants
            for (Enchantment existing : leftEnchants.keySet()) {
                if (!ench.isCompatibleWith(existing)) {
                    return false;
                }
            }
        }

        return true;
    }

}
