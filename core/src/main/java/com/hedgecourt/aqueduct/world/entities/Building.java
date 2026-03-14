package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.world.AqueductWorld;

public abstract class Building extends Entity {

  protected boolean walkable;

  protected float constructionUnitsRequired;
  protected float constructionUnitsCompleted;
  protected boolean constructionLocationValid;

  protected float waterCapacity;
  protected float waterInventory;
  protected float waterCost;
  protected float waterOutputRate;
  protected boolean waterConnected = false;

  public Building(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  @Override
  public void update(float delta) {
    // gotta pay the piper
    waterInventory = Math.max(waterInventory - waterCost * delta, 0f);
  }

  public Building freshCopy() {
    return world.getBuildingFactory().create(type, 0f, 0f);
  }

  public boolean isActive() {
    // TODO do we really need isWaterConected for a building to be active? or just waterInventory>0?
    return isConstructionComplete() && isWaterConnected() && waterInventory > 0;
  }

  // ── construction ───────────────────────────────────────────────────────────

  public void addConstructionUnits(float amount) {
    boolean wasNotStarted = !this.isConstructionStarted();
    boolean wasNotComplete = !this.isConstructionComplete();

    constructionUnitsCompleted += amount;

    if (wasNotStarted && isConstructionStarted()) {
      // we broke ground!
      world.updateWalkabilityForEntity(this, walkable);
    } else if (wasNotComplete && isConstructionComplete()) {
      // grand opening! entity is already in world, nothing to do here.
      world.recomputeWaterNetwork();
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

  public float getConstructionUnitsRequired() {
    return constructionUnitsRequired;
  }

  public void setConstructionUnitsRequired(float constructionUnitsRequired) {
    this.constructionUnitsRequired = constructionUnitsRequired;
  }

  public boolean isWalkable() {
    return walkable;
  }

  public void setWalkable(boolean walkable) {
    this.walkable = walkable;
  }

  public float getConstructionUnitsCompleted() {
    return constructionUnitsCompleted;
  }

  public void setConstructionUnitsCompleted(float constructionUnitsCompleted) {
    this.constructionUnitsCompleted = constructionUnitsCompleted;
  }

  public float getWaterInventoryPct() {
    return (waterCapacity == 0) ? 1f : waterInventory / waterCapacity;
  }

  public float getWaterCapacity() {
    return waterCapacity;
  }

  public void setWaterCapacity(float waterCapacity) {
    this.waterCapacity = waterCapacity;
  }

  public float getWaterInventory() {
    return waterInventory;
  }

  public void setWaterInventory(float waterInventory) {
    this.waterInventory = waterInventory;
  }

  public float getWaterCost() {
    return waterCost;
  }

  public void setWaterCost(float waterCost) {
    this.waterCost = waterCost;
  }

  public float getWaterOutputRate() {
    return waterOutputRate;
  }

  public void setWaterOutputRate(float waterOutputRate) {
    this.waterOutputRate = waterOutputRate;
  }

  public boolean isWaterConnected() {
    return waterConnected;
  }

  public void setWaterConnected(boolean waterConnected) {
    this.waterConnected = waterConnected;
  }
}
