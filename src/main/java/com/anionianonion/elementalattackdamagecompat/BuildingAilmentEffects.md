# Building Ailment Effects

This mod allows developers to create RPG style elements, and create ailments for each one.

### todo: show how to download mod and use for intelliJ

## Getting started
In a static block within your mod's class (where your Mod constructor is), you have two options (pick one):

a. create a static block inside of the class

    @Mod(YourMod.MOD_ID)
    public class YourMod {

        ...
        static {
            API api = new API();
        }
        ...

        public YourMod(FMLJavaModLoadingContext context) {
            ...
        }
        ...
    }

OR

b. directly inside of your Mod constructor

    public YourMod(FMLJavaModLoadingContext context) {
        ...
        API api = new API();
        ...
    }

## Creating an element
Where you have written 
> API api = new API();
>
you can register an element called "fire" by doing
> api.createElement("fire");
>


```java
api.addNonDamagingAilmentEffect("viral", new NonDamagingAilmentEffectBuilder()
    .setNamespace(MOD_ID)
    .setId("viral")
    .doesVaryEffectDuration(false)
    .doesVaryEffectDuration(false)
    .setBaseDurationInSeconds(3)
    .setEffectStrength(0.5f)
    .stackingMode(AilmentStackingMode.ADDITIVE_STACKING)
    .setMaxStacks(6)
    .onApply((defender, instance) -> {
        UUID uuid = UUID.randomUUID();
        
        Objects.requireNonNull(defender.getAttribute(Attributes.MAX_HEALTH))
        .addPermanentModifier(new AttributeModifier(
            uuid,
            "viral_debuff",
            -0.5, // halve max HP
            AttributeModifier.Operation.MULTIPLY_TOTAL)
        );
        instance.addModifierForNewStack(Attributes.MAX_HEALTH, uuid);

        if (defender.getHealth() > defender.getMaxHealth()) {
                        defender.setHealth(defender.getMaxHealth());
        }
    })
    .onExpire((defender, instance) -> {

        //do not remove stacks manually here, as AilmentInstance handles it
        if (defender.getHealth() > defender.getMaxHealth()) {
            defender.setHealth(defender.getMaxHealth());
        }
    })
    .build());
```
