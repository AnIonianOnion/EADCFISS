package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.util.ModUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.anionianonion.elementalattackdamagecompat.util.ModUtils.normalize;

public class AilmentModifiers implements IAilmentModifiers, INBTSerializable<CompoundTag> {

    // Extra stacks per ailment (e.g. +1 Ignite stack)
    private final Map<String, Integer> extraStacks = new HashMap<>();

    private final Map<String, Integer> extraMaxStacks = new HashMap<>();

    // Replacements: original → replacement (e.g. IGNITE → SHOCK)
    private final Map<String, String> replacements = new HashMap<>();

    // Alternates: element → list of ailments (e.g. FIRE → [SCORCH])
    private final Map<String, List<String>> alternates = new HashMap<>();

    @Override
    public int getExtraStacks(String ailment) {
        return extraStacks.getOrDefault(normalize(ailment), 0);
    }

    @Override
    public int getExtraMaxStacks(String ailment) {
        return extraMaxStacks.getOrDefault(normalize(ailment), 0);
    }

    @Override
    public String getReplacement(String originalAilment) {
        return replacements.getOrDefault(normalize(originalAilment), null);
    }

    @Override
    public List<String> getAlternateAilments(String element) {
        return alternates.getOrDefault(normalize(element), List.of());
    }

    // --- Mutators you can call from items/ascendancies/etc. ---

    @Override
    public void setExtraStacks(String ailment, int stacks) {
        if (stacks <= 0) {
            extraStacks.remove(normalize(ailment));
        } else {
            extraStacks.put(normalize(ailment), stacks);
        }
    }

    @Override
    public void setExtraMaxStacks(String ailment, int stacks) {
        if(stacks <= 0) {
            extraMaxStacks.remove(normalize(ailment));
        } else {
          extraMaxStacks.put(normalize(ailment), stacks);
        }
    }

    @Override
    public void setReplacement(String originalAilment, String replacement) {
        if (replacement == null) {
            replacements.remove(normalize(originalAilment));
        } else {
            replacements.put(normalize(originalAilment), normalize(replacement));
        }
    }

    @Override
    public void setAlternateAilments(String element, List<String> ailments) {
        if (ailments == null || ailments.isEmpty()) {
            alternates.remove(normalize(element));
        } else {
            alternates.put(normalize(element), ailments.stream().map(ModUtils::normalize).toList());
        }
    }

    // --- Saving ---

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        //extra max stacks
        CompoundTag maxStacksTag = new CompoundTag();
        extraMaxStacks.forEach(maxStacksTag::putInt);
        tag.put("extraMaxStacks", maxStacksTag);

        // extra stacks
        CompoundTag stacksTag = new CompoundTag();
        extraStacks.forEach(stacksTag::putInt);
        tag.put("extraStacks", stacksTag);

        // replacements
        CompoundTag replTag = new CompoundTag();
        replacements.forEach(replTag::putString);
        tag.put("replacements", replTag);

        // alternates
        CompoundTag altTag = new CompoundTag();
        alternates.forEach((element, list) -> {
            ListTag arr = new ListTag();
            list.forEach(a -> arr.add(StringTag.valueOf(a)));
            altTag.put(element, arr);
        });
        tag.put("alternates", altTag);

        return tag;
    }

    // --- Loading ---

    @Override
    public void deserializeNBT(CompoundTag tag) {

        extraMaxStacks.clear();
        extraStacks.clear();
        replacements.clear();
        alternates.clear();

        //extra max stacks
        CompoundTag maxStacksTag = tag.getCompound("extraMaxStacks");
        for(String key : maxStacksTag.getAllKeys()) {
            extraMaxStacks.put(String.valueOf(key), maxStacksTag.getInt(key));
        }

        // extra stacks
        CompoundTag stacksTag = tag.getCompound("extraStacks");
        for (String key : stacksTag.getAllKeys()) {
            extraStacks.put(String.valueOf(key), stacksTag.getInt(key));
        }

        // replacements
        CompoundTag replTag = tag.getCompound("replacements");
        for (String key : replTag.getAllKeys()) {
            replacements.put(
                    String.valueOf(key),
                    replTag.getString(key)
            );
        }

        // alternates
        CompoundTag altTag = tag.getCompound("alternates");
        for (String key : altTag.getAllKeys()) {
            String element = String.valueOf(key);
            ListTag arr = altTag.getList(key, Tag.TAG_STRING);

            List<String> list = arr.stream()
                    .map(Tag::getAsString)
                    .toList();

            alternates.put(element, list);
        }
    }
}
