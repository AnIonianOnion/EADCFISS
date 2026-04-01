package com.anionianonion.elementalattackdamagecompat.capability;

import com.anionianonion.elementalattackdamagecompat.ailments.IAilmentData;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

public class AilmentDataCapability {

    public static final Capability<IAilmentData> INSTANCE =
            CapabilityManager.get(new CapabilityToken<>() {});
}
