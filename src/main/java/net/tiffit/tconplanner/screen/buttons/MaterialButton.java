package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;

import java.util.ArrayList;
import java.util.List;

public class MaterialButton extends Button {

    public final IMaterial material;
    public final ItemStack stack;
    private final PlannerScreen parent;
    public boolean selected = false;
    public Component errorText;

    public MaterialButton(int index, IMaterial material, ItemStack stack, int x, int y, PlannerScreen parent){
        super(x, y, 16, 16, stack.getHoverName(), button -> parent.setPart(material), Button.DEFAULT_NARRATION);
        this.material = material;
        this.stack = stack;
        this.parent = parent;
        if(parent.blueprint.isComplete()){
            Blueprint cloned = parent.blueprint.clone();
            cloned.materials[parent.selectedPart] = material;
            Component err = ToolStack.from(cloned.createOutput()).tryValidate();
            if(err == null){
                RecipeResult<?> result = cloned.validate();
                if(result.hasError())err = result.getMessage();
            }
            if(err != null)errorText = err.copy().withStyle(ChatFormatting.DARK_RED);
        }
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.renderItem(this.stack, getX(), getY());
        int right = getX() + width;
        int bottom = getY() + height;
        if(selected) gui.fill(getX(), getY(), right, bottom, 0x55_00_ff_00);
        if(errorText != null){
            gui.fill(getX(), getY(), right, bottom, 0x55_ff_00_00);
        }
        if(isHovered){
            gui.fill(getX(), getY(), right, bottom, 0x80_ffea00);
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        if(errorText == null) {
            parent.postRenderTasks.add(() -> {
                List<Component> tooltip = new ArrayList<>();
                if(Screen.hasControlDown() && stack.getItem() instanceof ToolPartItem){
                    ToolPartItem part = (ToolPartItem)stack.getItem();
                    tooltip.add(part.getName(stack));
                    List<ModifierEntry> entries = MaterialRegistry.getInstance().getTraits(material.getIdentifier(), part.getStatType());
                    for (ModifierEntry entry : entries) {
                        Modifier modifier = entry.getModifier();
                        tooltip.add(Component.empty().append(modifier.getDisplayName(entry.getLevel())).withStyle(ChatFormatting.UNDERLINE));
                        TextColor c = TextColor.fromRgb(modifier.getColor());
                        for (Component comp : modifier.getDescriptionList(entry.getLevel())) {
                            tooltip.add(Component.empty().append(comp).withStyle(Style.EMPTY.withColor(c)));
                        }
                    }
                }else{
                    tooltip.addAll(stack.getTooltipLines(Item.TooltipContext.of(parent.getMinecraft().level), parent.getMinecraft().player, TooltipFlag.Default.NORMAL));
                    if(!Screen.hasControlDown()){
                        tooltip.add(TranslationUtil.createComponent("parts.modifier_descriptions", TConstruct.makeTranslation("key", "ctrl").withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC)));
                    }
                }
                gui.renderComponentTooltip(parent.getFont(), tooltip, mouseX, mouseY);
            });
        }else {
            parent.postRenderTasks.add(() -> gui.renderTooltip(parent.getFont(), errorText, mouseX, mouseY));
        }
    }

    @Override
    public void onPress() {
        if(errorText == null){
            parent.setPart(material);
        }
    }

    @Override
    public void playDownSound(SoundManager sound) {
        if(errorText != null){
            sound.play(SimpleSoundInstance.forUI(SoundEvents.ANVIL_HIT, 1.0F));
        } else {
            super.playDownSound(sound);
        }
    }
}
