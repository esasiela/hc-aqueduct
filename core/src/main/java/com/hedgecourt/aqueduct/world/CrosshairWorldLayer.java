package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class CrosshairWorldLayer extends WorldLayer {

  private static final float SIZE = 20f;
  private static final float THICKNESS = 2f;
  private static final Color COLOR = Color.YELLOW;

  @Override
  public void update(float delta) {}

  @Override
  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    shapeDrawer.line(mouseX - SIZE, mouseY, mouseX + SIZE, mouseY, COLOR, THICKNESS);
    shapeDrawer.line(mouseX, mouseY - SIZE, mouseX, mouseY + SIZE, COLOR, THICKNESS);
  }
}
