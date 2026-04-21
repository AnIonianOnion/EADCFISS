package com.anionianonion.elementalattackdamagecompat.api;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentEffect;

interface APIRequirements
{
    public void createElement(String elementName);
    public void removeElement(String elementName);

    void addCustomResistAttributeForElement(String elementName, String resistKey);

    void addDefaultAilmentToElement(String element, String ailment);

    void removeAilmentFromElement(String element, String ailment);

    public void createDamagingAilment(String ailmentName);
    public void createNonDamagingAilment(String ailmentName);

    void addDamagingAilmentEffect(String ailment, AilmentEffect effect);

    void addNonDamagingAilmentEffect(String ailment, AilmentEffect effect);

    public void removeAilment(String ailmentName);
}
