package com.hedgecourt.aqueduct.ui.elements;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.FontManager;
import com.hedgecourt.aqueduct.FontManager.FontType;
import com.hedgecourt.aqueduct.ui.UiElement;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BuildButtonUiElement extends UiElement {

  private final GlyphLayout glyphLayout;
  private final BitmapFont buildButtonFont;

  // TODO replace this with some kind of BuildingDef
  private String buildingType;

  public BuildButtonUiElement(
      FontManager fontManager, float x, float y, float width, float height, String buildingType) {
    super(x, y, width, height);

    this.buildingType = buildingType;

    this.glyphLayout = fontManager.getGlyphLayout();
    this.buildButtonFont = fontManager.getFont(FontType.BUILD_BUTTON_UI);
  }

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    shapeDrawer.rectangle(bounds, Color.PURPLE, containsMouse() ? 4f : 1f);

    String buttonText = this.buildingType;
    glyphLayout.setText(buildButtonFont, buttonText);
    buildButtonFont.draw(
        batch,
        buttonText,
        bounds.x + (bounds.width - glyphLayout.width) / 2f,
        bounds.y + (bounds.height + glyphLayout.height) / 2f);
  }

  @Override
  public void onClick(float mouseX, float mouseY) {
    Gdx.app.log("BUILD_BUTTON", "clicked: " + buildingType);
  }
}
