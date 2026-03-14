package com.hedgecourt.aqueduct.factory;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.entities.Node;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeFactory {

  @JsonIgnoreProperties("scratch")
  private static class NodesJson {
    public List<NodeDefinition> nodes;
    public Object scratch;
  }

  private final Map<String, NodeDefinition> definitions = new HashMap<>();
  private final AqueductWorld world;
  private final SpriteFactory spriteFactory;
  private final ObjectMapper jackson = new ObjectMapper();

  public NodeFactory(AqueductWorld world) {
    this.world = world;
    spriteFactory = world.getSpriteFactory();
  }

  public Node create(String type, float x, float y) {
    NodeDefinition def = definitions.get(type);
    if (def == null) throw new InvalidNodeConfigException("no definition for type: " + type);

    float w = def.widthTiles * world.getTileWidth();
    float h = def.heightTiles * world.getTileHeight();

    Node node = new Node(world, x, y, w, h);

    node.setType(type);
    node.setDisplayName(def.displayName);
    node.setItemType(def.itemType);

    node.setWalkable(def.walkable);

    node.setRegenRate(def.regenRate);
    node.setRegenCooldown(def.regenCooldown);
    node.setMaxInventory(def.maxInventory);
    node.setHarvestRate(def.harvestRate);

    node.setSprite(def.sprite.freshCopy());

    return node;
  }

  public void loadAssets(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      NodesJson wrapper = jackson.readValue(json, NodesJson.class);
      if (wrapper.nodes == null)
        throw new InvalidNodeConfigException("no nodes defined in " + jsonPath);

      for (NodeDefinition def : wrapper.nodes) {
        if (def.spriteInfo == null) continue;
        EntitySprite sprite = spriteFactory.create(def.spriteInfo);
        for (String path : sprite.assetPaths()) {
          assetManager.load(path, Texture.class);
        }
      }
    } catch (Exception e) {
      throw new InvalidNodeConfigException("failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void loadDefinitions(String jsonPath, AssetManager assetManager) {
    try {
      String json = Gdx.files.internal(jsonPath).readString();
      NodesJson wrapper = jackson.readValue(json, NodesJson.class);

      if (wrapper.nodes == null || wrapper.nodes.isEmpty())
        throw new InvalidNodeConfigException("no nodes defined in " + jsonPath);

      for (NodeDefinition def : wrapper.nodes) {
        if (def.spriteInfo != null) {
          def.sprite = spriteFactory.create(def.spriteInfo);
        }
        definitions.put(def.type, def);
      }
    } catch (InvalidNodeConfigException e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidNodeConfigException("failed to parse " + jsonPath + ": " + e.getMessage());
    }
  }

  public void buildSprites(AssetManager assetManager) {
    for (NodeDefinition def : definitions.values()) {
      if (def.sprite != null) {
        def.sprite.build(assetManager);
      }
    }
  }

  public void clear() {
    definitions.clear();
  }

  public NodeDefinition get(String type) {
    NodeDefinition def = definitions.get(type);
    if (def == null) {
      throw new InvalidNodeConfigException("unknown node type '" + type + "'");
    }
    return def;
  }
}
