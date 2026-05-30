package com.anionianonion.elementalattackdamagecompat.ailments;

import net.minecraft.world.entity.LivingEntity;

import java.util.*;

import static com.anionianonion.elementalattackdamagecompat.util.ModUtils.normalize;

public class AilmentData implements IAilmentData {

    private final Map<String, AilmentInstance> ailmentsOnEntity = new HashMap<>();

    /**
    Gets or creates the ailment to add a stack to.
    */
    @Override
    public void addAilment(LivingEntity livingAttackerOrCaster, String ailmentKey, AilmentInstance newInst,
                           LivingEntity defender) {

        String finalAilmentKey = normalize(ailmentKey);
        var ailmentStackingMode = newInst.effect.getStackingMode();

        //On First Apply
        if(!ailmentsOnEntity.containsKey(finalAilmentKey) && AilmentEffectRegistry.getAllAilments().contains(finalAilmentKey)) {
            ailmentsOnEntity.put(finalAilmentKey, newInst);
            newInst.onFirstApplication(defender);
        }

        AilmentInstance exInst = ailmentsOnEntity.get(finalAilmentKey);
        exInst.setMaxStacks(newInst.maxStacks);

        Object payload = exInst.onStackApply(defender);

        AilmentInstance.StackEntry.ReplacementMode replacementMode = AilmentInstance.StackEntry.ReplacementMode.OLDEST;
        if(ailmentStackingMode == AilmentStackingMode.STACKING_THEN_STRONGEST_DAMAGE || ailmentStackingMode == AilmentStackingMode.STACKING_THEN_STRONGEST_INTENSITY) {
            replacementMode = AilmentInstance.StackEntry.ReplacementMode.WEAKEST;
        }

        exInst.addStack(defender, livingAttackerOrCaster, newInst.getDamage(),
                newInst.getEffectStrength(), newInst.getDuration(),
                replacementMode, ailmentStackingMode, payload);

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

            inst.tickStacks(entity);
            inst.tickEffect(entity);

            if (inst.tickDown()) {
                inst.onLastExpiration(entity);
                it.remove();
            }
        }
    }
}
