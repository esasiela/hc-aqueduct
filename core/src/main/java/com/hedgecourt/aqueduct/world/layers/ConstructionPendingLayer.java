package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import java.util.List;
import java.util.function.Supplier;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ConstructionPendingLayer extends WorldLayer {

  private static final Color NOT_STARTED_FILL_COLOR = new Color(1.0f, 0.0f, 0.0f, 0.3f);
  private static final Color NOT_STARTED_LINE_COLOR = new Color(1.0f, 0.0f, 0.0f, 1f);

  private static final Color HAS_PROGRESS_FILL_COLOR = new Color(1.0f, 0.647f, 0.0f, 0.3f);
  private static final Color HAS_PROGRESS_LINE_COLOR = new Color(1.0f, 0.647f, 0.0f, 1f);

  private final Supplier<List<BuildingEntity>> buildingSupplier;

  public ConstructionPendingLayer(Supplier<List<BuildingEntity>> buildingSupplier) {
    this.buildingSupplier = buildingSupplier;
  }

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (BuildingEntity building : buildingSupplier.get()) {

      float x = building.getBounds().x;
      float y = building.getBounds().y;
      float width = building.getBounds().width;
      float height = building.getBounds().height * (1 - building.getConstructionPct());

      shapeDrawer.filledRectangle(
          x,
          y,
          width,
          height,
          building.isConstructionStarted() ? HAS_PROGRESS_FILL_COLOR : NOT_STARTED_FILL_COLOR);
      shapeDrawer.rectangle(
          x,
          y,
          width,
          height,
          building.isConstructionStarted() ? HAS_PROGRESS_LINE_COLOR : NOT_STARTED_LINE_COLOR);
    }
  }
}
