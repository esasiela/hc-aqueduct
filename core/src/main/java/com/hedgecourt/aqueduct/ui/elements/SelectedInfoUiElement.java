package com.hedgecourt.aqueduct.ui.elements;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.hedgecourt.aqueduct.C;
import com.hedgecourt.aqueduct.FontManager;
import com.hedgecourt.aqueduct.FontManager.FontType;
import com.hedgecourt.aqueduct.ui.UiElement;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.entities.Building;
import com.hedgecourt.aqueduct.world.entities.Entity;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import com.hedgecourt.aqueduct.world.entities.Unit;
import com.hedgecourt.aqueduct.world.entities.Worker;
import java.util.ArrayList;
import java.util.Deque;
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

    List<Entity> selectedEntities = new ArrayList<>();
    for (Entity entity : world.getEntities()) {
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
      Entity entity = selectedEntities.getFirst();
      if (entity instanceof Building building) {
        lines = getBuildingLines(building);
      } else if (entity instanceof Unit unit) {
        lines = getUnitLines(unit);
      } else {
        lines = List.of("Selection type: " + entity.getClass().getSimpleName());
      }
    }

    font.draw(batch, String.join("\n", lines), bounds.x + 5f, bounds.y + bounds.height - 5f);

    shapeDrawer.rectangle(bounds, C.UI_LINE_COLOR, 1f);
  }

  private List<String> getUnitLines(Unit u) {
    List<String> lines = new ArrayList<>();

    lines.add("Unit: " + u.getDisplayName());
    lines.add("");

    if (u instanceof Worker w) {
      lines.add(w.getPlan().getPlanType() + " / " + w.getState());
      lines.add("");
      lines.add(
          String.format(
              "Bag: %d / %d (%d%%)",
              Math.round(w.getCarrying()),
              Math.round(w.getCarryCapacity()),
              Math.round(w.getCarryPct() * 100)));
      lines.add(String.format("     %s", w.getCarryingType() != null ? w.getCarryingType() : ""));
    }

    return lines;
  }

  private List<String> getBuildingLines(Building b) {
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

      if (b instanceof TownHall t) {
        lines.add("");

        Deque<Unit> queue = t.getTrainingQueue();
        lines.add("Training Queue: " + queue.size());
        if (!queue.isEmpty()) {
          Unit current = queue.peek();
          lines.add(
              String.format(
                  " Current: %s (%d%%)",
                  current.getClass().getSimpleName(), Math.round(current.getTrainingPct() * 100f)));

          queue.stream()
              .collect(
                  Collectors.groupingBy(u -> u.getClass().getSimpleName(), Collectors.counting()))
              .forEach((name, count) -> lines.add("  " + count + " " + name));
        }
      }

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

  private List<String> getMultiSelectionLines(List<Entity> selected) {
    Map<String, Long> counts =
        selected.stream()
            .collect(
                Collectors.groupingBy(e -> e.getClass().getSimpleName(), Collectors.counting()));

    return counts.entrySet().stream()
        .map(entry -> entry.getValue() + " " + entry.getKey())
        .collect(Collectors.toList());
  }
}
