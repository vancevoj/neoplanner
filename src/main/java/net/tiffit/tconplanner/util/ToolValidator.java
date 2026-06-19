package net.tiffit.tconplanner.util;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public final class ToolValidator {

    /**
     * Validate if a modifier is able to be removed from a tool
     * @param bp      The blueprint being edited
     * @param tool    The tool to try to remove a modifier from
     * @param modInfo The modifier to remove
     */
    public static RecipeResult<ItemStack> validateModRemoval(Blueprint bp, ToolStack tool, ModifierInfo modInfo){
        ToolStack toolClone = tool.copy();
        int toolBaseLevel = ToolStack.from(bp.createOutput(false)).getModifiers().getLevel(modInfo.modifier.getId());
        int minLevel = Math.max(0, toolBaseLevel);
        if(bp.modStack.getLevel(modInfo.modifier) + toolBaseLevel <= minLevel || !bp.modStack.isRecipeUsed((ITinkerStationRecipe) modInfo.recipe))
            return RecipeResult.failure("gui.tconplanner.modifiers.error.minlevel");
        toolClone.removeModifier(modInfo.modifier.getId(), 1);
        if(modInfo.needed > 0){
            toolClone.addModifierAmount(modInfo.modifier.getId(), modInfo.needed, modInfo.needed);
        }
        Component subtractError = toolClone.tryValidate();
        if(subtractError != null)return RecipeResult.failure(subtractError);
        Blueprint bpClone = bp.clone();
        bpClone.modStack.pop(modInfo);
        RecipeResult<?> bpResult = bpClone.validate();
        if(bpResult.hasError())return RecipeResult.failure(bpResult.getMessage());
        return RecipeResult.success(toolClone.createStack());
    }


}
