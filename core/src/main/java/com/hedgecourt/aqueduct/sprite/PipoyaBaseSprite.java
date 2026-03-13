package com.hedgecourt.aqueduct.sprite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hedgecourt.aqueduct.C;
import java.util.List;

public class PipoyaBaseSprite extends AbstractEntitySprite implements EntitySprite {

  private String path = C.PIPOYA_BASE_PATH;

  @JsonProperty(required = true)
  private int spriteId;

  @JsonProperty private int widthTiles = 1;

  @JsonProperty private int heightTiles = 1;

  private TextureRegion region;

  public PipoyaBaseSprite() {}

  @Override
  public List<String> assetPaths() {
    return List.of(path);
  }

  @Override
  public void build(AssetManager assetManager) {
    Texture texture = assetManager.get(path, Texture.class);
    int cols = texture.getWidth() / 32;
    int tileX = spriteId % cols;
    int tileY = spriteId / cols;
    region = new TextureRegion(texture, tileX * 32, tileY * 32, widthTiles * 32, heightTiles * 32);
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
    PipoyaBaseSprite copy = new PipoyaBaseSprite();
    copy.path = this.path;
    copy.spriteId = this.spriteId;
    copy.widthTiles = this.widthTiles;
    copy.heightTiles = this.heightTiles;
    copy.region = this.region;
    return copy;
  }
}
