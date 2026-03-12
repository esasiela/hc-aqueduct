package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Building;
import java.util.function.Supplier;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ConstructionPlacementCursorLayer extends WorldLayer {

  private static final Color VALID_LOCATION_FILL_COLOR = new Color(0f, 1f, 0f, 0.3f);
  private static final Color VALID_LOCATION_LINE_COLOR = new Color(0f, 1f, 0f, 1f);

  private static final Color INVALID_LOCATION_FILL_COLOR = new Color(1f, 0f, 0f, 0.3f);
  private static final Color INVALID_LOCATION_LINE_COLOR = new Color(1f, 0f, 0f, 1f);

  private final Supplier<Building> buildingSupplier;

  public ConstructionPlacementCursorLayer(Supplier<Building> buildingSupplier) {
    this.buildingSupplier = buildingSupplier;
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    Building building = this.buildingSupplier.get();
    if (building == null) return;

    building.draw(batch, shapeDrawer);
  }

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    Building building = buildingSupplier.get();
    if (building == null) return;

    shapeDrawer.filledRectangle(
        building.getBounds(),
        building.isConstructionLocationValid()
            ? VALID_LOCATION_FILL_COLOR
            : INVALID_LOCATION_FILL_COLOR);

    shapeDrawer.rectangle(
        building.getBounds(),
        building.isConstructionLocationValid()
            ? VALID_LOCATION_LINE_COLOR
            : INVALID_LOCATION_LINE_COLOR,
        1f);
  }
}
