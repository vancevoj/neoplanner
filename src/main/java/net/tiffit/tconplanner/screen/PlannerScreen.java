package net.tiffit.tconplanner.screen;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.tiffit.tconplanner.TConPlanner;
import net.tiffit.tconplanner.api.TCTool;
import net.tiffit.tconplanner.data.Blueprint;
import net.tiffit.tconplanner.data.ModifierInfo;
import net.tiffit.tconplanner.data.PlannerData;
import net.tiffit.tconplanner.util.MaterialSort;
import net.tiffit.tconplanner.util.ModifierStack;
import net.tiffit.tconplanner.util.TranslationUtil;
import org.lwjgl.glfw.GLFW;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IDisplayModifierRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.part.IToolPart;
import slimeknights.tconstruct.tables.client.inventory.TinkerStationScreen;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class PlannerScreen extends Screen {

    public static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(TConPlanner.MODID, "textures/gui/planner.png");
    /** 1.21 recipes have no getId(); we capture ids from RecipeHolders here keyed by recipe instance. */
    private static final Map<IDisplayModifierRecipe, ResourceLocation> RECIPE_IDS = new IdentityHashMap<>();
    private final HashMap<String, Object> cache = new HashMap<>();
    public Deque<Runnable> postRenderTasks = new ArrayDeque<>();
    private final TinkerStationScreen child;
    private final List<TCTool> tools = TCTool.getTools();
    private final List<IDisplayModifierRecipe> modifiers;
    private final PlannerData data;

    public Blueprint blueprint;

    public int selectedPart = 0;
    public int materialPage = 0;
    public MaterialSort<?> sorter;

    public ModifierInfo selectedModifier;

    public int selectedModifierStackIndex = -1;
    public ModifierStack modifierStack;

    public int left, top, guiWidth, guiHeight;
    private Component titleText;

    public PlannerScreen(TinkerStationScreen child) {
        super(TranslationUtil.createComponent("name"));
        this.child = child;
        data = TConPlanner.DATA;
        try {
            data.load();
        }catch (Exception ex){
            ex.printStackTrace();
        }

        modifiers = getModifierRecipes();
    }

    public PlannerScreen(TinkerStationScreen child, ToolStack stack) {
        this(child);
        ItemStack imported = stack.createStack();
        Optional<TCTool> optionalTCTool = TCTool.getTools().stream().filter(tool -> tool.getItem() == imported.getItem()).findAny();
        if(optionalTCTool.isPresent()){
            blueprint = new Blueprint(optionalTCTool.get());
            for (int i = 0; i < blueprint.materials.length; i++) {
                blueprint.materials[i] = stack.getMaterials().get(i).get();
            }
            selectedPart = -1;
        }
    }

    @Override
    protected void init() {
        guiWidth = 175;
        guiHeight = 204;
        left = width / 2 - guiWidth/2;
        top = height / 2 - guiHeight/2;
        refresh();
    }

    public Font getFont(){
        return font;
    }

    public void refresh(){
        // Reset screen
        clearWidgets();
        int toolSpace = 20;
        titleText = blueprint == null ? TranslationUtil.createComponent("notool") : blueprint.tool.getName();
        addRenderableWidget(new ToolSelectPanel(left - toolSpace * 5 - 4, top, toolSpace*5, toolSpace*3 + 23 + 4, tools, this));
        if(data.saved.size() > 0) {
            addRenderableWidget(new BookmarkSelectPanel(left - toolSpace * 5 - 4, top + 15 + 18*4, toolSpace * 5, toolSpace * 5 + 23 + 4, data, this));
        }
        //Everything in here should only be added if there is a tool selected
        if(blueprint != null){
            int topPanelSize = 115;
            ItemStack result = blueprint.createOutput();
            ToolStack resultStack = result.isEmpty() ? null : ToolStack.from(result);
            addRenderableWidget(new ToolTopPanel(left, top, guiWidth, topPanelSize, result, resultStack, data,this));
            if(selectedPart != -1){
                addRenderableWidget(new MaterialSelectPanel(left, top + topPanelSize, guiWidth, guiHeight - topPanelSize, this));
            }
            if(resultStack != null) {
                addRenderableWidget(new ModifierPanel(left + guiWidth, top, 115, guiHeight, result, resultStack, modifiers, this));
            }
        }
    }

    @Override
    public void render(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        renderBackground(gui, mouseX, mouseY, partialTick);
        gui.blit(TEXTURE, left, top, 0, 0, guiWidth, guiHeight);
        gui.drawCenteredString(font, titleText, left + guiWidth / 2, top + 7, 0xffffffff);

        super.render(gui, mouseX, mouseY, partialTick);
        Runnable task;
        while((task = postRenderTasks.poll()) != null)task.run();
    }

    public void setSelectedTool(int index) {
        setBlueprint(new Blueprint(tools.get(index)));
    }

    public void setBlueprint(Blueprint bp){
        blueprint = bp;
        this.materialPage = 0;
        sorter = null;
        selectedModifier = null;
        modifierStack = null;
        selectedModifierStackIndex = -1;
        setSelectedPart(-1);
    }

    public void setSelectedPart(int index) {
        this.selectedPart = index;
        this.materialPage = 0;
        sorter = null;
        refresh();
    }

    public void setPart(IMaterial material){
        blueprint.materials[selectedPart] = material;
        selectedModifier = null;
        selectedModifierStackIndex = -1;
        modifierStack = null;
        refresh();
    }

    @Override
    public boolean keyPressed(int key, int p_231046_2_, int p_231046_3_) {
        InputConstants.Key mouseKey = InputConstants.getKey(key, p_231046_2_);
        if (super.keyPressed(key, p_231046_2_, p_231046_3_)) {
            return true;
        } else if (minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        if(key == GLFW.GLFW_KEY_B && blueprint != null && blueprint.isComplete()){
            if(data.isBookmarked(blueprint))unbookmarkCurrent();
            else bookmarkCurrent();
            return true;
        }
        return false;
    }

    public void renderItemTooltip(GuiGraphics gui, ItemStack stack, int x, int y) {
        gui.renderTooltip(font, stack, x, y);
    }


    @Override
    public void onClose() {
        minecraft.setScreen(child);
    }

    public void bookmarkCurrent(){
        if(blueprint.isComplete()){
            data.saved.add(blueprint);
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
            net.tiffit.tconplanner.network.BookmarkClient.push();
        }
        refresh();
    }

    public void starCurrent(){
        if(blueprint.isComplete()){
            data.starred = blueprint;
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
            net.tiffit.tconplanner.network.BookmarkClient.push();
        }
        refresh();
    }

    public void unbookmarkCurrent(){
        if(blueprint.isComplete()){
            data.saved.removeIf(blueprint1 -> blueprint1.equals(blueprint));
            if(blueprint.equals(data.starred))data.starred = null;
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
            net.tiffit.tconplanner.network.BookmarkClient.push();
        }
        refresh();
    }

    public void unstarCurrent(){
        if(blueprint.isComplete()){
            data.starred = null;
            try {
                data.refresh();
            } catch (IOException e) {
                e.printStackTrace();
            }
            net.tiffit.tconplanner.network.BookmarkClient.push();
        }
        refresh();
    }

    public void randomize(){
        if(blueprint != null){
            setBlueprint(new Blueprint(blueprint.tool));
            Random r = new Random();
            List<IMaterial> materials = new ArrayList<>(MaterialRegistry.getInstance().getVisibleMaterials());
            for (int i = 0; i < blueprint.parts.length; i++) {
                IToolPart part = blueprint.parts[i];
                List<IMaterial> usable = materials.stream().filter(mat -> part.canUseMaterial(mat.getIdentifier())).collect(Collectors.toList());
                if(usable.size() > 0)blueprint.materials[i] = usable.get(r.nextInt(usable.size()));
            }
            selectedModifier = null;
            refresh();
        }
    }

    public void giveItemstack(ItemStack stack){
        ItemStack currentStack;
        Inventory inventory = minecraft.player.getInventory();
        for(int i = 0; i < inventory.items.size(); i++) {
            currentStack = inventory.items.get(i);
            if (currentStack.isEmpty()) {
                int slot = i;
                if (slot < 9) {
                    slot += 36;
                }
                minecraft.gameMode.handleCreativeModeItemAdd(stack, slot);
                return;
            }
        }
    }

    public void sort(MaterialSort<?> sort){
        if(sorter == sort)sorter = null;
        else sorter = sort;
        refresh();
    }

    public <T> T getCacheValue(String key, T defaultVal){
        return (T)cache.getOrDefault(key, defaultVal);
    }

    public void setCacheValue(String key, Object value){
        cache.put(key, value);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    /** Looks up the recipe id captured during {@link #getModifierRecipes()}. */
    public static ResourceLocation getRecipeId(IDisplayModifierRecipe recipe){
        return RECIPE_IDS.get(recipe);
    }

    public static List<IDisplayModifierRecipe> getModifierRecipes(){
        RecipeManager recipeManager = Minecraft.getInstance().level.getRecipeManager();
        RECIPE_IDS.clear();
        List<IDisplayModifierRecipe> cleanedList = new ArrayList<>();
        for (RecipeHolder<ITinkerStationRecipe> holder : recipeManager.getAllRecipesFor(TinkerRecipeTypes.TINKER_STATION.get())) {
            if(holder.value() instanceof IDisplayModifierRecipe recipe){
                RECIPE_IDS.putIfAbsent(recipe, holder.id());
                boolean contains = cleanedList.stream().anyMatch(recipe1 ->
                        recipe1.getDisplayResult().getModifier().equals(recipe.getDisplayResult().getModifier()) &&
                                Objects.equals(recipe1.getSlots(), recipe.getSlots()) &&
                                recipe1.getLevel().max() == recipe.getLevel().max());
                if(!contains)cleanedList.add(recipe);
            }
        }
        return cleanedList;
    }
}
