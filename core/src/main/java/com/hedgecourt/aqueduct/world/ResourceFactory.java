package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgecourt.aqueduct.InvalidResourceConfigException;
import com.hedgecourt.aqueduct.SpriteFactory;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceFactory {

  private static class ResourcesJson {
    public List<ResourceDefinition> resources;
  }

  private final Map<String, ResourceDefinition> defs = new HashMap<>();
  private final SpriteFactory spriteFactory;
  private final ObjectMapper jackson = new ObjectMapper();

  public ResourceFactory(AqueductWorld world) {
    this.spriteFactory = world.getSpriteFactory();
  }

  public void loadAssets(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      ResourcesJson wrapper = jackson.readValue(json, ResourcesJson.class);
      if (wrapper.resources == null) return;

      for (ResourceDefinition def : wrapper.resources) {
        if (def.spriteInfo == null) continue;
        EntitySprite sprite = spriteFactory.create(def.spriteInfo);
        for (String path : sprite.assetPaths()) {
          assetManager.load(path, Texture.class);
        }
      }
    } catch (Exception e) {
      throw new InvalidResourceConfigException(
          "failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void loadDefinitions(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      ResourcesJson wrapper = jackson.readValue(json, ResourcesJson.class);

      if (wrapper.resources == null || wrapper.resources.isEmpty())
        throw new InvalidResourceConfigException("no resources defined in " + jsonPath);

      for (ResourceDefinition def : wrapper.resources) {
        if (def.spriteInfo != null) {
          def.sprite = spriteFactory.create(def.spriteInfo);
        }
        defs.put(def.type, def);
      }
    } catch (InvalidResourceConfigException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidResourceConfigException(
          "failed to parse " + jsonPath + ": " + e.getMessage());
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

  public ResourceDefinition get(String type) {
    ResourceDefinition def = defs.get(type);
    if (def == null) {
      throw new InvalidResourceConfigException("unknown resource type '" + type + "'");
    }
    return def;
  }
}
