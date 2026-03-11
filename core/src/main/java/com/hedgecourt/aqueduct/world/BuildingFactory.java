package com.hedgecourt.aqueduct.world;

import com.hedgecourt.aqueduct.InvalidBuildingConfigException;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import com.hedgecourt.aqueduct.world.entities.Pipe;
import com.hedgecourt.aqueduct.world.entities.Sprinkler;

public class BuildingFactory {

  private final AqueductWorld world;

  public BuildingFactory(AqueductWorld world) {
    this.world = world;
  }

  public BuildingEntity create(String type, float x, float y) {
    BuildingEntity building =
        switch (type) {
          case "pipe" -> new Pipe(world, x, y, 32, 32);
          case "sprinkler" -> new Sprinkler(world, x, y, 32, 32);
          default -> throw new InvalidBuildingConfigException("Unknown type: " + type);
        };
    building.setBuildingType(type);
    building.setConstructionUnitsRequired(type.equals("pipe") ? 100f : 200f);

    return building;
  }
}
