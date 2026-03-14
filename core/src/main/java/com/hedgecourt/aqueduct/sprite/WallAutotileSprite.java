package com.hedgecourt.aqueduct.sprite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.function.Supplier;

public class WallAutotileSprite extends AbstractEntitySprite {

  @JsonProperty(required = true)
  private String path;

  private TextureRegion[] regions;

  private Supplier<Integer> bitmaskSupplier;

  public void setBitmaskSupplier(Supplier<Integer> bitmaskSupplier) {
    this.bitmaskSupplier = bitmaskSupplier;
  }

  @Override
  public List<String> assetPaths() {
    return List.of(path);
  }

  @Override
  public void build(AssetManager assetManager) {
    Texture texture = assetManager.get(path, Texture.class);
    TextureRegion[][] grid = TextureRegion.split(texture, 32, 32);
    regions = new TextureRegion[16];
    for (int i = 0; i < 16; i++) {
      regions[i] = grid[i / 4][i % 4];
    }
  }

  @Override
  public void draw(SpriteBatch batch, float x, float y, float width, float height) {
    if (regions == null) return;
    int index = bitmaskSupplier != null ? bitmaskSupplier.get() : 0;
    batch.draw(regions[index], x, y, width, height);
  }

  @Override
  public EntitySprite freshCopy() {
    WallAutotileSprite copy = new WallAutotileSprite();
    copy.path = this.path;
    copy.regions = this.regions;
    return copy;
  }
}
