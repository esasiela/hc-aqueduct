package com.hedgecourt.aqueduct;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.hedgecourt.aqueduct.world.AqueductWorld;
import com.hedgecourt.aqueduct.world.BuildingFactory;
import com.hedgecourt.aqueduct.world.ItemDefinition;
import com.hedgecourt.aqueduct.world.ItemFactory;
import com.hedgecourt.aqueduct.world.MapGraph;
import com.hedgecourt.aqueduct.world.Pathfinder;
import com.hedgecourt.aqueduct.world.UnitFactory;
import com.hedgecourt.aqueduct.world.entities.Building;
import com.hedgecourt.aqueduct.world.entities.Node;
import com.hedgecourt.aqueduct.world.entities.Unit;

public class AqueductLoader {
  private final AqueductWorld world;
  private final AssetManager assetManager;

  public AqueductLoader(AqueductWorld world, AssetManager assetManager) {
    this.world = world;
    this.assetManager = assetManager;
  }

  public void load(
      String itemsJsonFilename,
      String buildingsJsonFilename,
      String unitsJsonFilename,
      String mapFilename)
      throws InvalidMapException, InvalidItemConfigException {
    world.clear();

    /* ****
     * Instantiate sprite & building factory so we can get asset paths
     */
    SpriteFactory spriteFactory = new SpriteFactory();
    world.setSpriteFactory(spriteFactory);

    BuildingFactory buildingFactory = new BuildingFactory(world);
    world.setBuildingFactory(buildingFactory);

    ItemFactory itemFactory = new ItemFactory(world);
    world.setItemFactory(itemFactory);

    UnitFactory unitFactory = new UnitFactory(world);
    world.setUnitFactory(unitFactory);

    /* ****
     * Load assets
     */
    buildingFactory.loadAssets(buildingsJsonFilename, assetManager);
    itemFactory.loadAssets(itemsJsonFilename, assetManager);
    unitFactory.loadAssets(unitsJsonFilename, assetManager);

    assetManager.finishLoading();

    /* ****
     * Load factory definitions, must go after assetManager is loaded
     */
    buildingFactory.loadDefinitions(buildingsJsonFilename, assetManager);
    buildingFactory.buildSprites(assetManager);

    itemFactory.loadDefinitions(itemsJsonFilename, assetManager);
    itemFactory.buildSprites(assetManager);

    unitFactory.loadDefinitions(unitsJsonFilename, assetManager);
    unitFactory.buildSprites(assetManager);

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
    Unit worker1 =
        world
            .getUnitFactory()
            .create("worker", world.getMapWidth() / 6f, world.getMapHeight() / 3f);
    world.add(worker1);

    Unit worker2 =
        world
            .getUnitFactory()
            .create("worker", world.getMapWidth() / 6f + 64f, world.getMapHeight() / 3f);
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
        String itemType = obj.getName();
        if (itemType == null || itemType.isEmpty()) {
          throw new RuntimeException(
              "EntityLayer: node object missing name (itemType) " + "at (" + x + "," + y + ")");
        }

        ItemDefinition itemDefinition = world.getItemFactory().get(itemType);

        Node node = new Node(world, centerX, centerY, w, h, itemDefinition);
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
