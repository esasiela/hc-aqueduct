package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldLayer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class TileHighlightWorldLayer extends WorldLayer {

  private static final Color COLOR = new Color(1f, 1f, 1f, 0.4f);
  private static final float THICKNESS = 1f;

  private final AqueductWorld world;

  public TileHighlightWorldLayer(AqueductWorld world) {
    this.world = world;
  }

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    int tileW = world.getTileWidth();
    int tileH = world.getTileHeight();

    // snap mouse to tile
    int tileX = (int) (mouseX / tileW);
    int tileY = (int) (mouseY / tileH);

    float drawX = tileX * tileW;
    float drawY = tileY * tileH;

    shapeDrawer.setColor(COLOR);
    shapeDrawer.rectangle(drawX, drawY, tileW, tileH, THICKNESS);
  }
}
