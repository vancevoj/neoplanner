package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.tiffit.tconplanner.api.TCTool;
import net.tiffit.tconplanner.screen.PlannerScreen;

public class ToolTypeButton extends Button {

    private final TCTool tool;
    private final boolean selected;
    public final int index;
    private final PlannerScreen parent;

    public ToolTypeButton(int index, TCTool tool, PlannerScreen parent) {
        super(0, 0, 18, 18, tool.getDescription(), button -> parent.setSelectedTool(index), Button.DEFAULT_NARRATION);
        this.tool = tool;
        this.index = index;
        this.parent = parent;
        this.selected = parent.blueprint != null && tool == parent.blueprint.tool;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.blit(PlannerScreen.TEXTURE, getX(), getY(), 213, 41 + (selected ? 18 : 0), 18, 18);
        gui.renderItem(tool.getRenderTool(), getX() + 1, getY() + 1);
        if(isHovered){
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderItemTooltip(gui, tool.getRenderTool(), mouseX, mouseY));
    }
}
