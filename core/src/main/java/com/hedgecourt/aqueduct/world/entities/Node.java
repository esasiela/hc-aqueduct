package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.sprite.PipoyaBaseNodeSprite;
import com.hedgecourt.aqueduct.world.AqueductWorld;

public class Node extends Entity {

  private boolean walkable;

  private float inventory;

  private String itemType;
  private float regenRate;
  private float regenCooldown;
  private float maxInventory;
  private float harvestRate;

  private float regenCooldownTimer = 0f;

  public Node(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  // ── inventory ─────────────────────────────────────────────────────────────

  public boolean isEmpty() {
    return inventory <= 0f;
  }

  public boolean isFull() {
    return inventory >= maxInventory;
  }

  public float getInventory() {
    return inventory;
  }

  public float getMaxInventory() {
    return maxInventory;
  }

  public float getHarvestRate() {
    return harvestRate;
  }

  public boolean isWalkable() {
    return walkable;
  }

  public void setWalkable(boolean walkable) {
    this.walkable = walkable;
  }

  public String getItemType() {
    return itemType;
  }

  public void setItemType(String itemType) {
    this.itemType = itemType;
  }

  public void setInventory(float inventory) {
    this.inventory = inventory;
  }

  public float getRegenRate() {
    return regenRate;
  }

  public void setRegenRate(float regenRate) {
    this.regenRate = regenRate;
  }

  public float getRegenCooldown() {
    return regenCooldown;
  }

  public void setRegenCooldown(float regenCooldown) {
    this.regenCooldown = regenCooldown;
  }

  public void setMaxInventory(float maxInventory) {
    this.maxInventory = maxInventory;
  }

  public void setHarvestRate(float harvestRate) {
    this.harvestRate = harvestRate;
  }

  public boolean isOnCooldown() {
    return regenCooldownTimer > 0f;
  }

  @Override
  public void setSprite(EntitySprite sprite) {
    super.setSprite(sprite);
    if (sprite instanceof PipoyaBaseNodeSprite nodeSprite) {
      nodeSprite.setIsOnCooldown(this::isOnCooldown);
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
      regenCooldownTimer = regenCooldown;
    }

    inventory = Math.min(inventory + regenRate * delta, maxInventory);
  }

  @Override
  public String getHoverTooltip() {
    return displayName + ": " + String.format("%.1f", inventory) + "/" + maxInventory;
  }
}
