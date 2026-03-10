package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.utils.Disposable;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import com.hedgecourt.aqueduct.world.entities.Worker;
import java.util.ArrayList;
import java.util.List;

public class AqueductWorld implements Disposable {
  private final List<WorldEntity> worldEntities = new ArrayList<>();
  private final List<Worker> workers = new ArrayList<>();
  private final List<Node> nodes = new ArrayList<>();
  private final List<TownHall> townHalls = new ArrayList<>();

  private final List<ConstructionEntityHelper> constructionPendingList = new ArrayList<>();

  private final ResourceConfig resourceConfig = new ResourceConfig();

  private TiledMap map;
  private MapGraph mapGraph;
  private Pathfinder pathfinder;

  private int tileWidth;
  private int tileHeight;
  private int mapTilesWide;
  private int mapTilesTall;

  public void update(float delta) {
    for (WorldEntity entity : worldEntities) {
      entity.update(delta);
    }
  }

  public void clear() {
    workers.clear();
    nodes.clear();
    townHalls.clear();
    worldEntities.clear();

    resourceConfig.clear();
  }

  public void add(WorldEntity entity) {
    worldEntities.add(entity);
    if (entity instanceof Worker worker) workers.add(worker);

    if (entity instanceof Node node) {
      nodes.add(node);
      updateWalkabilityForEntity(node, false);
    }
    if (entity instanceof TownHall townHall) {
      townHalls.add(townHall);
      updateWalkabilityForEntity(townHall, false);
    }
  }

  public void remove(WorldEntity entity) {
    worldEntities.remove(entity);
    if (entity instanceof Worker worker) workers.remove(worker);

    if (entity instanceof Node node) {
      nodes.remove(node);
      updateWalkabilityForEntity(node, true);
    }

    if (entity instanceof TownHall townHall) {
      townHalls.remove(townHall);
      updateWalkabilityForEntity(townHall, true);
    }
  }

  private void updateWalkabilityForEntity(WorldEntity entity, boolean isWalkable) {
    int tileX = (int) (entity.getPosition().x - entity.getWidth() / 2f) / tileWidth;
    int tileY = (int) (entity.getPosition().y - entity.getHeight() / 2f) / tileHeight;
    int tilesWide = (int) (entity.getWidth() / tileWidth);
    int tilesTall = (int) (entity.getHeight() / tileHeight);

    for (int x = tileX; x < tileX + tilesWide; x++) {
      for (int y = tileY; y < tileY + tilesTall; y++) {
        mapGraph.setWalkable(x, y, isWalkable);
      }
    }
  }

  public TownHall getNearestTownHall(WorldEntity entity) {
    // TODO shouldnt workers just figure out where they want to go on their own?
    TownHall nearest = null;
    float bestDist = Float.MAX_VALUE;
    for (TownHall townHall : townHalls) {
      float dist = townHall.distanceTo(entity);
      if (dist < bestDist) {
        bestDist = dist;
        nearest = townHall;
      }
    }
    return nearest;
  }

  public WorldEntity getEntityAt(float x, float y) {
    for (WorldEntity entity : worldEntities) {
      if (entity.containsPoint(x, y)) return entity;
    }
    return null;
  }

  public ResourceConfig getResourceConfig() {
    return resourceConfig;
  }

  public void initializeMap(
      TiledMap map,
      MapGraph mapGraph,
      Pathfinder pathfinder,
      int tileWidth,
      int tileHeight,
      int mapTilesWide,
      int mapTilesTall) {
    this.map = map;
    this.mapGraph = mapGraph;
    this.pathfinder = pathfinder;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    this.mapTilesWide = mapTilesWide;
    this.mapTilesTall = mapTilesTall;
  }

  public List<WorldEntity> getWorldEntities() {
    return worldEntities;
  }

  public List<Worker> getWorkers() {
    return workers;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public List<TownHall> getTownHalls() {
    return townHalls;
  }

  public void addConstructionPending(ConstructionEntityHelper helper) {
    constructionPendingList.add(helper);
  }

  public void removeConstructionPending(ConstructionEntityHelper helper) {
    constructionPendingList.remove(helper);
  }

  public List<ConstructionEntityHelper> getConstructionPendingList() {
    return constructionPendingList;
  }

  public int getTileWidth() {
    return tileWidth;
  }

  public int getTileHeight() {
    return tileHeight;
  }

  public float getMapWidth() {
    return mapTilesWide * tileWidth;
  }

  public float getMapHeight() {
    return mapTilesTall * tileHeight;
  }

  public TiledMap getMap() {
    return map;
  }

  public MapGraph getMapGraph() {
    return mapGraph;
  }

  public Pathfinder getPathfinder() {
    return pathfinder;
  }

  @Override
  public void dispose() {
    if (map != null) map.dispose();
  }
}
