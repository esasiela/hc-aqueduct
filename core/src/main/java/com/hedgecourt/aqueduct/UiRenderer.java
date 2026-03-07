package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class UiRenderer implements Disposable {

  private final OrthographicCamera camera;
  private final ScreenViewport viewport;
  private final ShapeDrawer shapeDrawer;
  private final Vector3 unprojectScratch = new Vector3();

  public UiRenderer(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    this.shapeDrawer = shapeDrawer;
    camera = new OrthographicCamera();
    viewport = new ScreenViewport(camera);
    updateScreenBounds();
  }

  public void applyViewport() {
    viewport.apply();
  }

  public void resize(int width, int height) {
    updateScreenBounds();
  }

  private void updateScreenBounds() {
    int screenW = Gdx.graphics.getWidth();
    viewport.update(screenW, (int) C.UI_BOTTOM_HEIGHT, true);
  }

  public void draw(SpriteBatch batch) {
    batch.setProjectionMatrix(camera.combined);

    // mouse crosshair in ui space
    Vector2 mouse = mouseInUi();
    shapeDrawer.line(mouse.x - 20, mouse.y, mouse.x + 20, mouse.y, Color.CYAN, 2f);
    shapeDrawer.line(mouse.x, mouse.y - 20, mouse.x, mouse.y + 20, Color.CYAN, 2f);

    // ui corner markers
    float w = getUiWidth();
    float h = getUiHeight();
    float s = 50f;
    shapeDrawer.filledRectangle(0, 0, s, s, Color.PURPLE); // bottom-left
    shapeDrawer.filledRectangle(w - s, 0, s, s, Color.PURPLE); // bottom-right
    shapeDrawer.filledRectangle(0, h - s, s, s, Color.PURPLE); // top-left
    shapeDrawer.filledRectangle(w - s, h - s, s, s, Color.PURPLE); // top-right
  }

  @Override
  public void dispose() {}

  // ── coordinate space ──────────────────────────────────────────────────────

  public float getUiWidth() {
    return viewport.getScreenWidth();
  }

  public float getUiHeight() {
    return viewport.getScreenHeight();
  }

  public Vector2 mouseInUi() {
    unprojectScratch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
    viewport.unproject(unprojectScratch);
    return new Vector2(unprojectScratch.x, unprojectScratch.y);
  }
}
