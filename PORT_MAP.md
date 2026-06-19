# Tinkers' Planner — 1.18.2/Forge → 1.21.1/NeoForge port map

Authoritative rules for porting each `.java` file. Apply ALL relevant rules. Compile target is the
sibling NeoForge ports of Tinkers' Construct + Mantle (composite build). Do not change behavior; only
adapt APIs. Keep imports tidy (remove dead ones, add new ones).

## A. Vanilla GUI rendering (1.18 → 1.21)

- Rendering now goes through `GuiGraphics` (`net.minecraft.client.gui.GuiGraphics`), NOT `PoseStack`.
  - **`AbstractWidget.render(...)` is FINAL in 1.21.** Any subclass that overrode `render(PoseStack,...)` MUST instead
    override `protected void renderWidget(GuiGraphics gui, int mx, int my, float pt)`. (This applies to BOTH plain
    AbstractWidget subclasses AND Button subclasses — `renderButton` was also renamed to `renderWidget`.)
    When calling `super`, call `super.renderWidget(...)`. To render a child widget, call its public `child.render(gui, mx, my, pt)`.
  - Any helper method taking `PoseStack` for drawing → take `GuiGraphics` instead and thread it through.
- The raw matrix stack is still reachable: `gui.pose()` returns the `PoseStack` (for pushPose/popPose/translate/scale).
- Static draw helpers moved onto `GuiGraphics` (instance methods):
  - `Screen.fill(ms, x1,y1,x2,y2,color)`            → `gui.fill(x1,y1,x2,y2,color)`
  - `screen.blit(ms, x,y,u,v,w,h)`                  → `gui.blit(TEXTURE, x,y,u,v,w,h)`  (pass the ResourceLocation!)
  - `drawCenteredString(ms, font, text, x,y,color)` → `gui.drawCenteredString(font, text, x,y,color)`
  - `drawString(ms, font, text, x,y,color)`         → `gui.drawString(font, text, x,y,color)`  (defaults to shadow=true)
  - `font.draw(ms, text, x,y,color)`                → `gui.drawString(font, text, (int)x,(int)y,color, false)`
  - `screen.renderTooltip(ms, itemStack, x,y)`      → `gui.renderTooltip(font, itemStack, x,y)`
  - `screen.renderComponentTooltip(ms, list, x,y)`  → `gui.renderComponentTooltip(font, list, x,y)`
  - `screen.renderTooltip(ms, component, x,y)`       → `gui.renderTooltip(font, component, x,y)`
  - `itemRenderer.renderGuiItem(stack,x,y)` / `renderAndDecorateItem` → `gui.renderItem(stack, x, y)`
  - item count/damage overlay → `gui.renderItemDecorations(font, stack, x, y)`
  - `Screen.renderBackground(ms)` → `super.renderBackground(gui, mouseX, mouseY, partialTick)` (1.21 sig has 3 extra args; pass them through from render()).
- Color tint for blits: `RenderSystem.setShaderColor(r,g,b,a)` → `gui.setColor(r,g,b,a)`; reset with `gui.setColor(1,1,1,1)`.
  You can usually DELETE `RenderSystem.setShader(...)`/`setShaderTexture(...)` lines (GuiGraphics.blit handles them).
- `AbstractWidget` field access: `widget.x`→`widget.getX()`, `widget.y`→`widget.getY()`,
  set via `widget.setX(..)`/`widget.setY(..)`/`widget.setPosition(x,y)`. `width`/`height` remain public fields.
  Inside a widget subclass, prefer `getX()/getY()`.
- Narration: `public void updateNarration(NarrationElementOutput o)` (override) →
  `protected void updateWidgetNarration(NarrationElementOutput o)` (the new abstract method).
- `net.minecraft.client.gui.components.Widget` (interface) is GONE → use
  `net.minecraft.client.gui.components.Renderable` (has `render(GuiGraphics,int,int,float)`).
- `mouseScrolled(double mx, double my, double delta)` → `mouseScrolled(double mx, double my, double scrollX, double scrollY)`.
  When forwarding to children pass `(mx,my,scrollX,scrollY)`. Use `scrollY` where the old `scroll`/`delta` was used.
- `Button` subclass constructor: 1.18 `super(x,y,w,h,message,onPress)` →
  `super(x,y,w,h,message,onPress, Button.DEFAULT_NARRATION)` (the 7-arg protected ctor).
- `AbstractWidget` subclass constructor `super(x,y,w,h,message)` is unchanged (still valid).

## B. Text API (1.18 → 1.19+)

