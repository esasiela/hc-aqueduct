package com.hedgecourt.aqueduct.world.layers;

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

  public void commandAllMoveTo(float x, float y, Pathfinder pathfinder) {
    for (Worker worker : workers) {
      worker.commandMoveTo(x, y, pathfinder);
    }
  }

  @Override
  public void update(float delta) {
    for (Worker worker : workers) {
      worker.update(delta);
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
