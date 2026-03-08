package com.hedgecourt.aqueduct.world.layers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.hedgecourt.aqueduct.world.ResourceConfig;
import com.hedgecourt.aqueduct.world.WorldLayer;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import java.util.ArrayList;
import java.util.List;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class EntityLayer extends WorldLayer {

  private final List<Node> nodes = new ArrayList<>();
  private final List<TownHall> townHalls = new ArrayList<>();
  private final ResourceConfig resourceConfig;

  public EntityLayer(ResourceConfig resourceConfig) {
    this.resourceConfig = resourceConfig;
  }

  // ── map loading ───────────────────────────────────────────────────────────

  public void loadFromMap(TiledMap map, float tileHeight) {
    nodes.clear();
    townHalls.clear();

    MapObjects objects = map.getLayers().get("entities").getObjects();
    for (MapObject obj : objects) {
      String objClass = obj.getProperties().get("type", String.class);
      float x = obj.getProperties().get("x", Float.class);
      float y = obj.getProperties().get("y", Float.class);
      float w = obj.getProperties().get("width", Float.class);
      float h = obj.getProperties().get("height", Float.class);

      int rawId = obj.getProperties().get("id", Integer.class);
      String id = objClass + "_" + rawId;

      // Tiled Y is top-left, convert to center position with Y-up
      float centreX = x + w / 2f;
      float centreY = y + h / 2f;

      if ("node".equals(objClass)) {
        String resourceType = obj.getName();
        if (resourceType == null || resourceType.isEmpty()) {
          throw new RuntimeException(
              "EntityLayer: node object missing name (resourceType) " + "at (" + x + "," + y + ")");
        }
        nodes.add(new Node(id, resourceType, centreX, centreY, w, h, resourceConfig));

      } else if ("townhall".equals(objClass)) {
        townHalls.add(new TownHall(id, centreX, centreY, w, h));

      } else {
        throw new RuntimeException(
            "EntityLayer: unknown entity class '" + objClass + "' at (" + x + "," + y + ")");
      }
    }

    if (townHalls.isEmpty()) {
      throw new RuntimeException("EntityLayer: no townhall found in entities layer");
    }
  }

  public Node getNodeAt(float x, float y) {
    for (Node node : nodes) {
      if (node.containsPoint(x, y)) {
        return node;
      }
    }
    return null;
  }

  public TownHall getNearestTownHall(float x, float y) {
    TownHall nearest = null;
    float bestDist = Float.MAX_VALUE;
    for (TownHall th : townHalls) {
      float dist = th.distanceTo(x, y);
      if (dist < bestDist) {
        bestDist = dist;
        nearest = th;
      }
    }
    return nearest;
  }

  // ── accessors ─────────────────────────────────────────────────────────────

  public List<Node> getNodes() {
    return nodes;
  }

  public List<TownHall> getTownHalls() {
    return townHalls;
  }

  // ── update ────────────────────────────────────────────────────────────────

  @Override
  public void update(float delta) {
    for (Node node : nodes) node.update(delta);
    for (TownHall th : townHalls) th.update(delta);
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void drawEntities(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    for (Node node : nodes) node.draw(batch, shapeDrawer);
    for (TownHall th : townHalls) th.draw(batch, shapeDrawer);
  }
}
