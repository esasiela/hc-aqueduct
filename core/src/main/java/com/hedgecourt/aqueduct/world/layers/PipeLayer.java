package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Pipe;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PipeLayer extends WorldLayer {

  private final AqueductWorld world;

  public PipeLayer(AqueductWorld world) {
    this.world = world;
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void drawUnderlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Pipe pipe : world.getEntities(Pipe.class)) {
      if (pipe.isHovered()) {
        Vector2 pos = pipe.getPosition();
        shapeDrawer.rectangle(
            pos.x - pipe.getWidth() / 2f,
            pos.y - pipe.getHeight() / 2f,
            pipe.getWidth(),
            pipe.getHeight(),
            C.BUILDING_SELECTION_RECT_COLOR,
            2f);
      }
    }
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Pipe pipe : world.getEntities(Pipe.class)) pipe.draw(batch, shapeDrawer);
  }
}
