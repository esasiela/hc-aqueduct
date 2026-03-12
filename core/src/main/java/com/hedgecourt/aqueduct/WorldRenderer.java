package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Building;
import com.hedgecourt.aqueduct.world.entities.Entity;
import com.hedgecourt.aqueduct.world.layers.BuildingLayer;
import com.hedgecourt.aqueduct.world.layers.ConstructionPendingLayer;
import com.hedgecourt.aqueduct.world.layers.ConstructionPlacementCursorLayer;
import com.hedgecourt.aqueduct.world.layers.CrosshairWorldLayer;
import com.hedgecourt.aqueduct.world.layers.NodeLayer;
import com.hedgecourt.aqueduct.world.layers.TileHighlightWorldLayer;
import com.hedgecourt.aqueduct.world.layers.WorkerLayer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class WorldRenderer implements Disposable {

  private final AqueductWorld world;

  private final OrthographicCamera camera;
  private final ScreenViewport viewport;
  private final ShapeDrawer shapeDrawer;
  private final Vector3 unprojectScratch = new Vector3();

  private final OrthogonalTiledMapRenderer mapRenderer;

  private final CameraController cameraController;

  private final List<WorldLayer> layers = new ArrayList<>();

  private boolean selBoxActive = false;
  private final Vector2 selBoxStart = new Vector2();
  private final Vector2 selBoxCurrent = new Vector2();

  private final WorkerLayer workerLayer;

  public WorldRenderer(
      SpriteBatch batch,
      ShapeDrawer shapeDrawer,
      FontManager fontManager,
      AqueductWorld world,
      Supplier<Building> constructionPlacementEntitySupplier) {
    this.shapeDrawer = shapeDrawer;
    this.world = world;

    camera = new OrthographicCamera();
    viewport = new ScreenViewport(camera);
    cameraController = new CameraController(camera, viewport);

    mapRenderer = new OrthogonalTiledMapRenderer(world.getMap());
    updateScreenBounds();
    initCamera();

    workerLayer = new WorkerLayer(world, fontManager);
    addLayer(new ConstructionPendingLayer(world::getIncompleteBuildings));
    addLayer(new TileHighlightWorldLayer(world));

    addLayer(new BuildingLayer(world));
    addLayer(new NodeLayer(world));

    addLayer(workerLayer);

    addLayer(new CrosshairWorldLayer());

    // Make sure this one goes last, cursor on top
    addLayer(new ConstructionPlacementCursorLayer(constructionPlacementEntitySupplier));
  }

  public void resize(int screenWidth, int screenHeight) {
    updateScreenBounds();
  }

  public void initCamera() {
    cameraController.init((int) world.getMapWidth(), (int) world.getMapHeight());
  }

  private void updateScreenBounds() {
    int screenW = Gdx.graphics.getWidth();
    int screenH = Gdx.graphics.getHeight();
    int uiH = (int) C.UI_BOTTOM_HEIGHT;
    viewport.update(screenW, screenH - uiH, true);
    viewport.setScreenBounds(0, uiH, screenW, screenH - uiH);
  }

  public OrthographicCamera getCamera() {
    return camera;
  }

  public void applyViewport() {
    viewport.apply();
  }

  public void updateCamera(float delta) {
    if (cameraController != null) cameraController.update(delta);
  }

  public void preDraw(float delta) {
    Vector2 mouse = mouseInWorld();

    boolean foundHovered = false;
    for (Entity entity : world.getEntities()) {
      if (!foundHovered && entity.containsPoint(mouse.x, mouse.y)) {
        entity.setHovered(true);
        foundHovered = true;
      } else {
        entity.setHovered(false);
      }
    }

    for (WorldLayer layer : layers) {
      layer.preDraw(mouse.x, mouse.y);
    }
  }

  public void onScroll(float dx, float dy) {
    if (cameraController != null) cameraController.onScroll(dx, dy);
  }

  public void onZoom(float zoomDelta, float screenX, float screenY) {
    if (cameraController != null) cameraController.onZoom(zoomDelta, screenX, screenY);
  }

  public void updateSprites(float delta) {
    for (Entity entity : world.getEntities()) {
      entity.updateSprite(delta);
    }
  }

  public void render() {
    viewport.apply();
    Gdx.gl.glClearColor(C.CLEAR_R, C.CLEAR_G, C.CLEAR_B, 1f);
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    if (mapRenderer == null) return;
    mapRenderer.setView(camera);
    mapRenderer.render();
  }

  public void drawUnderlay(SpriteBatch batch) {
    batch.setProjectionMatrix(camera.combined);
    for (WorldLayer layer : layers) layer.drawUnderlay(batch, shapeDrawer);
  }

  public void drawEntities(SpriteBatch batch) {
    batch.setProjectionMatrix(camera.combined);
    for (WorldLayer layer : layers) layer.drawEntities(batch, shapeDrawer);
  }

  public void drawOverlay(SpriteBatch batch) {
    batch.setProjectionMatrix(camera.combined);
    for (WorldLayer layer : layers) layer.drawOverlay(batch, shapeDrawer);
  }

  public void addLayer(WorldLayer layer) {
    layers.add(layer);
  }

  public Vector2 getCameraPosition() {
    return new Vector2(camera.position.x, camera.position.y);
  }

  public float getCameraVisibleWidth() {
    return camera.viewportWidth * camera.zoom;
  }

  public float getCameraVisibleHeight() {
    return camera.viewportHeight * camera.zoom;
  }

  public void panCameraTo(float worldX, float worldY) {
    camera.position.x = worldX;
    camera.position.y = worldY;
    cameraController.clampCameraPublic();
    camera.update();
  }

  public Rectangle getVisibleWorldRect() {
    float vw = camera.viewportWidth * camera.zoom;
    float vh = camera.viewportHeight * camera.zoom;
    return new Rectangle(camera.position.x - vw / 2f, camera.position.y - vh / 2f, vw, vh);
  }

  public void clearSelectionBox() {
    this.selBoxActive = false;
  }

  public void setSelectionBox(boolean active, Vector2 start, Vector2 current) {
    this.selBoxActive = active;
    this.selBoxStart.set(start);
    this.selBoxCurrent.set(current);
  }

  public void drawSelectionBox(SpriteBatch batch) {
    if (!selBoxActive) return;

    batch.setProjectionMatrix(camera.combined);
    float x = Math.min(selBoxStart.x, selBoxCurrent.x);
    float y = Math.min(selBoxStart.y, selBoxCurrent.y);
    float w = Math.abs(selBoxStart.x - selBoxCurrent.x);
    float h = Math.abs(selBoxStart.y - selBoxCurrent.y);
    shapeDrawer.setColor(new Color(0f, 1f, 0f, 0.15f));
    shapeDrawer.filledRectangle(x, y, w, h);
    shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.rectangle(x, y, w, h, 1.5f);
  }

  private void illustrateExtremeDrawing(SpriteBatch batch) {
    // TODO - put this in a WorldLayer once I have them
    float s = 50f;

    // ── green: map corners ───────────────────────────────────────────────
    float mapW = world.getMapWidth();
    float mapH = world.getMapHeight();
    shapeDrawer.filledRectangle(0, 0, s, s, Color.GREEN);
    shapeDrawer.filledRectangle(mapW - s, 0, s, s, Color.GREEN);
    shapeDrawer.filledRectangle(0, mapH - s, s, s, Color.GREEN);
    shapeDrawer.filledRectangle(mapW - s, mapH - s, s, s, Color.GREEN);

    // ── orange: visible viewport corners ────────────────────────────────
    float vw = camera.viewportWidth * camera.zoom;
    float vh = camera.viewportHeight * camera.zoom;
    float left = camera.position.x - vw / 2f;
    float bottom = camera.position.y - vh / 2f;
    shapeDrawer.filledRectangle(left, bottom, s, s, Color.ORANGE);
    shapeDrawer.filledRectangle(left + vw - s, bottom, s, s, Color.ORANGE);
    shapeDrawer.filledRectangle(left, bottom + vh - s, s, s, Color.ORANGE);
    shapeDrawer.filledRectangle(left + vw - s, bottom + vh - s, s, s, Color.ORANGE);
  }

  public void illustrateCrosshairDrawing(SpriteBatch batch) {
    // ── mouse crosshair in world space ──────────────────────────────────
    Vector2 mouse = mouseInWorld();
    shapeDrawer.line(mouse.x - 20, mouse.y, mouse.x + 20, mouse.y, Color.YELLOW, 2f);
    shapeDrawer.line(mouse.x, mouse.y - 20, mouse.x, mouse.y + 20, Color.YELLOW, 2f);
  }

  public WorkerLayer getWorkerLayer() {
    return workerLayer;
  }

  @Override
  public void dispose() {
    if (mapRenderer != null) mapRenderer.dispose();
  }

  public Vector2 mouseInWorld() {
    unprojectScratch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    viewport.unproject(unprojectScratch);
    return new Vector2(unprojectScratch.x, unprojectScratch.y);
  }
}
