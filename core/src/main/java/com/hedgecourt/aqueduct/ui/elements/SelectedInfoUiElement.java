package com.hedgecourt.aqueduct.ui.elements;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.FontManager;
import com.hedgecourt.aqueduct.FontManager.FontType;
import com.hedgecourt.aqueduct.ui.UiElement;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.entities.BuildingEntity;
import com.hedgecourt.aqueduct.world.entities.Worker;
import com.hedgecourt.aqueduct.world.entities.WorldEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class SelectedInfoUiElement extends UiElement {

  private final AqueductWorld world;
  private final BitmapFont font;

  public SelectedInfoUiElement(
      AqueductWorld world, FontManager fontManager, float x, float y, float width, float height) {
    super(x, y, width, height);

    this.world = world;
    this.font = fontManager.getFont(FontType.BUILDING_INFO_UI);
  }

  @Override
  public void draw(SpriteBatch batch, ShapeDrawer shapeDrawer) {

    List<WorldEntity> selectedEntities = new ArrayList<>();
    for (WorldEntity entity : world.getEntities()) {
      if (entity.isSelected()) selectedEntities.add(entity);
    }

    List<String> lines;
    if (selectedEntities.isEmpty()) {
      lines = List.of("Selection: -- none --");
    } else if (selectedEntities.size() > 1) {
      lines = new ArrayList<>();
      lines.add("Multiple selections: " + selectedEntities.size());
      lines.addAll(getMultiSelectionLines(selectedEntities));
    } else {
      WorldEntity entity = selectedEntities.getFirst();
      if (entity instanceof BuildingEntity building) {
        lines = getBuildingLines(building);
      } else if (entity instanceof Worker worker) {
        lines = getWorkerLines(worker);
      } else {
        lines = List.of("Selection type: " + entity.getClass().getSimpleName());
      }
    }

    font.draw(batch, String.join("\n", lines), bounds.x + 5f, bounds.y + bounds.height - 5f);

    shapeDrawer.rectangle(bounds, C.UI_LINE_COLOR, 1f);
  }

  private List<String> getWorkerLines(Worker w) {
    List<String> lines = new ArrayList<>();

    lines.add("Worker:");
    lines.add("");
    lines.add(w.getPlan().getPlanType() + " / " + w.getState());
    lines.add("");
    lines.add(
        String.format(
            "Bag: %d / %d (%d%%)",
            Math.round(w.getCarrying()),
            Math.round(w.getCarryCapacity()),
            Math.round(w.getCarryPct() * 100)));
    lines.add(String.format("     %s", w.getCarryingType() != null ? w.getCarryingType() : ""));
    return lines;
  }

  private List<String> getBuildingLines(BuildingEntity b) {
    List<String> lines = new ArrayList<>();

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
    return lines;
  }

  private List<String> getMultiSelectionLines(List<WorldEntity> selected) {
    Map<String, Long> counts =
        selected.stream()
            .collect(
                Collectors.groupingBy(e -> e.getClass().getSimpleName(), Collectors.counting()));

    return counts.entrySet().stream()
        .map(entry -> entry.getValue() + " " + entry.getKey())
        .collect(Collectors.toList());
  }
}
