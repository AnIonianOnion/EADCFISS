package com.anionianonion.elementalattackdamagecompat.capability;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ElementalAttackDamageCompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {

        Entity entity = event.getObject();

        if (entity instanceof LivingEntity) {
            event.addCapability(
                    ResourceLocation.fromNamespaceAndPath(ElementalAttackDamageCompatMod.MOD_ID, "ailment_data"),
                    new AilmentDataProvider()
            );
            event.addCapability(
                    ResourceLocation.fromNamespaceAndPath(ElementalAttackDamageCompatMod.MOD_ID, "ailment_modifiers"),
                    new AilmentModifierProvider()
            );
        }
    }
}
