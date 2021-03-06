/**
 * 
 */
package eu.ec.eurostat.bd.proximus;

import java.io.IOException;
import java.net.MalformedURLException;

import org.geotools.data.shapefile.shp.ShapefileException;

/**
 * @author julien Gaffuri
 *
 */
public class ProximusProject {
	public static String BASE_PATH = "H:/geodata/";
	public static String BASE_PATH_ = BASE_PATH + "BE_mobile_phone_proximus/";
	public static String ESTAT_POP_NUTS_PATH = BASE_PATH + "eurobase/BE_pop_nuts3.csv";
	public static String ESTAT_POP_MUNICIPALITIES_PATH = BASE_PATH + "census_hub/population_be_2011_municipalities.csv";
	public static String GEOSTAT_POP_PATH = BASE_PATH_ + "grid_pop_2011.csv";
	public static String NUTS_PATH = BASE_PATH_ + "nuts3.shp";
	public static String MUNICIPALITIES_PATH = BASE_PATH_ + "municipalities.shp";
	public static String GEOSTAT_GRID_PATH = BASE_PATH_ + "grid.shp";
	public static String PROXIMUS_VORONOI = BASE_PATH_ + "voronoi.shp";
	public static String PROXIMUS_VORONOI_POP_PATH = BASE_PATH_ + "voronoi_data.csv";
	public static String BUILDINGS_SHP_PATH = BASE_PATH_ + "bu_be_housing.shp"; //

	public static String gridBuildingStats = BASE_PATH_ + "building_intersection_with_grid.csv";
	public static String buildingDensityFromGrid = BASE_PATH_ + "building_pop_from_grid.csv";
	public static String voronoiBuildingStats = BASE_PATH_ + "building_intersection_with_voronoi.csv";
	public static String buildingDensityFromVoronoi = BASE_PATH_ + "building_pop_from_voronoi.csv";

	public static void main(String[] args) throws ShapefileException, MalformedURLException, IOException {
		System.out.println("Start");

		//computeGridAttribute(GEOSTAT_GRID_PATH);

		/*StatisticalUnitsIntersectionMatrix.compute("municipality", MUNICIPALITIES_PATH, "CENSUS_ID", "grid", GEOSTAT_GRID_PATH, "CELLCODE", BASE_PATH_ + "");
		StatisticalUnitsIntersectionMatrix.compute("nuts", NUTS_PATH, "NUTS_ID", "grid", GEOSTAT_GRID_PATH, "CELLCODE", BASE_PATH_ + "");
		StatisticalUnitsIntersectionMatrix.compute("voronoi", PROXIMUS_VORONOI, "voronoi_id", "grid", GEOSTAT_GRID_PATH, "CELLCODE", BASE_PATH_ + "");
		StatisticalUnitsIntersectionMatrix.compute("grid", GEOSTAT_GRID_PATH, "CELLCODE", "building", BUILDINGS_SHP_PATH, "OBJECTID", BASE_PATH_ + "");
		StatisticalUnitsIntersectionMatrix.compute("voronoi", PROXIMUS_VORONOI, "voronoi_id", "building", BUILDINGS_SHP_PATH, "OBJECTID", BASE_PATH_ + "");*/

		//check consistency geostat/censushub
		//compute municipality figures from grids
		//StatisticalUnitsIntersectionMatrix.computeStatValueFromIntersection("municipality", "CENSUS_ID", "grid", "CELLCODE", GEOSTAT_POP_PATH, BASE_PATH_ + ""+"matrix_municipality_from_grid.csv", BASE_PATH_ + "");
		//TODO do it for all municipalities in europe

		//assess the building quantity for each grid cell
		//StatisticalUnitIntersectionWithGeoLayer.aggregateGeoStatsFromGeoToStatisticalUnits(GEOSTAT_GRID_PATH, "CELLCODE", BUILDINGS_SHP_PATH, gridBuildingStats);
		//STEP 1-2. assess the building quantity for each voronoi cell
		//StatisticalUnitIntersectionWithGeoLayer.aggregateGeoStatsFromGeoToStatisticalUnits(PROXIMUS_VORONOI, "voronoi_id", BUILDINGS_SHP_PATH, voronoiBuildingStats);

		//assess the building density based on grid pop
		//StatisticalUnitIntersectionWithGeoLayer.computeGeoStatValueFromStatUnitValue(BUILDINGS_SHP_PATH, "OBJECTID", GEOSTAT_GRID_PATH, "CELLCODE", GEOSTAT_POP_PATH, gridBuildingStats, buildingDensityFromGrid);
		//STEP 3. assess the building density based on voronoi pop
		//StatisticalUnitIntersectionWithGeoLayer.allocateGeoStatsFromStatisticalUnitsToGeo(BUILDINGS_SHP_PATH, "OBJECTID", PROXIMUS_VORONOI, "voronoi_id", PROXIMUS_VORONOI_POP_PATH, voronoiBuildingStats, buildingDensityFromVoronoi);

		//compare building densities - see discrepencies and possibly enrich model to reduce it (further)

		//aggregate building grid/population to grid
		//StatisticalUnitIntersectionWithGeoLayer.aggregateStatValueFomGeoValues(GEOSTAT_GRID_PATH, "CELLCODE", BUILDINGS_SHP_PATH, "OBJECTID", buildingDensityFromGrid, BASE_PATH_ + "grid_pop_from_building_pop_from_grid.csv");
		//compare with initial grid population - should be the same -> ok!

		//STEP 4. aggregate building voronoi/population to grid
		//StatisticalUnitIntersectionWithGeoLayer.aggregateStatValueFomGeoValues(GEOSTAT_GRID_PATH, "CELLCODE", BUILDINGS_SHP_PATH, "OBJECTID", buildingDensityFromVoronoi, BASE_PATH_ + "grid_pop_from_building_pop_from_voronoi.csv");


		//compare with proximus computes grid population
		//compare with initial grid population



		System.out.println("End");
	}





