package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.Gdx;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import java.util.ArrayDeque;
import java.util.Deque;

public class TownHall extends Building {

  private final Deque<Unit> trainingQueue = new ArrayDeque<>();

  public TownHall(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  public void deposit(String resourceType, float amount) {
    if (resourceType.equalsIgnoreCase("water")) waterInventory += amount;
  }

  @Override
  public void update(float delta) {
    super.update(delta);

    updateTrainingQueue(delta);
  }

  public void addToTrainingQueue(Unit unit) {
    // TODO filter allowed types that this building can train
    this.trainingQueue.add(unit);
  }

  private void updateTrainingQueue(float delta) {
    if (trainingQueue.isEmpty()) return;

    Unit unit = trainingQueue.peek();
    if (unit instanceof Worker worker) {
      worker.addTrainingPoints(C.WORKER_TRAINING_RATE * delta);

      if (worker.isTrainingComplete()) {
        // Happy Birthday!
        // TODO better rally point for townhall
        trainingQueue.poll();

        float spawnX = position.x - width;
        float spawnY = position.y - height;

        // random jitter to allow applySeparation to work its magic
        worker.setPosition(
            (float) (spawnX + (Math.random() - 0.5f) * 2f),
            (float) (spawnY + (Math.random() - 0.5f) * 2f));

        world.add(worker);
      }
    } else {
      // unsupported training type.
      Gdx.app.log("TOWNHALL", "cannot train unsupported unit type: " + unit.getType());
      trainingQueue.poll();
    }
  }

  public Deque<Unit> getTrainingQueue() {
    return trainingQueue;
  }
}
