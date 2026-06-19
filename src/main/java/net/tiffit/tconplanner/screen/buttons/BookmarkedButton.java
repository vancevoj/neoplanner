package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.Icon;

public class BookmarkedButton extends Button {

    public static final Icon STAR_ICON = new Icon(6, 0);

    private final PlannerScreen parent;
    private final ItemStack stack;
    private final int index;
    private final Blueprint blueprint;
    private final boolean starred;
    private boolean selected;

    public BookmarkedButton(int index, Blueprint blueprint, boolean starred, PlannerScreen parent){
        super(0, 0, 18, 18, Component.literal(""), button -> parent.setBlueprint(blueprint.clone()), Button.DEFAULT_NARRATION);
        this.index = index;
        this.blueprint = blueprint;
        this.starred = starred;
        this.parent = parent;
        stack = blueprint.createOutput();
        this.selected = parent.blueprint != null && parent.blueprint.equals(blueprint);
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.blit(PlannerScreen.TEXTURE, getX(), getY(), 213, 41 + (selected ? 18 : 0), 18, 18);
        gui.renderItem(this.stack, getX() + 1, getY() + 1);
        if(starred){
            gui.pose().pushPose();
            gui.pose().translate(getX() + 11, getY() + 11, 105);
            gui.pose().scale(0.5f, 0.5f, 0.5f);
            STAR_ICON.render(parent, gui, 0, 0);
            gui.pose().popPose();
        }
        if(isHovered){
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderItemTooltip(gui, this.stack, mouseX, mouseY));
    }
}
