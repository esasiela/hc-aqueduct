package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.List;

public class Pathfinder {

  private final MapGraph graph;
  private final IndexedAStarPathFinder<MapNode> pathFinder;
  private final ManhattanHeuristic heuristic = new ManhattanHeuristic();
  private final int tileWidth;
  private final int tileHeight;

  public Pathfinder(MapGraph graph, int tileWidth, int tileHeight) {
    this.graph = graph;
    this.tileWidth = tileWidth;
    this.tileHeight = tileHeight;
    this.pathFinder = new IndexedAStarPathFinder<>(graph);
  }

  /**
   * Find a path from world position (startX, startY) to (goalX, goalY). Returns a list of
   * world-space waypoints (tile centers), or empty list if no path found.
   */
  public List<Vector2> findPath(float startX, float startY, float goalX, float goalY) {
    MapNode startNode = worldToNode(startX, startY);
    MapNode goalNode = worldToNode(goalX, goalY);

    List<Vector2> waypoints = new ArrayList<>();

    if (startNode == null || goalNode == null) return waypoints;
    if (!graph.isWalkable(startNode.tileX(), startNode.tileY())) return waypoints;
    if (!graph.isWalkable(goalNode.tileX(), goalNode.tileY())) return waypoints;

    GraphPath<MapNode> path = new DefaultGraphPath<>();
    boolean found = pathFinder.searchNodePath(startNode, goalNode, heuristic, path);

    if (!found) return waypoints;

    for (MapNode node : path) {
      waypoints.add(tileCenter(node.tileX(), node.tileY()));
    }

    return waypoints;
  }

  public Vector2 nearestWalkableApproach(WorldEntity to, WorldEntity from) {
    return nearestWalkableApproach(to, from.getPosition().x, from.getPosition().y);
  }

  public Vector2 nearestWalkableApproach(WorldEntity entity, float fromX, float fromY) {
    int tileW = tileWidth;
    int tileH = tileHeight;

    // entity tile footprint
    int left = (int) ((entity.getPosition().x - entity.getWidth() / 2f) / tileW);
    int bottom = (int) ((entity.getPosition().y - entity.getHeight() / 2f) / tileH);
    int right = (int) ((entity.getPosition().x + entity.getWidth() / 2f - 1) / tileW);
    int top = (int) ((entity.getPosition().y + entity.getHeight() / 2f - 1) / tileH);

    // expand by 1 to get adjacent ring
    int ringLeft = left - 1;
    int ringBottom = bottom - 1;
    int ringRight = right + 1;
    int ringTop = top + 1;

    Vector2 best = null;
    float bestDist = Float.MAX_VALUE;

    for (int tx = ringLeft; tx <= ringRight; tx++) {
      for (int ty = ringBottom; ty <= ringTop; ty++) {
        // skip the entity's own footprint tiles
        if (tx >= left && tx <= right && ty >= bottom && ty <= top) continue;
        if (!graph.isWalkable(tx, ty)) continue;

        float cx = tx * tileW + tileW / 2f;
        float cy = ty * tileH + tileH / 2f;
        float dist = Vector2.dst(fromX, fromY, cx, cy);
        if (dist < bestDist) {
          bestDist = dist;
          best = new Vector2(cx, cy);
        }
      }
    }
    return best; // null if completely surrounded by unwalkable tiles
  }

  private MapNode worldToNode(float worldX, float worldY) {
    int tileX = (int) (worldX / tileWidth);
    int tileY = (int) (worldY / tileHeight);
    return graph.getNode(tileX, tileY);
  }

  private Vector2 tileCenter(int tileX, int tileY) {
    return new Vector2(tileX * tileWidth + tileWidth / 2f, tileY * tileHeight + tileHeight / 2f);
  }
}
