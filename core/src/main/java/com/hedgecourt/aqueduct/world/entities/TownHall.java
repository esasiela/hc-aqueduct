package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldEntity;
import java.util.HashMap;
import java.util.Map;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class TownHall extends WorldEntity {

  private final String id;
  private final Map<String, Float> inventory = new HashMap<>();
  private TextureRegion sprite;

  public TownHall(
      AqueductWorld world,
      String id,
      float x,
      float y,
      float width,
      float height,
      TextureRegion sprite) {
    super(world, x, y, width, height);
    this.id = id;
    this.sprite = sprite;
  }

  public String getId() {
    return id;
  }

  public void deposit(String resourceType, float amount) {
    inventory.merge(resourceType, amount, Float::sum);
  }

  public float getInventory(String resourceType) {
    return inventory.getOrDefault(resourceType, 0f);
  }

  // ── update ────────────────────────────────────────────────────────────────

  @Override
  public void update(float delta) {
    // future: consume water, power connected buildings
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    batch.draw(sprite, position.x - width / 2f, position.y - height / 2f, width, height);
  }

  @Override
  public String getHoverTooltip() {
    StringBuilder sb = new StringBuilder("Town Hall");
    for (Map.Entry<String, Float> entry : inventory.entrySet()) {
      sb.append("\n")
          .append(entry.getKey())
          .append(": ")
          .append(String.format("%.1f", entry.getValue()));
    }
    return sb.toString();
  }
}
