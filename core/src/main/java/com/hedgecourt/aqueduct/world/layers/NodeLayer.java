package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Node;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class NodeLayer extends WorldLayer {

  private final AqueductWorld world;

  public NodeLayer(AqueductWorld world) {
    this.world = world;
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Node node : world.getEntities(Node.class)) node.draw(batch, shapeDrawer);
  }

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Node node : world.getEntities(Node.class)) {
      /* ****
       * Selection Decoration
       */
      if (node.isSelected()) {
        shapeDrawer.setColor(C.WORKER_SELECTION_RING_COLOR);
        shapeDrawer.circle(node.getPosition().x, node.getPosition().y, node.getWidth() * 0.6f, 2f);
      }
      /* ****
       * Node Fullness Bar
       */
      float bagBarHeight = 4f;
      float barBarYOffset = 4f;
      shapeDrawer.filledRectangle(
          node.getPosition().x - node.getWidth() / 2f,
          node.getPosition().y + node.getHeight() / 2f + barBarYOffset,
          node.getWidth(),
          bagBarHeight,
          C.BAG_BAR_CAPACITY_COLOR);
      shapeDrawer.filledRectangle(
          node.getPosition().x - node.getWidth() / 2f,
          node.getPosition().y + node.getHeight() / 2f + barBarYOffset,
          node.getWidth() * (node.getInventory() / node.getMaxInventory()),
          bagBarHeight,
          C.BAG_BAR_CARRY_COLOR);
    }
  }
}
