package com.hedgecourt.aqueduct.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class CrosshairUiElement extends UiElement {

  private static final float SIZE = 20f;
  private static final float THICKNESS = 2f;
  private static final Color COLOR = Color.CYAN;

  public CrosshairUiElement() {
    super(0, 0, 0, 0); // no fixed bounds, follows mouse
  }

  @Override
  public void update(float delta) {}

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    shapeDrawer.line(mouseX - SIZE, mouseY, mouseX + SIZE, mouseY, COLOR, THICKNESS);
    shapeDrawer.line(mouseX, mouseY - SIZE, mouseX, mouseY + SIZE, COLOR, THICKNESS);
  }
}
