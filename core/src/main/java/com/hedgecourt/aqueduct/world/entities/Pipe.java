package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Pipe extends BuildingEntity {

  public Pipe(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  @Override
  public void update(float delta) {}

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer drawer) {
    drawer.filledCircle(position.x, position.y, (width - 5f) / 2f, Color.ORANGE);
  }
}
