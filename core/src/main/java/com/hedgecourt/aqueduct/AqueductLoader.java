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
import com.hedgecourt.aqueduct.world.BuildingFactory;
import com.hedgecourt.aqueduct.world.MapGraph;
import com.hedgecourt.aqueduct.world.Pathfinder;
import com.hedgecourt.aqueduct.world.ResourceDefinition;
import com.hedgecourt.aqueduct.world.ResourceFactory;
import com.hedgecourt.aqueduct.world.UnitFactory;
import com.hedgecourt.aqueduct.world.entities.Building;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.Worker;

public class AqueductLoader {
  private final AqueductWorld world;
  private final AssetManager assetManager;

  public AqueductLoader(AqueductWorld world, AssetManager assetManager) {
    this.world = world;
    this.assetManager = assetManager;
  }

  public void load(
      String resourcesJsonFilename,
      String buildingsJsonFilename,
      String unitsJsonFilename,
      String mapFilename)
      throws InvalidMapException, InvalidResourceConfigException {
    world.clear();

    /* ****
     * Instantiate sprite & building factory so we can get asset paths
     */
    SpriteFactory spriteFactory = new SpriteFactory();
    world.setSpriteFactory(spriteFactory);

    BuildingFactory buildingFactory = new BuildingFactory(world);
    world.setBuildingFactory(buildingFactory);

    ResourceFactory resourceFactory = new ResourceFactory(world);
    world.setResourceFactory(resourceFactory);

    UnitFactory unitFactory = new UnitFactory(world);
    world.setUnitFactory(unitFactory);

    /* ****
     * Load assets
     */
    assetManager.load(C.WORKER_DEFAULT_SPRITE_PATH, Texture.class);

    String pipoyaBaseChipPath = "maps/[Base]BaseChip_pipo.png";
    String workerSpritePath1 = "characters/pipoya/Animal/Dog-01-3r.png";
    String workerSpritePath2 = "characters/pipoya/Animal/Cat-01-2r.png";
    String townhallSpritePath = "maps/TREE_HOUSE4.png";
    assetManager.load(pipoyaBaseChipPath, Texture.class);
    assetManager.load(workerSpritePath1, Texture.class);
    assetManager.load(workerSpritePath2, Texture.class);
    assetManager.load(townhallSpritePath, Texture.class);

    buildingFactory.loadAssets(buildingsJsonFilename, assetManager);
    resourceFactory.loadAssets(resourcesJsonFilename, assetManager);
    unitFactory.loadAssets(unitsJsonFilename, assetManager);

    assetManager.finishLoading();

    /* ****
     * Load buildingFactory, must go after assetManager is loaded
     */
    buildingFactory.loadDefinitions(buildingsJsonFilename, assetManager);
    buildingFactory.buildSprites(assetManager);

    resourceFactory.loadDefinitions(resourcesJsonFilename, assetManager);
    resourceFactory.buildSprites(assetManager);

    unitFactory.loadDefinitions(unitsJsonFilename, assetManager);
    unitFactory.buildSprites(assetManager);

    /* ****
     * Node Resource Definitions
     */
    // world.getResourceConfig().load(resourcesJsonFilename);

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

    /* ****
     * Workers
     */
    Texture workerTexture1 = assetManager.get(workerSpritePath1, Texture.class);
    TextureRegion[][] grid1 = TextureRegion.split(workerTexture1, 32, 32);

    Texture workerTexture2 = assetManager.get(workerSpritePath2, Texture.class);
    TextureRegion[][] grid2 = TextureRegion.split(workerTexture2, 32, 32);

    Worker worker1 =
        new Worker(
            world,
            world.getMapWidth() / 6f,
            world.getMapHeight() / 3f,
            C.ENTITY_RENDER_SIZE,
            C.ENTITY_RENDER_SIZE);
    worker1.buildSprites(grid1);
    world.add(worker1);

    Worker worker2 =
        new Worker(
            world,
            world.getMapWidth() / 6f + 64f,
            world.getMapHeight() / 3f,
            C.ENTITY_RENDER_SIZE,
            C.ENTITY_RENDER_SIZE);
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
      float centerX = x + w / 2f;
      float centerY = y + h / 2f;

      if ("node".equals(objClass)) {
        String resourceType = obj.getName();
        if (resourceType == null || resourceType.isEmpty()) {
          throw new RuntimeException(
              "EntityLayer: node object missing name (resourceType) " + "at (" + x + "," + y + ")");
        }

        ResourceDefinition resourceDefinition = world.getResourceFactory().get(resourceType);

        Node node = new Node(world, centerX, centerY, w, h, resourceDefinition);
        node.setId(id);

        world.add(node);

      } else if ("townhall".equals(objClass)) {

        Building building = world.getBuildingFactory().create("townhall", centerX, centerY);
        building.setId(id);

        world.add(building);
        building.addConstructionUnits(building.getConstructionUnitsRequired());

      } else {
        throw new InvalidMapException(
            "unknown entity class: '" + objClass + "' at (" + x + "," + y + ")");
      }
    }
  }
}
