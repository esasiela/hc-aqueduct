package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hedgecourt.aqueduct.world.MapGraph;
import com.hedgecourt.aqueduct.world.Pathfinder;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.layers.CrosshairWorldLayer;
import java.util.ArrayList;
import java.util.List;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class WorldRenderer implements Disposable {

  private final OrthographicCamera camera;
  private final ScreenViewport viewport;
  private final ShapeDrawer shapeDrawer;
  private final Vector3 unprojectScratch = new Vector3();

  private TiledMap map;
  private OrthogonalTiledMapRenderer mapRenderer;
  private int tileWidth;
  private int tileHeight;
  private int mapTilesWide;
  private int mapTilesTall;

  private MapGraph mapGraph;
  private Pathfinder pathfinder;

  private CameraController cameraController;

  private final List<WorldLayer> layers = new ArrayList<>();

  public WorldRenderer(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    this.shapeDrawer = shapeDrawer;
    camera = new OrthographicCamera();
    viewport = new ScreenViewport(camera);
    updateScreenBounds();

    addLayer(new CrosshairWorldLayer());
  }

  public void loadMap(String path) {
    map = new TmxMapLoader().load(path);
    tileWidth = map.getProperties().get("tilewidth", Integer.class);
    tileHeight = map.getProperties().get("tileheight", Integer.class);
    mapTilesWide = map.getProperties().get("width", Integer.class);
    mapTilesTall = map.getProperties().get("height", Integer.class);

    TiledMapTileLayer wallsLayer = (TiledMapTileLayer) map.getLayers().get("walls");
    mapGraph = new MapGraph(mapTilesWide, mapTilesTall, wallsLayer);
    pathfinder = new Pathfinder(mapGraph, tileWidth, tileHeight);

    mapRenderer = new OrthogonalTiledMapRenderer(map);

    cameraController = new CameraController(camera, viewport);
    cameraController.init(mapTilesWide * tileWidth, mapTilesTall * tileHeight);
  }

  public void resize(int width, int height) {
    updateScreenBounds();
    if (cameraController != null) {
      cameraController.init(mapTilesWide * tileWidth, mapTilesTall * tileHeight);
    }
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

  public void update(float delta) {
    if (cameraController != null) cameraController.update(delta);
    Vector2 mouse = mouseInWorld();
    for (WorldLayer layer : layers) {
      layer.preUpdate(mouse.x, mouse.y);
      layer.update(delta);
    }
  }

  public void onScroll(float dx, float dy) {
    if (cameraController != null) cameraController.onScroll(dx, dy);
  }

  public void onZoom(float zoomDelta, float screenX, float screenY) {
    if (cameraController != null) cameraController.onZoom(zoomDelta, screenX, screenY);
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

  private void illustrateExtremeDrawing(SpriteBatch batch) {
    // TODO - put this in a WorldLayer once I have them
    float s = 50f;

    // ── green: map corners ───────────────────────────────────────────────
    float mapW = getMapWidth();
    float mapH = getMapHeight();
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

  public MapGraph getMapGraph() {
    return mapGraph;
  }

  public Pathfinder getPathfinder() {
    return pathfinder;
  }

  @Override
  public void dispose() {
    if (map != null) map.dispose();
    if (mapRenderer != null) mapRenderer.dispose();
  }

  // ── coordinate space ──────────────────────────────────────────────────────

  public float getWorldWidth() {
    return viewport.getScreenWidth();
  }

  public float getWorldHeight() {
    return viewport.getScreenHeight();
  }

  public float getMapWidth() {
    return mapTilesWide * tileWidth;
  }

  public float getMapHeight() {
    return mapTilesTall * tileHeight;
  }

  public Vector2 mouseInWorld() {
    unprojectScratch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    viewport.unproject(unprojectScratch);
    return new Vector2(unprojectScratch.x, unprojectScratch.y);
  }
}
