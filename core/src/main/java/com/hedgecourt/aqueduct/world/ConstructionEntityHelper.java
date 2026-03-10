package com.hedgecourt.aqueduct.world;

import com.hedgecourt.aqueduct.world.entities.Pipe;

public class ConstructionEntityHelper {
  private final AqueductWorld world;
  private final WorldEntity entity;
  private boolean validLocation = false;
  private final float constructionUnitsRequired;
  private float constructionUnitsCompleted = 0f;

  public ConstructionEntityHelper(
      AqueductWorld world, WorldEntity entity, float constructionUnitsRequired) {
    this.world = world;
    this.entity = entity;
    this.constructionUnitsRequired = constructionUnitsRequired;
  }

  public void addConstructionUnits(float amount) {
    boolean wasNotStarted = !this.isStarted();
    boolean wasNotComplete = !this.isComplete();

    constructionUnitsCompleted += amount;

    if (wasNotStarted && isStarted()) {
      // we broke ground!
      world.updateWalkabilityForEntity(entity, false);
    } else if (wasNotComplete && isComplete()) {
      // grand opening!
      world.removeConstructionPending(this);
      world.add(entity);
    }
  }

  public ConstructionEntityHelper freshCopy() {
    // TODO replace with BuildingDef.createEntity() when BuildingDef exists
    return new ConstructionEntityHelper(
        world,
        new Pipe(
            entity.getPosition().x, entity.getPosition().y, entity.getWidth(), entity.getHeight()),
        constructionUnitsRequired);
  }

  public boolean isComplete() {
    return constructionUnitsCompleted >= constructionUnitsRequired;
  }

  public boolean isStarted() {
    return constructionUnitsCompleted > 0f;
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

  public float getConstructionPct() {
    return (constructionUnitsRequired == 0)
        ? 1f
        : constructionUnitsCompleted / constructionUnitsRequired;
  }
}
