package net.tiffit.tconplanner.data;

import net.minecraft.resources.ResourceLocation;
import net.tiffit.tconplanner.screen.PlannerScreen;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.SlotType;

import java.util.Objects;

public class ModifierInfo {

    public final IDisplayModifierRecipe recipe;
    public final Modifier modifier;
    public final SlotType.SlotCount count;
    /** Recipe id captured at construction (1.21 recipes have no getId(); see PlannerScreen recipe-id map). */
    public final ResourceLocation id;
    /** Amount needed per level for incremental modifiers, or 0 if not incremental. */
    public final int needed;

    public ModifierInfo(IDisplayModifierRecipe recipe){
        this.recipe = recipe;
        this.modifier = recipe.getDisplayResult().getModifier();
        this.count = recipe.getSlots();
        this.id = PlannerScreen.getRecipeId(recipe);
        this.needed = recipe.isIncremental() ? recipe.getDisplayResult().getNeeded() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ModifierInfo info = (ModifierInfo) o;
        return Objects.equals(id, info.id) && modifier.getId().equals(info.modifier.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, modifier.getId());
    }
}
