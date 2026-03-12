package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hedgecourt.aqueduct.ui.UiElement;
import com.hedgecourt.aqueduct.ui.elements.ConstructionButtonUiElement;
import com.hedgecourt.aqueduct.ui.elements.CrosshairUiElement;
import com.hedgecourt.aqueduct.ui.elements.MinimapUiElement;
import com.hedgecourt.aqueduct.ui.elements.SelectedInfoUiElement;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class UiRenderer implements Disposable {

  public static final float UI_PADDING = 10f;

  private final AqueductWorld world;

  private final FontManager fontManager;

  private final OrthographicCamera camera;
  private final ScreenViewport viewport;
  private final ShapeDrawer shapeDrawer;
  private final Vector3 unprojectScratch = new Vector3();

  private final List<UiElement> elements = new ArrayList<>();
  private UiElement hoveredElement = null;

  private MinimapUiElement minimap;

  public UiRenderer(
      AqueductWorld world,
      SpriteBatch batch,
      ShapeDrawer shapeDrawer,
      FontManager fontManager,
      WorldRenderer worldRenderer,
      Consumer<String> onConstructionButtonClicked) {
    this.world = world;
    this.shapeDrawer = shapeDrawer;
    this.fontManager = fontManager;
    camera = new OrthographicCamera();
    viewport = new ScreenViewport(camera);
    updateScreenBounds();

    minimap = new MinimapUiElement(world, worldRenderer);
    addElement(minimap);

    addElement(
        new SelectedInfoUiElement(
            world,
            fontManager,
            minimap.getBounds().x + minimap.getBounds().width + UI_PADDING,
            UI_PADDING,
            200f,
            getUiHeight() - 2f * UI_PADDING));

    float buildButtonWidth = 100f;
    float buildButtonHeight = (getUiHeight() - 2f * UI_PADDING) / 2f;
    addElement(
        new ConstructionButtonUiElement(
            fontManager,
            getUiWidth() - UI_PADDING - buildButtonWidth,
            UI_PADDING,
            buildButtonWidth,
            buildButtonHeight,
            world.getBuildingFactory().get("pipe"),
            onConstructionButtonClicked));

    addElement(
        new ConstructionButtonUiElement(
            fontManager,
            getUiWidth() - UI_PADDING - buildButtonWidth,
            UI_PADDING + buildButtonHeight,
            buildButtonWidth,
            buildButtonHeight,
            world.getBuildingFactory().get("townhall"),
            onConstructionButtonClicked));

    addElement(
        new ConstructionButtonUiElement(
            fontManager,
            getUiWidth() - UI_PADDING - buildButtonWidth * 2f,
            UI_PADDING,
            buildButtonWidth,
            buildButtonHeight,
            world.getBuildingFactory().get("sprinkler"),
            onConstructionButtonClicked));

    addElement(
        new ConstructionButtonUiElement(
            fontManager,
            getUiWidth() - UI_PADDING - buildButtonWidth * 2f,
            UI_PADDING + buildButtonHeight,
            buildButtonWidth,
            buildButtonHeight,
            world.getBuildingFactory().get("pump"),
            onConstructionButtonClicked));

    addElement(new CrosshairUiElement());
  }

  public boolean isMinimapDragging() {
    return minimap != null && minimap.isDragging();
  }

  public void handleMinimapDrag(float mouseX, float mouseY) {
    if (minimap != null) minimap.handleDrag(mouseX, mouseY);
  }

  public void addElement(UiElement element) {
    elements.add(element);
  }

  public void applyViewport() {
    viewport.apply();
  }

  public void resize(int width, int height) {
    updateScreenBounds();
  }

  private void updateScreenBounds() {
    int screenW = Gdx.graphics.getWidth();
    viewport.update(screenW, (int) C.UI_BOTTOM_HEIGHT, true);
  }

  private void illustrateExtremeDrawing(SpriteBatch batch) {
    // TODO put this in a UiElement once i have that API
    // ui corner markers
    float w = getUiWidth();
    float h = getUiHeight();
    float s = 50f;
    shapeDrawer.filledRectangle(0, 0, s, s, Color.PURPLE); // bottom-left
    shapeDrawer.filledRectangle(w - s, 0, s, s, Color.PURPLE); // bottom-right
    shapeDrawer.filledRectangle(0, h - s, s, s, Color.PURPLE); // top-left
    shapeDrawer.filledRectangle(w - s, h - s, s, s, Color.PURPLE); // top-right
  }

  public void illustrateCrosshairs(SpriteBatch batch) {
    batch.setProjectionMatrix(camera.combined);

    // mouse crosshair in ui space
    Vector2 mouse = mouseInUi();
    shapeDrawer.line(mouse.x - 20, mouse.y, mouse.x + 20, mouse.y, Color.CYAN, 2f);
    shapeDrawer.line(mouse.x, mouse.y - 20, mouse.x, mouse.y + 20, Color.CYAN, 2f);
  }

  public void draw(SpriteBatch batch) {
    viewport.apply();
    batch.setProjectionMatrix(camera.combined);

    Vector2 mouse = mouseInUi();
    for (UiElement element : elements) {
      element.preUpdate(mouse.x, mouse.y);
    }

    // ── hover dispatch ────────────────────────────────────────────────────
    UiElement nowHovered = null;
    for (UiElement element : elements) {
      if (element.containsMouse()) {
        nowHovered = element;
        break;
      }
    }
    if (nowHovered != hoveredElement) {
      if (hoveredElement != null) hoveredElement.onMouseExit();
      if (nowHovered != null) nowHovered.onMouseEnter(mouse.x, mouse.y);
      hoveredElement = nowHovered;
    }

    // ── draw ──────────────────────────────────────────────────────────────
    for (UiElement element : elements) {
      element.draw(batch, shapeDrawer);
    }
  }

  public boolean handleClick(float mouseX, float mouseY) {
    for (UiElement element : elements) {
      if (element.containsMouse()) {
        element.onClick(mouseX, mouseY);
        return true;
      }
    }
    return false;
  }

  public void handleTouchDown(float mouseX, float mouseY) {
    if (minimap != null && minimap.containsMouse()) {
      minimap.onTouchDown(mouseX, mouseY);
    }
  }

  @Override
  public void dispose() {}

  // ── coordinate space ──────────────────────────────────────────────────────

  public float getUiWidth() {
    return viewport.getScreenWidth();
  }

  public float getUiHeight() {
    return viewport.getScreenHeight();
  }

  public Vector2 mouseInUi() {
    unprojectScratch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    viewport.unproject(unprojectScratch);
    return new Vector2(unprojectScratch.x, unprojectScratch.y);
  }
}
