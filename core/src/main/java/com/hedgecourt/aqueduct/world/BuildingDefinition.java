package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BuildingDefinition {
  public final String buildingType;
  public final String displayName;
  public final float widthTiles;
  public final float heightTiles;
  public final float constructionUnitsRequired;

  public TextureRegion sprite;

  public BuildingDefinition(
      String buildingType,
      String displayName,
      float widthTiles,
      float heightTiles,
      float constructionUnitsRequired) {
    this.buildingType = buildingType;
    this.displayName = displayName;
    this.widthTiles = widthTiles;
    this.heightTiles = heightTiles;
    this.constructionUnitsRequired = constructionUnitsRequired;
  }
}
