package com.hedgecourt.aqueduct.world;

import com.hedgecourt.aqueduct.sprite.EntitySprite;

public class ResourceDefinition {
  public final String type;
  public final String displayName;
  public final float regenRate;
  public final float regenCooldown;
  public final float maxInventory;
  public final float harvestRate;

  public EntitySprite sprite;

  public ResourceDefinition(
      String type,
      String displayName,
      float regenRate,
      float regenCooldown,
      float maxInventory,
      float harvestRate) {
    this.type = type;
    this.displayName = displayName;
    this.regenRate = regenRate;
    this.regenCooldown = regenCooldown;
    this.maxInventory = maxInventory;
    this.harvestRate = harvestRate;
  }
}
