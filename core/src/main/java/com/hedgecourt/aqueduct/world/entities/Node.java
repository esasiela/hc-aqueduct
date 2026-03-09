package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.ResourceDef;
import com.hedgecourt.aqueduct.world.WorldEntity;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Node extends WorldEntity {

  private final String id;
  private final ResourceDef def;
  private float inventory;

  public Node(String id, float x, float y, float width, float height, ResourceDef def) {
    super(x, y, width, height);
    this.id = id;
    this.def = def;
    this.inventory = def.maxInventory;
  }

  // ── inventory ─────────────────────────────────────────────────────────────

  public boolean isEmpty() {
    return inventory <= 0f;
  }

  public boolean isFull() {
    return inventory >= def.maxInventory;
  }

  public float getInventory() {
    return inventory;
  }

  public float getMaxInventory() {
    return def.maxInventory;
  }

  public float getHarvestRate() {
    return def.harvestRate;
  }

  public String getResourceType() {
    return def.type;
  }

  public String getId() {
    return id;
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
    if (!isFull()) {
      inventory = Math.min(def.maxInventory, inventory + def.regenRate * delta);
    }
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    // placeholder: full=blue, empty=grey
    shapeDrawer.setColor(isEmpty() ? Color.GRAY : Color.BLUE);
    shapeDrawer.filledRectangle(position.x - width / 2f, position.y - height / 2f, width, height);
  }

  @Override
  public String getHoverTooltip() {
    return def.displayName + ": " + String.format("%.1f", inventory) + "/" + def.maxInventory;
  }
}
