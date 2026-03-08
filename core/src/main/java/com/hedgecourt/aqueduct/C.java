package com.hedgecourt.aqueduct;

public class C {

  // ── screen layout ─────────────────────────────────────────────────────────
  public static final float UI_BOTTOM_HEIGHT = 200f;
  public static final float UI_RIGHT_WIDTH = 0f;

  // ── entities ──────────────────────────────────────────────────────────────
  public static final float ANIMATION_FRAME_DURATION = 0.15f;

  public static final float WORKER_BASE_SPEED = 90f; // pixels per second
  public static final float WORKER_SEPARATION_RADIUS = 20f;
  public static final float WORKER_SEPARATION_STRENGTH = 60f;
  public static final float WORKER_CARRY_CAPACITY = 20f;
  public static final int WORKER_NODE_MEMORY_DEPTH = 3;

  public static final float ENTITY_RENDER_SIZE = 24f;

  public static final float INTERACTION_RANGE = 6f;

  public static final float DELIVER_RATE = 10f;

  // ── colors ────────────────────────────────────────────────────────────────
  public static final float CLEAR_R = 0.2f;
  public static final float CLEAR_G = 0.2f;
  public static final float CLEAR_B = 0.2f;

  // ── camera ────────────────────────────────────────────────────────────────
  public static final float ZOOM_MIN = 0.25f; // 4x zoom in, see the sweat
  public static final float ZOOM_MAX = 1f; // computed at runtime from map size, this is fallback
  public static final float ZOOM_SPEED = 0.1f; // how much each pinch/scroll step zooms
  public static final float SCROLL_THRESHOLD = 0.25f; // ignore tiny accidental scrolls

  // ── misc ────────────────────────────────────────────────────────────────
  public static final float DRAG_THRESHOLD = 6f;
}
