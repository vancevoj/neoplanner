package net.tiffit.tconplanner.screen.buttons.modifiers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.tiffit.tconplanner.screen.PlannerScreen;

public class ModPreviewWidget extends AbstractWidget {
    private final ItemStack stack;
    private final boolean disabled;
    private final PlannerScreen parent;

    public ModPreviewWidget(int x, int y, ItemStack stack, PlannerScreen parent){
        super(x, y, 16, 16, Component.literal(""));
        this.parent = parent;
        this.disabled = stack.isEmpty();
        this.stack = disabled ? new ItemStack(Items.BARRIER) : stack;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.renderItem(this.stack, getX(), getY());
        if(isHovered && !disabled){
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderItemTooltip(gui, this.stack, mouseX, mouseY));
    }

    @Override
    public void playDownSound(SoundManager sound) {}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_169152_) {

    }
}
