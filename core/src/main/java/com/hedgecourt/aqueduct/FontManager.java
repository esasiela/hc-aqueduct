package com.hedgecourt.aqueduct;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

public class FontManager implements Disposable {
  private FreeTypeFontGenerator generator;
  private final GlyphLayout glyphLayout = new GlyphLayout();

  private final Map<FontType, BitmapFont> fontCache = new HashMap<>();

  public enum FontType {
    DEFAULT,
    DEBUG,
    WORKER_PLAN_OVERLAY,
    BUILD_BUTTON_UI,
    BUILDING_INFO_UI,
    PAUSE_PANEL
  }

  public void load() {
    try {
      generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Inconsolata-Regular.ttf"));
    } catch (Exception e) {
      Gdx.app.log("FONT", "error loading freeTypeFontGenerator", e);
    }

    generateMonoFont(FontType.DEFAULT, 10);
    generateMonoFont(FontType.DEBUG, 12);
    generateMonoFont(FontType.WORKER_PLAN_OVERLAY, 12);
    generateMonoFont(FontType.BUILD_BUTTON_UI, 12);
    generateMonoFont(FontType.BUILDING_INFO_UI, 12);

    generateFont(FontType.PAUSE_PANEL, 16);

    generator.dispose();
  }

  private void generateFont(FontType fontType, float scale) {
    BitmapFont font = new BitmapFont();
    font.getData().setScale(scale);
    fontCache.put(fontType, font);
  }

  private void generateMonoFont(FontType fontType, int size) {
    FreeTypeFontParameter param = new FreeTypeFontParameter();
    param.size = size;
    BitmapFont font = generator.generateFont(param);
    fontCache.put(fontType, font);
  }

  public BitmapFont getFont(FontType fontType) {
    if (!fontCache.containsKey(fontType)) {
      Gdx.app.log("FONT", "no font found for " + fontType);
    }
    return fontCache.getOrDefault(fontType, fontCache.get(FontType.DEFAULT));
  }

  @Override
  public void dispose() {
    fontCache.forEach(
        (fontType, font) -> {
          font.dispose();
        });
  }

  public GlyphLayout getGlyphLayout() {
    return glyphLayout;
  }
}
