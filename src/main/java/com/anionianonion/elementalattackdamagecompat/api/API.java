package com.anionianonion.elementalattackdamagecompat.api;

import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffectRegistry;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffectRegistry.AilmentCategory;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentsRegistry;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffect;

public class API implements APIRequirements {

    // ---------------------------
    // ELEMENT MANAGEMENT
    // ---------------------------

    @Override
    public void createElement(String element) {
        AilmentsRegistry.createElement(element);
    }

    @Override
    public void removeElement(String elementName) {
        AilmentsRegistry.removeElement(elementName);
    }

    /**
    For non-ISS elements, you may apply resistance from this attribute for that element.
     */
    @Override
    public void addCustomResistAttributeForElement(String elementName, String resistKey) {
        ModAttributes.customSchoolToResistAttributeKey.putIfAbsent(elementName, resistKey);
    }

    /**
    This mod, when calculating damage, assumes (because of Iron's Spells and Spellbooks' normalized resist attributes) that the specific elemental resistance is normalized between 1 and 2, where 1 is 0% resistance to that element, and 2 is 100%.
     Then it renormalizes it to be between 0 and 1, where 0 is 0% resist to that, and 1 is 100%. If your resist attribute is already normalized between 0 and 1, you may add a 1 as the offset here to
     stop double-normalization.
     */
    public void setOffsetForResistAttribute(String resistKey, int offset) {
        ModAttributes.resistAttributeKeyToResistOffset.putIfAbsent(resistKey, offset);
    }


    @Override
    public void addDefaultAilmentToElement(String element, String ailment) {
        AilmentsRegistry.addDefaultAilmentToElement(element, ailment);
    }

    @Override
    public void removeAilmentFromElement(String element, String ailment) {
        AilmentsRegistry.removeAilmentFromElement(element, ailment);
    }


    // ---------------------------
    // AILMENT REGISTRATION
    // ---------------------------

    @Override
    public void createDamagingAilment(String ailment) {
        // Register ailment with a placeholder effect until user assigns one
        AilmentEffectRegistry.registerAilment(
                ailment,
                AilmentCategory.DAMAGING,
                AilmentEffect.empty() // safe placeholder, never null
        );
    }

    @Override
    public void createNonDamagingAilment(String ailment) {
        AilmentEffectRegistry.registerAilment(
                ailment,
                AilmentCategory.NON_DAMAGING,
                AilmentEffect.empty()
        );
    }

    @Override
    public void addDamagingAilmentEffect(String ailment, AilmentEffect effect) {
        AilmentEffectRegistry.registerAilment(
                ailment,
                AilmentCategory.DAMAGING,
                effect
        );
    }

    @Override
    public void addNonDamagingAilmentEffect(String ailment, AilmentEffect effect) {
        AilmentEffectRegistry.registerAilment(
                ailment,
                AilmentCategory.NON_DAMAGING,
                effect
        );
    }


    // ---------------------------
    // AILMENT REMOVAL
    // ---------------------------

    @Override
    public void removeAilment(String ailment) {
        AilmentEffectRegistry.removeAilment(ailment);
    }


    // ---------------------------
    // DEBUG / MAINTENANCE
    // ---------------------------

    public void printAilmentRegistry() {
        AilmentEffectRegistry.printDebug();
    }
}
