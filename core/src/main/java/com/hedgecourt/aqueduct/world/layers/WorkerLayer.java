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
import com.hedgecourt.aqueduct.world.Pathfinder;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import com.hedgecourt.aqueduct.world.entities.Worker;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class WorkerLayer extends WorldLayer {

  private static final Color SELECTION_RING_COLOR = Color.WHITE;
  private static final Color SELECTION_BOX_INDICATOR_COLOR = new Color(1f, 1f, 1f, 0.4f);
  private static final Color BAG_BAR_CAPACITY_COLOR = new Color(0.5f, 0.5f, 0.5f, 1.0f);
  private static final Color BAG_BAR_CARRY_COLOR = Color.BLUE;

  private final GlyphLayout glyphLayout;
  private final BitmapFont workerPlanFont;

  private final List<Worker> workers = new ArrayList<>();
  private Rectangle activeSelBox = null;

  public WorkerLayer(FontManager fontManager) {
    this.glyphLayout = fontManager.getGlyphLayout();
    this.workerPlanFont = fontManager.getFont(FontType.WORKER_PLAN_OVERLAY);
  }

  public void addWorker(Worker worker) {
    workers.add(worker);
  }

  public List<Worker> getWorkers() {
    return workers;
  }

  public void commandSelectedHarvest(Node node, EntityLayer entityLayer, Pathfinder pathfinder) {
    for (Worker worker : workers) {
      if (worker.isSelected()) {
        TownHall nearest =
            entityLayer.getNearestTownHall(worker.getPosition().x, worker.getPosition().y);
        worker.commandHarvest(node, nearest, pathfinder);
      }
    }
  }

  public void commandSelectedMoveTo(Vector2 target, Pathfinder pathfinder) {
    for (Worker worker : workers) {
      if (worker.isSelected()) {
        worker.commandMoveTo(target, pathfinder);
      }
    }
  }

  public void commandAllMoveTo(Vector2 target, Pathfinder pathfinder) {
    for (Worker worker : workers) {
      worker.commandMoveTo(target, pathfinder);
    }
  }

  public boolean handleLeftClick(float worldX, float worldY, boolean shift) {
    Worker clicked = null;
    for (Worker worker : workers) {
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
    for (Worker worker : workers) {
      if (selRect.contains(worker.getPosition())) {
        worker.select();
      }
    }
  }

  public boolean hasSelection() {
    for (Worker worker : workers) {
      if (worker.isSelected()) return true;
    }
    return false;
  }

  @Override
  public void update(float delta) {
    for (Worker worker : workers) {
      worker.update(delta);
    }
    applySeparation(delta);
  }

  private void applySeparation(float delta) {
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
    for (Worker worker : workers) {
      /* ****
       * Selection Decoration
       */
      if (worker.isSelected()) {
        shapeDrawer.setColor(SELECTION_RING_COLOR);
        shapeDrawer.circle(
            worker.getPosition().x, worker.getPosition().y, C.ENTITY_RENDER_SIZE * 0.6f, 2f);
      }
      /* ****
       * Selection-Box Decoration
       */
      if (activeSelBox != null && activeSelBox.contains(worker.getPosition())) {
        shapeDrawer.setColor(SELECTION_BOX_INDICATOR_COLOR);
        shapeDrawer.circle(
            worker.getPosition().x, worker.getPosition().y, C.ENTITY_RENDER_SIZE * 0.45f, 1.5f);
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
          BAG_BAR_CAPACITY_COLOR);
      shapeDrawer.filledRectangle(
          worker.getPosition().x - worker.getWidth() / 2f,
          worker.getPosition().y + worker.getHeight() / 2f + barBarYOffset,
          worker.getWidth() * (worker.getCarrying() / worker.getCarryCapacity()),
          bagBarHeight,
          BAG_BAR_CARRY_COLOR);
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
    for (Worker worker : workers) {
      TextureRegion frame = worker.getCurrentFrame();
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
    for (Worker worker : workers) {
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
    for (Worker worker : workers) {
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
    for (Worker worker : workers) {
      if (worker.getClass() == type && viewport.contains(worker.getPosition())) {
        worker.select();
      } else {
        worker.deselect();
      }
    }
  }

  public void deselectAll() {
    for (Worker worker : workers) {
      worker.deselect();
    }
  }

  public void setActiveSelBox(Rectangle rect) {
    this.activeSelBox = rect;
  }
}
