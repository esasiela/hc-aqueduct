package com.hedgecourt.aqueduct.tilegen;

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
  static final Color WALL_COLOR = Color.BLACK;

  public static void main(String[] args) throws Exception {
    int imgW = TILE_SIZE * COLS;
    int imgH = TILE_SIZE * ROWS;

    BufferedImage sheet = new BufferedImage(imgW, imgH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = sheet.createGraphics();
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // fully transparent background
    g.setColor(new Color(0, 0, 0, 0));
    g.fillRect(0, 0, imgW, imgH);

    g.setColor(WALL_COLOR);

    for (int bitmask = 0; bitmask < 16; bitmask++) {
      int col = bitmask % COLS;
      int row = bitmask / COLS;
      int ox = col * TILE_SIZE; // origin x of this tile
      int oy = row * TILE_SIZE; // origin y of this tile

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

    int cx = ox + TILE_SIZE / 2;
    int cy = oy + TILE_SIZE / 2;
    int half = TILE_SIZE / 2;
    int hw = WALL_THICKNESS / 2;

    if (!n && !e && !s && !w) {
      // island — small square in center
      g.fillRect(cx - hw, cy - hw, WALL_THICKNESS, WALL_THICKNESS);
      return;
    }

    // filled center square wherever any connection exists
    g.fillRect(cx - hw, cy - hw, WALL_THICKNESS, WALL_THICKNESS);

    // arms — rect from center to each connected edge
    if (n) g.fillRect(cx - hw, oy,      WALL_THICKNESS, half);
    if (s) g.fillRect(cx - hw, cy,      WALL_THICKNESS, half);
    if (e) g.fillRect(cx,      cy - hw, half,           WALL_THICKNESS);
    if (w) g.fillRect(ox,      cy - hw, half,           WALL_THICKNESS);
  }
}
