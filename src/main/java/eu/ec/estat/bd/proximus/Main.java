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

/**
 * @author julien Gaffuri
 *
 */
public class Main {
	public static String BASE_PATH = "A:/geodata/";
	public static String ESTAT_POP_PATH = BASE_PATH + "eurobase/BE_pop_nuts3.csv";
	public static String NUTS_PATH = BASE_PATH + "BE_mobile_phone_proximus/comp/nuts3.shp";
	public static String GEOSTAT_GRID_PATH = BASE_PATH + "BE_mobile_phone_proximus/comp/grid.shp";
	public static String PROXIMUS_VORONOI = BASE_PATH + "BE_mobile_phone_proximus/heatmap_final_ETRS989.shp";

	/**
	 * @param shp1
	 * @param idField1
	 * @param shp2
	 * @param idField2
	 * @param out
	 * @throws ShapefileException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void computeStatUnitDatasetsIntersectionMatrix(String shp1, String idField1, String shp2, String idField2, String out) throws ShapefileException, MalformedURLException, IOException{
		//create out file
		File outFile = new File(out);
		if(outFile.exists()) outFile.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile, true));

		//get input feature collections
		FeatureCollection<SimpleFeatureType, SimpleFeature> fc1 = getFeatureCollection(shp1);

		FeatureIterator<SimpleFeature> it1 = fc1.features();
		while (it1.hasNext()) {
			//get input feature 1 data
			SimpleFeature f1 = it1.next();
			Geometry geom1 = (Geometry) f1.getDefaultGeometryProperty().getValue();
			double a1 = geom1.getArea();
			String id1 = f1.getAttribute(idField1).toString();
			System.out.println(id1);

			FeatureCollection<SimpleFeatureType, SimpleFeature> fc2 = getFeatureCollection(shp2, geom1.getEnvelope(), "the_geom");
			FeatureIterator<SimpleFeature> it2 = fc2.features();
			while (it2.hasNext()) {
				//get input feature 2 data
				SimpleFeature f2 = it2.next();
				Geometry geom2 = (Geometry) f2.getDefaultGeometryProperty().getValue();

				//check intersection
				if(!geom1.intersects(geom2)) continue;
				double interArea = geom1.intersection(geom2).getArea();
				if(interArea == 0) continue;

				//compute area ratios
				double ratio1 = interArea/a1, ratio2 = interArea/geom2.getArea();

				//store relation data
				bw.write(id1+","+f2.getAttribute(idField2).toString()+","+ratio1+","+ratio2);
				bw.newLine();
			}
			it2.close();
		}
		it1.close();
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
			return source.getFeatures(filter);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	//compute Eurostat population dataset from geostat grid and compare with published one
	//TODO: get pop data at LAU level from fabio
	public static void validateEurostatGeostat() throws ShapefileException, MalformedURLException, IOException{
		//test consistency between estat NUTS3/LAU data and geostat grid

		//load grid cells data: id-population. only csv?
		//load estat population data: id-population
		//load intersection matrix

		//go through list of estat SU
		//get list of cells from matrix
		//compute population from cell population using matrix data
		//save as csv: id-population-populationComputed-difference
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

		//computeStatUnitDatasetsIntersectionMatrix(NUTS_PATH, "NUTS_ID", GEOSTAT_GRID_PATH, "CELLCODE", BASE_PATH+"BE_mobile_phone_proximus/comp/matrix_nuts_grid.csv");
		//computeStatUnitDatasetsIntersectionMatrix(PROXIMUS_VORONOI, "voronoi_id", GEOSTAT_GRID_PATH, "CELLCODE", BASE_PATH+"BE_mobile_phone_proximus/comp/matrix_proximus_grid.csv");

		validateEurostatGeostat();
		//getBuildingStatByGridCell();
		//getPopulationGridFromMobilePhoneData();

		System.out.println("End");
	}

}
