package com.anionianonion.elementalattackdamagecompat.capability;

import com.anionianonion.elementalattackdamagecompat.ailments.AilmentData;
import com.anionianonion.elementalattackdamagecompat.ailments.IAilmentData;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AilmentDataProvider implements ICapabilityProvider {

    private final LazyOptional<IAilmentData> optional =
            LazyOptional.of(AilmentData::new);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == AilmentDataCapability.INSTANCE ? optional.cast() : LazyOptional.empty();
    }
}
