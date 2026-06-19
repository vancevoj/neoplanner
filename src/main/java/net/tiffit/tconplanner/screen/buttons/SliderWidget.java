package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.tiffit.tconplanner.screen.PlannerScreen;

import java.util.function.Consumer;

public class SliderWidget extends AbstractWidget {

    private final PlannerScreen parent;
    private final Consumer<Integer> listener;
    private final int min, max;
    private double percent;
    private int value;

    public SliderWidget(int x, int y, int width, int height, Consumer<Integer> listener, int min, int max, int value, PlannerScreen parent) {
        super(x, y, width, height, Component.literal(""));
        this.parent = parent;
        this.listener = listener;
        this.min = min;
        this.max = max;
        this.value = value;
        percent = (value - min)/(double)(max-min);
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        int center = getY() + height/2;
        for(int dx = getX() - 2; dx < getX() + width + 2; dx++){
            gui.blit(PlannerScreen.TEXTURE, dx, center - 2, 176, 78, 1, 4);
        }
        int sliderX = getX() + (int)(width*percent);
        gui.blit(PlannerScreen.TEXTURE, sliderX - 2, getY(), 178, 78, 4, 20);
        Font font = Minecraft.getInstance().font;
        int minValSize = font.width(min + "");
        gui.drawString(font, min + "", getX() - minValSize - 5, getY() + 6, 0xff_ff_ff_ff);
        gui.drawString(font, max + "", getX() + width + 5, getY() + 6, 0xff_ff_ff_ff);
        gui.drawCenteredString(font, value + "", sliderX, getY() + 22, 0xff_ff_ff_ff);
    }

    @Override
    public void onClick(double mx, double my) {
        updateVal(mx);
    }

    @Override
    protected void onDrag(double mx, double my, double dx, double dy) {
        if(mx >= getX() - 5 && my >= getY() && mx <= getX() + width + 5 && my <= getY() + width) {
            updateVal(mx);
        }
    }

    private void updateVal(double mouseX){
        percent = Mth.clamp((mouseX - getX())/width, 0, 1);
        int oldVal = value;
        value = (int)Mth.clamp((max-min)*percent + min, min, max);
        if(value != oldVal)listener.accept(value);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_169152_) {

    }
}
