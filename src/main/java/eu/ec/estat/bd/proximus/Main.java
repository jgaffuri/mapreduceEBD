/**
 * 
 */
package eu.ec.estat.bd.proximus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;

import eu.ec.estat.java4eurostat.base.StatsHypercube;
import eu.ec.estat.java4eurostat.base.StatsIndex;
import eu.ec.estat.java4eurostat.io.CSV;
import eu.ec.estat.java4eurostat.io.DicUtil;

/**
 * @author julien Gaffuri
 *
 */
public class Main {
	public static String BASE_PATH = "A:/geodata/";
	public static String ESTAT_POP_PATH = BASE_PATH + "eurobase/BE_pop_nuts3.csv";
	public static String GEOSTAT_POP_PATH = BASE_PATH + "BE_mobile_phone_proximus/comp/grid_pop_2011.csv";
	public static String NUTS_PATH = BASE_PATH + "BE_mobile_phone_proximus/comp/nuts3.shp";
	public static String GEOSTAT_GRID_PATH = BASE_PATH + "BE_mobile_phone_proximus/comp/grid.shp";
	public static String PROXIMUS_VORONOI = BASE_PATH + "BE_mobile_phone_proximus/heatmap_final_ETRS989.shp";

	public static String matrix_nuts_grid = BASE_PATH+"BE_mobile_phone_proximus/comp/matrix_nuts_grid.csv";
	public static String matrix_proximus_grid = BASE_PATH+"BE_mobile_phone_proximus/comp/matrix_proximus_grid.csv";

	/**
	 * @param name1
	 * @param shp1
	 * @param idField1
	 * @param name2
	 * @param shp2
	 * @param idField2
	 * @param out
	 * @throws ShapefileException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void computeStatUnitDatasetsIntersectionMatrix(String name1, String shp1, String idField1, String name2, String shp2, String idField2, String out) throws ShapefileException, MalformedURLException, IOException{
		//create out file
		File outFile = new File(out);
		if(outFile.exists()) outFile.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile, true));
		//write header
		bw.write(name1+","+name2+","+name2+"_to_"+name1+","+name1+"_to_"+name2);
		bw.newLine();

		//load feature collections
		System.out.print("Loading...");
		Collection<SimpleFeature> fc1 = getFeatureCollectionF(shp1);
		Collection<SimpleFeature> fc2 = getFeatureCollectionF(shp2);
		System.out.println(" Done.");

		for(SimpleFeature f1 : fc1) {
			Geometry geom1 = (Geometry) f1.getDefaultGeometryProperty().getValue();
			double a1 = geom1.getArea();
			String id1 = f1.getAttribute(idField1).toString();
			System.out.println(id1);

			for(SimpleFeature f2 : fc2) {
				//get input feature 2 data
				Geometry geom2 = (Geometry) f2.getDefaultGeometryProperty().getValue();

				//check intersection
				if(!geom1.intersects(geom2)) continue;
				double interArea = geom1.intersection(geom2).getArea();
				if(interArea == 0) continue;

				//compute area ratios
				double ratio2 = interArea/a1, ratio1 = interArea/geom2.getArea();

				//store relation data
				bw.write(id1+","+f2.getAttribute(idField2).toString()+","+ratio1+","+ratio2);
				bw.newLine();
			}
		}
		bw.close();

	}

	public static FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(String shpFilePath, Geometry intersects, String geometryAttribute){
		//ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
		Filter filter = ff.intersects(ff.property(geometryAttribute), ff.literal(intersects));
		return getFeatureCollection(shpFilePath, filter);
	}
	public static FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(String shpFilePath){ return getFeatureCollection(shpFilePath, Filter.INCLUDE); }
	public static FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(String shpFilePath, Filter filter){
		try {
			File file = new File(shpFilePath);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("url", file.toURI().toURL());

			DataStore dataStore = DataStoreFinder.getDataStore(map);
			String typeName = dataStore.getTypeNames()[0];
			FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
			dataStore.dispose();
			return source.getFeatures(filter);
		} catch (MalformedURLException e) { e.printStackTrace();
		} catch (IOException e) { e.printStackTrace(); }
		return null;
	}

	public static Collection<SimpleFeature> getFeatureCollectionF(String shpFilePath){ return getFeatureCollectionF(shpFilePath, Filter.INCLUDE); }
	public static Collection<SimpleFeature> getFeatureCollectionF(String shpFilePath, Filter filter){
		Collection<SimpleFeature> col = new HashSet<SimpleFeature>();
		FeatureIterator<SimpleFeature> it = getFeatureCollection(shpFilePath, filter).features();
		while (it.hasNext()) col.add(it.next());
		it.close();
		return col;
	}


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

	public static void getBuildingStatByGridCell() {
		//intersection between geostat grid and building layer

		//go through grid cells
		//get all buildings intersecting the grid (with spatial index)
		//compute stat on buildings: total area/volume, number, building size distribution
		//compute also building density from pop data. Asumption: should be +/- constant.
		//export as csv file
	}

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

		//computeStatUnitDatasetsIntersectionMatrix("nuts", NUTS_PATH, "NUTS_ID", "grid", GEOSTAT_GRID_PATH, "CELLCODE", matrix_nuts_grid);
		//computeStatUnitDatasetsIntersectionMatrix("phone", PROXIMUS_VORONOI, "voronoi_id", "grid", GEOSTAT_GRID_PATH, "CELLCODE", matrix_proximus_grid);

		validateEurostatGeostat(BASE_PATH+"BE_mobile_phone_proximus/comp/validation_nuts_geostat.csv");
		//getBuildingStatByGridCell();
		//getPopulationGridFromMobilePhoneData();

		System.out.println("End");
	}

}
