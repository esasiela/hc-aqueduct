package com.hedgecourt.aqueduct.world;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.hedgecourt.aqueduct.sprite.EntitySprite;

public class ItemDefinition {
  public String type;
  public String displayName;
  public float regenRate;
  public float regenCooldown;
  public float maxInventory;
  public float harvestRate;

  public JsonNode spriteInfo; // raw, handled by SpriteFactory

  @JsonIgnore public EntitySprite sprite;

  public ItemDefinition() {}
}
