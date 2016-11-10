/**
 * 
 */
package eu.ec.eurostat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Various functions to manipulate shapefiles efficiently
 * 
 * @author julien Gaffuri
 *
 */
public class ShapeFile {
	private String path;
	private DataStore dataStore;
	private FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;

	/**
	 * A shapefile
	 * 
	 * @param path
	 */
	public ShapeFile(String path){
		try {
			this.path = path;
			Map<String, Object> mapStat = new HashMap<String, Object>(); mapStat.put("url", new File(this.path).toURI().toURL());
			dataStore = DataStoreFinder.getDataStore(mapStat);
			featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
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

	public Collection<SimpleFeature> getFeatureCollectionF(){ return getFeatureCollectionF(Filter.INCLUDE); }
	public Collection<SimpleFeature> getFeatureCollectionF(Filter filter){
		Collection<SimpleFeature> col = new HashSet<SimpleFeature>();
		FeatureIterator<SimpleFeature> it = getFeatureCollection(filter).features();
		while (it.hasNext()) col.add(it.next());
		it.close();
		return col;
	}


	
	
	public static void saveSHP(SimpleFeatureCollection sfs, String outPath, String outFile) {
		try {
			new File(outPath).mkdirs();
			ShapefileDataStoreFactory dsf = new ShapefileDataStoreFactory();
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", new File(outPath+outFile).toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);
			ShapefileDataStore ds = (ShapefileDataStore) dsf.createNewDataStore(params);
			ds.createSchema(sfs.getSchema());

			Transaction tr = new DefaultTransaction("create");
			String tn = ds.getTypeNames()[0];
			SimpleFeatureSource fs_ = ds.getFeatureSource(tn);

			if (fs_ instanceof SimpleFeatureStore) {
				SimpleFeatureStore fst = (SimpleFeatureStore) fs_;

				fst.setTransaction(tr);
				try {
					fst.addFeatures(sfs);
					tr.commit();
				} catch (Exception problem) {
					problem.printStackTrace();
					tr.rollback();
				} finally {
					tr.close();
				}
			} else {
				System.out.println(tn + " does not support read/write access");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public interface SelectionCriteria { boolean keep(Object o); }
	public void filter(SelectionCriteria selCri){
		//TODO
		//return (val.equals(((SimpleFeature)f).getAttribute(att)));
	}
	

	

	public static ShapeFile union(String outPath, ShapeFile... shapefiles){ return union(outPath, Filter.INCLUDE, shapefiles); }
	public static ShapeFile union(String outPath, Filter filter, ShapeFile... shapefiles){
		try {
			if(shapefiles.length == 0) return null; //TODO create empty shapefile?

			File file = new File(outPath);
			if(file.exists()) file.delete();

			DataStore dataStore = new ShapefileDataStoreFactory().createNewDataStore(Collections.singletonMap( "url", (Serializable) file.toURI().toURL() ));
			dataStore.createSchema(shapefiles[0].getFeatureType());

			//TODO
			
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

}
