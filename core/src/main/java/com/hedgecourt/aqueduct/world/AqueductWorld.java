package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.utils.Disposable;
import com.hedgecourt.aqueduct.SpriteFactory;
import com.hedgecourt.aqueduct.world.entities.Building;
import com.hedgecourt.aqueduct.world.entities.Entity;
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
  private final List<Entity> worldEntities = new ArrayList<>();

  private final Map<TownHall, Set<GridPoint2>> waterNetworkTiles = new HashMap<>();

  private SpriteFactory spriteFactory;
  private BuildingFactory buildingFactory;
  private ResourceFactory resourceFactory;
  private UnitFactory unitFactory;

  private TiledMap map;
  private MapGraph mapGraph;
  private Pathfinder pathfinder;

  private int tileWidth;
  private int tileHeight;
  private int mapTilesWide;
  private int mapTilesTall;

  public void update(float delta) {
    for (Entity entity : new ArrayList<>(worldEntities)) {
      entity.update(delta);
    }
    distributeWater(delta);
  }

  public void clear() {
    worldEntities.clear();
    if (buildingFactory != null) buildingFactory.clear();
    if (resourceFactory != null) resourceFactory.clear();
  }

  public void add(Entity entity) {
    worldEntities.add(entity);

    if (entity instanceof Building building) {
      recomputeWaterNetwork();
    }
  }

  public void remove(Entity entity) {
    worldEntities.remove(entity);

    if (entity instanceof Node node) {
      updateWalkabilityForEntity(node, true);
    }

    if (entity instanceof Building building) {
      updateWalkabilityForEntity(building, true);
      recomputeWaterNetwork();
    }
  }

  public void updateWalkabilityForEntity(Entity entity, boolean isWalkable) {
    int tileX = (int) (entity.getPosition().x - entity.getWidth() / 2f) / tileWidth;
    int tileY = (int) (entity.getPosition().y - entity.getHeight() / 2f) / tileHeight;
    int tilesWide = (int) (entity.getWidth() / tileWidth);
    int tilesTall = (int) (entity.getHeight() / tileHeight);

    for (int x = tileX; x < tileX + tilesWide; x++) {
      for (int y = tileY; y < tileY + tilesTall; y++) {
        mapGraph.setWalkable(x, y, isWalkable);
      }
    }

    // notify moving workers to recompute
    for (Worker worker : getEntities(Worker.class)) {
      if (worker.isMoving()) {
        worker.commandRecomputePath();
      }
    }
  }

  public Entity getEntityAt(float x, float y) {
    for (Entity entity : worldEntities) {
      if (entity.containsPoint(x, y)) return entity;
    }
    return null;
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

  public <T extends Entity> List<T> getEntities(Class<T> type) {
    List<T> result = new ArrayList<>();
    for (Entity entity : worldEntities) {
      if (type.isInstance(entity)) {
        result.add(type.cast(entity));
      }
    }
    return result;
  }

  public List<Entity> getEntities() {
    return worldEntities;
  }

  public TownHall getNearestTownHall(Entity entity) {
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

  public <T extends Building> T getSelectedBuilding(Class<T> type) {
    List<T> result = new ArrayList<>();
    for (Building building : getEntities(type)) {
      if (building.isSelected()) result.add(type.cast(building));
    }
    if (result.isEmpty()) return null;

    if (result.size() > 1)
      throw new IllegalStateException("Does not support multiple selected buildings");

    return result.getFirst();
  }

  public List<Node> getAbundantNodes() {
    List<Node> result = new ArrayList<>();

    for (Node node : getEntities(Node.class)) {
      if (!node.isEmpty()) result.add(node);
    }
    return result;
  }

  public List<Building> getIncompleteBuildings() {
    List<Building> result = new ArrayList<>();

    for (Building building : getEntities(Building.class)) {
      if (!building.isConstructionComplete()) result.add(building);
    }
    return result;
  }

  public List<Building> getConstructionPendingList() {
    List<Building> result = new ArrayList<>();

    for (Building building : getEntities(Building.class)) {
      if (!building.isConstructionStarted()) result.add(building);
    }
    return result;
  }

  public Building getIncompleteBuildingAt(float x, float y) {
    for (Building building : getIncompleteBuildings()) {
      if (building.containsPoint(x, y)) return building;
    }
    return null;
  }

  public Building getConstructionPendingAt(float x, float y) {
    for (Building building : getConstructionPendingList()) {
      if (building.containsPoint(x, y)) return building;
    }
    return null;
  }

  private void distributeWater(float delta) {
    List<TownHall> townHalls = getEntities(TownHall.class);

    for (TownHall th : townHalls) {
      // find eligible recipients
      List<Building> recipients = new ArrayList<>();
      for (Building building : getEntities(Building.class)) {

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

      for (Building recipient : recipients) {
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
    for (Building building : getEntities(Building.class)) {
      building.setWaterConnected(isWaterConnected(building));
    }
  }

  public boolean isWaterConnected(Building building) {
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

  private Set<GridPoint2> getEntityTiles(Entity entity) {
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

  public SpriteFactory getSpriteFactory() {
    return spriteFactory;
  }

  public void setSpriteFactory(SpriteFactory spriteFactory) {
    this.spriteFactory = spriteFactory;
  }

  public BuildingFactory getBuildingFactory() {
    return buildingFactory;
  }

  public void setBuildingFactory(BuildingFactory buildingFactory) {
    this.buildingFactory = buildingFactory;
  }

  public ResourceFactory getResourceFactory() {
    return resourceFactory;
  }

  public void setResourceFactory(ResourceFactory resourceFactory) {
    this.resourceFactory = resourceFactory;
  }

  public UnitFactory getUnitFactory() {
    return unitFactory;
  }

  public void setUnitFactory(UnitFactory unitFactory) {
    this.unitFactory = unitFactory;
  }

  @Override
  public void dispose() {
    if (map != null) map.dispose();
  }
}
