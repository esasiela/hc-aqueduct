package com.hedgecourt.aqueduct.tilegen;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class WallTileGenerator4 {

  static final int TILE_SIZE = 32;
  static final int COLS = 4;
  static final int ROWS = 4;
  static final int WALL_THICKNESS = 8;
  static final int ENDCAP_EXTRA = 3;
  static final int BORDER_THICKNESS = 2;
  static final boolean ENABLE_ENDCAPS = true;
  static final boolean ENABLE_BORDER = true;
  static final boolean ENABLE_ROUND_ELBOWS = true;
  static final int ELBOW_INSET = 3;

  static final Color WALL_COLOR = new Color(80, 80, 80, 255);
  static final Color BORDER_COLOR = new Color(0, 0, 0, 255);

  public static void main(String[] args) throws Exception {
    generate();
  }

  public static void generate() throws IOException {
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

    File out = new File("assets/tilegen/wall_tiles_4directions.png");
    out.getParentFile().mkdirs();
    ImageIO.write(sheet, "PNG", out);
    System.out.println(
        WallTileGenerator4.class.getSimpleName() + " - written: " + out.getAbsolutePath());
  }

  static void drawTile(Graphics2D g, int ox, int oy, int bitmask) {
    boolean c = true;
    boolean n = (bitmask & 8) != 0;
    boolean e = (bitmask & 4) != 0;
    boolean s = (bitmask & 2) != 0;
    boolean w = (bitmask & 1) != 0;
    int connectionCount = (n ? 1 : 0) + (e ? 1 : 0) + (s ? 1 : 0) + (w ? 1 : 0);

    int cx = ox + TILE_SIZE / 2;
    int cy = oy + TILE_SIZE / 2;
    int ft = TILE_SIZE;
    int ht = TILE_SIZE / 2;
    int fw = WALL_THICKNESS;
    int hw = WALL_THICKNESS / 2;
    int b = BORDER_THICKNESS;

    // start off with center rect + rect from each edge to center, and prune them as you go.
    java.awt.Rectangle cRect = new java.awt.Rectangle(cx - hw, cy - hw, fw, fw);
    java.awt.Rectangle nRect = new java.awt.Rectangle(cx - hw, oy, fw, ht);
    java.awt.Rectangle eRect = new java.awt.Rectangle(cx, cy - hw, ht, fw);
    java.awt.Rectangle sRect = new java.awt.Rectangle(cx - hw, cy, fw, ht);
    java.awt.Rectangle wRect = new java.awt.Rectangle(ox, cy - hw, ht, fw);

    if (ENABLE_ROUND_ELBOWS) {
      if (n && e && !s && !w) {
        drawRoundedElbow(g, ox + TILE_SIZE - ELBOW_INSET, oy + ELBOW_INSET, 180);
        c = false;
        nRect.height = ELBOW_INSET;
        eRect.x = ox + ft - ELBOW_INSET;
        eRect.width = ELBOW_INSET;

      } else if (n && w && !s && !e) {
        drawRoundedElbow(g, ox + ELBOW_INSET, oy + ELBOW_INSET, 270);
        c = false;
        nRect.height = ELBOW_INSET;
        wRect.width = ELBOW_INSET;

      } else if (s && e && !n && !w) {
        drawRoundedElbow(g, ox + TILE_SIZE - ELBOW_INSET, oy + TILE_SIZE - ELBOW_INSET, 90);
        c = false;
        sRect.y = sRect.y + (ht - ELBOW_INSET);
        sRect.height = ELBOW_INSET;
        eRect.x = eRect.x + (ht - ELBOW_INSET);
        eRect.width = ELBOW_INSET;

      } else if (s && w && !n && !e) {
        drawRoundedElbow(g, ox + ELBOW_INSET, oy + TILE_SIZE - ELBOW_INSET, 0);
        c = false;
        sRect.y = sRect.y + (ht - ELBOW_INSET);
        sRect.height = ELBOW_INSET;
        wRect.width = ELBOW_INSET;
      }
    }

    if (ENABLE_BORDER) {
      // draw border pass first (slightly larger, in border color)
      g.setColor(BORDER_COLOR);

      // center
      if (c) g.fillRect(cRect.x - b, cRect.y - b, cRect.width + b * 2, cRect.height + b * 2);

      // arms
      if (n) g.fillRect(nRect.x - b, nRect.y, nRect.width + b * 2, nRect.height);
      if (e) g.fillRect(eRect.x, eRect.y - b, eRect.width, eRect.height + b * 2);
      if (s) g.fillRect(sRect.x - b, sRect.y, sRect.width + b * 2, sRect.height);
      if (w) g.fillRect(wRect.x, wRect.y - b, wRect.width, wRect.height + b * 2);

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
    if (c) g.fillRect(cRect.x, cRect.y, cRect.width, cRect.height);

    // arms
    if (n) g.fillRect(nRect.x, nRect.y, nRect.width, nRect.height);
    if (e) g.fillRect(eRect.x, eRect.y, eRect.width, eRect.height);
    if (s) g.fillRect(sRect.x, sRect.y, sRect.width, sRect.height);
    if (w) g.fillRect(wRect.x, wRect.y, wRect.width, wRect.height);

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
    int r = TILE_SIZE / 2 - ELBOW_INSET;

    // border pass
    if (ENABLE_BORDER) {
      g.setColor(BORDER_COLOR);
      fillArcCentered(g, arcCenterX, arcCenterY, r + hw + b, startAngle);
    }

    // wall fill pass
    g.setColor(WALL_COLOR);
    fillArcCentered(g, arcCenterX, arcCenterY, r + hw, startAngle);

    // inner border ring
    if (ENABLE_BORDER) {
      g.setColor(BORDER_COLOR);
      fillArcCentered(g, arcCenterX, arcCenterY, r - hw, startAngle);
    }

    // Hassan, CHOP!
    g.setComposite(AlphaComposite.Clear);
    fillArcCentered(g, arcCenterX, arcCenterY, r - hw - (ENABLE_BORDER ? b : 0), startAngle);
    g.setComposite(AlphaComposite.SrcOver);
  }

  static void fillArcCentered(Graphics2D g, int cx, int cy, int radius, int startAngle) {
    g.fillArc(cx - radius, cy - radius, radius * 2, radius * 2, startAngle, 90);
  }
}
