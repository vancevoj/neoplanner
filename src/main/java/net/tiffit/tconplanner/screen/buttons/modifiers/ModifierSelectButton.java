package net.tiffit.tconplanner.screen.buttons.modifiers;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.*;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.screen.ModifierPanel;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.DummyTinkersStationInventory;
import net.tiffit.tconplanner.util.ModifierStateEnum;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.client.modifiers.ModifierIconManager;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.impl.DurabilityShieldModifier;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ModifierSelectButton extends Button {

    private static final Style ERROR_STYLE = Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED));

    private final IDisplayModifierRecipe recipe;
    private final Modifier modifier;
    private final boolean selected;
    private final Component error;
    private final Component displayName;
    public final ModifierStateEnum state;
    private final PlannerScreen parent;
    private final List<ItemStack> recipeStacks = new ArrayList<>();

    private final MutableComponent levelText;

    public ModifierSelectButton(IDisplayModifierRecipe recipe, ModifierStateEnum state, @Nullable Component error, int level, ToolStack tool, PlannerScreen parent) {
        super(0, 0, 100, 18, Component.literal(""), e -> {}, Button.DEFAULT_NARRATION);
        this.recipe = recipe;
        this.modifier = recipe.getDisplayResult().getModifier();
        this.parent = parent;
        this.selected = false;
        this.state = state;
        this.error = error;
        for (int i = 0; i < recipe.getInputCount(); i++) {
            recipeStacks.addAll(recipe.getDisplayItems(i));
        }
        displayName = level == 0 ? modifier.getDisplayName() : modifier.getDisplayName(level);
        boolean singleUse = modifier instanceof NoLevelsModifier || modifier instanceof DurabilityShieldModifier;
        int maxLevel = singleUse ? 1 : recipe.getLevel().max();
        int currentLevel = singleUse ? tool.getModifiers().getLevel(modifier.getId()) : parent.blueprint.modStack.getLevel(modifier);
        if(currentLevel > maxLevel && maxLevel > 0)currentLevel = maxLevel;
        levelText = Component.literal(currentLevel + "/" +(maxLevel > 0 ? maxLevel : "∞"));
        if(error != null)levelText.withStyle(ChatFormatting.DARK_RED);
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        switch (state){
            case APPLIED:
                gui.setColor(0.5f, 1f, 0.5f, 1f); break;
            case UNAVAILABLE:
                gui.setColor(1f, 0.5f, 0.5f, 1f); break;
            default:
                gui.setColor(1f, 1f, 1f, 1f);
        }
        gui.blit(PlannerScreen.TEXTURE, getX(), getY(), 0, 224, 100, 18);
        gui.setColor(1f, 1f, 1f, 1f);
        if(isHoveredOrFocused()){
            gui.renderItem(recipeStacks.get((int)((System.currentTimeMillis() / 1000) % recipeStacks.size())), getX() + 1, getY() + 1);
        }else{
            ModifierIconManager.renderIcon(gui, modifier, getX()+1, getY()+1, 0, 16);
        }
        Font font = Minecraft.getInstance().font;

        gui.pose().pushPose();
        gui.pose().translate(getX() + 20, getY() + 2, 0);
        float nameWidth = font.width(displayName);
        int maxWidth = width - 22;
        if(nameWidth > maxWidth){
            float scale = maxWidth/nameWidth;
            gui.pose().scale(scale, scale, 1);
        }
        gui.drawString(font, displayName, 0, 0, 0xff_ff_ff_ff);
        gui.pose().popPose();

        gui.pose().pushPose();
        gui.pose().translate(getX() + 20, getY() + 11, 0);
        gui.pose().scale(0.5f, 0.5f, 1);
        if(recipe.getSlots() != null) {
            SlotType.SlotCount count = recipe.getSlots();
            MutableComponent text = count.count() == 1 ? TranslationUtil.createComponent("modifiers.usedslot", count.type().getDisplayName()) :
                    TranslationUtil.createComponent("modifiers.usedslots", count.count(), count.type().getDisplayName());
            gui.drawString(font, text, 0, 0, 0xff_ff_ff_ff);
        }
        gui.pose().popPose();

        gui.pose().pushPose();
        gui.pose().translate(getX() + width - 1, getY() + 11, 0);
        gui.pose().scale(0.5f, 0.5f, 1);
        gui.drawString(font, levelText, -font.width(levelText), 0, 0xff_ff_ff_ff);
        gui.pose().popPose();
        if(isHovered){
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> {
            List<Component> tooltips = new ArrayList<>(modifier.getDescriptionList());
            if(error != null)tooltips.add(error.copy().withStyle(ERROR_STYLE));
            gui.renderComponentTooltip(parent.getFont(), tooltips, mouseX, mouseY);
        });
    }

    @Override
    public void onPress() {
        switch (state){
            case AVAILABLE: {
                ModifierInfo info = new ModifierInfo(recipe);
                parent.selectedModifier = info;
                parent.refresh();
                break;
            }
            case APPLIED: {
                parent.selectedModifier = new ModifierInfo(recipe);
                parent.refresh();
                break;
            }
        }
    }

    @Override
    public void playDownSound(SoundManager sound) {
        if(state == ModifierStateEnum.UNAVAILABLE){
            sound.play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_HIT, 1.0F));
        } else {
            super.playDownSound(sound);
        }
    }

    public static ModifierSelectButton create(IDisplayModifierRecipe recipe, ToolStack tstack, ItemStack stack, PlannerScreen screen){
        ITinkerStationRecipe tsrecipe = (ITinkerStationRecipe) recipe;
        ModifierStateEnum mstate = ModifierStateEnum.UNAVAILABLE;
        Component error = null;
        Modifier modifier = recipe.getDisplayResult().getModifier();
        int currentLevel = tstack.getModifiers().getLevel(modifier.getId());
        if (currentLevel != 0)
            mstate = ModifierStateEnum.APPLIED;
        RecipeResult<?> validatedResult = tsrecipe.getValidatedResult(new DummyTinkersStationInventory(stack), Minecraft.getInstance().level.registryAccess());
        if(validatedResult.hasError())error = validatedResult.getMessage();
        else {
            if(currentLevel >= 1 && (modifier instanceof NoLevelsModifier || modifier instanceof DurabilityShieldModifier)){
                error = RecipeResult.failure(ModifierPanel.KEY_MAX_LEVEL, modifier.getDisplayName(), 1).getMessage();
            }else{
                if(mstate != ModifierStateEnum.APPLIED)mstate = ModifierStateEnum.AVAILABLE;
            }
        }
        if(!validatedResult.hasError()){
            if(mstate != ModifierStateEnum.APPLIED)mstate = ModifierStateEnum.AVAILABLE;
        }
        return new ModifierSelectButton(recipe, mstate, error, currentLevel, tstack, screen);
    }
}
