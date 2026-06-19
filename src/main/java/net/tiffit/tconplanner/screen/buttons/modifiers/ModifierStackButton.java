package net.tiffit.tconplanner.screen.buttons.modifiers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.TranslationUtil;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.tools.SlotType;

import java.util.ArrayList;
import java.util.List;

public class ModifierStackButton extends Button {

    private final Modifier modifier;
    private final IDisplayModifierRecipe recipe;
    private final ModifierInfo modifierInfo;
    private final PlannerScreen parent;
    private final Component displayName;
    private final ItemStack display;
    private final int index;

    public ModifierStackButton(ModifierInfo modifierInfo, int index, int level, ItemStack display, PlannerScreen parent) {
        super(0, 0, 100, 18, Component.literal(""), e -> {
        }, Button.DEFAULT_NARRATION);
        this.modifierInfo = modifierInfo;
        this.parent = parent;
        this.modifier = modifierInfo.modifier;
        this.recipe = modifierInfo.recipe;
        this.display = display;
        this.index = index;
        displayName = modifier.getDisplayName(level);
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if(parent.selectedModifierStackIndex == index){
            gui.setColor(255/255f, 200/255f, 0f, 1f);
        }
        gui.blit(PlannerScreen.TEXTURE, getX(), getY(), 0, 224, 100, 18);
        gui.setColor(1f, 1f, 1f, 1f);
        gui.renderItem(display, getX() + 1, getY() + 1);
        Font font = Minecraft.getInstance().font;
        gui.pose().pushPose();
        gui.pose().translate(getX() + 20, getY() + 2, 0);
        float nameWidth = font.width(displayName);
        int maxWidth = width - 22;
        if (nameWidth > maxWidth) {
            float scale = maxWidth / nameWidth;
            gui.pose().scale(scale, scale, 1);
        }
        gui.drawString(font, displayName, 0, 0, 0xff_ff_ff_ff);
        gui.pose().popPose();

        gui.pose().pushPose();
        gui.pose().translate(getX() + 20, getY() + 11, 0);
        gui.pose().scale(0.5f, 0.5f, 1);
        if (recipe.getSlots() != null) {
            SlotType.SlotCount count = recipe.getSlots();
            MutableComponent text = count.count() == 1 ? TranslationUtil.createComponent("modifiers.usedslot", count.type().getDisplayName()) :
                    TranslationUtil.createComponent("modifiers.usedslots", count.count(), count.type().getDisplayName());
            gui.drawString(font, text, 0, 0, 0xff_ff_ff_ff);
        }
        gui.pose().popPose();
        if (isHovered) {
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> {
            List<Component> tooltips = new ArrayList<>(modifier.getDescriptionList());
            gui.renderComponentTooltip(parent.getFont(), tooltips, mouseX, mouseY);
        });
    }

    @Override
    public void onPress() {
        parent.selectedModifierStackIndex = index;
        parent.refresh();
    }
}
