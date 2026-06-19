package net.tiffit.tconplanner.screen.buttons;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.tiffit.tconplanner.screen.PlannerScreen;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.tools.part.IToolPart;

public class ToolPartButton extends Button {

    private final ItemStack stack;
    private final IMaterial material;
    public final IToolPart part;
    private final PlannerScreen parent;
    public final int index;

    public ToolPartButton(int index, int x, int y, IToolPart part, IMaterial material, PlannerScreen parent){
        super(x, y, 16, 16, Component.literal(""), button -> parent.setSelectedPart(index), Button.DEFAULT_NARRATION);
        this.index = index;
        this.part = part;
        this.parent = parent;
        this.material = material;
        stack = material == null ? new ItemStack(part.asItem()) : part.withMaterialForDisplay(material.getIdentifier());
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        boolean selected = parent.selectedPart == index;
        gui.pose().pushPose();
        gui.pose().translate(0, 0, 1);
        gui.setColor(1f, 1f, 1f, 0.7f);
        gui.blit(PlannerScreen.TEXTURE, getX() - 1, getY() - 1, 176 + (material == null ? 18 : 0), 41 + (selected ? 18 : 0), 18, 18);
        gui.setColor(1f, 1f, 1f, 1f);
        gui.pose().popPose();
        gui.renderItem(this.stack, getX(), getY());
        if(isHovered){
            renderToolTip(gui, mouseX, mouseY);
        }
    }

    public void renderToolTip(GuiGraphics gui, int mouseX, int mouseY) {
        parent.postRenderTasks.add(() -> parent.renderItemTooltip(gui, this.stack, mouseX, mouseY));
    }


}
