/**
 * 
 */
package eu.ec.estat.bd.proximus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.geotools.data.shapefile.shp.ShapefileException;

import eu.ec.estat.java4eurostat.base.StatsHypercube;
import eu.ec.estat.java4eurostat.base.StatsIndex;
import eu.ec.estat.java4eurostat.io.CSV;
import eu.ec.estat.java4eurostat.io.DicUtil;

/**
 * @author julien Gaffuri
 *
 */
public class Main {
	public static String BASE_PATH = "H:/geodata/";
	public static String ESTAT_POP_PATH = BASE_PATH + "eurobase/BE_pop_nuts3.csv";
	public static String GEOSTAT_POP_PATH = BASE_PATH + "BE_mobile_phone_proximus/mob/grid_pop_2011.csv";
	public static String NUTS_PATH = BASE_PATH + "BE_mobile_phone_proximus/mob/nuts3.shp";
	public static String GEOSTAT_GRID_PATH = BASE_PATH + "BE_mobile_phone_proximus//mob/grid.shp";
	public static String PROXIMUS_VORONOI = BASE_PATH + "BE_mobile_phone_proximus/heatmap_final_ETRS989.shp";
	public static String BUILDINGS_SHP_PATH = BASE_PATH + "BE_mobile_phone_proximus/mob/buildings_wal_lux.shp";

	public static String matrix_nuts_grid = BASE_PATH+"BE_mobile_phone_proximus/mob/matrix_nuts_grid.csv";
	public static String matrix_proximus_grid = BASE_PATH+"BE_mobile_phone_proximus/mob/matrix_proximus_grid.csv";
	public static String stats_grid_building_intersection = BASE_PATH+"BE_mobile_phone_proximus/mob/stats_grid_building_intersection.csv";
	public static String voronoi_building_intersection = BASE_PATH+"BE_mobile_phone_proximus/mob/voronoi_building_intersection.csv";



	//compute Eurostat population dataset from geostat grid and compare with published one
	//TODO: get pop data at LAU level from fabio
	public static void validateEurostatGeostat(String out) throws ShapefileException, MalformedURLException, IOException{
		//test consistency between estat NUTS3/LAU data and geostat grid

		//load estat population data
		HashMap<String, String> estatPop = DicUtil.load(ESTAT_POP_PATH, ",");
		//load grid cells data
		HashMap<String, String> geostatPop = DicUtil.load(GEOSTAT_POP_PATH, ",");
		//load intersection matrix
		StatsHypercube matrix = CSV.load(matrix_nuts_grid, "grid_to_nuts"); matrix.delete("nuts_to_grid");
		StatsIndex matrixI = new StatsIndex(matrix, "nuts", "grid"); matrix = null;
		//matrixI.print();

		//create out file
		File outFile = new File(out);
		if(outFile.exists()) outFile.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile, true));
		//write header
		bw.write("nuts,EBpop,fromGridPop,diff,error");
		bw.newLine();

		//go through list of estat SU
		for(String nutsId : matrixI.getKeys()){
			//get list of cells from matrix
			StatsIndex cells = matrixI.getSubIndex(nutsId);

			//compute population from cell population using matrix data
			double pop = 0;
			for(String cellId : cells.getKeys()){
				String gsPop = geostatPop.get(cellId);
				if(gsPop == null) continue;
				int cellPop = Integer.parseInt(gsPop);
				double weight = cells.getSingleValue(cellId);
				pop += cellPop*weight;
			}

			int ebPop = Integer.parseInt(estatPop.get(nutsId));
			double diff = ebPop - pop;
			double err = diff/ebPop;

			//save as csv: id-population-populationComputed-difference
			bw.write(nutsId+","+ebPop+","+pop+","+diff+","+err);
			bw.newLine();
		}
		bw.close();
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
			File newFile = new File(BASE_PATH + "BE_mobile_phone_proximus//mob/grid____.shp");
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




	public static void getPopulationGridFromMobilePhoneData() {

		//load intersection matrix data
		//load grid cell building statistics data

		//go through voronoi cells
		//get all grid cells intersecting (from intersection matrix)
		//compute value of cell (weighted part): naive approach (based on cell area) and with building stats + difference
		//export as csv

	}

	public static void main(String[] args) throws ShapefileException, MalformedURLException, IOException {
		System.out.println("Start");

		//computeGridAttribute(GEOSTAT_GRID_PATH);

		//StatisticalUnitsIntersectionMatrix.compute("nuts", NUTS_PATH, "NUTS_ID", "grid", GEOSTAT_GRID_PATH, "CELLCODE", matrix_nuts_grid);
		//StatisticalUnitsIntersectionMatrix.compute("phone", PROXIMUS_VORONOI, "voronoi_id", "grid", GEOSTAT_GRID_PATH, "CELLCODE", matrix_proximus_grid);

		//validateEurostatGeostat(BASE_PATH+"BE_mobile_phone_proximus/mob/validation_nuts_geostat.csv");

		//StatisticalUnitIntersectionWithGeoLayer.compute(GEOSTAT_GRID_PATH, "CELLCODE", BUILDINGS_SHP_PATH, stats_grid_building_intersection);
		//StatisticalUnitIntersectionWithGeoLayer.compute(PROXIMUS_VORONOI, "voronoi_id", BUILDINGS_SHP_PATH, voronoi_building_intersection);

		System.out.println("End");
	}

}
