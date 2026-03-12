package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.world.AqueductWorld;

public abstract class Unit extends Entity {

  protected String unitType;
  protected String displayName;

  // TODO dont hardcode training points required
  protected float trainingPointsRequired = 100f;
  protected float trainingPointsCompleted;

  public Unit(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  public void addTrainingPoints(float amount) {
    if (isTrainingComplete()) return;

    trainingPointsCompleted += amount;
  }

  public boolean isTrainingComplete() {
    return trainingPointsCompleted >= trainingPointsRequired;
  }

  public boolean isTrainingStarted() {
    return trainingPointsCompleted > 0f;
  }

  public float getTrainingPct() {
    return (trainingPointsRequired == 0f) ? 1f : trainingPointsCompleted / trainingPointsRequired;
  }

  public float getTrainingPointsRequired() {
    return trainingPointsRequired;
  }

  public float getTrainingPointsCompleted() {
    return trainingPointsCompleted;
  }

  public String getUnitType() {
    return unitType;
  }

  public String getDisplayName() {
    return displayName;
  }
}
