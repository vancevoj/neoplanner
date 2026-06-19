package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.Icon;

import java.awt.*;

public class IconButton extends Button {

    private final Icon icon;
    private final PlannerScreen parent;
    private SoundEvent pressSound = SoundEvents.UI_BUTTON_CLICK.value();
    private Color color = Color.WHITE;

    public IconButton(int x, int y, Icon icon, Component tooltip, PlannerScreen parent, Button.OnPress action) {
        super(x, y, 12, 12, tooltip, action, Button.DEFAULT_NARRATION);
        this.icon = icon;
        this.parent = parent;
    }

    public IconButton withSound(SoundEvent sound){
        this.pressSound = sound;
        return this;
    }

    public IconButton withColor(Color color){
        this.color = color;
        return this;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        gui.setColor(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, isHovered ? 1 : 0.8F);
        icon.render(parent, gui, getX(), getY());
        gui.setColor(1f, 1f, 1f, 1f);
        if(isHovered){
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> gui.renderTooltip(parent.getFont(), this.getMessage(), mouseX, mouseY));
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if(pressSound != null)handler.play(SimpleSoundInstance.forUI(pressSound, 1.0F));
    }
}
