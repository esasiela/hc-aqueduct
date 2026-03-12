package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.hedgecourt.aqueduct.InvalidResourceConfigException;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.sprite.SpriteFactory;
import java.util.HashMap;
import java.util.Map;

public class ResourceFactory {

  private final Map<String, ResourceDefinition> defs = new HashMap<>();
  private final AqueductWorld world;
  private final SpriteFactory spriteFactory;

  public ResourceFactory(AqueductWorld world) {
    this.world = world;
    this.spriteFactory = new SpriteFactory();
  }

  public void loadAssets(String jsonPath, AssetManager assetManager) {
    JsonValue root = new JsonReader().parse(Gdx.files.internal(jsonPath));
    JsonValue resources = root.get("resources");
    if (resources == null)
      throw new InvalidResourceConfigException("missing 'resources' array in " + jsonPath);

    for (JsonValue entry : resources) {
      JsonValue spriteInfo = entry.get("spriteInfo");
      if (spriteInfo == null) continue;

      EntitySprite sprite = spriteFactory.create(spriteInfo);
      for (String path : sprite.assetPaths()) {
        assetManager.load(path, Texture.class);
      }
    }
  }

  public void loadDefinitions(String jsonPath, AssetManager assetManager) {
    JsonValue root = new JsonReader().parse(Gdx.files.internal(jsonPath));
    JsonValue resources = root.get("resources");
    if (resources == null)
      throw new InvalidResourceConfigException("missing 'resources' array in " + jsonPath);

    for (JsonValue entry : resources) {
      ResourceDefinition def = parse(entry, jsonPath);

      JsonValue spriteInfo = entry.get("spriteInfo");
      def.sprite = (spriteInfo != null) ? spriteFactory.create(spriteInfo) : null;

      defs.put(def.type, def);
    }
    if (defs.isEmpty()) {
      throw new InvalidResourceConfigException("no resources defined in " + jsonPath);
    }
  }

  public void buildSprites(AssetManager assetManager) {
    for (ResourceDefinition def : defs.values()) {
      if (def.sprite != null) {
        def.sprite.build(assetManager);
      }
    }
  }

  public void clear() {
    defs.clear();
  }

  private ResourceDefinition parse(JsonValue e, String jsonPath) {
    String type = requireString(e, "type", jsonPath);
    String displayName = requireString(e, "displayName", jsonPath);
    float regenRate = requireFloat(e, "regenRate", jsonPath);
    float maxInventory = requireFloat(e, "maxInventory", jsonPath);
    float harvestRate = requireFloat(e, "harvestRate", jsonPath);
    return new ResourceDefinition(type, displayName, regenRate, maxInventory, harvestRate);
  }

  public ResourceDefinition get(String type) {
    ResourceDefinition def = defs.get(type);
    if (def == null) {
      throw new InvalidResourceConfigException("unknown resource type '" + type + "'");
    }
    return def;
  }

  // ── fail-fast helpers ─────────────────────────────────────────────────────

  private String requireString(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new InvalidResourceConfigException("missing required field '" + key + "' in " + path);
    }
    return e.getString(key);
  }

  private float requireFloat(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new InvalidResourceConfigException("missing required field '" + key + "' in " + path);
    }
    return e.getFloat(key);
  }

  private int requireInt(JsonValue e, String key, String path) {
    if (!e.has(key) || e.get(key).isNull()) {
      throw new InvalidResourceConfigException("missing required field '" + key + "' in " + path);
    }
    return e.getInt(key);
  }
}
