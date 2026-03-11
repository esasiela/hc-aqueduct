package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.InvalidBuildingConfigException;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import com.hedgecourt.aqueduct.world.entities.Pipe;
import com.hedgecourt.aqueduct.world.entities.Sprinkler;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuildingFactory {

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

    building.setSprite(def.sprite);

    return building;
  }

  // ── defaults ─────────────────────────────────────────────────────

  public void loadDefinitions(String jsonPath, AssetManager assetManager) {
    JsonValue root = new JsonReader().parse(Gdx.files.internal(jsonPath));
    JsonValue buildings = root.get("buildings");
    if (buildings == null)
      throw new InvalidBuildingConfigException("missing 'buildings' array in " + jsonPath);

    for (JsonValue entry : buildings) {
      BuildingDefinition def = parse(entry, jsonPath);

      JsonValue spriteInfo = entry.get("spriteInfo");
      def.sprite = spriteInfo != null ? parseSpriteInfo(spriteInfo, assetManager) : null;

      definitions.put(def.buildingType, def);
    }
    if (definitions.isEmpty()) {
      throw new InvalidBuildingConfigException("no resources defined in " + jsonPath);
    }
  }

  public void loadAssets(String jsonPath, AssetManager assetManager) {
    List<String> assetPaths = new ArrayList<>();

    JsonValue root = new JsonReader().parse(Gdx.files.internal(jsonPath));
    JsonValue buildings = root.get("buildings");
    if (buildings == null)
      throw new InvalidBuildingConfigException("missing 'buildings' array in " + jsonPath);

    for (JsonValue entry : buildings) {
      String buildingType = requireString(entry, "type", jsonPath);

      JsonValue spriteInfo = entry.get("spriteInfo");
      if (spriteInfo == null) continue;

      String spriteType = requireString(spriteInfo, "type", jsonPath);

      switch (spriteType) {
        case "PNG":
          String pngPath = requireString(spriteInfo, "path", jsonPath);
          assetManager.load(pngPath, Texture.class);
          break;
        case "PIPOYA_BASE":
          assetManager.load(C.PIPOYA_BASE_PATH, Texture.class);
          break;
        default:
          throw new InvalidBuildingConfigException("unknown sprite type: " + spriteType);
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

  private TextureRegion parseSpriteInfo(JsonValue spriteInfo, AssetManager assetManager) {
    String type = requireString(spriteInfo, "type", "spriteInfo");
    return switch (type) {
      case "PNG" -> parsePngSprite(spriteInfo, assetManager);
      case "PIPOYA_BASE" -> parsePipoyaBaseSprite(spriteInfo, assetManager);
      default -> throw new InvalidBuildingConfigException("Unknown spriteInfo type: " + type);
    };
  }

  private TextureRegion parsePngSprite(JsonValue e, AssetManager assetManager) {
    String path = requireString(e, "path", "spriteInfo.PNG");
    Texture texture = assetManager.get(path, Texture.class);
    return new TextureRegion(texture);
  }

  private TextureRegion parsePipoyaBaseSprite(JsonValue e, AssetManager assetManager) {
    int spriteId = requireInt(e, "spriteId", "spriteInfo.PIPOYA_BASE");
    Texture sheet = assetManager.get(C.PIPOYA_BASE_PATH, Texture.class);
    TextureRegion[][] grid = TextureRegion.split(sheet, 32, 32);
    return grid[spriteId / 8][spriteId % 8];
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
