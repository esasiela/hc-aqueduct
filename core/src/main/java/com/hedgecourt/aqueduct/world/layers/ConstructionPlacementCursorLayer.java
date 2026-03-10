package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.ConstructionEntityHelper;
import com.hedgecourt.aqueduct.world.WorldLayer;
import java.util.function.Supplier;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ConstructionPlacementCursorLayer extends WorldLayer {

  private static final Color VALID_LOCATION_FILL_COLOR = new Color(0f, 1f, 0f, 0.3f);
  private static final Color VALID_LOCATION_LINE_COLOR = new Color(0f, 1f, 0f, 1f);

  private static final Color INVALID_LOCATION_FILL_COLOR = new Color(1f, 0f, 0f, 0.3f);
  private static final Color INVALID_LOCATION_LINE_COLOR = new Color(1f, 0f, 0f, 1f);

  private final Supplier<ConstructionEntityHelper> helperSupplier;

  public ConstructionPlacementCursorLayer(Supplier<ConstructionEntityHelper> helperSupplier) {
    this.helperSupplier = helperSupplier;
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    ConstructionEntityHelper helper = helperSupplier.get();
    if (helper == null || helper.getEntity() == null) return;

    helper.getEntity().draw(batch, shapeDrawer);
  }

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    ConstructionEntityHelper helper = helperSupplier.get();
    if (helper == null || helper.getEntity() == null) return;

    shapeDrawer.filledRectangle(
        helper.getEntity().getBounds(),
        helper.isValidLocation() ? VALID_LOCATION_FILL_COLOR : INVALID_LOCATION_FILL_COLOR);

    shapeDrawer.rectangle(
        helper.getEntity().getBounds(),
        helper.isValidLocation() ? VALID_LOCATION_LINE_COLOR : INVALID_LOCATION_LINE_COLOR,
        1f);
  }
}