	/*public static void computeGridAttribute(String gridSHP) throws ShapefileException, MalformedURLException, IOException{
		try {
			//1kmN3134E3799 MinN MinE

			//open shapefile
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("url", new File(gridSHP).toURI().toURL());
			DataStore dataStore = DataStoreFinder.getDataStore(map);
			FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
			FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(Filter.INCLUDE);

			//create feature collection for modified features
			//List<SimpleFeature> features = new ArrayList<SimpleFeature>();
			//FeatureCollection<SimpleFeatureType, SimpleFeature> features;
			DefaultFeatureCollection features = new DefaultFeatureCollection("internal", collection.getSchema());
			//SimpleFeatureCollection simpleCollection = DataUtilities.simple(collection);

			//read features
			FeatureIterator<SimpleFeature> it = collection.features();
			while (it.hasNext()) {
				SimpleFeature f = it.next();
				Envelope env = ((Geometry) f.getDefaultGeometryProperty().getValue()).getEnvelopeInternal();
				String n = ""+((int)env.getMinY()/1000);
				String e = ""+((int)env.getMinX()/1000);
				String id = "1kmN"+n+"E"+e;
				//if(id.length()!=13) System.out.println(id);
				f.setAttribute("CELLCODE", id);
				System.out.println( f.getAttribute("CELLCODE") );

				features.add(f);
			}
			it.close();

			//create shapefile
			File newFile = new File(BASE_PATH_ + "grid____.shp");
			ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", newFile.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);
			ShapefileDataStore newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
			newDataStore.createSchema(collection.getSchema());

			//write to shape file
			Transaction transaction = new DefaultTransaction("create");
			SimpleFeatureSource featureSource = newDataStore.getFeatureSource(newDataStore.getTypeNames()[0]);
			if (featureSource instanceof SimpleFeatureStore) {
				SimpleFeatureStore featureStore = (SimpleFeatureStore) featureSource;
				featureStore.setTransaction(transaction);
				try {
					featureStore.addFeatures(features);
					transaction.commit();
				} catch (Exception problem) {
					problem.printStackTrace();
					transaction.rollback();
				} finally {
					transaction.close();
				}
			} else {
				System.out.println("not support read/write access");
			}

		} catch (Exception e) { e.printStackTrace(); }
	}*/

}
