package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.sprite.PipoyaBaseNodeSprite;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.ResourceDefinition;

public class Node extends Entity {

  private final String id;
  private final ResourceDefinition resourceDefinition;
  private float inventory;

  private float regenCooldownTimer = 0f;

  public Node(
      AqueductWorld world,
      String id,
      float x,
      float y,
      float width,
      float height,
      ResourceDefinition resourceDefinition) {
    super(world, x, y, width, height);
    this.id = id;
    this.resourceDefinition = resourceDefinition;
    this.inventory = resourceDefinition.maxInventory;

    setSprite(resourceDefinition.sprite);
  }

  // ── inventory ─────────────────────────────────────────────────────────────

  public boolean isEmpty() {
    return inventory <= 0f;
  }

  public boolean isFull() {
    return inventory >= resourceDefinition.maxInventory;
  }

  public float getInventory() {
    return inventory;
  }

  public float getMaxInventory() {
    return resourceDefinition.maxInventory;
  }

  public float getHarvestRate() {
    return resourceDefinition.harvestRate;
  }

  public String getResourceType() {
    return resourceDefinition.type;
  }

  public String getId() {
    return id;
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

  // ── update ────────────────────────────────────────────────────────────────

  @Override
  public void update(float delta) {
    if (regenCooldownTimer > 0f) {
      regenCooldownTimer -= delta;
      return;
    }

    if (inventory <= 0f) {
      regenCooldownTimer = resourceDefinition.regenCooldown;
    }

    inventory =
        Math.min(inventory + resourceDefinition.regenRate * delta, resourceDefinition.maxInventory);
  }

  @Override
  public String getHoverTooltip() {
    return resourceDefinition.displayName
        + ": "
        + String.format("%.1f", inventory)
        + "/"
        + resourceDefinition.maxInventory;
  }
}
