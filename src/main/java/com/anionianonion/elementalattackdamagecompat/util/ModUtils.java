package com.anionianonion.elementalattackdamagecompat.util;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffect;
import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffect;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

public class ModUtils {

    public static String normalize(String key) {
        return key.toLowerCase(Locale.ROOT).trim();
    }

    public static String capitalize(String word) {
        String firstLetter = word.substring(0, 1).toUpperCase();
        String restOfString = word.substring(1);

        return firstLetter + restOfString;
    }

    public static int getDurationInTicks(AilmentEffect ae, float damage, LivingEntity livingDefender, LivingEntity livingAttackerOrCaster) {
        if(ae instanceof NonDamagingAilmentEffect ndae) {
            return ndae.isUsingVaryingEffectDuration() ?
                    (int) (ndae.computeVariableEffectDuration(damage, livingDefender, livingAttackerOrCaster) * 20) :
                    getBasicDurationInTicks(ndae, livingAttackerOrCaster);
        }
        else {
            return getBasicDurationInTicks(ae, livingAttackerOrCaster);
        }
    }

    /**
     Freeze duration doesn't have a constant value,
     so this method can only be used on AilmentEffects with constant values.
     */
    public static int getBasicDurationInTicks(AilmentEffect ae, LivingEntity livingAttackerOrCaster) {

        return (int) (ae.getDurationInSeconds(livingAttackerOrCaster) * 20);
    }
}
