package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.sprite.PipoyaBaseNodeSprite;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.ItemDefinition;

public class Node extends Entity {

  private final ItemDefinition itemDefinition;
  private float inventory;

  private float regenCooldownTimer = 0f;

  public Node(
      AqueductWorld world,
      float x,
      float y,
      float width,
      float height,
      ItemDefinition itemDefinition) {
    super(world, x, y, width, height);
    this.itemDefinition = itemDefinition;
    this.inventory = itemDefinition.maxInventory;

    setSprite(itemDefinition.sprite);
  }

  // ── inventory ─────────────────────────────────────────────────────────────

  public boolean isEmpty() {
    return inventory <= 0f;
  }

  public boolean isFull() {
    return inventory >= itemDefinition.maxInventory;
  }

  public float getInventory() {
    return inventory;
  }

  public float getMaxInventory() {
    return itemDefinition.maxInventory;
  }

  public float getHarvestRate() {
    return itemDefinition.harvestRate;
  }

  public String getItemType() {
    return itemDefinition.type;
  }

  public boolean isOnCooldown() {
    return regenCooldownTimer > 0f;
  }

  @Override
  public void setSprite(EntitySprite sprite) {
    EntitySprite copy = sprite.freshCopy();
    super.setSprite(copy);
    if (copy instanceof PipoyaBaseNodeSprite nodeSprite) {
      nodeSprite.setHasInventory(() -> this.getInventory() > 0);
    }
  }

  /** Harvest up to amount from node, returns how much was actually harvested. */
  public float harvest(float amount) {
    float actual = Math.min(amount, inventory);
    inventory -= actual;
    return actual;
  }

  @Override
  public void update(float delta) {
    if (isOnCooldown()) {
      regenCooldownTimer -= delta;
      return;
    }

    if (inventory <= 0f) {
      regenCooldownTimer = itemDefinition.regenCooldown;
    }

    inventory = Math.min(inventory + itemDefinition.regenRate * delta, itemDefinition.maxInventory);
  }

  @Override
  public String getHoverTooltip() {
    return itemDefinition.displayName
        + ": "
        + String.format("%.1f", inventory)
        + "/"
        + itemDefinition.maxInventory;
  }
}