- `new TextComponent("s")`         → `Component.literal("s")`
- `new TranslatableComponent(k,a)` → `Component.translatable(k, a)`
- Imports `net.minecraft.network.chat.TextComponent` / `TranslatableComponent` → drop; use `net.minecraft.network.chat.Component` / `MutableComponent`.

## C. ResourceLocation (constructor now private)

- `new ResourceLocation(ns, path)` → `ResourceLocation.fromNamespaceAndPath(ns, path)`
- `new ResourceLocation(str)`      → `ResourceLocation.parse(str)`
- `item.getRegistryName()`         → `BuiltInRegistries.ITEM.getKey(item)` (import `net.minecraft.core.registries.BuiltInRegistries`)
- subclassed ids (`new MaterialId(str)`, `new ModifierId(str)`) are still fine — those ctors are public.

## D. Sound

- `SoundEvents.UI_BUTTON_CLICK` is now `Holder<SoundEvent>`. Change fields typed `SoundEvent` that hold these to
  `Holder<SoundEvent>` (`net.minecraft.core.Holder`). `SimpleSoundInstance.forUI(Holder<SoundEvent>, float)` exists.

## E. Forge → NeoForge (imports + annotations)

- `net.minecraftforge.api.distmarker.Dist`            → `net.neoforged.api.distmarker.Dist`
- `net.minecraftforge.eventbus.api.SubscribeEvent`    → `net.neoforged.bus.api.SubscribeEvent`
- `net.minecraftforge.fml.common.Mod`                 → `net.neoforged.fml.common.Mod`
- `@Mod.EventBusSubscriber(Dist.CLIENT)`              → `@EventBusSubscriber(value = Dist.CLIENT)` ; import `net.neoforged.fml.common.EventBusSubscriber`
- `net.minecraftforge.client.event.ScreenEvent`       → `net.neoforged.neoforge.client.event.ScreenEvent`
  - `ScreenEvent.InitScreenEvent.Post` → `ScreenEvent.Init.Post`
  - `ScreenEvent.DrawScreenEvent.Post` → `ScreenEvent.Render.Post`
  - `ScreenEvent.DrawScreenEvent.Pre`  → `ScreenEvent.Render.Pre`
  - `event.getPoseStack()`             → `event.getGuiGraphics()`  (returns GuiGraphics)
  - `event.addListener(widget)`        → still `event.addListener(widget)` on `ScreenEvent.Init.Post`
- `net.minecraftforge.common.ForgeConfigSpec`         → `net.neoforged.neoforge.common.ModConfigSpec` (all nested types: `ModConfigSpec.IntValue`, `.EnumValue`, `.Builder`). `.configure(...)` still returns `Pair`.
- `FMLClientSetupEvent` → `net.neoforged.fml.event.lifecycle.FMLClientSetupEvent`
- DROP entirely: `IExtensionPoint`, `IExtensionPoint.DisplayTest`, `NetworkConstants`, `FMLJavaModLoadingContext`,
  `ModLoadingContext.registerExtensionPoint(...)`. (Client-side display-test is set in neoforge.mods.toml.)

## F. Tinkers' Construct API drift (3.6/1.18 → 3.11.2/1.21) — VERIFIED against NTC source

- **ToolStack**: `from(ItemStack)`, `copy()`, `createStack()`, `createStack(int)`,
  `addModifier(ModifierId, int)`, `removeModifier(ModifierId, int)`, `getPersistentData()`→`ToolDataNBT`,
  `rebuildStats()`, `getMaterials()`→`MaterialNBT`, `getDefinition()`→`ToolDefinition`, `isInitialized(ItemStack)` (static).
  - `getModifierLevel(Modifier)` **REMOVED** → `tool.getModifiers().getLevel(modifierId)` (ModifierNBT.getLevel(ModifierId)).
  - `validate()` returning ValidatedResult **REMOVED** → `Component tryValidate()` (null = valid).
  - `addModifierAmount(ModifierId modifier, int amount, int needed)` — sets incremental amount.
  - `getMaterial(int)` likely → `tool.getMaterials().get(int)` returns `MaterialVariant` (`.getId()`→MaterialId, `.getMaterial()`→IMaterial). VERIFY when used.
- **ToolDataNBT** (from getPersistentData): `addSlots(SlotType, int)`, `setSlots(SlotType,int)`, `getSlots(SlotType)`.
- **ValidatedResult** class **REMOVED** → use `slimeknights.tconstruct.library.recipe.RecipeResult<T>`:
  `RecipeResult.pass()`, `success(T)`, `failure(Component)`, `failure(String key, Object... args)`;
  instance `isSuccess()`, `hasError()`, `getResult()`, `getMessage()`. (Old `ValidatedResult.PASS` → `RecipeResult.pass()`.)
