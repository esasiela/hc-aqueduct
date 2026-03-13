package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgecourt.aqueduct.InvalidUnitConfigException;
import com.hedgecourt.aqueduct.SpriteFactory;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.world.entities.Unit;
import com.hedgecourt.aqueduct.world.entities.Worker;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitFactory {

  @JsonIgnoreProperties("scratch")
  private static class UnitsJson {
    public List<UnitDefinition> units;
    public Object scratch;
  }

  private final Map<String, UnitDefinition> definitions = new HashMap<>();
  private final AqueductWorld world;
  private final SpriteFactory spriteFactory;
  private final ObjectMapper jackson = new ObjectMapper();

  public UnitFactory(AqueductWorld world) {
    this.world = world;
    spriteFactory = world.getSpriteFactory();
  }

  public Unit create(String type, float x, float y) {
    UnitDefinition def = definitions.get(type);
    if (def == null) throw new InvalidUnitConfigException("no defaults for type: " + type);

    float w = def.widthPx;
    float h = def.heightPx;

    Unit unit =
        switch (type) {
          case "worker" -> new Worker(world, x, y, w, h);
          default -> throw new InvalidUnitConfigException("unknown type: " + type);
        };
    unit.setType(type);

    unit.setDisplayName(def.displayName);
    unit.setTrainingPointsRequired(def.trainingPointsRequired);

    unit.setSprite(def.sprite.freshCopy());

    return unit;
  }

  public void loadAssets(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      UnitsJson wrapper = jackson.readValue(json, UnitsJson.class);
      if (wrapper.units == null)
        throw new InvalidUnitConfigException("no units defined in " + jsonPath);

      for (UnitDefinition def : wrapper.units) {
        if (def.spriteInfo == null) continue;
        EntitySprite sprite = spriteFactory.create(def.spriteInfo);
        for (String path : sprite.assetPaths()) {
          assetManager.load(path, Texture.class);
        }
      }
    } catch (Exception e) {
      throw new InvalidUnitConfigException("failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void loadDefinitions(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      UnitsJson wrapper = jackson.readValue(json, UnitsJson.class);

      if (wrapper.units == null || wrapper.units.isEmpty())
        throw new InvalidUnitConfigException("no units defined in " + jsonPath);

      for (UnitDefinition def : wrapper.units) {
        if (def.spriteInfo != null) {
          def.sprite = spriteFactory.create(def.spriteInfo);
        }
        definitions.put(def.type, def);
      }
    } catch (InvalidUnitConfigException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidUnitConfigException("failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void buildSprites(AssetManager assetManager) {
    for (UnitDefinition def : definitions.values()) {
      if (def.sprite != null) {
        def.sprite.build(assetManager);
      }
    }
  }

  public void clear() {
    definitions.clear();
  }

  public UnitDefinition get(String type) {
    UnitDefinition def = definitions.get(type);
    if (def == null) {
      throw new InvalidUnitConfigException("unknown unit type '" + type + "'");
    }
    return def;
  }
}
