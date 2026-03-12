package com.hedgecourt.aqueduct;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.sprite.PipoyaBaseNodeSprite;
import com.hedgecourt.aqueduct.sprite.PipoyaBaseSprite;
import com.hedgecourt.aqueduct.sprite.SingleTextureSprite;
import com.hedgecourt.aqueduct.sprite.TwoFramePngSprite;
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
          PipoyaBaseNodeSprite.class,
          "TWO_FRAME_PNG",
          TwoFramePngSprite.class);

  private final ObjectMapper jackson = new ObjectMapper();

  public EntitySprite create(JsonNode spriteInfo) {
    String type = spriteInfo.get("type").asText();
    Class<? extends EntitySprite> implClass = SPRITE_TYPES.get(type);
    if (implClass == null) {
      throw new RuntimeException("Unknown spriteInfo type: " + type);
    }
    try {
      return jackson.treeToValue(spriteInfo, implClass);
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse spriteInfo: " + e.getMessage());
    }
  }

  public List<String> assetPaths(JsonNode spriteInfo) {
    return create(spriteInfo).assetPaths();
  }
}
