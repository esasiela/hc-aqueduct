package com.hedgecourt.aqueduct.world.entities;

import com.hedgecourt.aqueduct.world.AqueductWorld;

public class TownHall extends Building {

  public TownHall(AqueductWorld world, float x, float y, float width, float height) {
    super(world, x, y, width, height);
  }

  public void deposit(String resourceType, float amount) {
    if (resourceType.equalsIgnoreCase("water")) waterInventory += amount;
  }
}
