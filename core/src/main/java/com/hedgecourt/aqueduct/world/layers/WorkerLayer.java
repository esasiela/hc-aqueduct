package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.Pathfinder;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Worker;
import java.util.ArrayList;
import java.util.List;
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
}
