package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgecourt.aqueduct.InvalidBuildingConfigException;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.sprite.PipoyaBaseSprite;
import com.hedgecourt.aqueduct.sprite.SingleTextureSprite;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import com.hedgecourt.aqueduct.world.entities.Pipe;
import com.hedgecourt.aqueduct.world.entities.Sprinkler;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import java.util.HashMap;
import java.util.Map;

public class BuildingFactory {

  private static final Map<String, Class<? extends EntitySprite>> SPRITE_TYPES =
      Map.of(
          "PNG", SingleTextureSprite.class,
          "PIPOYA_BASE", PipoyaBaseSprite.class);

  private final ObjectMapper jackson = new ObjectMapper();

  private final Map<String, BuildingDefinition> definitions = new HashMap<>();
  private final AqueductWorld world;

  public BuildingFactory(AqueductWorld world) {
    this.world = world;
  }

  public BuildingEntity create(String type, float x, float y) {
    BuildingDefinition def = definitions.get(type);
    if (def == null) throw new InvalidBuildingConfigException("no defaults for type: " + type);

    float w = def.widthTiles * world.getTileWidth();
    float h = def.heightTiles * world.getTileHeight();

    BuildingEntity building =
        switch (type) {
          case "townhall" -> new TownHall(world, x, y, w, h);
          case "pipe" -> new Pipe(world, x, y, w, h);
          case "sprinkler" -> new Sprinkler(world, x, y, w, h);
          default -> throw new InvalidBuildingConfigException("Unknown type: " + type);
        };
    building.setBuildingType(type);

    building.setDisplayName(def.displayName);
    building.setConstructionUnitsRequired(def.constructionUnitsRequired);
    building.setWaterCapacity(def.waterCapacity);
    building.setWaterCost(def.waterCost);
    building.setWaterOutputRate(def.waterOutputRate);

    building.setEntitySprite(def.entitySprite);

    return building;
  }

  public void buildSprites(AssetManager assetManager) {
    for (BuildingDefinition def : definitions.values()) {
      if (def.entitySprite != null) {
        def.entitySprite.build(assetManager);
      }
    }
  }

  public void loadDefinitions(String jsonPath, AssetManager assetManager) {
    JsonValue root = new JsonReader().parse(Gdx.files.internal(jsonPath));
    JsonValue buildings = root.get("buildings");
    if (buildings == null)
      throw new InvalidBuildingConfigException("missing 'buildings' array in " + jsonPath);

    for (JsonValue entry : buildings) {
      BuildingDefinition def = parse(entry, jsonPath);

      JsonValue spriteInfo = entry.get("spriteInfo");
      def.entitySprite = (spriteInfo != null) ? parseSpriteInfo(spriteInfo) : null;

      definitions.put(def.buildingType, def);
    }
    if (definitions.isEmpty()) {
      throw new InvalidBuildingConfigException("no resources defined in " + jsonPath);
    }
  }

  public void loadAssets(String jsonPath, AssetManager assetManager) {
    JsonValue root = new JsonReader().parse(Gdx.files.internal(jsonPath));
    JsonValue buildings = root.get("buildings");
    if (buildings == null)
      throw new InvalidBuildingConfigException("missing 'buildings' array in " + jsonPath);

    for (JsonValue entry : buildings) {
      JsonValue spriteInfo = entry.get("spriteInfo");
      if (spriteInfo == null) continue;

      EntitySprite sprite = parseSpriteInfo(spriteInfo);
      for (String path : sprite.assetPaths()) {
        assetManager.load(path, Texture.class);
      }
    }
  }

  public void clear() {
    definitions.clear();
  }

  private BuildingDefinition parse(JsonValue e, String jsonPath) {
    String type = requireString(e, "type", jsonPath);
    String displayName = requireString(e, "displayName", jsonPath);
    float widthTiles = requireFloat(e, "widthTiles", jsonPath);
    float heightTiles = requireFloat(e, "heightTiles", jsonPath);
    float constructionUnitsRequired = requireFloat(e, "constructionUnitsRequired", jsonPath);
    float waterCapacity = requireFloat(e, "waterCapacity", jsonPath);
    float waterCost = requireFloat(e, "waterCost", jsonPath);
    float waterOutputRate = requireFloat(e, "waterOutputRate", jsonPath);

    return new BuildingDefinition(
        type,
        displayName,
        widthTiles,
        heightTiles,
        constructionUnitsRequired,
        waterCapacity,
        waterCost,
        waterOutputRate);
  }

  public BuildingDefinition get(String type) {
    BuildingDefinition def = definitions.get(type);
    if (def == null) {
      throw new InvalidBuildingConfigException("unknown resource type '" + type + "'");
    }
    return def;
  }

  // ── load sprite textures ─────────────────────────────────────────────────────

  private EntitySprite parseSpriteInfo(JsonValue spriteInfo) {
    String type = spriteInfo.getString("type");
    Class<? extends EntitySprite> implClass = SPRITE_TYPES.get(type);
    if (implClass == null) {
      throw new InvalidBuildingConfigException("Unknown spriteInfo type: " + type);
    }
    try {
      return jackson.treeToValue(
          jackson.readTree(spriteInfo.toJson(JsonWriter.OutputType.json)), implClass);
    } catch (Exception e) {
      throw new InvalidBuildingConfigException("Failed to parse spriteInfo: " + e.getMessage());
    }
  }

  // ── fail-fast helpers ─────────────────────────────────────────────────────

  private String requireString(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new InvalidBuildingConfigException("missing required field '" + key + "' in " + path);
    }
    return e.getString(key);
  }

  private float requireFloat(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new InvalidBuildingConfigException("missing required field '" + key + "' in " + path);
    }
    return e.getFloat(key);
  }

  private int requireInt(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new InvalidBuildingConfigException("missing required field '" + key + "' in " + path);
    }
    return e.getInt(key);
  }
}
