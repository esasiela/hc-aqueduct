package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Disposable;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.Pipe;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import com.hedgecourt.aqueduct.world.entities.Worker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class AqueductWorld implements Disposable {
  private final List<WorldEntity> worldEntities = new ArrayList<>();
  private final List<Worker> workers = new ArrayList<>();
  private final List<Node> nodes = new ArrayList<>();

  private final Map<TownHall, Set<GridPoint2>> waterNetworkTiles = new HashMap<>();

  private final ResourceConfig resourceConfig = new ResourceConfig();

  private BuildingFactory buildingFactory;

  private TiledMap map;
  private MapGraph mapGraph;
  private Pathfinder pathfinder;

  private int tileWidth;
  private int tileHeight;
  private int mapTilesWide;
  private int mapTilesTall;

  public void update(float delta) {
    for (WorldEntity entity : new ArrayList<>(worldEntities)) {
      entity.update(delta);
    }
    distributeWater(delta);
  }

  public void clear() {
    workers.clear();
    nodes.clear();
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

    if (entity instanceof BuildingEntity building) {
      updateWalkabilityForEntity(building, false);
      recomputeWaterNetwork();
    }
  }

  public void remove(WorldEntity entity) {
    worldEntities.remove(entity);
    if (entity instanceof Worker worker) workers.remove(worker);

    if (entity instanceof Node node) {
      nodes.remove(node);
      updateWalkabilityForEntity(node, true);
    }

    if (entity instanceof BuildingEntity building) {
      updateWalkabilityForEntity(building, true);
      recomputeWaterNetwork();
    }
  }

  public void updateWalkabilityForEntity(WorldEntity entity, boolean isWalkable) {
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
    for (TownHall townHall : getEntities(TownHall.class)) {
      if (!townHall.isConstructionComplete()) continue;

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

  public <T extends WorldEntity> List<T> getEntities(Class<T> type) {
    List<T> result = new ArrayList<>();
    for (WorldEntity entity : worldEntities) {
      if (type.isInstance(entity)) {
        result.add(type.cast(entity));
      }
    }
    return result;
  }

  public List<WorldEntity> getEntities() {
    return worldEntities;
  }

  public List<Worker> getWorkers() {
    return workers;
  }

  public List<Node> getNodes() {
    return nodes;
  }

  public List<BuildingEntity> getIncompleteBuildings() {
    List<BuildingEntity> result = new ArrayList<>();

    for (BuildingEntity building : getEntities(BuildingEntity.class)) {
      if (!building.isConstructionComplete()) result.add(building);
    }
    return result;
  }

  public List<BuildingEntity> getConstructionPendingList() {
    List<BuildingEntity> result = new ArrayList<>();

    for (BuildingEntity building : getEntities(BuildingEntity.class)) {
      if (!building.isConstructionStarted()) result.add(building);
    }
    return result;
  }

  public BuildingEntity getIncompleteBuildingAt(float x, float y) {
    for (BuildingEntity building : getIncompleteBuildings()) {
      if (building.containsPoint(x, y)) return building;
    }
    return null;
  }

  public BuildingEntity getConstructionPendingAt(float x, float y) {
    for (BuildingEntity building : getConstructionPendingList()) {
      if (building.containsPoint(x, y)) return building;
    }
    return null;
  }

  private void distributeWater(float delta) {
    List<TownHall> townHalls = getEntities(TownHall.class);

    for (TownHall th : townHalls) {
      // find eligible recipients
      List<BuildingEntity> recipients = new ArrayList<>();
      for (BuildingEntity building : getEntities(BuildingEntity.class)) {

        if (building instanceof TownHall) continue;
        if (!building.isConstructionComplete()) continue;
        if (!building.isWaterConnected()) continue;
        if (building.getWaterInventory() >= building.getWaterCapacity()) continue;
        recipients.add(building);
      }

      if (recipients.isEmpty()) continue;

      float available = Math.min(th.getWaterInventory(), th.getWaterOutputRate() * delta);

      float share = available / recipients.size();
      float totalDistributed = 0f;

      for (BuildingEntity recipient : recipients) {
        float space = recipient.getWaterCapacity() - recipient.getWaterInventory();
        float amount = Math.min(share, space);
        recipient.setWaterInventory(recipient.getWaterInventory() + amount);
        totalDistributed += amount;
      }

      th.setWaterInventory(th.getWaterInventory() - totalDistributed);
    }
  }

  public void recomputeWaterNetwork() {
    waterNetworkTiles.clear();

    List<TownHall> townHalls = getEntities(TownHall.class);
    Set<GridPoint2> pipeTiles = new HashSet<>();

    // build set of all pipe tiles
    for (Pipe pipe : getEntities(Pipe.class)) {
      if (!pipe.isConstructionComplete()) continue;
      pipeTiles.addAll(getEntityTiles(pipe));
    }

    // flood fill from each TownHall
    for (TownHall th : townHalls) {
      Set<GridPoint2> reachable = new HashSet<>();
      Queue<GridPoint2> frontier = new LinkedList<>();

      // seed frontier with TownHall's own tiles
      for (GridPoint2 tile : getEntityTiles(th)) {
        frontier.add(tile);
        reachable.add(tile);
      }

      while (!frontier.isEmpty()) {
        GridPoint2 current = frontier.poll();
        for (GridPoint2 neighbor : getCardinalNeighbors(current)) {
          if (!reachable.contains(neighbor) && pipeTiles.contains(neighbor)) {
            reachable.add(neighbor);
            frontier.add(neighbor);
          }
        }
      }

      waterNetworkTiles.put(th, reachable);
    }

    // update waterConnected on all buildings
    for (BuildingEntity building : getEntities(BuildingEntity.class)) {
      building.setWaterConnected(isWaterConnected(building));
    }
  }

  public boolean isWaterConnected(BuildingEntity building) {
    if (building instanceof TownHall) return true;

    Set<GridPoint2> adjacentTiles = new HashSet<>();
    for (GridPoint2 tile : getEntityTiles(building)) {
      adjacentTiles.addAll(getCardinalNeighbors(tile));
    }

    for (Set<GridPoint2> reachable : waterNetworkTiles.values()) {
      for (GridPoint2 adj : adjacentTiles) {
        if (reachable.contains(adj)) return true;
      }
    }
    return false;
  }

  private Set<GridPoint2> getEntityTiles(WorldEntity entity) {
    Set<GridPoint2> tiles = new HashSet<>();
    int left = (int) ((entity.getPosition().x - entity.getWidth() / 2f) / tileWidth);
    int bottom = (int) ((entity.getPosition().y - entity.getHeight() / 2f) / tileHeight);
    int right = (int) ((entity.getPosition().x + entity.getWidth() / 2f - 1) / tileWidth);
    int top = (int) ((entity.getPosition().y + entity.getHeight() / 2f - 1) / tileHeight);
    for (int tx = left; tx <= right; tx++) {
      for (int ty = bottom; ty <= top; ty++) {
        tiles.add(new GridPoint2(tx, ty));
      }
    }
    return tiles;
  }

  private List<GridPoint2> getCardinalNeighbors(GridPoint2 tile) {
    return List.of(
        new GridPoint2(tile.x + 1, tile.y),
        new GridPoint2(tile.x - 1, tile.y),
        new GridPoint2(tile.x, tile.y + 1),
        new GridPoint2(tile.x, tile.y - 1));
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

  public BuildingFactory getBuildingFactory() {
    return buildingFactory;
  }

  public void setBuildingFactory(BuildingFactory buildingFactory) {
    this.buildingFactory = buildingFactory;
  }

  @Override
  public void dispose() {
    if (map != null) map.dispose();
  }
}