- **ITinkerStationRecipe.getValidatedResult**: now `getValidatedResult(ITinkerStationContainer inv, RegistryAccess access)`
  returning `RecipeResult<LazyToolStack>`. Get RegistryAccess via `Minecraft.getInstance().level.registryAccess()`.
- **PartRequirement** class **REMOVED** + `ToolDefinition.getData().getParts()` gone →
  parts list = `slimeknights.tconstruct.library.tools.definition.module.material.ToolPartsHook.parts(toolDefinition)` → `List<IToolPart>`.
- **Modifier**: `getId()`→`ModifierId` (still present). NO `getMaxLevel()`.
- **ModifierId**: ctors `(String)`, `(String,String)`, `(ResourceLocation)` public.
- **ModifierEntry**: `getModifier()`, `getId()`, `getLevel()`, `getNeeded()`, `getDisplayName()`.
- **IncrementalModifier** class + `IncrementalModifier.setAmount(...)` **REMOVED**. **ModifierRecipeLookup.getNeededPerLevel(...)** **REMOVED**.
  - "needed per level" now comes from the recipe: `IDisplayModifierRecipe.getDisplayResult().getNeeded()` and `IDisplayModifierRecipe.isIncremental()`.
  - To set incremental amount on a tool: `tool.addModifierAmount(modId, amount, needed)`.
- **SlotType**: `getIfPresent(String)`, `getName()`, `getDisplayName()`, `getOrCreate(String)`.
  - **SlotType.SlotCount** is a RECORD: accessors `type()` and `count()` (NOT getType()/getCount()).
- **IDisplayModifierRecipe**: `getDisplayResult()`→ModifierEntry, `getModifier()`, `getSlots()`→`@Nullable SlotCount`,
  `getSlotType()`→`@Nullable SlotType`, `getLevel()`→IntRange (use `.max()` for old getMaxLevel()), `isIncremental()`.
  - Old `recipe.getMaxLevel()` → `recipe.getLevel().max()`.
- **Recipes have NO getId() in 1.21.** Recipe IDs come from `RecipeHolder<T>` (`holder.id()`, `holder.value()`).
  Use `recipeManager.getAllRecipesFor(type)` → `List<RecipeHolder<T>>`. (See PlannerScreen for the id-map helper.)
- **MaterialRegistry**: instance via `MaterialRegistry.getInstance()`. `getMaterial(MaterialId)`,
  `getVisibleMaterials()`/`getAllMaterials()`. Old static `MaterialRegistry.getMaterials()` → `MaterialRegistry.getInstance().getVisibleMaterials()`.
  - Material stats: `MaterialRegistry.getInstance().getMaterialStats(matId, statsId)`→`Optional<T>`. `getClassForStat` REMOVED.
