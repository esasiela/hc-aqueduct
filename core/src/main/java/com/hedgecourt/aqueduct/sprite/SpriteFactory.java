package com.hedgecourt.aqueduct.sprite;

import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;

public class SpriteFactory {

  private static final Map<String, Class<? extends EntitySprite>> SPRITE_TYPES =
      Map.of(
          "PNG",
          SingleTextureSprite.class,
          "PIPOYA_BASE",
          PipoyaBaseSprite.class,
          "PIPOYA_BASE_NODE",
          PipoyaBaseNodeSprite.class);

  private final ObjectMapper jackson = new ObjectMapper();

  public EntitySprite create(JsonValue spriteInfo) {
    String type = spriteInfo.getString("type");
    Class<? extends EntitySprite> implClass = SPRITE_TYPES.get(type);
    if (implClass == null) {
      throw new RuntimeException("Unknown spriteInfo type: " + type);
    }
    try {
      return jackson.treeToValue(
          jackson.readTree(spriteInfo.toJson(JsonWriter.OutputType.json)), implClass);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse spriteInfo: " + e.getMessage());
    }
  }

  public List<String> assetPaths(JsonValue spriteInfo) {
    return create(spriteInfo).assetPaths();
  }
}
