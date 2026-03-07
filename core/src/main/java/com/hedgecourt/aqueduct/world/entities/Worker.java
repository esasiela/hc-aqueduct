package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.Pathfinder;
import com.hedgecourt.aqueduct.world.WorldEntity;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

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
    MOVING
  }

  // ── animation ─────────────────────────────────────────────────────────────

  private Map<Direction, Animation<TextureRegion>> animations;
  private float animTime = 0f;
  private final int IDLE_FRAME = 1;

  // ── movement ──────────────────────────────────────────────────────────────

  private float speed;
  private Direction facing = Direction.SOUTH;
  private WorkerState state = WorkerState.IDLE;
  private final Queue<Vector2> waypoints = new LinkedList<>();

  // ── constructor ───────────────────────────────────────────────────────────

  public Worker(float x, float y) {
    super(x, y, C.ENTITY_RENDER_SIZE, C.ENTITY_RENDER_SIZE);
    this.speed = C.WORKER_BASE_SPEED;
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

  public void commandMoveTo(float x, float y, Pathfinder pathfinder) {
    waypoints.clear();
    List<Vector2> path = pathfinder.findPath(position.x, position.y, x, y);
    if (!path.isEmpty()) {
      waypoints.addAll(path);
      enterState(WorkerState.MOVING);
    }
  }

  // ── state machine ─────────────────────────────────────────────────────────

  private void enterState(WorkerState newState) {
    this.state = newState;
    switch (newState) {
      case IDLE:
        animTime = 0f;
        break;
      case MOVING:
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
    }
  }

  private void updateMoving(float delta) {
    Vector2 target = waypoints.peek();
    if (target == null) {
      enterState(WorkerState.IDLE);
      return;
    }

    Vector2 dir = new Vector2(target).sub(position);
    float distance = dir.len();

    if (distance < 2f) {
      position.set(target);
      waypoints.poll();
      if (waypoints.isEmpty()) {
        enterState(WorkerState.IDLE);
      }
      return;
    }

    // update facing direction
    dir.nor();
    if (Math.abs(dir.x) > Math.abs(dir.y)) {
      facing = dir.x > 0 ? Direction.EAST : Direction.WEST;
    } else {
      facing = dir.y > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    // move toward target
    float step = speed * delta;
    if (step > distance) step = distance;
    position.mulAdd(dir, step);

    animTime += delta;
  }

  // ── update ────────────────────────────────────────────────────────────────

  @Override
  public void update(float delta) {
    updateState(delta);
  }

  // ── drawing ───────────────────────────────────────────────────────────────

  public TextureRegion getCurrentFrame() {
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

  public Direction getFacing() {
    return facing;
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
}
