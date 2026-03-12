package com.hedgecourt.aqueduct.world.entities;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.hedgecourt.aqueduct.sprite.EntitySprite;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import space.earlygrey.shapedrawer.ShapeDrawer;

public abstract class Entity {

  protected AqueductWorld world;

  protected Vector2 position;
  protected float width;
  protected float height;
  protected boolean selected;
  protected boolean hovered;

  protected EntitySprite sprite;

  public Entity(AqueductWorld world, float x, float y, float width, float height) {
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

  public float distanceTo(Entity entity) {
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

  public void updateSprite(float delta) {
    if (sprite != null) sprite.update(delta);
  }

  public void draw(SpriteBatch batch, ShapeDrawer drawer) {
    if (sprite != null)
      sprite.draw(batch, position.x - width / 2f, position.y - height / 2f, width, height);
  }

  // ── getters & setters  ────────────────────────────────────────────────────────────

  public Vector2 getPosition() {
    return position;
  }

  public float getWidth() {
    return width;
  }

  public float getHeight() {
    return height;
  }

  public void setPosition(float x, float y) {
    position.set(x, y);
  }

  public EntitySprite getSprite() {
    return sprite;
  }

  public void setSprite(EntitySprite sprite) {
    this.sprite = sprite;
  }
}
