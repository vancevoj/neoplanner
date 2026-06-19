package net.tiffit.tconplanner.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.screen.PlannerScreen;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModifierStack {
    private final LinkedList<ModifierInfo> stack = new LinkedList<>();
    private final HashMap<ModifierId, Integer> incrementalDiffMap = new HashMap<>();

    public void push(ModifierInfo info){
        stack.add(info);
    }

    public void pop(ModifierInfo info){
        stack.removeLastOccurrence(info);
    }

    public void moveDown(int index){
        ModifierInfo info = stack.remove(index);
        stack.add(index + 1, info);
    }

    /** Amount needed per level for the given modifier, derived from the matching stacked incremental recipe. */
    public int getNeeded(Modifier modifier){
        for (ModifierInfo info : stack) {
            if(info.modifier.equals(modifier))return info.needed;
        }
        return 0;
    }

    public void setIncrementalDiff(Modifier modifier, int amount){
        incrementalDiffMap.put(modifier.getId(), Mth.clamp(amount,0, getNeeded(modifier)));
    }

    public int getIncrementalDiff(Modifier modifier){
        return incrementalDiffMap.getOrDefault(modifier.getId(), 0);
    }

    public boolean isRecipeUsed(ITinkerStationRecipe recipe){
        ResourceLocation id = PlannerScreen.getRecipeId((IDisplayModifierRecipe) recipe);
        return stack.stream().anyMatch(info -> java.util.Objects.equals(info.id, id));
    }

    public int getLevel(Modifier modifier){
        return (int) stack.stream().filter(info1 -> info1.modifier.equals(modifier)).count();
    }

    public void applyIncrementals(ToolStack tool){
        stack.stream().distinct().forEach(info -> {
            Modifier mod = info.modifier;
            int amount = info.needed;
            if(amount > 0){
                tool.addModifierAmount(mod.getId(), amount - getIncrementalDiff(mod), amount);
            }
        });
    }

    public List<ModifierInfo> getStack(){
        return ImmutableList.copyOf(stack);
    }

    public CompoundTag toNBT(){
        CompoundTag tag = new CompoundTag();
        ListTag modList = new ListTag();
        for (ModifierInfo info : stack) {
            if(info.id != null)modList.add(StringTag.valueOf(info.id.toString()));
        }
        tag.put("mods", modList);
        ListTag diffList = new ListTag();
        for (Map.Entry<ModifierId, Integer> entry : incrementalDiffMap.entrySet()) {
            CompoundTag diffNBT = new CompoundTag();
            diffNBT.putString("mod", entry.getKey().toString());
            diffNBT.putInt("amount", entry.getValue());
            diffList.add(diffNBT);
        }
        tag.put("diff", diffList);
        return tag;
    }

    public void fromNBT(CompoundTag tag){
        stack.clear();
        incrementalDiffMap.clear();
        ListTag modList = tag.getList("mods", 8);
        Map<ResourceLocation, IDisplayModifierRecipe> recipesMap = PlannerScreen.getModifierRecipes().stream()
                .filter(recipe -> PlannerScreen.getRecipeId(recipe) != null)
                .collect(Collectors.toMap(PlannerScreen::getRecipeId, recipe -> recipe, (a, b) -> a));
        for(int i = 0; i < modList.size(); i++){
            ResourceLocation resourceLocation = ResourceLocation.parse(modList.getString(i));
            if(recipesMap.containsKey(resourceLocation)) {
                push(new ModifierInfo(recipesMap.get(resourceLocation)));
            }
        }
        ListTag diffList = tag.getList("diff", 10);
        for(int i = 0; i < diffList.size(); i++){
            CompoundTag diffNBT = diffList.getCompound(i);
            ModifierId modId = new ModifierId(diffNBT.getString("mod"));
            int amount = diffNBT.getInt("amount");
            incrementalDiffMap.put(modId, amount);
        }
    }
}
