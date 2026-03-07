package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class UiRenderer implements Disposable {

  private final OrthographicCamera camera;
  private final Vector3 unprojectScratch = new Vector3();
  private final ShapeDrawer shapeDrawer;

  public UiRenderer(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    this.shapeDrawer = shapeDrawer;
    camera = new OrthographicCamera();
    float screenW = Gdx.graphics.getWidth();
    float screenH = Gdx.graphics.getHeight();
    camera.setToOrtho(false, screenW, C.UI_BOTTOM_HEIGHT);
    camera.position.set(screenW / 2f, C.UI_BOTTOM_HEIGHT / 2f, 0);
    camera.update();
  }

  public void beginViewport() {
    int bbW = Gdx.graphics.getBackBufferWidth();
    int bbH = Gdx.graphics.getBackBufferHeight();
    int uiH = C.toPhysicalPixels(C.UI_BOTTOM_HEIGHT);
    Gdx.gl.glViewport(0, 0, bbW, uiH);
    camera.update();
  }

  public void endViewport() {
    Gdx.gl.glViewport(0, 0, Gdx.graphics.getBackBufferWidth(), Gdx.graphics.getBackBufferHeight());
  }

  public void draw(SpriteBatch batch) {
    batch.setProjectionMatrix(camera.combined);
  }

  @Override
  public void dispose() {}

  // ── coordinate space ──────────────────────────────────────────────────────

  public float getUiWidth() {
    return camera.viewportWidth;
  }

  public float getUiHeight() {
    return camera.viewportHeight;
  }

  public Vector2 mouseInUi() {
    unprojectScratch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    camera.unproject(unprojectScratch);
    return new Vector2(unprojectScratch.x, unprojectScratch.y);
  }
}
