package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Building;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BuildingLayer extends WorldLayer {

  private final AqueductWorld world;

  public BuildingLayer(AqueductWorld world) {
    this.world = world;
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void drawUnderlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Building building : world.getEntities(Building.class)) {
      if (building.isSelected()) {
        Vector2 pos = building.getPosition();
        shapeDrawer.rectangle(
            pos.x - building.getWidth() / 2f,
            pos.y - building.getHeight() / 2f,
            building.getWidth(),
            building.getHeight(),
            C.BUILDING_SELECTION_RECT_COLOR,
            2f);
      }
      if (building.isHovered()) {
        float hoverOffset = 3f;
        Vector2 pos = building.getPosition();
        shapeDrawer.rectangle(
            pos.x - building.getWidth() / 2f + hoverOffset,
            pos.y - building.getHeight() / 2f + hoverOffset,
            building.getWidth() - 2f * hoverOffset,
            building.getHeight() - 2f * hoverOffset,
            C.BUILDING_HOVER_RECT_COLOR,
            2f);
      }
    }
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Building building : world.getEntities(Building.class)) building.draw(batch, shapeDrawer);
  }
}
