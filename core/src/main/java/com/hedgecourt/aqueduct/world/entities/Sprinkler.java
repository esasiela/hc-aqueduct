package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Sprinkler extends BuildingEntity {
  public Sprinkler(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer drawer) {
    super.draw(batch, drawer);

    if (!isActive()) return;

    float pct = getWaterInventoryPct();
    float maxLineLength = width * 1.5f;
    float lineLength = maxLineLength * pct;

    for (int i = 0; i < 8; i++) {
      float angleDeg = i * 45f;
      float angleRad = angleDeg * MathUtils.degreesToRadians;
      float endX = position.x + MathUtils.cos(angleRad) * lineLength;
      float endY = position.y + MathUtils.sin(angleRad) * lineLength;
      drawer.line(position.x, position.y, endX, endY, Color.CYAN, 2f);
    }
  }
}
