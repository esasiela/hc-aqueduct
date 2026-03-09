package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.WorldEntity;
import com.hedgecourt.aqueduct.world.entities.Worker.WorkerPlan.PlanType;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class Worker extends WorldEntity {

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
    DELIVERING
  }

  // ── animation ─────────────────────────────────────────────────────────────

  private Map<Direction, Animation<TextureRegion>> animations;
  private float animTime = 0f;
  private final int IDLE_FRAME = 1;

  // ── movement ──────────────────────────────────────────────────────────────

  private WorkerState state = WorkerState.IDLE;
  private final WorkerPlan plan;

  private final Queue<Vector2> waypoints = new LinkedList<>();

  private float speed;
  private Direction facing = Direction.SOUTH;

  private float carrying = 0f;
  private float carryCapacity = C.WORKER_CARRY_CAPACITY;
  private String carryingType = null;

  private final LinkedList<String> nodeMemory = new LinkedList<>();

  private final AqueductWorld world;

  // ── constructor ───────────────────────────────────────────────────────────

  public Worker(AqueductWorld world, float x, float y) {
    super(x, y, C.ENTITY_RENDER_SIZE, C.ENTITY_RENDER_SIZE);
    this.world = world;
    this.speed = C.WORKER_BASE_SPEED;

    this.plan = new WorkerPlan();
  }

  // ── sprites ───────────────────────────────────────────────────────────────

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
      case IDLE:
        animTime = 0f;
        break;
      case MOVING:
        break;
      case HARVESTING:
        animTime = 0f;
        rememberNode(plan.node.getId());
        break;
      case DELIVERING:
        animTime = 0f;
        break;
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

    float step = speed * delta;
    if (step > distance) step = distance;
    position.mulAdd(dir, step);
    animTime += delta;
  }

  private void onArrival() {
    if (plan.planType == PlanType.HARVEST) {
      // harvesting loop - arrived at node or town hall?
      float distToNode = distanceTo(plan.node);
      float distToHall = distanceTo(plan.townHall);

      // TODO check if with HARVEST_RANGE or DELIVER_RANGE
      if (distToNode <= distToHall) {
        enterState(WorkerState.HARVESTING);
      } else {
        enterState(WorkerState.DELIVERING);
      }
    } else if (plan.planType == PlanType.MOVE) {
      clearPlan(true);
    } else {
      enterState(WorkerState.IDLE);
    }
  }

  private void updateHarvesting(float delta) {
    Node targetNode = plan.node;

    // TODO harvest range check

    if (targetNode.isEmpty()) {
      // TODO if worker has capacity, look for nearby suitable nodes
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
      moveTo(plan.townHall);
    }
  }

  private void updateDelivering(float delta) {
    // TODO delivery range check
    TownHall townHall = plan.townHall;

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
      moveTo(plan.node);
    }
  }

  // ── helpers ────────────────────────────────────────────────────────────────

  private boolean moveTo(Vector2 dest) {
    waypoints.clear();
    List<Vector2> path = world.getPathfinder().findPath(position.x, position.y, dest.x, dest.y);

    if (path.isEmpty()) return false;

    waypoints.addAll(path);
    enterState(WorkerState.MOVING);
    return true;
  }

  private boolean moveTo(WorldEntity dest) {
    return moveTo(dest.getPosition());
  }

  private boolean moveAdjacentTo(WorldEntity dest) {
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

  private Vector2 approachPoint(WorldEntity target) {
    Vector2 dir = new Vector2(target.getPosition()).sub(position).nor();
    float range = (target.getWidth() / 2f) + C.INTERACTION_RANGE;
    return new Vector2(target.getPosition()).sub(dir.scl(range));
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

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    // Worker drawing is handled by WorkerLayer
  }

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

  public float getSpeed() {
    return speed;
  }

  public void setSpeed(float speed) {
    this.speed = speed;
  }

  public Queue<Vector2> getWaypoints() {
    return waypoints;
  }

  public static class WorkerPlan {
    public enum PlanType {
      IDLE,
      MOVE,
      HARVEST
    }

    PlanType planType = PlanType.IDLE;

    Node node;
    TownHall townHall;

    public PlanType getPlanType() {
      return planType;
    }
  }
}
