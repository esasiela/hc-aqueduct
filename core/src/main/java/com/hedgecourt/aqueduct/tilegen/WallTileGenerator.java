package com.hedgecourt.aqueduct.tilegen;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class WallTileGenerator {

  static final int TILE_SIZE = 32;
  static final int COLS = 4;
  static final int ROWS = 4;
  static final int WALL_THICKNESS = 8;
  static final int ENDCAP_EXTRA = 3;
  static final int BORDER_THICKNESS = 2;
  static final boolean ENABLE_ENDCAPS = true;
  static final boolean ENABLE_BORDER = true;
  static final boolean ENABLE_ROUND_ELBOWS = true;

  static final Color WALL_COLOR = new Color(80, 80, 80, 255);
  static final Color BORDER_COLOR = new Color(0, 0, 0, 255);

  public static void main(String[] args) throws Exception {
    int imgW = TILE_SIZE * COLS;
    int imgH = TILE_SIZE * ROWS;

    BufferedImage sheet = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = sheet.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // fully transparent background
    g.setColor(new Color(0, 0, 0, 0));
    g.fillRect(0, 0, imgW, imgH);

    for (int bitmask = 0; bitmask < 16; bitmask++) {
      int col = bitmask % COLS;
      int row = bitmask / COLS;
      int ox = col * TILE_SIZE;
      int oy = row * TILE_SIZE;
      drawTile(g, ox, oy, bitmask);
    }

    g.dispose();

    File out = new File("assets/tilegen/wall_tiles.png");
    out.getParentFile().mkdirs();
    ImageIO.write(sheet, "PNG", out);
    System.out.println("Written: " + out.getAbsolutePath());
  }

  static void drawTile(Graphics2D g, int ox, int oy, int bitmask) {
    boolean n = (bitmask & 8) != 0;
    boolean e = (bitmask & 4) != 0;
    boolean s = (bitmask & 2) != 0;
    boolean w = (bitmask & 1) != 0;
    int connectionCount = (n ? 1 : 0) + (e ? 1 : 0) + (s ? 1 : 0) + (w ? 1 : 0);

    int cx = ox + TILE_SIZE / 2;
    int cy = oy + TILE_SIZE / 2;
    int half = TILE_SIZE / 2;
    int hw = WALL_THICKNESS / 2;
    int b = BORDER_THICKNESS;

    if (ENABLE_ROUND_ELBOWS) {
      if (n && e && !s && !w) {
        drawRoundedElbow(g, ox + TILE_SIZE, oy, 180);
        return;
      }
      if (n && w && !s && !e) {
        drawRoundedElbow(g, ox, oy, 270);
        return;
      }
      if (s && e && !n && !w) {
        drawRoundedElbow(g, ox + TILE_SIZE, oy + TILE_SIZE, 90);
        return;
      }
      if (s && w && !n && !e) {
        drawRoundedElbow(g, ox, oy + TILE_SIZE, 0);
        return;
      }
    }

    if (ENABLE_BORDER) {
      // draw border pass first (slightly larger, in border color)
      g.setColor(BORDER_COLOR);

      // center
      g.fillRect(cx - hw - b, cy - hw - b, WALL_THICKNESS + b * 2, WALL_THICKNESS + b * 2);

      // arms
      if (n) g.fillRect(cx - hw - b, oy, WALL_THICKNESS + b * 2, half);
      if (s) g.fillRect(cx - hw - b, cy, WALL_THICKNESS + b * 2, half);
      if (e) g.fillRect(cx, cy - hw - b, half, WALL_THICKNESS + b * 2);
      if (w) g.fillRect(ox, cy - hw - b, half, WALL_THICKNESS + b * 2);

      // endcap borders
      if (ENABLE_ENDCAPS) {
        int er = hw + ENDCAP_EXTRA + b;
        if (connectionCount <= 1) {
          fillCircle(g, cx, cy, er);
        }
      }
    }

    // draw fill pass (normal size, in wall color)
    g.setColor(WALL_COLOR);

    // arms
    // center
    g.fillRect(cx - hw, cy - hw, WALL_THICKNESS, WALL_THICKNESS);

    // arms
    if (n) g.fillRect(cx - hw, oy, WALL_THICKNESS, half);
    if (s) g.fillRect(cx - hw, cy, WALL_THICKNESS, half);
    if (e) g.fillRect(cx, cy - hw, half, WALL_THICKNESS);
    if (w) g.fillRect(ox, cy - hw, half, WALL_THICKNESS);

    // endcap fills
    if (ENABLE_ENDCAPS) {
      int er = hw + ENDCAP_EXTRA;
      if (connectionCount <= 1) {
        fillCircle(g, cx, cy, er);
      }
    }
  }

  static void fillCircle(Graphics2D g, int cx, int cy, int radius) {
    g.fillOval(cx - radius, cy - radius, radius * 2, radius * 2);
  }

  static void drawRoundedElbow(Graphics2D g, int arcCenterX, int arcCenterY, int startAngle) {
    int hw = WALL_THICKNESS / 2;
    int b = BORDER_THICKNESS;

    // border pass
    if (ENABLE_BORDER) {
      g.setColor(BORDER_COLOR);
      fillArcCentered(g, arcCenterX, arcCenterY, TILE_SIZE / 2 + hw + b, startAngle);
    }

    // wall fill pass
    g.setColor(WALL_COLOR);
    fillArcCentered(g, arcCenterX, arcCenterY, TILE_SIZE / 2 + hw, startAngle);

    // inner border ring
    if (ENABLE_BORDER) {
      g.setColor(BORDER_COLOR);
      fillArcCentered(g, arcCenterX, arcCenterY, TILE_SIZE / 2 - hw, startAngle);
    }

    // Hassan, CHOP!
    g.setComposite(AlphaComposite.Clear);
    fillArcCentered(
        g, arcCenterX, arcCenterY, TILE_SIZE / 2 - hw - (ENABLE_BORDER ? b : 0), startAngle);
    g.setComposite(AlphaComposite.SrcOver);
  }

  static void fillArcCentered(Graphics2D g, int cx, int cy, int radius, int startAngle) {
    g.fillArc(cx - radius, cy - radius, radius * 2, radius * 2, startAngle, 90);
  }
}
