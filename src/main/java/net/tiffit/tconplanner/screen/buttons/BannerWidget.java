package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.tiffit.tconplanner.screen.PlannerScreen;

public class BannerWidget extends AbstractWidget {

    private final PlannerScreen parent;

    public BannerWidget(int x, int y, Component text, PlannerScreen parent) {
        super(x, y, 90, 19, text);
        this.parent = parent;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.blit(PlannerScreen.TEXTURE, getX(), getY(), 0, 205, width, height);
        gui.drawCenteredString(Minecraft.getInstance().font, getMessage(), getX() + width/2, getY() + 5, 0xff_90_90_ff);
    }

    @Override
    public void playDownSound(SoundManager SoundManager) {}

    @Override
    protected void updateWidgetNarration(NarrationElementOutput p_169152_) {

    }
}
