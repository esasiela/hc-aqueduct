package com.hedgecourt.aqueduct.overlay;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.FontManager;
import com.hedgecourt.aqueduct.FontManager.FontType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class PauseOverlayPanel extends OverlayPanel {

  private final BitmapFont font;
  private final GlyphLayout glyphLayout;

  public PauseOverlayPanel(FontManager fontManager) {
    this.font = fontManager.getFont(FontType.PAUSE_PANEL);
    this.glyphLayout = fontManager.getGlyphLayout();
  }

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    shapeDrawer.filledRectangle(bounds, C.PAUSE_PANEL_OVERLAY_COLOR);

    shapeDrawer.rectangle(bounds, C.PAUSE_PANEL_LINE_COLOR);

    String text = "PAUSED";
    glyphLayout.setText(font, text);
    font.setColor(C.PAUSE_PANEL_TEXT_COLOR);
    font.draw(
        batch,
        text,
        bounds.x + (bounds.width - glyphLayout.width) / 2f,
        bounds.y + (bounds.height + glyphLayout.height) / 2f);
    font.setColor(Color.WHITE);
  }

  @Override
  public boolean isModal() {
    return false;
  }

  @Override
  public boolean handleClick(float x, float y) {
    return false;
  }
}
