package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.WorldEntity;
import com.hedgecourt.aqueduct.world.WorldLayer;
import java.util.function.Supplier;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ConstructionPlacementCursorLayer extends WorldLayer {

  private final Supplier<WorldEntity> entitySupplier;

  public ConstructionPlacementCursorLayer(Supplier<WorldEntity> entitySupplier) {
    this.entitySupplier = entitySupplier;
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    WorldEntity entity = entitySupplier.get();
    if (entity == null) return;

    entity.draw(batch, shapeDrawer);
  }

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    WorldEntity entity = entitySupplier.get();
    if (entity == null) return;

    shapeDrawer.rectangle(entity.getBounds(), Color.PURPLE, 1f);
  }
}
