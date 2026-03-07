package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class WorldRenderer implements Disposable {

  private final OrthographicCamera camera;
  private final Vector3 unprojectScratch = new Vector3();
  private final ShapeDrawer shapeDrawer;

  private TiledMap map;
  private OrthogonalTiledMapRenderer mapRenderer;
  private int tileWidth;
  private int tileHeight;
  private int mapTilesWide;
  private int mapTilesTall;

  public WorldRenderer(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    this.shapeDrawer = shapeDrawer;
    camera = new OrthographicCamera();
    float worldW = Gdx.graphics.getWidth() - C.UI_RIGHT_WIDTH;
    float worldH = Gdx.graphics.getHeight() - C.UI_BOTTOM_HEIGHT;
    camera.setToOrtho(false, worldW, worldH);
    camera.position.set(worldW / 2f, worldH / 2f, 0);
    camera.update();
  }

  public void loadMap(String path) {
    map = new TmxMapLoader().load(path);
    tileWidth = map.getProperties().get("tilewidth", Integer.class);
    tileHeight = map.getProperties().get("tileheight", Integer.class);
    mapTilesWide = map.getProperties().get("width", Integer.class);
    mapTilesTall = map.getProperties().get("height", Integer.class);
    mapRenderer = new OrthogonalTiledMapRenderer(map);
    centerCamera();
  }

  private void centerCamera() {
    float mapPixelW = mapTilesWide * tileWidth;
    float mapPixelH = mapTilesTall * tileHeight;
    camera.position.set(mapPixelW / 2f, mapPixelH / 2f, 0);
    camera.update();
  }

  public void beginViewport() {
    int bbW = Gdx.graphics.getBackBufferWidth();
    int bbH = Gdx.graphics.getBackBufferHeight();
    int uiH =
        Math.round(
            C.UI_BOTTOM_HEIGHT * Gdx.graphics.getBackBufferHeight() / Gdx.graphics.getHeight());
    Gdx.gl.glViewport(0, uiH, bbW, bbH - uiH);
    camera.update();
  }

  public void endViewport() {
    Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
  }

  public void render() {
    if (mapRenderer == null) return;
    mapRenderer.setView(camera);
    mapRenderer.render();
  }

  public void drawUnderlay(SpriteBatch batch) {}

  public void drawEntities(SpriteBatch batch) {
    batch.setProjectionMatrix(camera.combined);
  }

  public void drawOverlay(SpriteBatch batch) {
    batch.setProjectionMatrix(camera.combined);
    if (map == null) return;
    float mapPixelW = mapTilesWide * tileWidth;
    float mapPixelH = mapTilesTall * tileHeight;
    shapeDrawer.rectangle(0, 0, mapPixelW, mapPixelH, Color.RED, 2f);
  }

  @Override
  public void dispose() {
    if (map != null) map.dispose();
    if (mapRenderer != null) mapRenderer.dispose();
  }

  // ── coordinate space ──────────────────────────────────────────────────────

  public float getWorldWidth() {
    return camera.viewportWidth;
  }

  public float getWorldHeight() {
    return camera.viewportHeight;
  }

  public Vector2 mouseInWorld() {
    unprojectScratch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    camera.unproject(unprojectScratch);
    return new Vector2(unprojectScratch.x, unprojectScratch.y);
  }
}
