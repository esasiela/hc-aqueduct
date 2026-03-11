package com.hedgecourt.aqueduct.ui.elements;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.FontManager;
import com.hedgecourt.aqueduct.FontManager.FontType;
import com.hedgecourt.aqueduct.ui.UiElement;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import java.util.ArrayList;
import java.util.List;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class BuildingInfoUiElement extends UiElement {

  private final AqueductWorld world;
  private final BitmapFont font;

  public BuildingInfoUiElement(
      AqueductWorld world, FontManager fontManager, float x, float y, float width, float height) {
    super(x, y, width, height);

    this.world = world;
    this.font = fontManager.getFont(FontType.BUILDING_INFO_UI);
  }

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {
    List<String> lines = new ArrayList<>();
    BuildingEntity b = null;
    for (BuildingEntity entity : world.getEntities(BuildingEntity.class)) {
      if (entity.isSelected()) {
        b = entity;
        break;
      }
    }

    if (b != null) {
      lines.add("Building: " + b.getDisplayName());
      lines.add("");

      if (b.isConstructionComplete()) {
        lines.add("Connected: " + (b.isWaterConnected() ? "Yes" : "No"));
        lines.add("");

        lines.add(
            String.format(
                "Water Qty  : %d / %d (%d%%)",
                Math.round(b.getWaterInventory()),
                Math.round(b.getWaterCapacity()),
                Math.round(b.getWaterInventoryPct() * 100f)));
        lines.add(String.format("Cost       : %d", Math.round(b.getWaterCost())));
        lines.add(String.format("Output Rate: %d", Math.round(b.getWaterOutputRate())));
      } else {
        // unitsCompleted / unitsRequired (pct)
        lines.add(
            String.format(
                "Construction: %d / %d (%d%%)",
                Math.round(b.getConstructionUnitsCompleted()),
                Math.round(b.getConstructionUnitsRequired()),
                Math.round(b.getConstructionPct() * 100f)));
      }

    } else {
      lines.add("Building: -- none --");
    }
    font.draw(batch, String.join("\n", lines), bounds.x + 5f, bounds.y + bounds.height - 5f);

    shapeDrawer.rectangle(bounds, C.UI_LINE_COLOR, 1f);
  }
}
