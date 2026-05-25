package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

import java.util.*;

import static com.anionianonion.elementalattackdamagecompat.util.ModUtils.normalize;

public class AilmentData implements IAilmentData {

    private final Map<String, AilmentInstance> ailmentsOnEntity = new HashMap<>();

    @Override
    public void addAilment(String ailmentKey, AilmentInstance newInst,
                           LivingEntity defender) {

        ailmentKey = normalize(ailmentKey);
        AilmentEffect effect = newInst.effect;
        AilmentStackingMode mode = effect.getStackingMode();

        // FIRST APPLICATION
        if (!ailmentsOnEntity.containsKey(ailmentKey)) {
            // Ignite and other non-stacking ailments should NOT add a stack here
            if (effect.getStackingMode() == AilmentStackingMode.ADDITIVE_STACKING) {
                Object payload = effect.createStackPayload(defender, newInst);
                newInst.addStack(defender, newInst.totalDamage, newInst.totalEffectStrength, newInst.getDuration(), AilmentInstance.StackEntry.ReplacementMode.OLDEST, payload);
            }

            ailmentsOnEntity.put(ailmentKey, newInst);
            effect.onApply(defender, newInst);
            return;
        }

        //old instance is never null
        AilmentInstance old = ailmentsOnEntity.get(ailmentKey);
        old.setMaxStacks(newInst.maxStacks);

        switch (mode) {

            case STRONGEST_WINS -> {
                if (newInst.totalDamage > old.totalDamage) {
                    old.onExpire(defender, old);
                    ailmentsOnEntity.put(ailmentKey, newInst);
                    effect.onApply(defender, newInst);
                } else if (newInst.totalDamage == old.totalDamage) {
                    old.refresh(newInst.getDuration());
                }
            }

            case STRONGEST_INTENSITY -> {
                if(old.maxStacks <= 1) {
                    if (newInst.strongestEffectStrength > old.strongestEffectStrength) {
                        old.onExpire(defender, old);
                        ailmentsOnEntity.put(ailmentKey, newInst);
                        effect.onApply(defender, newInst);
                    }
                    return;
                }
                // If more than 1 stack allowed → behave like additive stacking
                Object payload = effect.createStackPayload(defender, newInst);

                old.addStack(
                        defender,
                        newInst.totalDamage,
                        newInst.totalEffectStrength,
                        newInst.getDuration(),
                        AilmentInstance.StackEntry.ReplacementMode.WEAKEST,
                        payload
                );

                effect.onApply(defender, old);
                old.refresh(newInst.getDuration());

            }

            case STRONGEST_DURATION -> {
                if (newInst.getDuration() > old.getDuration()) {
                    old.onExpire(defender, old);
                    ailmentsOnEntity.put(ailmentKey, newInst);
                    effect.onApply(defender, newInst);
                }
            }

            case ADDITIVE_STACKING -> {
                Object payload = effect.createStackPayload(defender, newInst);

                old.addStack(
                        defender,
                        newInst.totalDamage,
                        newInst.totalEffectStrength,
                        newInst.getDuration(),
                        AilmentInstance.StackEntry.ReplacementMode.OLDEST,
                        payload
                );

                effect.onApply(defender, old);
                old.refresh(newInst.getDuration());
            }

            case NO_STACKING_REFRESH_DURATION -> {
                old.refresh(newInst.getDuration());
            }
        }
    }

    @Override
    public Map<String, AilmentInstance> getAilmentsOnEntity() {
        return ailmentsOnEntity;
    }

    @Override
    public void tick(LivingEntity entity) {

        Iterator<Map.Entry<String, AilmentInstance>> it = ailmentsOnEntity.entrySet().iterator();

        while (it.hasNext()) {
            var entry = it.next();
            var inst = entry.getValue();

            inst.tickStacks(entity, inst.effect);
            inst.tickEffect(entity, inst);

            if (inst.tickDown()) {
                inst.onExpire(entity, inst);
                it.remove();
            }
        }
    }
}
