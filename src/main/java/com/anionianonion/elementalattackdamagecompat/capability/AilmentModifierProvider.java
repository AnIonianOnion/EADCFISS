package com.anionianonion.elementalattackdamagecompat.capability;

import com.anionianonion.elementalattackdamagecompat.ailments.IAilmentModifiers;
import com.anionianonion.elementalattackdamagecompat.ailments.AilmentModifiers;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AilmentModifierProvider implements ICapabilityProvider {

    private final AilmentModifiers backend = new AilmentModifiers();

    private final LazyOptional<IAilmentModifiers> optional =
            LazyOptional.of(() -> backend);


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == AilmentModifierCapability.INSTANCE ? optional.cast() : LazyOptional.empty();
    }
}
