package com.anionianonion.elementalattackdamagecompat.capability;

import com.anionianonion.elementalattackdamagecompat.ailments.IAilmentModifiers;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class AilmentModifierCapability {

    public static final Capability<IAilmentModifiers> INSTANCE =
            CapabilityManager.get(new CapabilityToken<>() {});
}
