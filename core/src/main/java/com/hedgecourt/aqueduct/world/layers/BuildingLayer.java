package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BuildingLayer extends WorldLayer {

  private final AqueductWorld world;

  public BuildingLayer(AqueductWorld world) {
    this.world = world;
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void drawUnderlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (BuildingEntity building : world.getEntities(BuildingEntity.class)) {
      if (building.isHovered()) {
        Vector2 pos = building.getPosition();
        shapeDrawer.rectangle(
            pos.x - building.getWidth() / 2f,
            pos.y - building.getHeight() / 2f,
            building.getWidth(),
            building.getHeight(),
            C.BUILDING_SELECTION_RECT_COLOR,
            2f);
      }
    }
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (BuildingEntity building : world.getEntities(BuildingEntity.class)) building.draw(batch, shapeDrawer);
  }
}
