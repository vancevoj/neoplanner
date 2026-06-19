package net.tiffit.tconplanner.screen.ext;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.tiffit.tconplanner.EventListener;
import net.tiffit.tconplanner.screen.buttons.BookmarkedButton;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExtItemStackButton extends Button {
    public static ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath("tconstruct", "textures/gui/tinker_station.png");

    private final ItemStack stack;
    private final Screen screen;
    private final List<Component> tooltips;

    public ExtItemStackButton(int x, int y, ItemStack stack, List<Component> tooltips, Button.OnPress action, Screen screen) {
        super(x, y, 16, 16, Component.literal(""), action, Button.DEFAULT_NARRATION);
        this.stack = stack;
        this.screen = screen;
        this.tooltips = tooltips == null ? Collections.emptyList() : tooltips;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = screen.getMinecraft();
        gui.blit(BACKGROUND, getX() - 1, getY() - 1, 194, 0, 18, 18);
        if(!isHoveredOrFocused()){
            gui.fill(getX(), getY(), getX() + 16, getY() + 16, 0xff_a29b81);
        }
        gui.renderItem(stack, getX(), getY());
        gui.pose().pushPose();
        gui.setColor(1f, 1f, 1f, 0.6f);
        BookmarkedButton.STAR_ICON.render(screen, gui, getX() + 2, getY() + 2);
        gui.setColor(1f, 1f, 1f, 1f);
        gui.pose().popPose();
        if (this.isHoveredOrFocused()) {
            EventListener.postRenderQueue.offer(() -> {
                List<Component> itemTip = stack.getTooltipLines(Item.TooltipContext.of(mc.level), mc.player, TooltipFlag.Default.NORMAL);
                List<Component> result = Stream.concat(itemTip.stream(), tooltips.stream()).collect(Collectors.toList());
                gui.renderComponentTooltip(mc.font, result, mouseX, mouseY);
            });
        }
    }
}
