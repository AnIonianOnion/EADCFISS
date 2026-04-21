package com.anionianonion.elementalattackdamagecompat;

import com.anionianonion.elementalattackdamagecompat.ailments.NonDamagingAilmentEffectBuilder;
import com.anionianonion.elementalattackdamagecompat.ailments.ailment_effects.*;
import com.anionianonion.elementalattackdamagecompat.api.API;
import com.anionianonion.elementalattackdamagecompat.commands.HealCommand;
import com.anionianonion.elementalattackdamagecompat.items.ModItems;
import com.mojang.logging.LogUtils;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Mod(ElementalAttackDamageCompatMod.MOD_ID)
public class ElementalAttackDamageCompatMod {

    public static final String MOD_ID = "elementalattackdamagecompat";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static boolean IS_RANDOM_DAMAGE_MOD_ENABLED;

    static {
        API api = new API();

        api.createElement("fire");
        api.createElement("ice");
        api.createElement("lightning");
        api.createElement("holy");
        api.createElement("ender");
        api.createElement("blood");
        api.createElement("nature");
        api.createElement("eldritch");
        api.createElement("sound");
        api.createElement("geo");
        api.createElement("aqua");
        api.createElement("technomancy");
        api.createElement("abyssal");

        api.createDamagingAilment("ignite");
        api.addDefaultAilmentToElement("fire", "ignite");
        api.addDamagingAilmentEffect("ignite", new IgniteEffect());

        api.createNonDamagingAilment("chill");
        api.addDefaultAilmentToElement("ice", "chill");
        api.addNonDamagingAilmentEffect("chill", new ChillEffect());

        api.createNonDamagingAilment("freeze");
        api.addDefaultAilmentToElement("ice", "freeze");
        api.addNonDamagingAilmentEffect("freeze", new FreezeEffect());

        api.createNonDamagingAilment("shock");
        api.addDefaultAilmentToElement("lightning", "shock");
        api.addNonDamagingAilmentEffect("shock", new ShockEffect());

        api.createDamagingAilment("poison");
        api.addDefaultAilmentToElement("nature", "poison");
        api.addDamagingAilmentEffect("poison", new PoisonEffect());

        api.createDamagingAilment("bleed");
        api.addDefaultAilmentToElement("blood", "bleed");
        api.addDamagingAilmentEffect("bleed", new BleedEffect());


        api.createNonDamagingAilment("scorch");
        api.addNonDamagingAilmentEffect("scorch", new ScorchEffect());

        api.createNonDamagingAilment("brittle");
        api.addNonDamagingAilmentEffect("brittle", new BrittleEffect());

        api.createNonDamagingAilment("sap");
        api.addNonDamagingAilmentEffect("sap", new SapEffect());

        api.createNonDamagingAilment("viral");
        api.addDefaultAilmentToElement("eldritch", "viral");
        api.addNonDamagingAilmentEffect("viral", new NonDamagingAilmentEffectBuilder()
                .setNamespace(MOD_ID)
                .setId("viral")
                .doesVaryEffectDuration(false)
                .doesVaryEffectDuration(false)
                .setBaseDurationInSeconds(3f)
                .setEffectStrength(0.5f)
                .onApply((defender, instance) -> {
                    UUID uuid = UUID.randomUUID();
                    instance.addModifierId(uuid);

                    Objects.requireNonNull(defender.getAttribute(Attributes.MAX_HEALTH))
                            .addTransientModifier(new AttributeModifier(
                                    uuid,
                                    "viral_debuff",
                                    -0.5, // halve max HP
                                    AttributeModifier.Operation.MULTIPLY_TOTAL
                            ));

                    if (defender.getHealth() > defender.getMaxHealth()) {
                        defender.setHealth(defender.getMaxHealth());
                    }
                })
                .onExpire((defender, instance) -> {
                    var attr = defender.getAttribute(Attributes.MAX_HEALTH);
                    if (attr == null) return;

                    for (UUID uuid : instance.getAttributeModifierIds()) {
                        attr.removeModifier(uuid);
                    }

                    instance.clearModifierIds();

                    if (defender.getHealth() > defender.getMaxHealth()) {
                        defender.setHealth(defender.getMaxHealth());
                    }
                })
                .build());
    }

    public ElementalAttackDamageCompatMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::addAttributesToLivingEntities);

        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        ModAttributes.register(modEventBus);
        ModItems.register(modEventBus);

        IS_RANDOM_DAMAGE_MOD_ENABLED = ModList.get().isLoaded("randomdamagerange");
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

        //todo: make this less AI
        MinecraftForge.EVENT_BUS.addListener((RegisterCommandsEvent e) ->
                HealCommand.register(e.getDispatcher())
        );
    }

}
