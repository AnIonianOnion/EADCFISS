package com.anionianonion.elementalattackdamagecompat.datagen;

import com.anionianonion.elementalattackdamagecompat.ElementalAttackDamageCompatMod;
import com.anionianonion.elementalattackdamagecompat.ModAttributes;
import com.anionianonion.elementalattackdamagecompat.util.ModUtils;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

import java.util.HashMap;
import java.util.List;

public class ModLangProvider extends LanguageProvider {

    public ModLangProvider(PackOutput output) {
        super(output, ElementalAttackDamageCompatMod.MOD_ID, "en_us");
    }

    private final HashMap<String, String> schoolsToSchoolDisplayName = new HashMap<>();

    private void fillDefaultElementDisplayName() {
        for(String elementName : ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES) {
            String capitalized = ModUtils.capitalize(elementName);
            schoolsToSchoolDisplayName.put(elementName, capitalized);
        }
    }

    private void replaceDisplayNames() {
        /*
        schoolsToSchoolDisplayName.replace("blade", "Spellblade");
        schoolsToSchoolDisplayName.replace("ritual", "Occultic");
         */
    }

    @Override
    protected void addTranslations() {
        System.out.println("ELEMENTAL_ATTRIBUTE_NAMES = " + ModAttributes.ELEMENTAL_ATTRIBUTE_NAMES);
        fillDefaultElementDisplayName();
        replaceDisplayNames();
        String descriptionIncomplete = "The value you see to the right is incomplete. " +
                "Sum up each element's (base+added damage * total increased modifiers (aka. from attack/spell, specific element, and overall type) * total more modifiers (same thing).";

        add("eadcfiss.comment_id.1", "Elements");
        for(var entry : schoolsToSchoolDisplayName.entrySet()) {
            String schoolId = entry.getKey();
            String schoolDisplayName = entry.getValue();

            add(String.format("attribute.attack_damage.%s", schoolId), String.format("%s Damage to Attacks", schoolDisplayName));
            add(String.format("attribute.attack_damage.%s.desc", schoolId), descriptionIncomplete);

            add(String.format("attribute.spell_damage.%s", schoolId), String.format("%s Damage to Spells", schoolDisplayName));
            add(String.format("attribute.spell_damage.%s.desc", schoolId), descriptionIncomplete);

            add(String.format("attribute.attack_and_spell_damage.%s", schoolId), String.format("%s Damage to Spells and Attacks", schoolDisplayName));
            add(String.format("attribute.attack_and_spell_damage.%s.desc", schoolId), descriptionIncomplete);

            add(String.format("attribute.max_resistance.%s", schoolId), String.format("Max %s Resistance", schoolDisplayName));

            add(String.format("attribute.ailment.duration_of_%s_ailments", schoolId), String.format("Duration of %s Ailments", schoolDisplayName));

            add(String.format("enchantment.%s.%s_aspect", ElementalAttackDamageCompatMod.MOD_ID, schoolId), String.format("%s Aspect", ModUtils.capitalize(schoolId)));
        }

        add("eadcfiss.comment_id.2", "Ailments");
        for(String ailmentName : ModAttributes.AILMENT_NAMES) {
            String capitalizedAilmentName = ModUtils.capitalize(ailmentName);
            add(String.format("attribute.ailment.chance_to_inflict_%s", ailmentName), String.format("Chance to Inflict %s", capitalizedAilmentName));
            add(String.format("attribute.ailment.%s_duration", ailmentName), String.format("%s Duration", capitalizedAilmentName));
        }

        for(String ailmentName : ModAttributes.NON_DAMAGING_AILMENT_NAMES) {
            String capitalizedAilmentName = ModUtils.capitalize(ailmentName);
            add(String.format("attribute.ailment.%s_effect", ailmentName), String.format("%s Effect", capitalizedAilmentName));
        }

        add("eadcfiss.comment_id.3", "School Groups");
        String[] schoolGroups = List.of("Primal", "Vivid", "Wild", "Tuning", "Emotional").toArray(new String[0]); //idiom
        for(int i = 0; i < schoolGroups.length; i++) {
            add(String.format("attribute.multipliers.type_%s_damage", (i + 1)), String.format("%s Damage", schoolGroups[i]));
        }

        add("eadcfiss.comment_id.4", "Crit Attributes");
        add("attribute.spell.crit_chance", "Spell Critical Strike Chance");
        add("attribute.spell.crit_damage", "Critical Strike Multiplier for Spell Damage");
        add("attribute.attack.crit_chance", "Attack Critical Strike Chance");
        add("attribute.attack.crit_damage", "Critical Strike Multiplier for Attack Damage");
        add("attribute.global.crit_chance", "Global Critical Strike Chance");
        add("attribute.global.crit_damage", "Global Critical Strike Multiplier");

        add("eadcfiss.comment_id.5", "School Groups");
        add("attribute.multipliers.attack_damage", "Attack Damage");
        add("attribute.multipliers.spell_damage", "Spell Damage");

        add("eadcfiss.comment_id.6", "Spell Suppression & Dodge");
        add("attribute.spell_suppression.chance", "Chance to Suppress Spell Damage");
        add("attribute.spell_suppression.prevented", "Suppressed Spell Damage Prevented");
        add("attribute.spell_dodge.chance", "Chance to Dodge Spell Hits");

        add("eadcfiss.comment_id.7", "Other");
        add("attribute.name.generic.attack_damage", "Physical Damage to Attacks");
        add("tooltip.enchantment_level_bonus.format", "%s %s");
        add("tooltip.enchantment_level_bonus.format.original_plus_bonus", "(%s + %s)");
    }
}
