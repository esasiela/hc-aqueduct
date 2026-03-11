package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.hedgecourt.aqueduct.InvalidBuildingConfigException;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import com.hedgecourt.aqueduct.world.entities.Pipe;
import com.hedgecourt.aqueduct.world.entities.Sprinkler;
import java.util.HashMap;
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
          case "pipe" -> new Pipe(world, x, y, w, h);
          case "sprinkler" -> new Sprinkler(world, x, y, w, h);
          default -> throw new InvalidBuildingConfigException("Unknown type: " + type);
        };
    building.setBuildingType(type);

    building.setDisplayName(def.displayName);
    building.setConstructionUnitsRequired(def.constructionUnitsRequired);

    return building;
  }

  // ── defaults ─────────────────────────────────────────────────────

  public void load(String jsonPath) {
    JsonValue root = new JsonReader().parse(Gdx.files.internal(jsonPath));
    JsonValue buildings = root.get("buildings");
    if (buildings == null)
      throw new InvalidBuildingConfigException("missing 'buildings' array in " + jsonPath);

    for (JsonValue entry : buildings) {
      BuildingDefinition def = parse(entry, jsonPath);
      definitions.put(def.buildingType, def);
    }
    if (definitions.isEmpty()) {
      throw new InvalidBuildingConfigException("no resources defined in " + jsonPath);
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
    return new BuildingDefinition(
        type, displayName, widthTiles, heightTiles, constructionUnitsRequired);
  }

  public BuildingDefinition get(String type) {
    BuildingDefinition def = definitions.get(type);
    if (def == null) {
      throw new InvalidBuildingConfigException("unknown resource type '" + type + "'");
    }
    return def;
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
