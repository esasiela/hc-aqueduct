package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class WorldLayer {

  protected float mouseX;
  protected float mouseY;

  // ── called by WorldRenderer before draw loop ────────────────────────────
  public void preDraw(float mouseX, float mouseY) {
    this.mouseX = mouseX;
    this.mouseY = mouseY;
  }

  // ── draw phases ───────────────────────────────────────────────────────────
  public void drawUnderlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {}

  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {}

  public void drawOverlay(SpriteBatch batch, ShapeDrawer shapeDrawer) {}
}
