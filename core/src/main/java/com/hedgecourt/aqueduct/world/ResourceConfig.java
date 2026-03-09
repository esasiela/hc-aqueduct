package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import java.util.HashMap;
import java.util.Map;

public class ResourceConfig {

  private final Map<String, ResourceDef> defs = new HashMap<>();

  public void load(String jsonPath) {
    JsonValue root = new JsonReader().parse(Gdx.files.internal(jsonPath));
    JsonValue resources = root.get("resources");
    if (resources == null) {
      throw new RuntimeException("ResourceConfig: missing 'resources' array in " + jsonPath);
    }
    for (JsonValue entry : resources) {
      ResourceDef def = parse(entry, jsonPath);
      defs.put(def.type, def);
    }
    if (defs.isEmpty()) {
      throw new RuntimeException("ResourceConfig: no resources defined in " + jsonPath);
    }
  }

  public void clear() {
    defs.clear();
  }

  private ResourceDef parse(JsonValue e, String jsonPath) {
    String type = requireString(e, "type", jsonPath);
    String displayName = requireString(e, "displayName", jsonPath);
    float regenRate = requireFloat(e, "regenRate", jsonPath);
    float maxInventory = requireFloat(e, "maxInventory", jsonPath);
    float harvestRate = requireFloat(e, "harvestRate", jsonPath);
    int spriteIdEmpty = requireInt(e, "spriteIdEmpty", jsonPath);
    int spriteIdFull = requireInt(e, "spriteIdFull", jsonPath);
    return new ResourceDef(
        type, displayName, regenRate, maxInventory, harvestRate, spriteIdEmpty, spriteIdFull);
  }

  public ResourceDef get(String type) {
    ResourceDef def = defs.get(type);
    if (def == null) {
      throw new RuntimeException("ResourceConfig: unknown resource type '" + type + "'");
    }
    return def;
  }

  // ── fail-fast helpers ─────────────────────────────────────────────────────

  private String requireString(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new RuntimeException("ResourceConfig: missing required field '" + key + "' in " + path);
    }
    return e.getString(key);
  }

  private float requireFloat(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new RuntimeException("ResourceConfig: missing required field '" + key + "' in " + path);
    }
    return e.getFloat(key);
  }

  private int requireInt(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new RuntimeException("ResourceConfig: missing required field '" + key + "' in " + path);
    }
    return e.getInt(key);
  }
}
