package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.sprite.DirectionalAnimatedSprite;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.entities.Worker.WorkerPlan.PlanType;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Worker extends Unit {

  // ── direction ─────────────────────────────────────────────────────────────

  public enum Direction {
    SOUTH,
    WEST,
    EAST,
    NORTH
  }

  // ── state ─────────────────────────────────────────────────────────────────

  public enum WorkerState {
    IDLE,
    MOVING,
    HARVESTING,
    DELIVERING,
    CONSTRUCTING
  }

  // ── animation ─────────────────────────────────────────────────────────────

  private Map<Direction, Animation<TextureRegion>> animations;
  private float animTime = 0f;
  private final int IDLE_FRAME = 1;

  // ── movement ──────────────────────────────────────────────────────────────

  private WorkerState state = WorkerState.IDLE;
  private final WorkerPlan plan;

  private final Deque<Vector2> waypoints = new LinkedList<>();

  private float moveSpeed;
  private Direction facing = Direction.SOUTH;

  private float carrying = 0f;
  private float carryCapacity;
  private String carryingType = null;

  private final LinkedList<String> nodeMemory = new LinkedList<>();

  // ── constructor ───────────────────────────────────────────────────────────

  public Worker(AqueductWorld world, float x, float y, float w, float h) {
    // TODO worker size needs to be defined elsewhere than hardcoded C
    super(world, x, y, w, h);
    this.moveSpeed = C.WORKER_BASE_SPEED;
    this.carryCapacity = C.WORKER_CARRY_CAPACITY;

    this.plan = new WorkerPlan();
  }

  // ── sprites ───────────────────────────────────────────────────────────────
  @Override
  public void setSprite(EntitySprite sprite) {
    EntitySprite copy = sprite.freshCopy();
    super.setSprite(copy);
    if (copy instanceof DirectionalAnimatedSprite das) {
      das.setFacing(() -> this.facing);
      das.setIsMoving(this::isMoving);
    }
  }

  public void buildSprites(TextureRegion[][] grid) {
    animations = new EnumMap<>(Direction.class);
    Direction[] dirs = Direction.values();
    for (int row = 0; row < dirs.length; row++) {
      animations.put(dirs[row], new Animation<>(C.ANIMATION_FRAME_DURATION, grid[row]));
    }
  }

  // ── commands ──────────────────────────────────────────────────────────────

  public void commandMoveTo(Vector2 target) {
    clearPlan();
    plan.planType = PlanType.MOVE;

    moveTo(target);
  }

  public void commandMoveAdjacentTo(Entity entity) {
    clearPlan();
    plan.planType = PlanType.MOVE;

    moveAdjacentTo(entity);
  }

  public void commandDeliver(TownHall townHall) {
    clearPlan();

    if (!moveAdjacentTo(townHall)) {
      enterState(WorkerState.IDLE);
      return;
    }

    plan.planType = PlanType.DELIVER;
    plan.townHall = townHall;
  }

  public void commandHarvest(Node node, TownHall townHall) {
    clearPlan();

    if (!moveAdjacentTo(node)) {
      enterState(WorkerState.IDLE);
      return;
    }

    plan.planType = PlanType.HARVEST;
    plan.node = node;
    plan.townHall = townHall;

    // TODO if you are carrying something different than this node, jettison bag contents :-(
  }

  public void commandConstruct(Building building) {
    clearPlan();

    if (!moveAdjacentTo(building)) {
      enterState(WorkerState.IDLE);
      return;
    }

    plan.planType = PlanType.CONSTRUCT;
    plan.underConstruction = building;
  }

  public void commandRecomputePath() {
    if (waypoints.isEmpty()) return;
    moveTo(waypoints.getLast());
  }

  // ── state machine ─────────────────────────────────────────────────────────

  private void clearPlan() {
    clearPlan(false);
  }

  private void clearPlan(boolean enterIdle) {
    waypoints.clear();
    plan.planType = PlanType.IDLE;
    plan.node = null;
    plan.townHall = null;
    if (enterIdle) enterState(WorkerState.IDLE);
  }

  private void enterState(WorkerState newState) {
    this.state = newState;
    switch (newState) {
      case IDLE, DELIVERING, CONSTRUCTING:
        animTime = 0f;
        break;
      case MOVING:
        break;
      case HARVESTING:
        animTime = 0f;
        rememberNode(plan.node.getId());
        break;
    }
  }

  private void onArrival() {
    if (plan.planType == PlanType.HARVEST) {
      // harvesting loop - arrived at node or town hall?
      float distToNode = distanceTo(plan.node);
      float distToHall = distanceTo(plan.townHall);

      if (distToNode <= distToHall) enterState(WorkerState.HARVESTING);
      else enterState(WorkerState.DELIVERING);

    } else if (plan.planType == PlanType.DELIVER) {
      // single-shot delivery and go idle
      enterState(WorkerState.DELIVERING);

    } else if (plan.planType == PlanType.CONSTRUCT) {
      enterState(WorkerState.CONSTRUCTING);

    } else if (plan.planType == PlanType.MOVE) {
      clearPlan(true);
    } else {
      enterState(WorkerState.IDLE);
    }
  }

  private void updateState(float delta) {
    switch (state) {
      case IDLE:
        break;
      case MOVING:
        updateMoving(delta);
        break;
      case HARVESTING:
        updateHarvesting(delta);
        break;
      case DELIVERING:
        updateDelivering(delta);
        break;
      case CONSTRUCTING:
        updateConstructing(delta);
        break;
    }
  }

  private void updateMoving(float delta) {
    Vector2 target = waypoints.peek();
    if (target == null) {
      onArrival();
      return;
    }

    Vector2 dir = new Vector2(target).sub(position);
    float distance = dir.len();

    if (distance < 2f) {
      position.set(target);
      waypoints.poll();
      if (waypoints.isEmpty()) {
        onArrival();
      }
      return;
    }

    dir.nor();
    if (Math.abs(dir.x) > Math.abs(dir.y)) {
      facing = dir.x > 0 ? Direction.EAST : Direction.WEST;
    } else {
      facing = dir.y > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    float step = moveSpeed * delta;
    if (step > distance) step = distance;
    position.mulAdd(dir, step);
    animTime += delta;
  }

  private void updateHarvesting(float delta) {
    Node targetNode = plan.node;

    if (!isWithinInteractionRange(targetNode)) {
      moveAdjacentTo(targetNode);
      return;
    }

    if (targetNode.isEmpty()) {
      // look for nearby work
      for (Node nextNode : world.getAbundantNodes()) {
        if (distanceTo(nextNode) <= 150f) {
          commandHarvest(nextNode, plan.townHall);
          return;
        }
      }

      // no nearby abundant nodes so bring anything you have to a townhall
      if (carrying > 0f) moveTo(plan.townHall);
      else enterState(WorkerState.IDLE);

      return;
    }

    float amount = targetNode.getHarvestRate() * delta;
    float harvested = targetNode.harvest(Math.min(amount, capacityRemaining()));
    carrying += harvested;
    carryingType = targetNode.getResourceType();

    if (capacityRemaining() <= 0) {
      // clamp bag contents to capacity
      carrying = carryCapacity;
      moveAdjacentTo(plan.townHall);
    }
  }

  private void updateDelivering(float delta) {
    TownHall townHall = plan.townHall;

    if (!isWithinInteractionRange(townHall)) {
      moveAdjacentTo(townHall);
      return;
    }

    if (!townHall.isConstructionComplete()) return;

    // TODO check townhall has capacity to receive delivery
    if (carrying > 0f && carryingType != null) {
      // TODO make DELIVER_RATE townHall-specific
      float amount = C.DELIVER_RATE * delta;

      townHall.deposit(carryingType, amount);

      carrying -= amount;
    }

    if (carrying <= 0) {
      // clamp bag contents to 0
      carrying = 0;
      carryingType = null;

      if (plan.planType == PlanType.HARVEST) {
        moveAdjacentTo(plan.node);
        return;
      }
      clearPlan(true);
    }
  }

  private void updateConstructing(float delta) {
    Building building = plan.underConstruction;

    if (!isWithinInteractionRange(building)) {
      moveAdjacentTo(building);
      return;
    }

    // TODO worker.constructionRate
    building.addConstructionUnits(C.CONSTRUCTION_RATE * delta);

    if (building.isConstructionComplete()) {
      // look for nearby work
      for (Building nextBuilding : world.getIncompleteBuildings()) {
        // TODO put worker scan for construction range
        if (distanceTo(nextBuilding) <= 150f) {
          commandConstruct(nextBuilding);
          return;
        }
      }
      clearPlan(true);
    }
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  public boolean isWithinInteractionRange(Entity target) {
    Rectangle bounds = target.getBounds();
    // find closest point on bounds rectangle to this entity's position
    float closestX = Math.max(bounds.x, Math.min(position.x, bounds.x + bounds.width));
    float closestY = Math.max(bounds.y, Math.min(position.y, bounds.y + bounds.height));
    float dist = Vector2.dst(position.x, position.y, closestX, closestY);
    return dist <= C.WORKER_INTERACTION_RANGE;
  }

  private boolean moveTo(Vector2 dest) {
    waypoints.clear();
    List<Vector2> path = world.getPathfinder().findPath(position.x, position.y, dest.x, dest.y);

    if (path.isEmpty()) return false;

    waypoints.addAll(path);
    enterState(WorkerState.MOVING);
    return true;
  }

  private boolean moveTo(Entity dest) {
    return moveTo(dest.getPosition());
  }

  private boolean moveAdjacentTo(Entity dest) {
    // ensure there is a walkable path to this node
    Vector2 approach = world.getPathfinder().nearestWalkableApproach(dest, this);
    if (approach == null) {
      return false;
    }
    // essentially guaranteed to return true, because of the approach check above
    return moveTo(approach);
  }

  private void rememberNode(String nodeId) {
    nodeMemory.remove(nodeId);
    nodeMemory.addFirst(nodeId);
    while (nodeMemory.size() > C.WORKER_NODE_MEMORY_DEPTH) {
      nodeMemory.removeLast();
    }
  }

  private boolean recentlyVisited(String nodeId) {
    return nodeMemory.contains(nodeId);
  }

  // ── update ────────────────────────────────────────────────────────────────

  @Override
  public void update(float delta) {
    updateState(delta);
  }

  // ── drawing ───────────────────────────────────────────────────────────────

  public TextureRegion getCurrentAnimationFrame() {
    if (animations == null) return null;
    Animation<TextureRegion> anim = animations.get(facing);
    if (state == WorkerState.IDLE) {
      return animations.get(facing).getKeyFrames()[IDLE_FRAME];
    }
    return anim.getKeyFrame(animTime, true);
  }

  // ── getters ───────────────────────────────────────────────────────────────

  public WorkerState getState() {
    return state;
  }

  public WorkerPlan getPlan() {
    return plan;
  }

  public Direction getFacing() {
    return facing;
  }

  public float getCarrying() {
    return carrying;
  }

  public float getCarryCapacity() {
    return carryCapacity;
  }

  public float capacityRemaining() {
    return carryCapacity - carrying;
  }

  public float getCarryPct() {
    return (carryCapacity == 0) ? 1f : carrying / carryCapacity;
  }

  public String getCarryingType() {
    return carryingType;
  }

  public float getMoveSpeed() {
    return moveSpeed;
  }

  public void setMoveSpeed(float moveSpeed) {
    this.moveSpeed = moveSpeed;
  }

  public boolean isMoving() {
    return state == WorkerState.MOVING;
  }

  public Deque<Vector2> getWaypoints() {
    return waypoints;
  }

  public static class WorkerPlan {
    public enum PlanType {
      IDLE,
      MOVE,
      HARVEST,
      DELIVER,
      CONSTRUCT
    }

    PlanType planType = PlanType.IDLE;

    Node node;
    TownHall townHall;
    Building underConstruction;

    public PlanType getPlanType() {
      return planType;
    }

    public Entity getTargetEntity() {
      return switch (planType) {
        case HARVEST -> node;
        case DELIVER -> townHall;
        case CONSTRUCT -> underConstruction;
        default -> null;
      };
    }
  }
}
