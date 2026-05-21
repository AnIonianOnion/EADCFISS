package com.anionianonion.elementalattackdamagecompat;


import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.*;
import com.anionianonion.elementalattackdamagecompat.commands.HealCommand;
import com.anionianonion.elementalattackdamagecompat.commands.SummonResistGolemCommand;
import com.anionianonion.elementalattackdamagecompat.datagen.DataGenerators;
import com.anionianonion.elementalattackdamagecompat.enchants.ModEnchantments;
import com.anionianonion.elementalattackdamagecompat.events.ApotheosisEvents;
import com.anionianonion.elementalattackdamagecompat.items.ModItems;
import com.anionianonion.elementalattackdamagecompat.util.EADC_DefaultSystemInitializer;
import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forgespi.language.IModInfo;
import org.slf4j.Logger;

import java.util.UUID;
import java.util.stream.Collectors;

@Mod(ElementalAttackDamageCompatMod.MOD_ID)
public class ElementalAttackDamageCompatMod {

    //todo: finish ailment system
    //todo: let pseudo-enchants count as enchants within Smithing Table.

    public static final String MOD_ID = "elementalattackdamagecompat";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean IS_RANDOM_DAMAGE_MOD_ENABLED, IS_APOTHEOSIS_MOD_ENABLED;

    static {
        EADC_DefaultSystemInitializer.initialize();
    }

    public ElementalAttackDamageCompatMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addAttributesToLivingEntities);
        modEventBus.register(new DataGenerators());

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModAttributes.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ModItems.register(modEventBus);


        IS_RANDOM_DAMAGE_MOD_ENABLED = ModList.get().isLoaded("randomdamagerange");
        IS_APOTHEOSIS_MOD_ENABLED = ModList.get().isLoaded("apotheosis");

        if(IS_APOTHEOSIS_MOD_ENABLED) {
            MinecraftForge.EVENT_BUS.register(new ApotheosisEvents());
        }
    }

    //only affects living entities by default
    private void addAttributesToLivingEntities(EntityAttributeModificationEvent event) {

        /*
        //registers attributes to only players
        for (var attribute : ModAttributes.ATTRIBUTES_REGISTRY.getEntries()) {
            event.add(EntityType.PLAYER, attribute.get());
        }
         */

        //all living entities
        for (var attribute : ModAttributes.ATTRIBUTES_REGISTRY.getEntries()) {
            for(var type : event.getTypes()) {
                event.add(type, attribute.get());
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        String mods = ModList.get().getMods().stream()
                .map(IModInfo::getModId)
                .sorted()
                .collect(Collectors.joining(", "));

        LOGGER.info("Loaded mods: {}", mods);

        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) ->
            {
                HealCommand.register(e.getDispatcher());
                SummonResistGolemCommand.register(e.getDispatcher());
            }
        );

        //Enchantments and other Minecraft or mod things must be enqueued, otherwise they will be null when Minecraft first boots up
        event.enqueueWork(EADC_DefaultSystemInitializer::delayedInitialization);
    }

}
