package com.hedgecourt.aqueduct;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.ScreenUtils;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class AqueductMain extends ApplicationAdapter {

  private SpriteBatch batch;
  private ShapeDrawer shapeDrawer;
  private Texture pixelTexture;

  private WorldRenderer worldRenderer;
  private UiRenderer uiRenderer;

  @Override
  public void create() {
    batch = new SpriteBatch();

    Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
    pixmap.setColor(Color.WHITE);
    pixmap.fill();
    pixelTexture = new Texture(pixmap);
    pixmap.dispose();
    shapeDrawer = new ShapeDrawer(batch, new TextureRegion(pixelTexture));

    worldRenderer = new WorldRenderer(batch, shapeDrawer);
    uiRenderer = new UiRenderer(batch, shapeDrawer);

    worldRenderer.loadMap("maps/test.tmx");
  }

  @Override
  public void render() {
    float delta = Gdx.graphics.getDeltaTime();

    // ── update ────────────────────────────────────────────────────────────
    handleInput(delta);
    updateWorld(delta);
    updateUi(delta);

    // ── draw ─────────────────────────────────────────────────────────────
    ScreenUtils.clear(C.CLEAR_R, C.CLEAR_G, C.CLEAR_B, 1f);

    worldRenderer.beginViewport();
    worldRenderer.render();
    batch.begin();
    worldRenderer.drawUnderlay(batch);
    worldRenderer.drawEntities(batch);
    worldRenderer.drawOverlay(batch);
    batch.end();
    worldRenderer.endViewport();

    uiRenderer.beginViewport();
    batch.begin();
    uiRenderer.draw(batch);
    batch.end();
    uiRenderer.endViewport();
  }

  @Override
  public void dispose() {
    batch.dispose();
    worldRenderer.dispose();
    uiRenderer.dispose();
    pixelTexture.dispose();
  }

  private void handleInput(float delta) {}

  private void updateWorld(float delta) {}

  private void updateUi(float delta) {}
}
