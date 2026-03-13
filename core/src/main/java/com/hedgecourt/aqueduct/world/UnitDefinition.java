package com.hedgecourt.aqueduct.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.hedgecourt.aqueduct.sprite.EntitySprite;

public class UnitDefinition {
  public String type;
  public String displayName;
  public float widthTiles;
  public float heightTiles;
  public float constructionUnitsRequired;
  public float waterCapacity;
  public float waterCost;
  public float waterOutputRate;

  public JsonNode spriteInfo; // raw, handled by SpriteFactory

  @JsonIgnore public EntitySprite sprite;

  public UnitDefinition() {}
}
