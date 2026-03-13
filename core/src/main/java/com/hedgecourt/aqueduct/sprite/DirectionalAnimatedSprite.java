package com.hedgecourt.aqueduct.sprite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.world.entities.Worker.Direction;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public class DirectionalAnimatedSprite extends AbstractEntitySprite {

  @JsonProperty(required = true)
  private String path;

  private EnumMap<Direction, Animation<TextureRegion>> animations;

  private Supplier<Direction> facing;
  private Supplier<Boolean> isMoving;

  private float animTime = 0f;

  public void setFacing(Supplier<Direction> facing) {
    this.facing = facing;
  }

  public void setIsMoving(Supplier<Boolean> isMoving) {
    this.isMoving = isMoving;
  }

  @Override
  public List<String> assetPaths() {
    return List.of(path);
  }

  @Override
  public void build(AssetManager assetManager) {
    Texture texture = assetManager.get(path, Texture.class);
    TextureRegion[][] grid = TextureRegion.split(texture, 32, 32);
    animations = new EnumMap<>(Direction.class);
    Direction[] dirs = Direction.values();
    for (int row = 0; row < dirs.length; row++) {
      animations.put(dirs[row], new Animation<>(C.ANIMATION_FRAME_DURATION, grid[row]));
    }
  }

  @Override
  public void update(float delta) {
    if (isMoving != null && isMoving.get()) {
      animTime += delta;
    }
  }

  @Override
  public void draw(SpriteBatch batch, float x, float y, float width, float height) {
    if (animations == null || facing == null) return;
    Direction dir = facing.get();
    Animation<TextureRegion> anim = animations.get(dir);
    TextureRegion frame =
        isMoving != null && isMoving.get()
            ? anim.getKeyFrame(animTime, true)
            : anim.getKeyFrames()[1]; // middle frame = idle pose
    batch.draw(frame, x, y, width, height);
  }

  @Override
  public EntitySprite freshCopy() {
    DirectionalAnimatedSprite copy = new DirectionalAnimatedSprite();
    copy.path = this.path;
    copy.animations = this.animations; // shared, TextureRegions are lightweight
    return copy;
  }
}
