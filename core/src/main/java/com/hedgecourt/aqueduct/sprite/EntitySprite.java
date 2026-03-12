package com.hedgecourt.aqueduct.sprite;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.List;

public interface EntitySprite {
  List<String> assetPaths(); // called before assetManager.finishLoading()

  void build(AssetManager assetManager); // called after finishLoading(), constructs internals

  void update(float delta); // called each frame, advances animation etc

  void draw(SpriteBatch batch, float x, float y, float width, float height);
}
