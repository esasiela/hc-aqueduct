package com.hedgecourt.aqueduct.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class UiElement {

  protected Rectangle bounds;
  protected float mouseX;
  protected float mouseY;

  public UiElement(float x, float y, float width, float height) {
    bounds = new Rectangle(x, y, width, height);
  }

  public Rectangle getBounds() {
    return bounds;
  }

  public boolean containsMouse() {
    return bounds.contains(mouseX, mouseY);
  }

  // ── called by UiRenderer before update loop ───────────────────────────────
  public void preUpdate(float mouseX, float mouseY) {
    this.mouseX = mouseX;
    this.mouseY = mouseY;
  }

  // ── event hooks ───────────────────────────────────────────────────────────
  public void onClick(float mouseX, float mouseY) {}

  public void onMouseEnter(float mouseX, float mouseY) {}

  public void onMouseExit() {}

  // ── frame hooks ───────────────────────────────────────────────────────────
  public abstract void update(float delta);

  public abstract void draw(SpriteBatch batch, ShapeDrawer shapeDrawer);
}
