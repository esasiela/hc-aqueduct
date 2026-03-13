package com.hedgecourt.aqueduct;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
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
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.entities.Building;
import com.hedgecourt.aqueduct.world.entities.Entity;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import com.hedgecourt.aqueduct.world.entities.Unit;
import com.hedgecourt.aqueduct.world.entities.Worker;
import com.hedgecourt.aqueduct.world.layers.WorkerLayer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class AqueductMain extends ApplicationAdapter {

  private AqueductWorld world;
  private AqueductLoader loader;

  private SpriteBatch batch;
  private ShapeDrawer shapeDrawer;
  private Texture pixelTexture;

  private FontManager fontManager;
  private AssetManager assetManager;

  private WorldRenderer worldRenderer;
  private WorkerLayer workerLayer;

  private UiRenderer uiRenderer;

  private WorldInputMode worldInputMode = WorldInputMode.NORMAL;

  private Building constructionPlacementEntity;

  // private boolean selectDragging = false;
  private final Vector2 selectDragStart = new Vector2();
  private final Vector2 selectDragCurrent = new Vector2();

  private boolean paused = false;

  @Override
  public void create() {
    world = new AqueductWorld();
    assetManager = new AssetManager();
    loader = new AqueductLoader(world, assetManager);
    loader.load("resources.json", "buildings.json", "units.json", "maps/test2.tmx");

    batch = new SpriteBatch();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();
    pixelTexture = new Texture(pixmap);
    pixmap.dispose();
    shapeDrawer = new ShapeDrawer(batch, new TextureRegion(pixelTexture));

    fontManager = new FontManager();
    fontManager.load();

    worldRenderer =
        new WorldRenderer(
            batch, shapeDrawer, fontManager, world, () -> constructionPlacementEntity);
    // TODO figure out how to get selection box logic out of workerLayer
    workerLayer = worldRenderer.getWorkerLayer();

    uiRenderer =
        new UiRenderer(
            world,
            batch,
            shapeDrawer,
            fontManager,
            worldRenderer,
            buildingType -> {
              constructionPlacementEntity = world.getBuildingFactory().create(buildingType, 0, 0);
              worldInputMode = WorldInputMode.CONSTRUCTION_PLACEMENT;
            },
            unitType -> {
              TownHall th = world.getSelectedBuilding(TownHall.class);
              if (th != null) th.addToTrainingQueue(world.getUnitFactory().create(unitType, 0, 0));
            });

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
              /* ****
               * Ui touch down (LEFT)
               */
              Vector2 uiPos = uiRenderer.mouseInUi();
              uiRenderer.handleTouchDown(uiPos.x, uiPos.y);
              /* ****
               * World touch down (LEFT)
               */
              switch (worldInputMode) {
                case NORMAL:
                  if (!uiRenderer.isMinimapDragging()) {
                    Vector2 worldPos = worldRenderer.mouseInWorld();
                    selectDragStart.set(worldPos);
                    selectDragCurrent.set(worldPos);
                  }
                  break;
                case SELECT_BOX:
                  break;
              }
            }
            return false;
          }

          @Override
          public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            boolean shiftIsPressed =
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                    || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

            if (button == Input.Buttons.LEFT) {
              /* ****
               * Ui touch up (LEFT)
               */
              Vector2 uiPos = uiRenderer.mouseInUi();
              if (uiRenderer.handleClick(uiPos.x, uiPos.y)) return true;
              /* ****
               * World touch up (LEFT)
               */
              switch (worldInputMode) {
                case NORMAL:
                  Vector2 worldPos = worldRenderer.mouseInWorld();
                  return handleLeftClickNormal(worldPos, shiftIsPressed);
                case SELECT_BOX:
                  Rectangle selRect = buildSelRect(selectDragStart, selectDragCurrent);
                  clearSelectionBox();
                  worldInputMode = WorldInputMode.NORMAL;
                  workerLayer.handleBoxSelect(selRect, shiftIsPressed);
                  return true;
                case CONSTRUCTION_PLACEMENT:
                  snapConstructionCursorToMouseTile();
                  if (constructionPlacementEntity.isConstructionLocationValid()) {
                    world.add(constructionPlacementEntity);
                    constructionPlacementEntity = constructionPlacementEntity.freshCopy();
                  }
                  return true;
              }
            }

            if (button == Input.Buttons.RIGHT) {
              /* ****
               * World touch up (RIGHT)
               */
              Vector2 worldPos = worldRenderer.mouseInWorld();
              switch (worldInputMode) {
                case NORMAL:
                  if (workerLayer.hasSelection()) {
                    Entity clickedEntity = world.getEntityAt(worldPos.x, worldPos.y);

                    if (clickedEntity == null) {
                      workerLayer.commandSelectedMoveTo(worldPos);
                      return true;
                    }

                    if (clickedEntity instanceof Node node) {
                      workerLayer.commandSelectedHarvest(node);
                      return true;
                    }

                    Building clickedIncompleteBuilding =
                        world.getIncompleteBuildingAt(worldPos.x, worldPos.y);
                    if (clickedIncompleteBuilding != null) {
                      workerLayer.commandSelectedConstruct(clickedIncompleteBuilding);
                      return true;
                    }

                    if (clickedEntity instanceof TownHall townHall) {
                      workerLayer.commandSelectedDeliver(townHall);
                      return true;
                    }
                  } else {
                    // no workers selected
                    Building pendingBuilding =
                        world.getConstructionPendingAt(worldPos.x, worldPos.y);
                    if (pendingBuilding != null && !pendingBuilding.isConstructionStarted()) {
                      world.remove(pendingBuilding);
                      return true;
                    }
                  }
                case SELECT_BOX:
                  clearSelectionBox();
                  worldInputMode = WorldInputMode.NORMAL;
                  return true;
                case CONSTRUCTION_PLACEMENT:
                  clearConstructionPlacement();
                  worldInputMode = WorldInputMode.NORMAL;
                  return true;
              }
            }
            return false;
          }

          @Override
          public boolean touchDragged(int screenX, int screenY, int pointer) {
            /* ****
             * Ui mouse drag
             */
            Vector2 uiPos = uiRenderer.mouseInUi();
            if (uiRenderer.isMinimapDragging()) {
              uiRenderer.handleMinimapDrag(uiPos.x, uiPos.y);
              return false;
            }
            /* ****
             * World mouse drag
             */
            switch (worldInputMode) {
              case NORMAL:
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                  Vector2 worldPos = worldRenderer.mouseInWorld();
                  selectDragCurrent.set(worldPos);
                  float dist = selectDragStart.dst(selectDragCurrent);
                  if (dist > C.DRAG_THRESHOLD) {
                    worldInputMode = WorldInputMode.SELECT_BOX;
                  }
                }
                break;
              case SELECT_BOX:
                if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
                  Vector2 worldPos = worldRenderer.mouseInWorld();
                  selectDragCurrent.set(worldPos);
                }
                break;
            }
            return false;
          }

          @Override
          public boolean keyUp(int keycode) {
            if (keycode == Input.Keys.SPACE) {
              paused = !paused;
              return true;
            }

            switch (worldInputMode) {
              case NORMAL:
                TownHall selectedHall = world.getSelectedBuilding(TownHall.class);
                if (selectedHall != null) {
                  if (keycode == Keys.W) {
                    Unit trainingWorker = world.getUnitFactory().create("worker", 0, 0);
                    selectedHall.addToTrainingQueue(trainingWorker);
                    return true;
                  }
                }

                break;
              case SELECT_BOX:
                break;
              case CONSTRUCTION_PLACEMENT:
                if (keycode == Input.Keys.ESCAPE) {
                  clearConstructionPlacement();
                  worldInputMode = WorldInputMode.NORMAL;
                }
            }
            return false;
          }
        });

    Gdx.input.setInputProcessor(multiplexer);
  }

  private boolean handleLeftClickNormal(Vector2 worldPos, boolean shiftIsPressed) {
    // What did you click on?
    Entity clickedEntity = world.getEntityAt(worldPos.x, worldPos.y);
    if (clickedEntity == null) {
      // click is on BG, not an entity
      deselectAll();
      return true;
    }

    if (clickedEntity instanceof Building building) {
      // building selection is single-select
      deselectAll();
      building.select();
      return true;
    }

    if (clickedEntity instanceof Worker) {
      workerLayer.handleLeftClick(worldPos.x, worldPos.y, shiftIsPressed);
      return true;
    }

    return false;
  }

  private void deselectAll() {
    for (Entity entity : world.getEntities()) {
      entity.deselect();
    }
  }

  private Rectangle buildSelRect(Vector2 a, Vector2 b) {
    float x = Math.min(a.x, b.x);
    float y = Math.min(a.y, b.y);
    float w = Math.abs(a.x - b.x);
    float h = Math.abs(a.y - b.y);
    return new Rectangle(x, y, w, h);
  }

  private void clearSelectionBox() {
    worldRenderer.clearSelectionBox();
  }

  private void clearConstructionPlacement() {
    constructionPlacementEntity = null;
  }

  @Override
  public void render() {
    float delta = Gdx.graphics.getDeltaTime();

    updateWorld(delta);
    updateUi(delta);

    ScreenUtils.clear(C.CLEAR_R, C.CLEAR_G, C.CLEAR_B, 1f);

    worldRenderer.render();

    updateWorldInput(delta);

    drawWorld();
    drawUi();
  }

  private void updateWorld(float delta) {
    if (!paused) {
      world.update(delta);
      workerLayer.applySeparation(delta);
      worldRenderer.updateSprites(delta);
    }

    worldRenderer.updateCamera(delta);

    worldRenderer.preDraw(delta);
  }

  private void updateUi(float delta) {}

  private void updateWorldInput(float delta) {
    if (worldInputMode == WorldInputMode.SELECT_BOX) {
      Rectangle selRect = buildSelRect(selectDragStart, selectDragCurrent);
      workerLayer.setActiveSelBox(selRect);
      worldRenderer.setSelectionBox(true, selectDragStart, selectDragCurrent);
    } else {
      workerLayer.setActiveSelBox(null);
      worldRenderer.clearSelectionBox();
    }

    if (worldInputMode == WorldInputMode.CONSTRUCTION_PLACEMENT) {
      snapConstructionCursorToMouseTile();
    }
  }

  private void snapConstructionCursorToMouseTile() {
    if (constructionPlacementEntity == null) return;

    Vector2 mouseWorldPos = worldRenderer.mouseInWorld();

    int tileW = world.getTileWidth();
    int tileH = world.getTileHeight();

    int tileX = (int) (mouseWorldPos.x / tileW);
    int tileY = (int) (mouseWorldPos.y / tileH);

    // snap lower-left to tile corner
    float snappedX = tileX * tileW + constructionPlacementEntity.getWidth() / 2f;
    float snappedY = tileY * tileH + constructionPlacementEntity.getHeight() / 2f;

    constructionPlacementEntity.setPosition(snappedX, snappedY);

    // to be valid, must pass pathfinder walkability check AND no existing constructionPending
    boolean isWalkable =
        world.getPathfinder().validateConstructionPlacementLocation(constructionPlacementEntity);

    boolean isClearOfConstruction = true;
    for (Building pendingBuilding : world.getConstructionPendingList()) {
      if (pendingBuilding.getBounds().overlaps(constructionPlacementEntity.getBounds())) {
        // TODO fancy [][] of tiles to know what tile is not clear of construction
        isClearOfConstruction = false;
        break;
      }
    }
    constructionPlacementEntity.setConstructionLocationValid(isWalkable && isClearOfConstruction);
  }

  private void drawWorld() {
    worldRenderer.applyViewport();
    batch.begin();
    worldRenderer.drawUnderlay(batch);
    worldRenderer.drawEntities(batch);
    worldRenderer.drawOverlay(batch);
    worldRenderer.drawSelectionBox(batch);
    batch.end();
  }

  private void drawUi() {
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

  private enum WorldInputMode {
    NORMAL,
    SELECT_BOX,
    CONSTRUCTION_PLACEMENT
  }
}
