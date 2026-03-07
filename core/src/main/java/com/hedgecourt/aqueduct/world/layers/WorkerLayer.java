package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.Pathfinder;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Worker;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class WorkerLayer extends WorldLayer {

  private final List<Worker> workers = new ArrayList<>();

  public void addWorker(Worker worker) {
    workers.add(worker);
  }

  public List<Worker> getWorkers() {
    return workers;
  }

  public void commandSelectedMoveTo(float x, float y, Pathfinder pathfinder) {
    for (Worker worker : workers) {
      if (worker.isSelected()) {
        worker.commandMoveTo(x, y, pathfinder);
      }
    }
  }

  public void commandAllMoveTo(float x, float y, Pathfinder pathfinder) {
    for (Worker worker : workers) {
      worker.commandMoveTo(x, y, pathfinder);
    }
  }

  public boolean handleLeftClick(float worldX, float worldY) {
    Worker clicked = null;
    for (Worker worker : workers) {
      if (worker.containsPoint(worldX, worldY)) {
        clicked = worker;
        break;
      }
    }
    // deselect all first
    for (Worker worker : workers) {
      worker.deselect();
    }
    if (clicked != null) {
      clicked.select();
      return true; // click was consumed by a worker
    }
    return false; // click hit background
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
  }

  @Override
  public void drawUnderlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Worker worker : workers) {
      if (worker.isSelected()) {
        float x = worker.getPosition().x;
        float y = worker.getPosition().y;
        float radius = C.ENTITY_RENDER_SIZE * 0.75f;
        shapeDrawer.setColor(Color.WHITE);
        shapeDrawer.circle(x, y, radius, 2f);
      }
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
}
