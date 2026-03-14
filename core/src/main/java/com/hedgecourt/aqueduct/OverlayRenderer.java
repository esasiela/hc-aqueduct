package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hedgecourt.aqueduct.overlay.OverlayPanel;
import java.util.ArrayList;
import java.util.List;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class OverlayRenderer implements Disposable {

  private final OrthographicCamera camera;
  private final ScreenViewport viewport;
  private final List<OverlayPanel> panels = new ArrayList<>();
  private OverlayPanel activePanel = null;

  public OverlayRenderer() {
    camera = new OrthographicCamera();
    viewport = new ScreenViewport(camera);
    resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
  }

  public void setActivePanel(OverlayPanel panel) {
    this.activePanel = panel;
  }

  public void clearActivePanel() {
    this.activePanel = null;
  }

  public boolean isActive() {
    return activePanel != null;
  }

  public boolean isModal() {
    return activePanel != null && activePanel.isModal();
  }

  public boolean handleClick(float x, float y) {
    if (activePanel == null) return false;
    return activePanel.handleClick(x, y);
  }

  public void applyViewport() {
    viewport.apply();
  }

  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    if (activePanel == null) return;
    batch.setProjectionMatrix(camera.combined);
    activePanel.draw(batch, shapeDrawer);
  }

  public void resize(int width, int height) {
    viewport.update(width, height, true);
    for (OverlayPanel panel : panels) {
      // panel.updateBounds(0, 0, width, height); // full viewport for all panels
      panel.updateBounds(width * 0.1f, height * 0.1f, width * 0.8f, height * 0.8f);
    }
  }

  public void addPanel(OverlayPanel panel) {
    panels.add(panel);
  }

  public float getWidth() {
    return viewport.getScreenWidth();
  }

  public float getHeight() {
    return viewport.getScreenHeight();
  }

  @Override
  public void dispose() {}
}
