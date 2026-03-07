package com.hedgecourt.aqueduct;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hedgecourt.aqueduct.world.entities.Worker;
import com.hedgecourt.aqueduct.world.layers.WorkerLayer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class AqueductMain extends ApplicationAdapter {

  private SpriteBatch batch;
  private ShapeDrawer shapeDrawer;
  private Texture pixelTexture;

  private AssetManager assetManager;
  private WorkerLayer workerLayer;

  private WorldRenderer worldRenderer;
  private UiRenderer uiRenderer;

  @Override
  public void create() {
    batch = new SpriteBatch();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();
    pixelTexture = new Texture(pixmap);
    pixmap.dispose();
    shapeDrawer = new ShapeDrawer(batch, new TextureRegion(pixelTexture));

    worldRenderer = new WorldRenderer(batch, shapeDrawer);
    uiRenderer = new UiRenderer(batch, shapeDrawer);

    worldRenderer.loadMap("maps/test2.tmx");

    /* ****
     * Asset Manager
     */
    String workerSpritePath = "characters/pipoya/Animal/Dog-01-3r.png";
    assetManager = new AssetManager();
    assetManager.load(workerSpritePath, Texture.class);
    assetManager.finishLoading();

    Texture workerTexture = assetManager.get(workerSpritePath, Texture.class);
    TextureRegion[][] grid = TextureRegion.split(workerTexture, 32, 32);

    workerLayer = new WorkerLayer();
    Worker worker = new Worker(worldRenderer.getMapWidth() / 2f, worldRenderer.getMapHeight() / 2f);
    worker.buildSprites(grid);
    workerLayer.addWorker(worker);
    worldRenderer.addLayer(workerLayer);

    /* ****
     * Input Multiplexer
     */

    InputMultiplexer multiplexer = new InputMultiplexer();
    multiplexer.addProcessor(
        new InputAdapter() {
          @Override
          public boolean scrolled(float amountX, float amountY) {
            if (Gdx.input.isKeyPressed(Input.Keys.SYM)) {
              worldRenderer.onZoom(amountY * C.ZOOM_SPEED, Gdx.input.getX(), Gdx.input.getY());
            } else {
              worldRenderer.onScroll(amountX, amountY);
            }
            return true;
          }

          @Override
          public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
              Vector2 worldPos = worldRenderer.mouseInWorld();
              workerLayer.commandAllMoveTo(worldPos.x, worldPos.y, worldRenderer.getPathfinder());
              return true;
            }
            return false;
          }
        });

    Gdx.input.setInputProcessor(multiplexer);
  }

  @Override
  public void render() {
    float delta = Gdx.graphics.getDeltaTime();

    handleInput(delta);
    updateWorld(delta);
    updateUi(delta);

    ScreenUtils.clear(C.CLEAR_R, C.CLEAR_G, C.CLEAR_B, 1f);

    // ── world ────────────────────────────────────────────────────────────
    worldRenderer.render();

    worldRenderer.applyViewport();
    batch.begin();
    worldRenderer.drawUnderlay(batch);
    worldRenderer.drawEntities(batch);
    worldRenderer.drawOverlay(batch);
    batch.end();

    // ── ui ───────────────────────────────────────────────────────────────
    uiRenderer.applyViewport();
    batch.begin();
    uiRenderer.draw(batch);
    batch.end();
  }

  @Override
  public void dispose() {
    batch.dispose();
    worldRenderer.dispose();
    uiRenderer.dispose();
    pixelTexture.dispose();
    assetManager.dispose();
  }

  @Override
  public void resize(int width, int height) {
    worldRenderer.resize(width, height);
    uiRenderer.resize(width, height);
  }

  private void handleInput(float delta) {}

  private void updateWorld(float delta) {
    worldRenderer.update(delta);
  }

  private void updateUi(float delta) {}
}
