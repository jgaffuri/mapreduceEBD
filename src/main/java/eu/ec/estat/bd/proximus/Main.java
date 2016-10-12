/**
 * 
 */
package eu.ec.estat.bd.proximus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.geotools.data.shapefile.files.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

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
	 * @param shp2
	 * @param out
	 * @throws ShapefileException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void computeStatUnitDatasetsIntersectionMatrix(String shp1, String idField1, String shp2, String idField2, String out) throws ShapefileException, MalformedURLException, IOException{
		ShapefileReader r1 = new ShapefileReader(new ShpFiles(new File(shp1)), true, true, new GeometryFactory());
		ShapefileReader r2 = new ShapefileReader(new ShpFiles(new File(shp2)), true, true, new GeometryFactory());

		//create out file
		File outFile = new File(out);
		if(outFile.exists()) outFile.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile, true));


		/*
		 * TODO
		 *  File file = new File("example.shp");
    Map<String, Object> map = new HashMap<String, Object>();
    map.put("url", file.toURI().toURL());

    DataStore dataStore = DataStoreFinder.getDataStore(map);
    String typeName = dataStore.getTypeNames()[0];

    FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore
            .getFeatureSource(typeName);
    Filter filter = Filter.INCLUDE; // ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")

    FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures(filter);
    try (FeatureIterator<SimpleFeature> features = collection.features()) {
        while (features.hasNext()) {
            SimpleFeature feature = features.next();
            System.out.print(feature.getID());
            System.out.print(": ");
            System.out.println(feature.getDefaultGeometryProperty().getValue());
        }
    }
		 * 
		 */

		while (r1.hasNext()) {
			Record obj1 = r1.nextRecord();
			Geometry geom1 = (Geometry) obj1.shape();
			double a1 = geom1.getArea();
			String id1 = "id1"; //TODO get id1
			while (r2.hasNext()) {
				Record obj2 = r2.nextRecord();
				Geometry geom2 = (Geometry) obj2.shape();
				String id2 = "id2"; //TODO get id2

				//check intersection
				if(!geom1.intersects(geom2)) continue;
				double area = geom1.intersection(geom2).getArea();
				if(area == 0) continue;

				//compute area ratios
				double ratio1 = area/a1, ratio2 = area/geom2.getArea();

				//store relation data
				bw.write(id1+","+id2+","+ratio1+","+ratio2);
				bw.newLine();
			}
		}
		r1.close();
		r2.close();
		bw.close();

	}


	//compute Eurostat population dataset from geostat grid and compare with published one
	//TODO: get pop data at LAU level from fabio
	public static void validateEurostatGeostat() throws ShapefileException, MalformedURLException, IOException{
		//test consistency between estat NUTS3/LAU data and geostat grid

		//load grid cells data: id-population. only csv?

		//load intersection matrix

		//go through list of estat SU
		//get list of cells from matrix
		//compute population from cell population using matrix data
		//save as csv
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

		//TODO check projection are the same
		computeStatUnitDatasetsIntersectionMatrix(NUTS_PATH, "NUTS_ID", GEOSTAT_GRID_PATH, "CELLCODE", BASE_PATH+"BE_mobile_phone_proximus/comp/matrix_nuts_grid.csv");
		//computeStatUnitDatasetsIntersectionMatrix(PROXIMUS_VORONOI, GEOSTAT_GRID_PATH, BASE_PATH+"BE_mobile_phone_proximus/comp/matrix_proximus_grid.csv");

		//validateEurostatGeostat();
		//getBuildingStatByGridCell();
		//getPopulationGridFromMobilePhoneData();

		System.out.println("End");
	}

}
