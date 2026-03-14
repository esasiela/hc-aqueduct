package com.hedgecourt.aqueduct.factory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.hedgecourt.aqueduct.sprite.EntitySprite;

public class UnitDefinition {
  public String type;
  public String displayName;
  public float widthPx;
  public float heightPx;
  public float trainingPointsRequired;

  public JsonNode spriteInfo; // raw, handled by SpriteFactory

  @JsonIgnore public EntitySprite sprite;

  public UnitDefinition() {}
}
