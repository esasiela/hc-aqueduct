package com.hedgecourt.aqueduct;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.MapGraph;
import com.hedgecourt.aqueduct.world.Pathfinder;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.TownHall;
import com.hedgecourt.aqueduct.world.entities.Worker;

public class AqueductLoader {
  private final AqueductWorld world;
  private final AssetManager assetManager;

  public AqueductLoader(AqueductWorld world, AssetManager assetManager) {
    this.world = world;
    this.assetManager = assetManager;
  }

  public void load(String resourcesJsonFilename, String mapFilename)
      throws InvalidMapException, InvalidResourceConfigException {
    world.clear();

    /* ****
     * Resource Config
     */
    world.getResourceConfig().load(resourcesJsonFilename);

    /* ****
     * Open Tiled Map
     */
    TiledMap map = new TmxMapLoader().load(mapFilename);
    int tileWidth = map.getProperties().get("tilewidth", Integer.class);
    int tileHeight = map.getProperties().get("tileheight", Integer.class);
    int mapTilesWide = map.getProperties().get("width", Integer.class);
    int mapTilesTall = map.getProperties().get("height", Integer.class);

    TiledMapTileLayer wallsLayer = (TiledMapTileLayer) map.getLayers().get("walls");
    MapGraph mapGraph = new MapGraph(mapTilesWide, mapTilesTall, wallsLayer);
    Pathfinder pathfinder = new Pathfinder(mapGraph, tileWidth, tileHeight);

    world.initializeMap(
        map, mapGraph, pathfinder, tileWidth, tileHeight, mapTilesWide, mapTilesTall);

    Texture pipoyaBaseChipTexture = new Texture("maps/[Base]BaseChip_pipo.png");

    /* ****
     * Workers
     */
    String workerSpritePath1 = "characters/pipoya/Animal/Dog-01-3r.png";
    String workerSpritePath2 = "characters/pipoya/Animal/Cat-01-2r.png";
    assetManager.load(workerSpritePath1, Texture.class);
    assetManager.load(workerSpritePath2, Texture.class);
    assetManager.finishLoading();

    Texture workerTexture1 = assetManager.get(workerSpritePath1, Texture.class);
    TextureRegion[][] grid1 = TextureRegion.split(workerTexture1, 32, 32);

    Texture workerTexture2 = assetManager.get(workerSpritePath2, Texture.class);
    TextureRegion[][] grid2 = TextureRegion.split(workerTexture2, 32, 32);

    Worker worker1 = new Worker(world, world.getMapWidth() / 6f, world.getMapHeight() / 3f);
    worker1.buildSprites(grid1);
    world.add(worker1);

    Worker worker2 = new Worker(world, world.getMapWidth() / 6f + 64f, world.getMapHeight() / 3f);
    worker2.buildSprites(grid2);
    world.add(worker2);

    /* ****
     * Entities from Tiled Map
     */
    MapObjects objects = map.getLayers().get("entities").getObjects();
    for (MapObject obj : objects) {
      String objClass = obj.getProperties().get("type", String.class);
      float x = obj.getProperties().get("x", Float.class);
      float y = obj.getProperties().get("y", Float.class);
      float w = obj.getProperties().get("width", Float.class);
      float h = obj.getProperties().get("height", Float.class);

      int rawId = obj.getProperties().get("id", Integer.class);
      String id = objClass + "_" + rawId;

      // Tiled Y is top-left, convert to center position with Y-up
      float centreX = x + w / 2f;
      float centreY = y + h / 2f;

      if ("node".equals(objClass)) {
        String resourceType = obj.getName();
        if (resourceType == null || resourceType.isEmpty()) {
          throw new RuntimeException(
              "EntityLayer: node object missing name (resourceType) " + "at (" + x + "," + y + ")");
        }
        world.add(
            new Node(id, centreX, centreY, w, h, world.getResourceConfig().get(resourceType)));

      } else if ("townhall".equals(objClass)) {
        world.add(
            new TownHall(
                id, centreX, centreY, w, h, getPipoyaBaseChip(pipoyaBaseChipTexture, 664)));
      } else {
        throw new InvalidMapException(
            "unknown entity class: '" + objClass + "' at (" + x + "," + y + ")");
      }
    }
  }

  private TextureRegion getPipoyaBaseChip(Texture texture, int spriteId) {
    // packed png 8 cols x 133 rows = 1064 total sprites, index starts at 0
    TextureRegion[][] baseTiles = TextureRegion.split(texture, 32, 32);
    return baseTiles[spriteId / 8][spriteId % 8];
  }
}
