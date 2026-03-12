package com.hedgecourt.aqueduct.ui.elements;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.FontManager;
import com.hedgecourt.aqueduct.FontManager.FontType;
import com.hedgecourt.aqueduct.ui.UiElement;
import com.hedgecourt.aqueduct.world.BuildingDefinition;
import java.util.function.Consumer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class ConstructionButtonUiElement extends UiElement {

  private final GlyphLayout glyphLayout;
  private final BitmapFont buildButtonFont;

  private final String buildingType;
  private final String displayName;
  private final Consumer<String> onClick;

  public ConstructionButtonUiElement(
      FontManager fontManager,
      float x,
      float y,
      float width,
      float height,
      BuildingDefinition buildingDefinition,
      Consumer<String> onClick) {
    super(x, y, width, height);

    this.buildingType = buildingDefinition.type;
    this.displayName = buildingDefinition.displayName;
    this.onClick = onClick;

    this.glyphLayout = fontManager.getGlyphLayout();
    this.buildButtonFont = fontManager.getFont(FontType.BUILD_BUTTON_UI);
  }

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    shapeDrawer.rectangle(bounds, C.UI_LINE_COLOR, containsMouse() ? 4f : 1f);

    String buttonText = this.displayName;
    glyphLayout.setText(buildButtonFont, buttonText);
    buildButtonFont.draw(
        batch,
        buttonText,
        bounds.x + (bounds.width - glyphLayout.width) / 2f,
        bounds.y + (bounds.height + glyphLayout.height) / 2f);
  }

  @Override
  public void onClick(float mouseX, float mouseY) {
    onClick.accept(buildingType);
  }
}
