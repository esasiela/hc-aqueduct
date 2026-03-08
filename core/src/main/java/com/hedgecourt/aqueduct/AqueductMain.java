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
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.Worker;
import com.hedgecourt.aqueduct.world.layers.WorkerLayer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class AqueductMain extends ApplicationAdapter {

  private SpriteBatch batch;
  private ShapeDrawer shapeDrawer;
  private Texture pixelTexture;

  private FontManager fontManager;

  private AssetManager assetManager;
  private WorkerLayer workerLayer;

  private WorldRenderer worldRenderer;
  private UiRenderer uiRenderer;

  private boolean selectDragging = false;
  private Vector2 selectDragStart = new Vector2();
  private Vector2 selectDragCurrent = new Vector2();

  private boolean paused = false;

  @Override
  public void create() {
    batch = new SpriteBatch();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();
    pixelTexture = new Texture(pixmap);
    pixmap.dispose();
    shapeDrawer = new ShapeDrawer(batch, new TextureRegion(pixelTexture));

    fontManager = new FontManager();
    fontManager.load();

    worldRenderer = new WorldRenderer(batch, shapeDrawer);
    uiRenderer = new UiRenderer(batch, shapeDrawer);

    worldRenderer.loadMap("maps/test2.tmx");

    /* ****
     * Asset Manager
     */
    String workerSpritePath1 = "characters/pipoya/Animal/Dog-01-3r.png";
    String workerSpritePath2 = "characters/pipoya/Animal/Cat-01-2r.png";
    assetManager = new AssetManager();
    assetManager.load(workerSpritePath1, Texture.class);
    assetManager.load(workerSpritePath2, Texture.class);
    assetManager.finishLoading();

    Texture workerTexture1 = assetManager.get(workerSpritePath1, Texture.class);
    TextureRegion[][] grid1 = TextureRegion.split(workerTexture1, 32, 32);

    Texture workerTexture2 = assetManager.get(workerSpritePath2, Texture.class);
    TextureRegion[][] grid2 = TextureRegion.split(workerTexture2, 32, 32);

    workerLayer = new WorkerLayer(fontManager);

    Worker worker1 =
        new Worker(worldRenderer.getMapWidth() / 2f, worldRenderer.getMapHeight() / 2f);
    worker1.buildSprites(grid1);
    workerLayer.addWorker(worker1);

    Worker worker2 =
        new Worker(worldRenderer.getMapWidth() / 2f + 64f, worldRenderer.getMapHeight() / 2f);
    worker2.buildSprites(grid2);
    workerLayer.addWorker(worker2);

    worldRenderer.addLayer(workerLayer);

    uiRenderer.setupMinimap(worldRenderer, workerLayer);

    /* ****
     * Input Multiplexer
     */

    InputMultiplexer multiplexer = new InputMultiplexer();

    GestureDetector gestureDetector =
        new GestureDetector(
            new GestureDetector.GestureAdapter() {
              @Override
              public boolean tap(float x, float y, int count, int button) {
                if (count == 2 && button == Input.Buttons.LEFT) {
                  Vector2 worldPos = worldRenderer.mouseInWorld();
                  workerLayer.handleDoubleClick(worldPos.x, worldPos.y, worldRenderer);
                  return true;
                }
                return false;
              }
            });
    multiplexer.addProcessor(gestureDetector);

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
          public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
              Vector2 uiPos = uiRenderer.mouseInUi();
              uiRenderer.handleTouchDown(uiPos.x, uiPos.y);
              if (!uiRenderer.isMinimapDragging()) {
                Vector2 worldPos = worldRenderer.mouseInWorld();
                selectDragStart.set(worldPos);
                selectDragCurrent.set(worldPos);
              }
            }
            return false;
          }

          @Override
          public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (button == Input.Buttons.LEFT) {
              Vector2 uiPos = uiRenderer.mouseInUi();
              boolean shift =
                  Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                      || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

              if (selectDragging) {
                // finalize selection box
                Rectangle selRect = buildSelRect(selectDragStart, selectDragCurrent);
                clearSelectionBox();
                workerLayer.handleBoxSelect(selRect, shift);
                return true;
              }

              boolean consumedByUi = uiRenderer.handleClick(uiPos.x, uiPos.y);
              if (!consumedByUi) {
                Vector2 worldPos = worldRenderer.mouseInWorld();
                workerLayer.handleLeftClick(worldPos.x, worldPos.y, shift);
              }
              return true;
            }
            if (button == Input.Buttons.RIGHT) {
              clearSelectionBox();
              if (workerLayer.hasSelection()) {
                Vector2 worldPos = worldRenderer.mouseInWorld();
                Node clickedNode = worldRenderer.getEntityLayer().getNodeAt(worldPos.x, worldPos.y);
                if (clickedNode != null) {
                  workerLayer.commandSelectedHarvest(
                      clickedNode, worldRenderer.getEntityLayer(), worldRenderer.getPathfinder());
                } else {
                  workerLayer.commandSelectedMoveTo(worldPos, worldRenderer.getPathfinder());
                }
              }
              return true;
            }
            return false;
          }

          @Override
          public boolean touchDragged(int screenX, int screenY, int pointer) {
            Vector2 uiPos = uiRenderer.mouseInUi();
            if (uiRenderer.isMinimapDragging()) {
              uiRenderer.handleMinimapDrag(uiPos.x, uiPos.y);
              return false;
            }
            if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
              Vector2 worldPos = worldRenderer.mouseInWorld();
              selectDragCurrent.set(worldPos);
              float dist = selectDragStart.dst(selectDragCurrent);
              if (dist > C.DRAG_THRESHOLD) {
                selectDragging = true;
              }
            }
            return false;
          }

          @Override
          public boolean keyUp(int keycode) {
            if (keycode == Input.Keys.SPACE) {
              paused = !paused;
              return true;
            }
            return false;
          }
        });

    Gdx.input.setInputProcessor(multiplexer);
  }

  private Rectangle buildSelRect(Vector2 a, Vector2 b) {
    float x = Math.min(a.x, b.x);
    float y = Math.min(a.y, b.y);
    float w = Math.abs(a.x - b.x);
    float h = Math.abs(a.y - b.y);
    return new Rectangle(x, y, w, h);
  }

  private void clearSelectionBox() {
    selectDragging = false;
    worldRenderer.clearSelectionBox();
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

    if (selectDragging) {
      Rectangle selRect = buildSelRect(selectDragStart, selectDragCurrent);
      workerLayer.setActiveSelBox(selRect);
      worldRenderer.setSelectionBox(true, selectDragStart, selectDragCurrent);
    } else {
      workerLayer.setActiveSelBox(null);
      worldRenderer.clearSelectionBox();
    }

    worldRenderer.applyViewport();
    batch.begin();
    worldRenderer.drawUnderlay(batch);
    worldRenderer.drawEntities(batch);
    worldRenderer.drawOverlay(batch);
    worldRenderer.drawSelectionBox(batch);
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
    fontManager.dispose();
  }

  @Override
  public void resize(int width, int height) {
    worldRenderer.resize(width, height);
    uiRenderer.resize(width, height);
  }

  private void handleInput(float delta) {}

  private void updateWorld(float delta) {
    worldRenderer.updateCamera(delta);
    if (!paused) {
      worldRenderer.updateLayers(delta);
    }
  }

  private void updateUi(float delta) {}
}
