package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class EntityLayer extends WorldLayer {

  private final AqueductWorld world;

  public EntityLayer(AqueductWorld world) {
    this.world = world;
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Node node : world.getNodes()) node.draw(batch, shapeDrawer);
    for (TownHall th : world.getTownHalls()) th.draw(batch, shapeDrawer);
  }
}
