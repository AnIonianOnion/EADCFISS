package com.anionianonion.elementalattackdamagecompat.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DataGenerators {

    @SubscribeEvent
    public void gatherData(GatherDataEvent e) {
        DataGenerator dataGenerator = e.getGenerator();
        PackOutput packOutput = dataGenerator.getPackOutput();

        dataGenerator.addProvider(true, new ModLangProvider(packOutput));
    }
}
