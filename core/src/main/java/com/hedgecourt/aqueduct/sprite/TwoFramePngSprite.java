package com.hedgecourt.aqueduct.sprite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Arrays;
import java.util.List;

public class TwoFramePngSprite extends AbstractEntitySprite implements EntitySprite {

  @JsonProperty(required = true)
  private String path;

  private TextureRegion[] regions;

  private static final float ANIMATION_DURATION = 1.0f;
  private float animationTimer = 0.0f;
  private int frameIndex = 0;

  public TwoFramePngSprite() {}

  @Override
  public List<String> assetPaths() {
    return List.of(path);
  }

  @Override
  public void build(AssetManager assetManager) {
    Texture texture = assetManager.get(path, Texture.class);
    int frameWidth = texture.getWidth() / 2;
    int frameHeight = texture.getHeight();
    regions = new TextureRegion[2];
    regions[0] = new TextureRegion(texture, 0, 0, frameWidth, frameHeight);
    regions[1] = new TextureRegion(texture, frameWidth, 0, frameWidth, frameHeight);
  }

  @Override
  public void update(float delta) {
    animationTimer += delta;
    if (animationTimer >= ANIMATION_DURATION) {
      animationTimer = 0.0f;
      frameIndex++;
      if (frameIndex > 1) frameIndex = 0;
    }
  }

  @Override
  public void draw(SpriteBatch batch, float x, float y, float width, float height) {
    if (regions == null) return;
    batch.draw(regions[frameIndex], x, y, width, height);
  }

  @Override
  public EntitySprite freshCopy() {
    TwoFramePngSprite copy = new TwoFramePngSprite();
    copy.path = this.path;
    copy.regions = Arrays.copyOf(this.regions, this.regions.length);
    return copy;
  }
}
