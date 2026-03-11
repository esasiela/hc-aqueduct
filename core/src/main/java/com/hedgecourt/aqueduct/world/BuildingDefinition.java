package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BuildingDefinition {
  public final String buildingType;
  public final String displayName;
  public final float widthTiles;
  public final float heightTiles;
  public final float constructionUnitsRequired;
  public final float waterCapacity;
  public final float waterCost;
  public final float waterOutputRate;

  public TextureRegion sprite;

  public BuildingDefinition(
      String buildingType,
      String displayName,
      float widthTiles,
      float heightTiles,
      float constructionUnitsRequired,
      float waterCapacity,
      float waterCost,
      float waterOutputRate) {
    this.buildingType = buildingType;
    this.displayName = displayName;
    this.widthTiles = widthTiles;
    this.heightTiles = heightTiles;
    this.constructionUnitsRequired = constructionUnitsRequired;
    this.waterCapacity = waterCapacity;
    this.waterCost = waterCost;
    this.waterOutputRate = waterOutputRate;
  }
}
