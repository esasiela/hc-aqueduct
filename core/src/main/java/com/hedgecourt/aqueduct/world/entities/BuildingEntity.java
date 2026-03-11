package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldEntity;

public abstract class BuildingEntity extends WorldEntity {

  protected String buildingType;
  protected String displayName;

  protected float constructionUnitsRequired;
  protected float constructionUnitsCompleted;
  protected boolean constructionLocationValid;

  protected float waterCapacity;
  protected float waterInventory;
  protected float waterCost;
  protected float waterIntakeRate;

  protected int widthTiles;
  protected int heightTiles;

  public BuildingEntity(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  public BuildingEntity freshCopy() {
    return world.getBuildingFactory().create(buildingType, 0f, 0f);
  }

  // ── construction ───────────────────────────────────────────────────────────

  public void addConstructionUnits(float amount) {
    boolean wasNotStarted = !this.isConstructionStarted();
    boolean wasNotComplete = !this.isConstructionComplete();

    constructionUnitsCompleted += amount;

    if (wasNotStarted && isConstructionStarted()) {
      // we broke ground!
      world.updateWalkabilityForEntity(this, false);
    } else if (wasNotComplete && isConstructionComplete()) {
      // grand opening! entity is already in world, nothing to do here.
      // TODO trigger completion effects, sounds, notifications
    }
  }

  public float getConstructionPct() {
    return (constructionUnitsRequired == 0)
        ? 1f
        : constructionUnitsCompleted / constructionUnitsRequired;
  }

  public boolean isConstructionComplete() {
    return constructionUnitsCompleted >= constructionUnitsRequired;
  }

  public boolean isConstructionStarted() {
    return constructionUnitsCompleted > 0f;
  }

  public boolean isConstructionLocationValid() {
    return constructionLocationValid;
  }

  public void setConstructionLocationValid(boolean constructionLocationValid) {
    this.constructionLocationValid = constructionLocationValid;
  }

  public String getBuildingType() {
    return buildingType;
  }

  public void setBuildingType(String buildingType) {
    this.buildingType = buildingType;
  }

  public float getConstructionUnitsRequired() {
    return constructionUnitsRequired;
  }

  public void setConstructionUnitsRequired(float constructionUnitsRequired) {
    this.constructionUnitsRequired = constructionUnitsRequired;
  }
}
