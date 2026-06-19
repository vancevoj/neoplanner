package net.tiffit.tconplanner.screen.ext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.tiffit.tconplanner.EventListener;
import net.tiffit.tconplanner.screen.PlannerScreen;
import net.tiffit.tconplanner.util.Icon;

import java.awt.*;
import java.util.function.Supplier;

public class ExtIconButton extends Button {

    private static final Supplier<Boolean> ALWAYS_TRUE = () -> true;

    private final Icon icon;
    private final Screen screen;
    private final Component tooltip;
    private Holder<SoundEvent> pressSound = SoundEvents.UI_BUTTON_CLICK;
    private Color color = Color.WHITE;

    private Supplier<Boolean> enabledFunc = ALWAYS_TRUE;

    public ExtIconButton(int x, int y, Icon icon, Component tooltip, Button.OnPress action, Screen screen) {
        super(x, y, 12, 12, Component.literal(""), action, Button.DEFAULT_NARRATION);
        this.icon = icon;
        this.screen = screen;
        this.tooltip = tooltip;
    }

    public ExtIconButton withSound(Holder<SoundEvent> sound){
        this.pressSound = sound;
        return this;
    }

    public ExtIconButton withColor(Color color){
        this.color = color;
        return this;
    }

    public ExtIconButton withEnabledFunc(Supplier<Boolean> func){
        this.enabledFunc = func;
        return this;
    }

    @Override
    public boolean mouseClicked(double p_231044_1_, double p_231044_3_, int p_231044_5_) {
        if(!enabledFunc.get())return false;
        return super.mouseClicked(p_231044_1_, p_231044_3_, p_231044_5_);
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        if(!enabledFunc.get())return;
        gui.setColor(color.getRed()/255f, color.getGreen()/255f, color.getBlue()/255f, isHovered ? 1 : 0.8F);
        icon.render(screen, gui, getX(), getY());
        gui.setColor(1f, 1f, 1f, 1f);
        if (this.isHoveredOrFocused()) {
            EventListener.postRenderQueue.offer(() -> gui.renderTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY));
        }
    }

    @Override
    public void playDownSound(SoundManager handler) {
        if(pressSound != null)handler.play(SimpleSoundInstance.forUI(pressSound, 1.0F));
    }
}
