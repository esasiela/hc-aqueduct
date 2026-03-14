package com.hedgecourt.aqueduct.ui.elements;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.FontManager;
import com.hedgecourt.aqueduct.FontManager.FontType;
import com.hedgecourt.aqueduct.factory.UnitDefinition;
import com.hedgecourt.aqueduct.ui.UiElement;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import java.util.function.Consumer;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class TrainingButtonUiElement extends UiElement {

  private final GlyphLayout glyphLayout;
  private final BitmapFont font;
  private final AqueductWorld world;
  private final String unitType;
  private final String displayName;
  private final Consumer<String> onClick;

  private final Rectangle scratchRect = new Rectangle();

  public TrainingButtonUiElement(
      AqueductWorld world,
      FontManager fontManager,
      float x,
      float y,
      float width,
      float height,
      UnitDefinition unitDefinition,
      Consumer<String> onClick) {
    super(x, y, width, height);
    this.world = world;
    this.unitType = unitDefinition.type;
    this.displayName = unitDefinition.displayName;
    this.onClick = onClick;
    this.glyphLayout = fontManager.getGlyphLayout();
    this.font = fontManager.getFont(FontType.BUILD_BUTTON_UI);
  }

  private boolean isEnabled() {
    return world.getSelectedBuilding(TownHall.class) != null;
  }

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    boolean enabled = isEnabled();
    shapeDrawer.rectangle(bounds, C.UI_LINE_COLOR, containsMouse() && enabled ? 4f : 1f);

    glyphLayout.setText(font, displayName);
    font.draw(
        batch,
        displayName,
        bounds.x + (bounds.width - glyphLayout.width) / 2f,
        bounds.y + (bounds.height + glyphLayout.height) / 2f);

    if (!enabled) {
      Rectangle disabledBounds = shrinkRectangle(bounds, 10f);
      shapeDrawer.rectangle(disabledBounds, C.UI_DISABLED_LINE_COLOR);
      shapeDrawer.line(
          disabledBounds.x,
          disabledBounds.y,
          disabledBounds.x + disabledBounds.width,
          disabledBounds.y + disabledBounds.height,
          C.UI_DISABLED_LINE_COLOR);
      shapeDrawer.line(
          disabledBounds.x,
          disabledBounds.y + disabledBounds.height,
          disabledBounds.x + disabledBounds.width,
          disabledBounds.y,
          C.UI_DISABLED_LINE_COLOR);
    }
  }

  private Rectangle shrinkRectangle(Rectangle bounds, float offset) {
    float x = bounds.x;
    float y = bounds.y;
    float width = bounds.width;
    float height = bounds.height;

    float new_width = width - (2 * offset);
    float new_height = height - (2 * offset);
    float new_x = x + offset;
    float new_y = y + offset;

    scratchRect.set(new_x, new_y, new_width, new_height);
    return scratchRect;
  }

  @Override
  public void onClick(float mouseX, float mouseY) {
    if (isEnabled()) onClick.accept(unitType);
  }
}
