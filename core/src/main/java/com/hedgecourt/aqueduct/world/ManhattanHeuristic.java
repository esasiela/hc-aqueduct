package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.ai.pfa.Heuristic;

public class ManhattanHeuristic implements Heuristic<MapNode> {

  @Override
  public float estimate(MapNode node, MapNode endNode) {
    return Math.abs(node.tileX() - endNode.tileX()) + Math.abs(node.tileY() - endNode.tileY());
  }
}
