package com.hedgecourt.aqueduct.world;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class WorldEntity {

  protected AqueductWorld world;

  protected Vector2 position;
  protected float width;
  protected float height;
  protected boolean selected;
  protected boolean hovered;

  public WorldEntity(AqueductWorld world, float x, float y, float width, float height) {
    this.world = world;
    this.position = new Vector2(x, y);
    this.width = width;
    this.height = height;
  }

  // ── bounds ────────────────────────────────────────────────────────────────

  public Rectangle getBounds() {
    return new Rectangle(position.x - width / 2f, position.y - height / 2f, width, height);
  }

  public boolean containsPoint(float x, float y) {
    return getBounds().contains(x, y);
  }

  // ── distance ──────────────────────────────────────────────────────────────

  public float distanceTo(float x, float y) {
    return position.dst(x, y);
  }

  public float distanceTo(WorldEntity entity) {
    return position.dst(entity.position);
  }

  public float distanceTo(Vector2 coords) {
    return position.dst(coords);
  }

  // ── selection & hover ─────────────────────────────────────────────────────

  public void select() {
    this.selected = true;
  }

  public void deselect() {
    this.selected = false;
  }

  public boolean isSelected() {
    return selected;
  }

  public void setHovered(boolean hovered) {
    this.hovered = hovered;
  }

  public boolean isHovered() {
    return hovered;
  }

  // ── tooltip ───────────────────────────────────────────────────────────────

  public String getHoverTooltip() {
    return null;
  }

  // ── frame hook ────────────────────────────────────────────────────────────

  public abstract void update(float delta);

  public abstract void draw(SpriteBatch batch, ShapeDrawer drawer);

  // ── getters ────────────────────────────────────────────────────────────

  public Vector2 getPosition() {
    return position;
  }

  public float getWidth() {
    return width;
  }

  public float getHeight() {
    return height;
  }

  // ── setters ────────────────────────────────────────────────────────────

  public void setPosition(float x, float y) {
    position.set(x, y);
  }
}
