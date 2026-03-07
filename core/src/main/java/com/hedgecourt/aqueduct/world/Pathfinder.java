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

  private MapNode worldToNode(float worldX, float worldY) {
    int tileX = (int) (worldX / tileWidth);
    int tileY = (int) (worldY / tileHeight);
    return graph.getNode(tileX, tileY);
  }

  private Vector2 tileCenter(int tileX, int tileY) {
    return new Vector2(tileX * tileWidth + tileWidth / 2f, tileY * tileHeight + tileHeight / 2f);
  }
}
