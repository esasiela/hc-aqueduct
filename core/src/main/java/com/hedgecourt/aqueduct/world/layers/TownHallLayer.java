package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class TownHallLayer extends WorldLayer {

  private final AqueductWorld world;

  public TownHallLayer(AqueductWorld world) {
    this.world = world;
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void drawUnderlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (TownHall townHall : world.getTownHalls()) {
      if (townHall.isHovered()) {
        Vector2 pos = townHall.getPosition();
        shapeDrawer.rectangle(
            pos.x - townHall.getWidth() / 2f,
            pos.y - townHall.getHeight() / 2f,
            townHall.getWidth(),
            townHall.getHeight(),
            C.BUILDING_SELECTION_RECT_COLOR,
            2f);
      }
    }
  }

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (TownHall th : world.getTownHalls()) th.draw(batch, shapeDrawer);
  }
}
