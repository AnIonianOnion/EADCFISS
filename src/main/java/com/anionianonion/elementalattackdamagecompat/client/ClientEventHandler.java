package com.anionianonion.elementalattackdamagecompat.client;

import com.anionianonion.elementalattackdamagecompat.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {

        // Disable ONLY vanilla falling crit particles
        if (Config.disableVanillaFallingCrit && event.isVanillaCritical()) {
            event.setResult(CriticalHitEvent.Result.DENY);
            return;
        }

        // Disable ONLY vanilla fully-charged bow crit particles
        if (Config.disableVanillaFullyChargedBowCrit) {
            Player player = event.getEntity();
            if (player.getMainHandItem().getItem() instanceof BowItem ||
                    player.getMainHandItem().getItem() instanceof CrossbowItem) {

                // Vanilla bow crits show CRIT particles even if not falling
                event.setResult(CriticalHitEvent.Result.DENY);
            }
        }

    }


}
