/**
 * 
 */
package eu.ec.eurostat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Various functions to ease shapefiles manipulation
 * 
 * @author julien Gaffuri
 *
 */
public class ShapeFile {
	private DataStore dataStore;
	private SimpleFeatureStore featureSource;
	//SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);

	/**
	 * Build and open a shapefile
	 * 
	 * @param path
	 */
	public ShapeFile(String path){ open(path); }

	public ShapeFile(SimpleFeatureType ft, String folderPath, String fileName){
		try {
			//create shapefile
			new File(folderPath).mkdirs();
			File f = new File(folderPath+fileName);
			if(f.exists()) f.delete();
			HashMap<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", f.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);
			ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
			ds.createSchema(ft);

			//open shapefile
			open(folderPath+fileName);
		} catch (Exception e) { e.printStackTrace(); }
	}

	public ShapeFile(String[] ft, String folderPath, String fileName){
		//TODO - use feature type construction methods
	}


	private void open(String path){
		try {
			HashMap<String, Object> params = new HashMap<String, Object>(); params.put("url", new File(path).toURI().toURL());
			dataStore = DataStoreFinder.getDataStore(params);
			featureSource = (SimpleFeatureStore) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
		} catch (Exception e) { e.printStackTrace(); }
	}


	/**
	 * Dispose the datastore.
	 * 
	 * @return
	 */
	public ShapeFile dispose(){
		dataStore.dispose();
		return this;
	}

	public SimpleFeatureType getFeatureType() { return featureSource.getSchema(); }

	public FeatureIterator<SimpleFeature> getFeatures() { return getFeatures(Filter.INCLUDE); }
	public FeatureIterator<SimpleFeature> getFeatures(BoundingBox intersectionBB, String geometryAttribute, FilterFactory2 ff) {
		//Filter filter = ff.intersects(ff.property(geometryAttribute), ff.literal(StatUnitGeom));
		return getFeatures(ff.bbox(ff.property(geometryAttribute), intersectionBB));
	}
	public FeatureIterator<SimpleFeature> getFeatures(Filter filter) {
		try {
			return ((SimpleFeatureCollection) featureSource.getFeatures(filter)).features();
		} catch (IOException e) { e.printStackTrace(); }
		return null;
	}

	public int count(){ return count(Filter.INCLUDE); }
	public int count(Filter filter){
		try {
			return featureSource.getCount(new Query( featureSource.getSchema().getTypeName(), filter ));
		} catch (IOException e) { e.printStackTrace(); }
		return -1;
	}

	public SimpleFeatureCollection getFeatureCollection(Geometry geomIntersects, String geometryAttribute){
		//ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
		Filter filter = ff.intersects(ff.property(geometryAttribute), ff.literal(geomIntersects));
		return getFeatureCollection(filter);
	}
	public SimpleFeatureCollection getFeatureCollection(){ return getFeatureCollection(Filter.INCLUDE); }
	public SimpleFeatureCollection getFeatureCollection(Filter filter){
		try {
			return (SimpleFeatureCollection) featureSource.getFeatures(filter);
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public ShapeFile filter(Filter filter, String outPath, String outFile){
		SimpleFeatureCollection sfc = getFeatureCollection(filter);
		return new ShapeFile(sfc.getSchema(), outPath, outFile).add(sfc);
	}

	public ShapeFile add(SimpleFeature f) {
		DefaultFeatureCollection fs = new DefaultFeatureCollection(null, f.getFeatureType());
		fs.add(f);
		return add(fs);
	}
	public ShapeFile add(SimpleFeatureCollection fs) {
		try {
			Transaction tr = new DefaultTransaction("create");
			featureSource.setTransaction(tr);
			try {
				featureSource.addFeatures(fs);
				tr.commit();
			} catch (Exception problem) {
				problem.printStackTrace();
				tr.rollback();
			} finally {
				tr.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}

	private void doBlabla(String... outputFile){
		//TODO
		//create output file
		//create list of ids
		//go through files
		//go through features
		//if feature attribute is in list of ids, continue
		//add feature id to list
		//add feature to save buffer (feature collection or collection?)
		//save buffer regularly
	}

	public static void main(String[] args) throws Exception {
		System.out.println("Start");

		//FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		//Filter f = ff.propertyLessThan( ff.property( "AGE"), ff.literal( 12 ) );
		Filter f = CQL.toFilter( "NATUR_CODE = 'BAT'" );
		new ShapeFile("H:/geodata/merge.shp").filter(f, "H:/geodata/", "merge_BAT.shp");

		System.out.println("end");
	}

}
