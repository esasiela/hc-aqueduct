package com.hedgecourt.aqueduct.sprite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class SingleTextureSprite extends AbstractEntitySprite implements EntitySprite {

  @JsonProperty(required = true)
  private String path;

  private TextureRegion region;

  public SingleTextureSprite() {}

  @Override
  public List<String> assetPaths() {
    return List.of(path);
  }

  @Override
  public void build(AssetManager assetManager) {
    region = new TextureRegion(assetManager.get(path, Texture.class));
  }

  @Override
  public void update(float delta) {}

  @Override
  public void draw(SpriteBatch batch, float x, float y, float width, float height) {
    if (region == null) return;
    batch.draw(region, x, y, width, height);
  }

  @Override
  public EntitySprite freshCopy() {
    SingleTextureSprite copy = new SingleTextureSprite();
    copy.path = this.path;
    copy.region = this.region;
    return copy;
  }
}
