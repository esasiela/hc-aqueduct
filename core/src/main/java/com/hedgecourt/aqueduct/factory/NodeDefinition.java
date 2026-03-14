package com.hedgecourt.aqueduct.factory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.hedgecourt.aqueduct.sprite.EntitySprite;

public class NodeDefinition {
  public String type;
  public String displayName;
  public String itemType;
  public float widthTiles;
  public float heightTiles;
  public boolean walkable;
  public float regenRate;
  public float regenCooldown;
  public float maxInventory;
  public float harvestRate;

  public JsonNode spriteInfo; // raw, handled by SpriteFactory

  @JsonIgnore public EntitySprite sprite;

  public NodeDefinition() {}
}
