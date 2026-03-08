package com.hedgecourt.aqueduct.world;

public class ResourceDef {
  public final String type;
  public final String displayName;
  public final float regenRate;
  public final float maxInventory;
  public final float harvestRate;
  public final int spriteIdEmpty;
  public final int spriteIdFull;

  public ResourceDef(
      String type,
      String displayName,
      float regenRate,
      float maxInventory,
      float harvestRate,
      int spriteIdEmpty,
      int spriteIdFull) {
    this.type = type;
    this.displayName = displayName;
    this.regenRate = regenRate;
    this.maxInventory = maxInventory;
    this.harvestRate = harvestRate;
    this.spriteIdEmpty = spriteIdEmpty;
    this.spriteIdFull = spriteIdFull;
  }
}
