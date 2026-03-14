package com.hedgecourt.aqueduct.overlay;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class OverlayPanel {
  protected Rectangle bounds = new Rectangle();

  public void updateBounds(float x, float y, float width, float height) {
    bounds.set(x, y, width, height);
  }

  public abstract void draw(SpriteBatch batch, ShapeDrawer shapeDrawer);

  public abstract boolean isModal();

  public abstract boolean handleClick(float x, float y);
}
