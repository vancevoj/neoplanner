package net.tiffit.tconplanner.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class PlannerPanel extends AbstractWidget {

    protected final List<AbstractWidget> children = new ArrayList<>();
    protected final PlannerScreen parent;

    public PlannerPanel(int x, int y, int width, int height, PlannerScreen parent) {
        super(x, y, width, height, Component.literal(""));
        this.parent = parent;
    }

    public void addChild(AbstractWidget widget){
        widget.setX(widget.getX() + getX());
        widget.setY(widget.getY() + getY());
        children.add(widget);
    }

    @Override
    protected void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        this.isHovered = mouseX >= getX() && mouseY >= getY() && mouseX < getX() + this.width && mouseY < getY() + this.height;
        for (Renderable child : children) {
            child.render(gui, mouseX, mouseY, partialTick);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean result = isHoveredOrFocused();
        for (AbstractWidget child : children) {
            if(child.mouseClicked(mouseX, mouseY, button))result = true;
        }
        return result;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int p_231048_5_) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if(child.mouseReleased(mouseX, mouseY, p_231048_5_))result = true;
        }
        return result;
    }

    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if(child.mouseDragged(mx, my, button, dx, dy))result = true;
        }
        return result;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if(child.isMouseOver(mouseX, mouseY)) {
                if (child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) result = true;
            }
        }
        return result;
    }

    public boolean keyPressed(int p_231046_1_, int p_231046_2_, int p_231046_3_) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if(child.keyPressed(p_231046_1_, p_231046_2_, p_231046_3_))result = true;
        }
        return result;
    }

    public boolean keyReleased(int p_223281_1_, int p_223281_2_, int p_223281_3_) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if(child.keyReleased(p_223281_1_, p_223281_2_, p_223281_3_))result = true;
        }
        return result;
    }

    public boolean charTyped(char p_231042_1_, int p_231042_2_) {
        boolean result = false;
        for (AbstractWidget child : children) {
            if(child.charTyped(p_231042_1_, p_231042_2_))result = true;
        }
        return result;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_169152_) {

    }
}
