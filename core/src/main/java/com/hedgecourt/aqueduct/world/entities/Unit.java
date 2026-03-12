package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.world.AqueductWorld;

public abstract class Unit extends Entity {

  protected String unitType;
  protected String displayName;

  protected float trainingUnitsRequired;
  protected float trainingUnitsCompleted;

  public Unit(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  public void addTrainingUnits(float amount) {
    if (isTrainingComplete()) return;

    trainingUnitsCompleted += amount;
  }

  public boolean isTrainingComplete() {
    return trainingUnitsCompleted >= trainingUnitsRequired;
  }

  public boolean isTrainingStarted() {
    return trainingUnitsCompleted > 0f;
  }

  public float getTrainingPct() {
    return (trainingUnitsRequired == 0f) ? 1f : trainingUnitsCompleted / trainingUnitsRequired;
  }

  public String getUnitType() {
    return unitType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public float getTrainingUnitsRequired() {
    return trainingUnitsRequired;
  }

  public float getTrainingUnitsCompleted() {
    return trainingUnitsCompleted;
  }
}
