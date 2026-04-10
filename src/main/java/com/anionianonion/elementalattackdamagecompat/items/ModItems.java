package com.anionianonion.elementalattackdamagecompat.items;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, ElementalAttackDamageCompatMod.MOD_ID);

    public static final RegistryObject<Item> SCORCH_HELMET =
            ITEMS.register("scorch_helmet",
                    () -> new ScorchHelmetItem(
                            ArmorMaterials.DIAMOND,
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> SAP_CHESTPLATE =
            ITEMS.register("sap_chestplate",
                    () -> new SapChestplateItem(
                            ArmorMaterials.DIAMOND,
                            new Item.Properties()
                    )
            );

    public static final RegistryObject<Item> BRITTLE_BOOTS =
            ITEMS.register("brittle_boots",
                    () -> new BrittleBootsItem(
                            ArmorMaterials.DIAMOND,
                            new Item.Properties()
                    )
            );


    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
