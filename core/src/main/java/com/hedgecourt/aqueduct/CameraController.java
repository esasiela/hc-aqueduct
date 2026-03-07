package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CameraController {

  private final OrthographicCamera camera;
  private float mapPixelW;
  private float mapPixelH;
  private float viewportW;
  private float viewportH;
  private float maxZoom;

  private float zoomAccumulator = 0f;
  private float pendingZoomScreenX = 0f;
  private float pendingZoomScreenY = 0f;

  private final Vector3 unprojectScratch = new Vector3();

  private final ScreenViewport viewport;

  public CameraController(OrthographicCamera camera, ScreenViewport viewport) {
    this.camera = camera;
    this.viewport = viewport;
  }

  public void init(float mapPixelW, float mapPixelH) {
    this.mapPixelW = mapPixelW;
    this.mapPixelH = mapPixelH;
    this.viewportW = viewport.getScreenWidth();
    this.viewportH = viewport.getScreenHeight();

    float zoomToFitW = mapPixelW / viewportW;
    float zoomToFitH = mapPixelH / viewportH;
    maxZoom = Math.max(zoomToFitW, zoomToFitH);

    resetToHome();
  }

  public void update(float delta) {
    if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
      resetToHome();
    }
    if (Gdx.input.isKeyPressed(Input.Keys.EQUALS)) {
      onZoom(-C.ZOOM_SPEED, Gdx.input.getX(), Gdx.input.getY());
    }
    if (Gdx.input.isKeyPressed(Input.Keys.MINUS)) {
      onZoom(C.ZOOM_SPEED, Gdx.input.getX(), Gdx.input.getY());
    }

    if (Math.abs(zoomAccumulator) > 0.001f) {
      applyZoom(zoomAccumulator, pendingZoomScreenX, pendingZoomScreenY);
      zoomAccumulator = 0f;
    }
  }

  public void resetToHome() {
    camera.zoom = 1.0f;
    camera.position.set(mapPixelW / 2f, mapPixelH / 2f, 0);
    camera.update();
    clampCamera();
    camera.update();
  }

  /** Called from scrolled() input event. dx/dy are Magic Mouse scroll deltas. */
  public void onScroll(float dx, float dy) {
    if (Math.abs(dx) < C.SCROLL_THRESHOLD && Math.abs(dy) < C.SCROLL_THRESHOLD) return;
    float panSpeed = 10f * camera.zoom;
    camera.position.x += dx * panSpeed;
    camera.position.y -= dy * panSpeed;
    clampCamera();
    camera.update();
  }

  /**
   * Accumulates zoom delta toward screen point (screenX, screenY). Applied once per frame in
   * update().
   */
  public void onZoom(float zoomDelta, float screenX, float screenY) {
    zoomAccumulator += zoomDelta;
    pendingZoomScreenX = screenX;
    pendingZoomScreenY = screenY;
  }

  public void applyZoom(float zoomDelta, float screenX, float screenY) {
    unprojectScratch.set(screenX, screenY, 0);
    viewport.unproject(unprojectScratch);
    float worldX = unprojectScratch.x;
    float worldY = unprojectScratch.y;

    camera.zoom = MathUtils.clamp(camera.zoom + zoomDelta, C.ZOOM_MIN, maxZoom);
    camera.update();

    unprojectScratch.set(screenX, screenY, 0);
    viewport.unproject(unprojectScratch);

    camera.position.x += worldX - unprojectScratch.x;
    camera.position.y += worldY - unprojectScratch.y;

    clampCamera();
    camera.update();
  }

  private void clampCamera() {
    float effectiveW = viewportW * camera.zoom;
    float effectiveH = viewportH * camera.zoom;

    if (effectiveW >= mapPixelW) {
      camera.position.x = mapPixelW / 2f;
    } else {
      float minX = effectiveW / 2f;
      float maxX = mapPixelW - effectiveW / 2f;
      camera.position.x = MathUtils.clamp(camera.position.x, minX, maxX);
    }

    if (effectiveH >= mapPixelH) {
      camera.position.y = mapPixelH / 2f;
    } else {
      float minY = effectiveH / 2f;
      float maxY = mapPixelH - effectiveH / 2f;
      camera.position.y = MathUtils.clamp(camera.position.y, minY, maxY);
    }
  }

  public void clampCameraPublic() {
    clampCamera();
  }
}
