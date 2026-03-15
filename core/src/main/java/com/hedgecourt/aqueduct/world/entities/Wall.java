package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.sprite.WallAutotile4Sprite;
import com.hedgecourt.aqueduct.sprite.WallAutotile8Sprite;
import com.hedgecourt.aqueduct.world.AqueductWorld;

public class Wall extends Building {

  private int bitmask = 0;

  public Wall(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  public void setBitmask(int bitmask) {
    this.bitmask = bitmask;
  }

  public int getBitmask() {
    return bitmask;
  }

  @Override
  public void setSprite(EntitySprite sprite) {
    super.setSprite(sprite);
    if (sprite instanceof WallAutotile4Sprite s) s.setBitmaskSupplier(() -> this.bitmask);
    if (sprite instanceof WallAutotile8Sprite s) s.setBitmaskSupplier(() -> this.bitmask);
  }

  @Override
  public void onConstructionComplete() {
    world.recomputeWallBitmasks();
  }
}
