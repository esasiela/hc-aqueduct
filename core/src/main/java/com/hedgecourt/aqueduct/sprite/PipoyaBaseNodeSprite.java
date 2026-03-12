package com.hedgecourt.aqueduct.sprite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hedgecourt.aqueduct.C;
import java.util.List;
import java.util.function.Supplier;

public class PipoyaBaseNodeSprite extends AbstractEntitySprite {

  @JsonProperty(required = true)
  private int spriteIdFull;

  @JsonProperty(required = true)
  private int spriteIdEmpty;

  private TextureRegion regionFull;
  private TextureRegion regionEmpty;

  private Supplier<Boolean> hasInventory;

  public void setHasInventory(Supplier<Boolean> hasInventory) {
    this.hasInventory = hasInventory;
  }

  @Override
  public List<String> assetPaths() {
    return List.of(C.PIPOYA_BASE_PATH);
  }

  @Override
  public void build(AssetManager assetManager) {
    Texture texture = assetManager.get(C.PIPOYA_BASE_PATH, Texture.class);
    TextureRegion[][] grid = TextureRegion.split(texture, 32, 32);
    int cols = texture.getWidth() / 32;
    regionFull = grid[spriteIdFull / cols][spriteIdFull % cols];
    regionEmpty = grid[spriteIdEmpty / cols][spriteIdEmpty % cols];
  }

  @Override
  public void draw(SpriteBatch batch, float x, float y, float width, float height) {
    TextureRegion region = (hasInventory != null && hasInventory.get()) ? regionFull : regionEmpty;
    if (region != null) batch.draw(region, x, y, width, height);
  }

  @Override
  public EntitySprite freshCopy() {
    PipoyaBaseNodeSprite copy = new PipoyaBaseNodeSprite();

    copy.spriteIdFull = this.spriteIdFull;
    copy.spriteIdEmpty = this.spriteIdEmpty;

    copy.regionFull = this.regionFull;
    copy.regionEmpty = this.regionEmpty;

    return copy;
  }
}
