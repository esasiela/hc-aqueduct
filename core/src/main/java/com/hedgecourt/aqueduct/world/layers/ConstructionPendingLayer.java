package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.ConstructionEntityHelper;
import com.hedgecourt.aqueduct.world.WorldLayer;
import java.util.List;
import java.util.function.Supplier;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ConstructionPendingLayer extends WorldLayer {

  private static final Color NOT_STARTED_FILL_COLOR = new Color(1.0f, 0.647f, 0.0f, 0.3f);
  private static final Color NOT_STARTED_LINE_COLOR = new Color(1.0f, 0.647f, 0.0f, 1f);

  private static final Color HAS_PROGRESS_FILL_COLOR = new Color(0.5f, 0.5f, 0.5f, 0.3f);
  private static final Color HAS_PROGRESS_LINE_COLOR = new Color(0.5f, 0.5f, 0.5f, 1f);

  private final Supplier<List<ConstructionEntityHelper>> helperListSupplier;

  public ConstructionPendingLayer(Supplier<List<ConstructionEntityHelper>> helperListSupplier) {
    this.helperListSupplier = helperListSupplier;
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (ConstructionEntityHelper helper : this.helperListSupplier.get()) {
      if (helper.getEntity() == null) return;

      helper.getEntity().draw(batch, shapeDrawer);
    }
  }

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (ConstructionEntityHelper helper : this.helperListSupplier.get()) {
      if (helper.getEntity() == null) return;

      float x = helper.getEntity().getBounds().x;
      float y = helper.getEntity().getBounds().y;
      float width = helper.getEntity().getBounds().width;
      float height = helper.getEntity().getBounds().height * (1 - helper.getConstructionPct());

      shapeDrawer.filledRectangle(
          x,
          y,
          width,
          height,
          helper.isStarted() ? HAS_PROGRESS_FILL_COLOR : NOT_STARTED_FILL_COLOR);
      shapeDrawer.rectangle(
          x,
          y,
          width,
          height,
          helper.isStarted() ? HAS_PROGRESS_LINE_COLOR : NOT_STARTED_LINE_COLOR);
    }
  }
}
