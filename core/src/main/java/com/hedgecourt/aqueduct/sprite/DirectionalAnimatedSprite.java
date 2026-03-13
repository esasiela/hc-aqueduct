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
  private List<String> paths;

  @SuppressWarnings("unchecked")
  private EnumMap<Direction, Animation<TextureRegion>>[] animationSets;

  private int spriteIndex = 0;
  private static int nextIndex = 0;

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
    return paths;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void build(AssetManager assetManager) {
    animationSets = new EnumMap[paths.size()];
    Direction[] dirs = Direction.values();

    for (int i = 0; i < paths.size(); i++) {
      Texture texture = assetManager.get(paths.get(i), Texture.class);
      TextureRegion[][] grid = TextureRegion.split(texture, 32, 32);
      EnumMap<Direction, Animation<TextureRegion>> anims = new EnumMap<>(Direction.class);
      for (int row = 0; row < dirs.length; row++) {
        anims.put(dirs[row], new Animation<>(C.ANIMATION_FRAME_DURATION, grid[row]));
      }
      animationSets[i] = anims;
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
    if (animationSets == null || facing == null) return;
    EnumMap<Direction, Animation<TextureRegion>> anims = animationSets[spriteIndex];
    Animation<TextureRegion> anim = anims.get(facing.get());
    TextureRegion frame =
        isMoving != null && isMoving.get()
            ? anim.getKeyFrame(animTime, true)
            : anim.getKeyFrames()[1];
    batch.draw(frame, x, y, width, height);
  }

  @Override
  public EntitySprite freshCopy() {
    DirectionalAnimatedSprite copy = new DirectionalAnimatedSprite();
    copy.paths = this.paths;
    copy.animationSets = this.animationSets;
    copy.spriteIndex = nextIndex % animationSets.length;
    nextIndex++;
    return copy;
  }
}
