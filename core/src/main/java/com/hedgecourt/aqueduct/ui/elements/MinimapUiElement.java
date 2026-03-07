package com.hedgecourt.aqueduct.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.WorldRenderer;
import com.hedgecourt.aqueduct.ui.UiElement;
import com.hedgecourt.aqueduct.world.entities.Worker;
import com.hedgecourt.aqueduct.world.layers.WorkerLayer;
import java.util.List;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class MinimapUiElement extends UiElement {

  private static final float SIZE = 180f;
  private static final float PAD = 10f;
  private static final Color COLOR_BG = new Color(0.15f, 0.15f, 0.15f, 1f); // dark grey letterbox
  private static final Color COLOR_MAP = new Color(0.13f, 0.37f, 0.13f, 1f); // dark green map
  private static final Color COLOR_VIEW = new Color(1f, 1f, 1f, 0.8f); // white viewport rect
  private static final Color COLOR_WORKER = new Color(1f, 1f, 0f, 1f); // yellow unselected
  private static final Color COLOR_SEL = new Color(1f, 1f, 1f, 1f); // white selected

  private final WorldRenderer worldRenderer;
  private final WorkerLayer workerLayer;

  // map rect within minimap (accounts for letterboxing)
  private Rectangle mapRect = new Rectangle();
  private boolean dragging = false;

  public MinimapUiElement(WorldRenderer worldRenderer, WorkerLayer workerLayer) {
    super(PAD, (C.UI_BOTTOM_HEIGHT - SIZE) / 2f, SIZE, SIZE);
    this.worldRenderer = worldRenderer;
    this.workerLayer = workerLayer;
    updateMapRect();
  }

  private void updateMapRect() {
    float mapW = worldRenderer.getMapWidth();
    float mapH = worldRenderer.getMapHeight();
    float scaleX = SIZE / mapW;
    float scaleY = SIZE / mapH;
    float scale = Math.min(scaleX, scaleY);

    float scaledW = mapW * scale;
    float scaledH = mapH * scale;

    // center the map rect within the minimap bounds
    mapRect.set(
        bounds.x + (SIZE - scaledW) / 2f, bounds.y + (SIZE - scaledH) / 2f, scaledW, scaledH);
  }

  // ── coordinate conversion ─────────────────────────────────────────────────

  private Vector2 worldToMinimap(float worldX, float worldY) {
    float mapW = worldRenderer.getMapWidth();
    float mapH = worldRenderer.getMapHeight();
    return new Vector2(
        mapRect.x + (worldX / mapW) * mapRect.width, mapRect.y + (worldY / mapH) * mapRect.height);
  }

  private Vector2 minimapToWorld(float minimapX, float minimapY) {
    float mapW = worldRenderer.getMapWidth();
    float mapH = worldRenderer.getMapHeight();
    float relX = (minimapX - mapRect.x) / mapRect.width;
    float relY = (minimapY - mapRect.y) / mapRect.height;
    return new Vector2(relX * mapW, relY * mapH);
  }

  private boolean inMapRect(float x, float y) {
    return mapRect.contains(x, y);
  }

  // ── event hooks ───────────────────────────────────────────────────────────

  @Override
  public void onClick(float mouseX, float mouseY) {
    if (inMapRect(mouseX, mouseY)) {
      panCameraTo(mouseX, mouseY);
    }
  }

  public void onTouchDown(float mouseX, float mouseY) {
    if (inMapRect(mouseX, mouseY)) {
      dragging = true;
    }
  }

  @Override
  public void onMouseEnter(float mouseX, float mouseY) {
    dragging = false;
  }

  @Override
  public void onMouseExit() {
    dragging = false;
  }

  // ── update ────────────────────────────────────────────────────────────────

  @Override
  public void update(float delta) {
    if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
      dragging = false;
    }
  }

  private void panCameraTo(float minimapX, float minimapY) {
    Vector2 worldPos = minimapToWorld(minimapX, minimapY);
    worldRenderer.panCameraTo(worldPos.x, worldPos.y);
  }

  public boolean isDragging() {
    return dragging;
  }

  public void handleDrag(float mouseX, float mouseY) {
    if (inMapRect(mouseX, mouseY)) {
      panCameraTo(mouseX, mouseY);
    }
  }

  // ── draw ──────────────────────────────────────────────────────────────────

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    // ── dark grey background (letterbox) ─────────────────────────────────
    shapeDrawer.setColor(COLOR_BG);
    shapeDrawer.filledRectangle(bounds.x, bounds.y, bounds.width, bounds.height);

    // ── dark green map area ───────────────────────────────────────────────
    shapeDrawer.setColor(COLOR_MAP);
    shapeDrawer.filledRectangle(mapRect.x, mapRect.y, mapRect.width, mapRect.height);

    // ── camera viewport rectangle ─────────────────────────────────────────
    drawViewportRect(shapeDrawer);

    // ── worker dots ───────────────────────────────────────────────────────
    List<Worker> workers = workerLayer.getWorkers();
    for (Worker worker : workers) {
      Vector2 dot = worldToMinimap(worker.getPosition().x, worker.getPosition().y);
      shapeDrawer.setColor(worker.isSelected() ? COLOR_SEL : COLOR_WORKER);
      shapeDrawer.filledCircle(dot.x, dot.y, 3f);
    }

    // ── border ────────────────────────────────────────────────────────────
    shapeDrawer.setColor(Color.WHITE);
    shapeDrawer.rectangle(bounds.x, bounds.y, bounds.width, bounds.height, 1.5f);
  }

  private void drawViewportRect(ShapeDrawer shapeDrawer) {
    float mapW = worldRenderer.getMapWidth();
    float mapH = worldRenderer.getMapHeight();

    float camX = worldRenderer.getCameraPosition().x;
    float camY = worldRenderer.getCameraPosition().y;
    float camW = worldRenderer.getCameraVisibleWidth();
    float camH = worldRenderer.getCameraVisibleHeight();

    // clamp viewport rect to map bounds for display
    float left = Math.max(camX - camW / 2f, 0);
    float bottom = Math.max(camY - camH / 2f, 0);
    float right = Math.min(camX + camW / 2f, mapW);
    float top = Math.min(camY + camH / 2f, mapH);

    Vector2 bl = worldToMinimap(left, bottom);
    Vector2 tr = worldToMinimap(right, top);

    shapeDrawer.setColor(COLOR_VIEW);
    shapeDrawer.rectangle(bl.x, bl.y, tr.x - bl.x, tr.y - bl.y, 1.5f);
  }
}
