package com.anionianonion.elementalattackdamagecompat;

import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

@Mod(ElementalAttackDamageCompatMod.MOD_ID)
public class ElementalAttackDamageCompatMod {

    public static final String MOD_ID = "elementalattackdamagecompat";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean IS_RANDOM_DAMAGE_MOD_ENABLED;

    public ElementalAttackDamageCompatMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addAttributesToPlayers);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModAttributes.register(modEventBus);

        IS_RANDOM_DAMAGE_MOD_ENABLED = ModList.get().isLoaded("randomdamagerange");
    }

    private void addAttributesToPlayers(EntityAttributeModificationEvent event) {

        //registers attributes to only players
        for (var attribute : ModAttributes.ATTRIBUTES_REGISTRY.getEntries()) {
            event.add(EntityType.PLAYER, attribute.get());
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM ELEMENTAL ATTACK DAMAGE COMPAT MOD'S COMMON SETUP");
        LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
    }

}
