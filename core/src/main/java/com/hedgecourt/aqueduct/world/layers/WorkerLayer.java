package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.FontManager;
import com.hedgecourt.aqueduct.FontManager.FontType;
import com.hedgecourt.aqueduct.WorldRenderer;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldEntity;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import com.hedgecourt.aqueduct.world.entities.Worker;
import java.util.List;
import java.util.Queue;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class WorkerLayer extends WorldLayer {

  private static final Color SELECTION_BOX_INDICATOR_COLOR = new Color(1f, 1f, 1f, 0.4f);

  private final GlyphLayout glyphLayout;
  private final BitmapFont workerPlanFont;

  private Rectangle activeSelBox = null;

  private final AqueductWorld world;

  public WorkerLayer(AqueductWorld world, FontManager fontManager) {
    this.world = world;
    this.glyphLayout = fontManager.getGlyphLayout();
    this.workerPlanFont = fontManager.getFont(FontType.WORKER_PLAN_OVERLAY);
  }

  public void commandSelectedHarvest(Node node) {
    for (Worker worker : world.getEntities(Worker.class)) {
      if (worker.isSelected()) {
        TownHall nearest = world.getNearestTownHall(worker);
        worker.commandHarvest(node, nearest);
      }
    }
  }

  public void commandSelectedDeliver(TownHall townHall) {
    for (Worker worker : world.getEntities(Worker.class)) {
      if (worker.isSelected()) {
        worker.commandDeliver(townHall);
      }
    }
  }

  public void commandSelectedConstruct(BuildingEntity building) {
    for (Worker worker : world.getEntities(Worker.class)) {
      if (worker.isSelected()) worker.commandConstruct(building);
    }
  }

  public void commandSelectedMoveTo(Vector2 target) {
    for (Worker worker : world.getEntities(Worker.class)) {
      if (worker.isSelected()) {
        worker.commandMoveTo(target);
      }
    }
  }

  public boolean handleLeftClick(float worldX, float worldY, boolean shift) {
    Worker clicked = null;
    for (Worker worker : world.getEntities(Worker.class)) {
      if (worker.containsPoint(worldX, worldY)) {
        clicked = worker;
        break;
      }
    }

    if (shift) {
      if (clicked != null) {
        // toggle clicked worker
        if (clicked.isSelected()) clicked.deselect();
        else clicked.select();
        return true;
      }
      // shift+click background - do nothing
      return false;
    }

    // regular click - deselect all, select clicked
    deselectAll();
    if (clicked != null) {
      clicked.select();
      return true;
    }
    return false;
  }

  public void handleBoxSelect(Rectangle selRect, boolean shift) {
    if (!shift) deselectAll();
    for (Worker worker : world.getEntities(Worker.class)) {
      if (selRect.contains(worker.getPosition())) {
        worker.select();
      }
    }
  }

  public boolean hasSelection() {
    for (Worker worker : world.getEntities(Worker.class)) {
      if (worker.isSelected()) return true;
    }
    return false;
  }

  public void applySeparation(float delta) {
    List<Worker> workers = world.getEntities(Worker.class);

    for (int i = 0; i < workers.size(); i++) {
      for (int j = i + 1; j < workers.size(); j++) {
        Worker a = workers.get(i);
        Worker b = workers.get(j);

        float dx = a.getPosition().x - b.getPosition().x;
        float dy = a.getPosition().y - b.getPosition().y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < C.WORKER_SEPARATION_RADIUS && dist > 0.01f) {
          float force = (C.WORKER_SEPARATION_RADIUS - dist) / C.WORKER_SEPARATION_RADIUS;
          float nx = dx / dist;
          float ny = dy / dist;
          float push = force * C.WORKER_SEPARATION_STRENGTH * delta;

          a.getPosition().x += nx * push;
          a.getPosition().y += ny * push;
          b.getPosition().x -= nx * push;
          b.getPosition().y -= ny * push;
        }
      }
    }
  }

  @Override
  public void drawUnderlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Worker worker : world.getEntities(Worker.class)) {
      /* ****
       * Selection Decoration
       */
      if (worker.isSelected()) {
        shapeDrawer.setColor(C.WORKER_SELECTION_RING_COLOR);
        shapeDrawer.circle(
            worker.getPosition().x, worker.getPosition().y, worker.getWidth() * 0.6f, 2f);

        WorldEntity target = worker.getPlan().getTargetEntity();
        if (target == null) continue;
        Rectangle bounds = target.getBounds();
        float buf = C.WORKER_INTERACTION_RANGE;
        shapeDrawer.filledRectangle(
            bounds.x - buf,
            bounds.y - buf,
            bounds.width + buf * 2,
            bounds.height + buf * 2,
            new Color(0f, 0f, 1f, 0.3f));
      }
      /* ****
       * Hover Decoration
       */
      if (worker.isHovered()) {
        shapeDrawer.setColor(C.WORKER_HOVER_RING_COLOR);
        shapeDrawer.circle(
            worker.getPosition().x, worker.getPosition().y, worker.getWidth() * 0.48f, 2f);
      }
      /* ****
       * Selection-Box Decoration
       */
      if (activeSelBox != null && activeSelBox.contains(worker.getPosition())) {
        shapeDrawer.setColor(SELECTION_BOX_INDICATOR_COLOR);
        shapeDrawer.circle(
            worker.getPosition().x, worker.getPosition().y, worker.getWidth() * 0.45f, 1.5f);
      }
      /* ****
       * Bag Bar
       */
      float bagBarHeight = 4f;
      float barBarYOffset = 4f;
      shapeDrawer.filledRectangle(
          worker.getPosition().x - worker.getWidth() / 2f,
          worker.getPosition().y + worker.getHeight() / 2f + barBarYOffset,
          worker.getWidth(),
          bagBarHeight,
          C.BAG_BAR_CAPACITY_COLOR);
      shapeDrawer.filledRectangle(
          worker.getPosition().x - worker.getWidth() / 2f,
          worker.getPosition().y + worker.getHeight() / 2f + barBarYOffset,
          worker.getWidth() * (worker.getCarrying() / worker.getCarryCapacity()),
          bagBarHeight,
          C.BAG_BAR_CARRY_COLOR);
      /* ****
       * Plan/State Text
       */
      String workerPlanText =
          worker.getPlan().getPlanType().name() + "/" + worker.getState().name();
      glyphLayout.setText(workerPlanFont, workerPlanText);
      workerPlanFont.draw(
          batch,
          workerPlanText,
          worker.getPosition().x - glyphLayout.width / 2f,
          worker.getPosition().y - (worker.getHeight() / 2f + 1f));
    }
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Worker worker : world.getEntities(Worker.class)) {
      TextureRegion frame = worker.getCurrentAnimationFrame();
      if (frame == null) continue;
      float renderSize = C.ENTITY_RENDER_SIZE;
      batch.draw(
          frame,
          worker.getPosition().x - renderSize / 2f,
          worker.getPosition().y - renderSize / 2f,
          renderSize,
          renderSize);
    }
  }

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Worker worker : world.getEntities(Worker.class)) {
      if (!worker.isSelected()) continue;
      if (worker.getState() != Worker.WorkerState.MOVING) continue;

      Queue<Vector2> waypoints = worker.getWaypoints();
      if (waypoints.isEmpty()) continue;

      // ── path preview ──────────────────────────────────────────────────
      shapeDrawer.setColor(Color.YELLOW);
      Vector2 prev = worker.getPosition();
      for (Vector2 waypoint : waypoints) {
        shapeDrawer.line(prev.x, prev.y, waypoint.x, waypoint.y, 1.5f);
        prev = waypoint;
      }

      // ── destination marker ────────────────────────────────────────────
      Vector2 dest = null;
      for (Vector2 waypoint : waypoints) {
        dest = waypoint;
      }
      if (dest != null) {
        shapeDrawer.setColor(Color.GREEN);
        float s = 6f;
        shapeDrawer.line(dest.x - s, dest.y - s, dest.x + s, dest.y + s, 2f);
        shapeDrawer.line(dest.x + s, dest.y - s, dest.x - s, dest.y + s, 2f);
      }
    }
  }

  public void handleDoubleClick(float worldX, float worldY, WorldRenderer worldRenderer) {
    // find clicked worker's type, select all visible of same type
    Worker clicked = null;
    for (Worker worker : world.getEntities(Worker.class)) {
      if (worker.containsPoint(worldX, worldY)) {
        clicked = worker;
        break;
      }
    }
    if (clicked == null) {
      // double-click on background, deselect all
      deselectAll();
      return;
    }

    // select all visible workers of same type
    // for now all workers are the same type, filter by class
    Class<?> type = clicked.getClass();
    Rectangle viewport = worldRenderer.getVisibleWorldRect();
    for (Worker worker : world.getEntities(Worker.class)) {
      if (worker.getClass() == type && viewport.contains(worker.getPosition())) {
        worker.select();
      } else {
        worker.deselect();
      }
    }
  }

  public void deselectAll() {
    for (WorldEntity worldEntity : world.getEntities()) {
      worldEntity.deselect();
    }
  }

  public void setActiveSelBox(Rectangle rect) {
    this.activeSelBox = rect;
  }
}
