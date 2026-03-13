package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgecourt.aqueduct.InvalidBuildingConfigException;
import com.hedgecourt.aqueduct.SpriteFactory;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.world.entities.Building;
import com.hedgecourt.aqueduct.world.entities.Pipe;
import com.hedgecourt.aqueduct.world.entities.Sprinkler;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingFactory {

  @JsonIgnoreProperties("scratch")
  private static class BuildingsJson {
    public List<BuildingDefinition> buildings;
    public Object scratch;
  }

  private final Map<String, BuildingDefinition> definitions = new HashMap<>();
  private final AqueductWorld world;
  private final SpriteFactory spriteFactory;
  private final ObjectMapper jackson = new ObjectMapper();

  public BuildingFactory(AqueductWorld world) {
    this.world = world;
    spriteFactory = world.getSpriteFactory();
  }

  public Building create(String type, float x, float y) {
    BuildingDefinition def = definitions.get(type);
    if (def == null) throw new InvalidBuildingConfigException("no defaults for type: " + type);

    float w = def.widthTiles * world.getTileWidth();
    float h = def.heightTiles * world.getTileHeight();

    Building building =
        switch (type) {
          case "townhall" -> new TownHall(world, x, y, w, h);
          case "pipe" -> new Pipe(world, x, y, w, h);
          case "sprinkler" -> new Sprinkler(world, x, y, w, h);
          default -> throw new InvalidBuildingConfigException("Unknown type: " + type);
        };
    building.setType(type);

    building.setDisplayName(def.displayName);
    building.setConstructionUnitsRequired(def.constructionUnitsRequired);
    building.setWaterCapacity(def.waterCapacity);
    building.setWaterCost(def.waterCost);
    building.setWaterOutputRate(def.waterOutputRate);

    building.setSprite(def.sprite);

    return building;
  }

  public void loadAssets(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      BuildingsJson wrapper = jackson.readValue(json, BuildingsJson.class);
      if (wrapper.buildings == null) return;

      for (BuildingDefinition def : wrapper.buildings) {
        if (def.spriteInfo == null) continue;
        EntitySprite sprite = spriteFactory.create(def.spriteInfo);
        for (String path : sprite.assetPaths()) {
          assetManager.load(path, Texture.class);
        }
      }
    } catch (Exception e) {
      throw new InvalidBuildingConfigException(
          "failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void loadDefinitions(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      BuildingsJson wrapper = jackson.readValue(json, BuildingsJson.class);

      if (wrapper.buildings == null || wrapper.buildings.isEmpty())
        throw new InvalidBuildingConfigException("no buildings defined in " + jsonPath);

      for (BuildingDefinition def : wrapper.buildings) {
        if (def.spriteInfo != null) {
          def.sprite = spriteFactory.create(def.spriteInfo);
        }
        definitions.put(def.type, def);
      }
    } catch (InvalidBuildingConfigException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidBuildingConfigException(
          "failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void buildSprites(AssetManager assetManager) {
    for (BuildingDefinition def : definitions.values()) {
      if (def.sprite != null) {
        def.sprite.build(assetManager);
      }
    }
  }

  public void clear() {
    definitions.clear();
  }

  public BuildingDefinition get(String type) {
    BuildingDefinition def = definitions.get(type);
    if (def == null) {
      throw new InvalidBuildingConfigException("unknown resource type '" + type + "'");
    }
    return def;
  }
}
