package com.hedgecourt.aqueduct;

import com.badlogic.gdx.graphics.Color;

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

  public static final float DELIVER_RATE = 10f;
  public static final float CONSTRUCTION_RATE = 10f;

  public static final float WORKER_INTERACTION_RANGE = 24f;

  // ── colors ────────────────────────────────────────────────────────────────
  public static final float CLEAR_R = 0.2f;
  public static final float CLEAR_G = 0.2f;
  public static final float CLEAR_B = 0.2f;

  public static final Color BUILDING_SELECTION_RECT_COLOR = new Color(0f, 1f, 0f, 1.0f);
  public static final Color SELECTION_RING_COLOR = Color.WHITE;
  public static final Color BAG_BAR_CAPACITY_COLOR = new Color(0.5f, 0.5f, 0.5f, 1.0f);
  public static final Color BAG_BAR_CARRY_COLOR = Color.BLUE;

  // ── camera ────────────────────────────────────────────────────────────────
  public static final float ZOOM_MIN = 0.25f; // 4x zoom in, see the sweat
  public static final float ZOOM_SPEED = 0.1f; // how much each pinch/scroll step zooms
  public static final float SCROLL_THRESHOLD = 0.35f; // ignore tiny accidental scrolls

  // ── misc ────────────────────────────────────────────────────────────────
  public static final float DRAG_THRESHOLD = 6f;
  public static final String PIPOYA_BASE_PATH = "maps/[Base]BaseChip_pipo.png";
}
