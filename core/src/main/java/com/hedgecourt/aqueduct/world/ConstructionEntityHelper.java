package com.hedgecourt.aqueduct.world;

public class ConstructionEntityHelper {
  private final WorldEntity entity;
  private boolean validLocation = false;
  private final float constructionUnitsRequired;
  private float constructionUnitsCompleted = 0f;

  public ConstructionEntityHelper(WorldEntity entity, float constructionUnitsRequired) {
    this.entity = entity;
    this.constructionUnitsRequired = constructionUnitsRequired;
  }

  public boolean isComplete() {
    return constructionUnitsCompleted >= constructionUnitsRequired;
  }

  public WorldEntity getEntity() {
    return entity;
  }

  public boolean isValidLocation() {
    return validLocation;
  }

  public void setValidLocation(boolean validLocation) {
    this.validLocation = validLocation;
  }

  public void addConstructionUnits(float amount) {
    constructionUnitsCompleted += amount;
  }

  public float getConstructionPct() {
    return (constructionUnitsRequired == 0)
        ? 1f
        : constructionUnitsCompleted / constructionUnitsRequired;
  }
}
