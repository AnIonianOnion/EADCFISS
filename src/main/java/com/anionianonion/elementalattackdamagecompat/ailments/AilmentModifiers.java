package com.anionianonion.elementalattackdamagecompat.ailments;

import com.anionianonion.elementalattackdamagecompat.Element;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class AilmentModifiers implements IAilmentModifiers, INBTSerializable<CompoundTag> {

    // Extra stacks per ailment (e.g. +1 Ignite stack)
    private final Map<Ailment, Integer> extraStacks = new EnumMap<>(Ailment.class);

    // Replacements: original → replacement (e.g. IGNITE → SHOCK)
    private final Map<Ailment, Ailment> replacements = new EnumMap<>(Ailment.class);

    // Alternates: element → list of ailments (e.g. FIRE → [SCORCH])
    private final Map<Element, List<Ailment>> alternates = new EnumMap<>(Element.class);

    @Override
    public int extraStacks(Ailment ailment) {
        return extraStacks.getOrDefault(ailment, 0);
    }

    @Override
    public Ailment replace(Ailment original) {
        return replacements.getOrDefault(original, null);
    }

    @Override
    public List<Ailment> alternate(Element element) {
        return alternates.getOrDefault(element, List.of());
    }

    // --- Mutators you can call from items/ascendancies/etc. ---

    public void setExtraStacks(Ailment ailment, int stacks) {
        if (stacks <= 0) {
            extraStacks.remove(ailment);
        } else {
            extraStacks.put(ailment, stacks);
        }
    }

    public void setReplacement(Ailment original, Ailment replacement) {
        if (replacement == null) {
            replacements.remove(original);
        } else {
            replacements.put(original, replacement);
        }
    }

    public void setAlternate(Element element, List<Ailment> ailments) {
        if (ailments == null || ailments.isEmpty()) {
            alternates.remove(element);
        } else {
            alternates.put(element, List.copyOf(ailments));
        }
    }

    // --- Saving ---

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();

        // extra stacks
        CompoundTag stacksTag = new CompoundTag();
        extraStacks.forEach((a, v) -> stacksTag.putInt(a.name(), v));
        tag.put("extraStacks", stacksTag);

        // replacements
        CompoundTag replTag = new CompoundTag();
        replacements.forEach((orig, repl) -> replTag.putString(orig.name(), repl.name()));
        tag.put("replacements", replTag);

        // alternates
        CompoundTag altTag = new CompoundTag();
        alternates.forEach((element, list) -> {
            ListTag arr = new ListTag();
            list.forEach(a -> arr.add(StringTag.valueOf(a.name())));
            altTag.put(element.name(), arr);
        });
        tag.put("alternates", altTag);

        return tag;
    }

    // --- Loading ---

    @Override
    public void deserializeNBT(CompoundTag tag) {

        extraStacks.clear();
        replacements.clear();
        alternates.clear();

        // extra stacks
        CompoundTag stacksTag = tag.getCompound("extraStacks");
        for (String key : stacksTag.getAllKeys()) {
            extraStacks.put(Ailment.valueOf(key), stacksTag.getInt(key));
        }

        // replacements
        CompoundTag replTag = tag.getCompound("replacements");
        for (String key : replTag.getAllKeys()) {
            replacements.put(
                    Ailment.valueOf(key),
                    Ailment.valueOf(replTag.getString(key))
            );
        }

        // alternates
        CompoundTag altTag = tag.getCompound("alternates");
        for (String key : altTag.getAllKeys()) {
            Element element = Element.valueOf(key);
            ListTag arr = altTag.getList(key, Tag.TAG_STRING);

            List<Ailment> list = arr.stream()
                    .map(t -> Ailment.valueOf(t.getAsString()))
                    .toList();

            alternates.put(element, list);
        }
    }
}
