package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;

public class C {

  // ── screen layout ─────────────────────────────────────────────────────────
  public static final float UI_BOTTOM_HEIGHT = 200f;
  public static final float UI_RIGHT_WIDTH = 0f;

  // ── world ─────────────────────────────────────────────────────────────────

  // ── colors ────────────────────────────────────────────────────────────────
  public static final float CLEAR_R = 0.2f;
  public static final float CLEAR_G = 0.2f;
  public static final float CLEAR_B = 0.2f;

  // ── camera ────────────────────────────────────────────────────────────────
  public static final float ZOOM_MIN = 0.25f; // 4x zoom in, see the sweat
  public static final float ZOOM_MAX = 1f; // computed at runtime from map size, this is fallback
  public static final float ZOOM_SPEED = 0.1f; // how much each pinch/scroll step zooms
  public static final float SCROLL_THRESHOLD = 0.25f; // ignore tiny accidental scrolls

  public static int toPhysicalPixels(float logicalPixels) {
    return Math.round(
        logicalPixels * Gdx.graphics.getBackBufferHeight() / Gdx.graphics.getHeight());
  }
}