- **IMaterial.getIdentifier()**→MaterialId. **MaterialId** extends ResourceId(ResourceLocation).
- **Material stat records**: `HeadMaterialStats`: `durability()`, `miningSpeed()`, `tier()`, `attack()`.
  `HandleMaterialStats`: `durability()`, `miningSpeed()`, `meleeSpeed()`, `attackDamage()`. (No `get` prefix; they're records.)
  Old `getDurability()/getMiningSpeed()/getAttack()/getAttackDamage()/getAttackSpeed()/getTier()` → the record accessors above.
- **IToolPart**: `canUseMaterial(MaterialId)` (param is MaterialId, not IMaterial), `getStatType()`→MaterialStatsId,
  `withMaterialForDisplay(MaterialId)`, `getMaterial(ItemStack)`→MaterialVariant, `asItem()`.
- **ToolBuildHandler.buildItemFromMaterials(IModifiable tool, MaterialNBT materials)`→ItemStack.
- **MaterialNBT.of(IMaterial... )`→MaterialNBT.
- **ModifierIconManager.renderIcon(GuiGraphics gui, Modifier modifier, int x, int y, int z, int size)** (static, takes GuiGraphics).
- **StationSlotLayout**: `getDisplayName()`, `getDescription()`, `getIcon()`→LayoutIcon (`.getValue(Class)`), `getInputSlots()`→`List<LayoutSlot>`, `getSortIndex()`, `isMain()`.
- **StationSlotLayoutLoader.getInstance().getSortedSlots()`→`List<StationSlotLayout>`.
- **LayoutSlot**: `getX()`, `getY()`, `getIcon()`→Pattern, `getTranslationKey()`.
- **IMaterialStats**: `getIdentifier()`→MaterialStatsId, `getLocalizedName()`, `getLocalizedInfo()`.
- **RecipeHelper (Mantle)**: `getJEIRecipes(RegistryAccess access, RecipeManager manager, RecipeType<T> type, Class<C> clazz)` → `List<C>`
  (RegistryAccess is now the FIRST arg). `getRecipes(RecipeManager, RecipeType, Class)`→`List<C>`.
- **TinkerRecipeTypes.TINKER_STATION** is a `DeferredHolder` → `.get()` returns the RecipeType.
- **TinkerStationScreen**: fields `cornerX`,`cornerY`; `getCurrentLayout()`→StationSlotLayout (use INSTEAD of reflection on `currentLayout`),
  `getMaxInputs()`, `getMenu()`, `getMinecraft()`. Block-entity getter: VERIFY `getTileEntity()` vs `getBlockEntity()`.
  `onToolSelection(StationSlotLayout)`. `SlotButtonItem.getLayout()`. `TinkerStationButtonsWidget.getButtons()`.

## H. Planner-specific (foundation is already ported — use these, do NOT modify foundation files)

- `PlannerScreen.bindTexture()` was REMOVED. Don't call it.
- `parent.blit(stack, x, y, u, v, w, h)` / `screen.blit(...)` where the target is the planner screen →
  `gui.blit(net.tiffit.tconplanner.screen.PlannerScreen.TEXTURE, x, y, u, v, w, h)`.
  For a DIFFERENT texture (e.g. `new ResourceLocation("tconstruct","textures/gui/tinker_station.png")`), keep that texture:
  declare it via `ResourceLocation.fromNamespaceAndPath("tconstruct", "...")` and `gui.blit(thatRL, ...)`.
- Font access from a widget: `parent.getFont()` (PlannerScreen.getFont() exists) or `Minecraft.getInstance().font`.
- Tooltips deferred to `parent.postRenderTasks` / `EventListener.postRenderQueue`: those lambdas now must capture the
  `GuiGraphics gui` and call `gui.renderTooltip(parent.getFont(), ...)` / `gui.renderComponentTooltip(parent.getFont(), list, x, y)`.
  (Old `parent.renderTooltip(stack,...)` / `parent.renderComponentTooltip(stack,...)` → use the `gui` instance methods.)
  `PlannerScreen.renderItemTooltip(GuiGraphics gui, ItemStack stack, int x, int y)` exists for item tooltips.
- `getItemRenderer().renderGuiItem(stack, x, y)` / `renderAndDecorateItem` → `gui.renderItem(stack, x, y)`;
  for the count/durability overlay add `gui.renderItemDecorations(parent.getFont(), stack, x, y)`.
- `RenderSystem.getModelViewStack()` now returns `org.joml.Matrix4fStack` (NOT PoseStack). For GUI item rendering do NOT
  use it. Wrap the draw in `gui.pose().pushPose(); gui.pose().translate(x, y, z); gui.pose().scale(sx, sy, 1); gui.renderItem(stack, 0, 0); gui.pose().popPose();`
  (translate by the widget x/y, render the item at 0,0). Drop any `RenderSystem.applyModelViewMatrix()` calls.
- `MaterialRegistry.getClassForStat(statsId)` REMOVED →
  `MaterialRegistry.getInstance().getDefaultStats(statsId)` then `.getClass()` (guard null first).
- AbstractWidget subclasses storing positions: `this.x -= n` → `this.setX(getX() - n)`.
- Custom `renderToolTip(PoseStack,...)` helper methods → change the param to `GuiGraphics`. Callers updated accordingly.
- `Blueprint.validate()` / `ToolStack.tryValidate()` and `ToolValidator.validateModRemoval(...)` now return
  `RecipeResult<?>` / `@Nullable Component`. Use `result.hasError()` / `result.getMessage()` (RecipeResult) or null-check (tryValidate).
- `ToolStack.from(stack).validate()` → there is no `validate()`. Use `ToolStack.from(stack).tryValidate()` (returns @Nullable Component; null = OK).
- `SlotType.SlotCount`: `count.getType()`→`count.type()`, `count.getCount()`→`count.count()`.
- `IDisplayModifierRecipe.getMaxLevel()` → `recipe.getLevel().max()`.
- `tool.getModifierLevel(modifier)` → `tool.getModifiers().getLevel(modifier.getId())`.

## G. Misc

- `NbtIo.writeCompressed(CompoundTag, File)` → `NbtIo.writeCompressed(CompoundTag, Path)` (use `file.toPath()`).
- `NbtIo.readCompressed(File)` → `NbtIo.readCompressed(Path, NbtAccounter)` e.g. `NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap())`.
- `@Mod` main class ctor: NeoForge passes `IEventBus modBus` (+ optional `ModContainer`, `Dist`). Register config via
  `modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC)`. Subscribe setup via `modBus.addListener(this::method)`.
