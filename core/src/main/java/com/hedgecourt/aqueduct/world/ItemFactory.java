package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgecourt.aqueduct.InvalidItemConfigException;
import com.hedgecourt.aqueduct.SpriteFactory;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemFactory {

  private static class ItemsJson {
    public List<ItemDefinition> items;
  }

  private final Map<String, ItemDefinition> definitions = new HashMap<>();
  private final SpriteFactory spriteFactory;
  private final ObjectMapper jackson = new ObjectMapper();

  public ItemFactory(AqueductWorld world) {
    this.spriteFactory = world.getSpriteFactory();
  }

  public void loadAssets(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      ItemsJson wrapper = jackson.readValue(json, ItemsJson.class);
      if (wrapper.items == null)
        throw new InvalidItemConfigException("no items defined in " + jsonPath);

      for (ItemDefinition def : wrapper.items) {
        if (def.spriteInfo == null) continue;
        EntitySprite sprite = spriteFactory.create(def.spriteInfo);
        for (String path : sprite.assetPaths()) {
          assetManager.load(path, Texture.class);
        }
      }
    } catch (Exception e) {
      throw new InvalidItemConfigException("failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void loadDefinitions(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      ItemsJson wrapper = jackson.readValue(json, ItemsJson.class);

      if (wrapper.items == null || wrapper.items.isEmpty())
        throw new InvalidItemConfigException("no items defined in " + jsonPath);

      for (ItemDefinition def : wrapper.items) {
        if (def.spriteInfo != null) {
          def.sprite = spriteFactory.create(def.spriteInfo);
        }
        definitions.put(def.type, def);
      }
    } catch (InvalidItemConfigException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidItemConfigException("failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void buildSprites(AssetManager assetManager) {
    for (ItemDefinition def : definitions.values()) {
      if (def.sprite != null) {
        def.sprite.build(assetManager);
      }
    }
  }

  public void clear() {
    definitions.clear();
  }

  public ItemDefinition get(String type) {
    ItemDefinition def = definitions.get(type);
    if (def == null) {
      throw new InvalidItemConfigException("unknown item type '" + type + "'");
    }
    return def;
  }
}
