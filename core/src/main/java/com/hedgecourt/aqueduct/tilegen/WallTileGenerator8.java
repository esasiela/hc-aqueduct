package com.hedgecourt.aqueduct.tilegen;

import com.hedgecourt.aqueduct.C;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class WallTileGenerator8 {

  static final int TILE_SIZE = 32;
  static final int COLS = 16;
  static final int ROWS = 16;
  static final int WALL_THICKNESS = 8;
  static final int ENDCAP_EXTRA = 3;
  static final int BORDER_THICKNESS = 2;
  static final boolean ENABLE_ENDCAPS = false;
  static final boolean ENABLE_BORDER = false;
  static final boolean ENABLE_ROUND_ELBOWS = false;
  static final int ELBOW_INSET = 0;

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

    for (int bitmask = 0; bitmask < ROWS * COLS; bitmask++) {
      int col = bitmask % COLS;
      int row = bitmask / COLS;
      int ox = col * TILE_SIZE;
      int oy = row * TILE_SIZE;
      drawTile(g, ox, oy, bitmask);
    }

    g.dispose();

    File out = new File("assets/tilegen/wall_tiles_8directions.png");
    out.getParentFile().mkdirs();
    ImageIO.write(sheet, "PNG", out);
    System.out.println(
        WallTileGenerator8.class.getSimpleName() + " - written: " + out.getAbsolutePath());
  }

  static void drawTile(Graphics2D gSheet, int tileX, int tileY, int bitmask) {
    int scratchW = TILE_SIZE * 3;
    int scratchH = TILE_SIZE * 3;
    BufferedImage scratch = new BufferedImage(scratchW, scratchH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = scratch.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // fully transparent background
    g.setColor(new Color(0, 0, 0, 0));
    g.fillRect(0, 0, scratchW, scratchH);

    boolean c = true;
    boolean n = (bitmask & C.WALL_BIT_N) != 0;
    boolean ne = (bitmask & C.WALL_BIT_NE) != 0;
    boolean e = (bitmask & C.WALL_BIT_E) != 0;
    boolean se = (bitmask & C.WALL_BIT_SE) != 0;
    boolean s = (bitmask & C.WALL_BIT_S) != 0;
    boolean sw = (bitmask & C.WALL_BIT_SW) != 0;
    boolean w = (bitmask & C.WALL_BIT_W) != 0;
    boolean nw = (bitmask & C.WALL_BIT_NW) != 0;

    int connectionCount =
        (n ? 1 : 0)
            + (ne ? 1 : 0)
            + (e ? 1 : 0)
            + (se ? 1 : 0)
            + (s ? 1 : 0)
            + (sw ? 1 : 0)
            + (w ? 1 : 0)
            + (nw ? 1 : 0);

    int ox = TILE_SIZE;
    int oy = TILE_SIZE;

    int cx = ox + TILE_SIZE / 2;
    int cy = oy + TILE_SIZE / 2;
    int ft = TILE_SIZE;
    int ht = TILE_SIZE / 2;
    int fw = WALL_THICKNESS;
    int hw = WALL_THICKNESS / 2;
    int b = BORDER_THICKNESS;

    g.setColor(WALL_COLOR);

    if (n) drawArm(g, cx, cy, 180);
    if (ne) drawArm(g, cx, cy, 225);
    if (e) drawArm(g, cx, cy, 270);
    if (se) drawArm(g, cx, cy, 315);
    if (s) drawArm(g, cx, cy, 0);
    if (sw) drawArm(g, cx, cy, 45);
    if (w) drawArm(g, cx, cy, 90);
    if (nw) drawArm(g, cx, cy, 135);

    // bury the turds
    fillCircle(g, cx, cy, hw);

    if (false) {
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

      if (!n && !e && !s && !w) {
        // TODO remove the specific island branch
        // int diagLen = Math.round(TILE_SIZE * 1.5f);
        // int diagYOffset = 6;
        int diagLen = (int) Math.ceil(TILE_SIZE * Math.sqrt(2));
        int diagYOffset = (int) Math.ceil(TILE_SIZE / 2.0 * Math.sqrt(2) - TILE_SIZE / 2.0);

        // NE arm
        /*
        g.rotate(Math.toRadians(45), cx, cy);
        g.fillRect(cx - hw, oy - diagYOffset, fw, diagLen / 2);
        g.rotate(Math.toRadians(-45), cx, cy);



        // SW arm
        g.rotate(Math.toRadians(45), cx, cy);
        g.fillRect(cx - hw, cy, fw, diagLen / 2);
        g.rotate(Math.toRadians(-45), cx, cy);



        // NW arm
        g.rotate(Math.toRadians(-45), cx, cy);
        g.fillRect(cx - hw, oy - diagYOffset, fw, diagLen / 2);
        g.rotate(Math.toRadians(45), cx, cy);



        // SE arm
        g.rotate(Math.toRadians(-45), cx, cy);
        g.fillRect(cx - hw, cy, fw, diagLen / 2);
        g.rotate(Math.toRadians(45), cx, cy);

         */
        nw = true;

        if (n) drawArm(g, cx, cy, 180);
        if (ne) drawArm(g, cx, cy, 225);
        if (e) drawArm(g, cx, cy, 270);
        if (se) drawArm(g, cx, cy, 315);
        if (s) drawArm(g, cx, cy, 0);
        if (sw) drawArm(g, cx, cy, 45);
        if (w) drawArm(g, cx, cy, 90);
        if (nw) drawArm(g, cx, cy, 135);
      } else {

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
    }

    /* ****
     * Clip n Copy
     */
    gSheet.drawImage(
        scratch.getSubimage(TILE_SIZE, TILE_SIZE, TILE_SIZE, TILE_SIZE), tileX, tileY, null);
    g.dispose();
  }

  static void drawArm(Graphics2D g, int cx, int cy, double angleDeg) {
    g.rotate(Math.toRadians(angleDeg), cx, cy);
    g.fillRect(cx - WALL_THICKNESS / 2, cy, WALL_THICKNESS, TILE_SIZE);
    g.rotate(Math.toRadians(-angleDeg), cx, cy);
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
