package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.tiffit.tconplanner.screen.PlannerScreen;

public class TextButton extends Button {

    private final PlannerScreen parent;
    private final Runnable onPress;
    private int color = 0xff_ff_ff;
    private Component tooltip = null;

    public TextButton(int x, int y, Component text, Runnable onPress, PlannerScreen parent) {
        super(x, y, 58, 18, text, e -> {}, Button.DEFAULT_NARRATION);
        this.parent = parent;
        this.onPress = onPress;
    }

    public TextButton withColor(int color){
        this.color = color;
        return this;
    }

    public TextButton withTooltip(Component tooltip){
        this.tooltip = tooltip;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.setColor(((color & 0xff0000) >> 16)/255f, ((color & 0x00ff00) >> 8)/255f, (color & 0x0000ff)/255f,1f);
        gui.blit(PlannerScreen.TEXTURE, getX(), getY(), 176, 183, width, height);
        gui.setColor(1f, 1f, 1f, 1f);
        gui.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width/2, getY() + 5, isHovered ? 0xffffffff : 0xa0ffffff);
        if(isHovered){
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        if(tooltip != null) {
            parent.postRenderTasks.add(() -> gui.renderTooltip(parent.getFont(), tooltip, mouseX, mouseY));
        }
    }

    @Override
    public void onPress() {
        onPress.run();
    }
}
