package net.tiffit.tconplanner.api;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.tools.definition.module.material.ToolPartsHook;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayout;
import slimeknights.tconstruct.library.tools.layout.StationSlotLayoutLoader;
import slimeknights.tconstruct.library.tools.part.IToolPart;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A plannable Tinkers' item. Enumerated from the {@code modifiable/multipart} tag (every tool/weapon/armor/shield
 * that is built from swappable parts) rather than from the Tinker Station layouts, so newer items (plate/slime armor,
 * shields, new weapons) are all included even when they have no station layout of their own.
 */
public class TCTool {
    private static List<TCTool> ALL_TOOLS = null;

    /** Station layout for this item, or null for items (armor/shields) that have none. */
    @Nullable
    private final StationSlotLayout layout;
    private final ItemStack renderTool;
    private final int partCount;

    private TCTool(Item item, IModifiable modifiable, @Nullable StationSlotLayout layout, int partCount){
        // build a display tool with render materials so the grid icon isn't a material-less magenta placeholder
        this.renderTool = ToolBuildHandler.buildToolForRendering(item, modifiable.getToolDefinition());
        this.layout = layout;
        this.partCount = partCount;
    }

    public Component getName(){
        return layout != null ? layout.getDisplayName() : renderTool.getHoverName();
    }

    public Component getDescription(){
        return layout != null ? layout.getDescription() : renderTool.getHoverName();
    }

    public ItemStack getRenderTool(){
        return renderTool;
    }

    public IModifiable getModifiable(){
        return (IModifiable) renderTool.getItem();
    }

    public Item getItem(){
        return renderTool.getItem();
    }

    public List<TCSlotPos> getSlotPos(){
        if(layout != null && layout.getInputSlots().size() >= partCount){
            return layout.getInputSlots().stream().map(TCSlotPos::new).collect(Collectors.toList());
        }
        // no station layout (armor/shields/new tools): lay the part slots out in a simple vertical column
        List<TCSlotPos> synth = new ArrayList<>(partCount);
        for(int i = 0; i < partCount; i++){
            synth.add(new TCSlotPos(33, 22 + i * 18));
        }
        return synth;
    }

    @Nullable
    public StationSlotLayout getLayout(){
        return layout;
    }

    public static List<TCTool> getTools(){
        if(ALL_TOOLS == null){
            // index the station layouts by item so we can reuse their nice slot positions where they exist
            Map<Item, StationSlotLayout> byItem = new HashMap<>();
            for(StationSlotLayout l : StationSlotLayoutLoader.getInstance().getSortedSlots()){
                ItemStack icon = l.getIcon().getValue(ItemStack.class);
                if(icon != null && !icon.isEmpty()){
                    byItem.putIfAbsent(icon.getItem(), l);
                }
            }
            ALL_TOOLS = new ArrayList<>();
            for(Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(TinkerTags.Items.MULTIPART_TOOL)){
                Item item = holder.value();
                if(!(item instanceof IModifiable modifiable)) continue;
                List<IToolPart> parts = ToolPartsHook.parts(modifiable.getToolDefinition());
                if(parts.isEmpty()) continue; // skip ancient/cast items with no swappable parts
                ALL_TOOLS.add(new TCTool(item, modifiable, byItem.get(item), parts.size()));
            }
        }
        return ALL_TOOLS;
    }

}
