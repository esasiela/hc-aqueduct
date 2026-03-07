package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultConnection;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.utils.Array;

public class MapGraph implements IndexedGraph<MapNode> {

  private final int mapWidth;
  private final int mapHeight;
  private final MapNode[][] nodes;
  private final boolean[][] walkable;
  private final float[][] moveCost;

  public MapGraph(int mapWidth, int mapHeight, TiledMapTileLayer wallsLayer) {
    this.mapWidth = mapWidth;
    this.mapHeight = mapHeight;

    walkable = new boolean[mapWidth][mapHeight];
    moveCost = new float[mapWidth][mapHeight];
    nodes = new MapNode[mapWidth][mapHeight];

    // initialize all tiles
    for (int x = 0; x < mapWidth; x++) {
      for (int y = 0; y < mapHeight; y++) {
        walkable[x][y] = wallsLayer.getCell(x, y) == null;
        moveCost[x][y] = 1.0f;
        nodes[x][y] = new MapNode(x, y, x + y * mapWidth);
      }
    }
  }

  // ── graph queries ─────────────────────────────────────────────────────────

  public boolean isWalkable(int tileX, int tileY) {
    if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) return false;
    return walkable[tileX][tileY];
  }

  public void setWalkable(int tileX, int tileY, boolean walkable) {
    if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) return;
    this.walkable[tileX][tileY] = walkable;
  }

  public float getMoveCost(int tileX, int tileY) {
    if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) return Float.MAX_VALUE;
    return moveCost[tileX][tileY];
  }

  public void setMoveCost(int tileX, int tileY, float cost) {
    if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) return;
    this.moveCost[tileX][tileY] = cost;
  }

  public MapNode getNode(int tileX, int tileY) {
    if (tileX < 0 || tileX >= mapWidth || tileY < 0 || tileY >= mapHeight) return null;
    return nodes[tileX][tileY];
  }

  // ── IndexedGraph implementation ───────────────────────────────────────────

  @Override
  public Array<Connection<MapNode>> getConnections(MapNode fromNode) {
    Array<Connection<MapNode>> connections = new Array<>();
    int x = fromNode.tileX();
    int y = fromNode.tileY();

    addIfWalkable(connections, fromNode, x + 1, y);
    addIfWalkable(connections, fromNode, x - 1, y);
    addIfWalkable(connections, fromNode, x, y + 1);
    addIfWalkable(connections, fromNode, x, y - 1);

    return connections;
  }

  private void addIfWalkable(
      Array<Connection<MapNode>> connections, MapNode from, int toX, int toY) {
    if (isWalkable(toX, toY)) {
      connections.add(new DefaultConnection<>(from, nodes[toX][toY]));
    }
  }

  @Override
  public int getIndex(MapNode node) {
    return node.index();
  }

  @Override
  public int getNodeCount() {
    return mapWidth * mapHeight;
  }
}
