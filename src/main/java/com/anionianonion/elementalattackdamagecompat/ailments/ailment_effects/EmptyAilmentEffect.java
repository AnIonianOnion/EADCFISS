package com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffect;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentStackingMode;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

public final class EmptyAilmentEffect implements AilmentEffect {

    public static final EmptyAilmentEffect INSTANCE = new EmptyAilmentEffect();

    private EmptyAilmentEffect() {}

    @Override
    public float getDurationInSeconds(LivingEntity livingAttackerOrCaster) {
        return 0f; // or 1f if you want it to auto-expire cleanly
    }

    @Override
    public List<AilmentStackingMode> getStackingModes() {
        return new ArrayList<>(List.of(AilmentStackingMode.STACKING_THEN_STRONGEST_DAMAGE, AilmentStackingMode.STACKING_THEN_STRONGEST_INTENSITY));
    }

}
