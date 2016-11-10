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
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
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
			HashMap<String, Object> mapStat = new HashMap<String, Object>(); mapStat.put("url", new File(this.path).toURI().toURL());
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

	/*public Collection<SimpleFeature> getFeatureCollectionF(){ return getFeatureCollectionF(Filter.INCLUDE); }
	public Collection<SimpleFeature> getFeatureCollectionF(Filter filter){
		Collection<SimpleFeature> col = new HashSet<SimpleFeature>();
		FeatureIterator<SimpleFeature> it = getFeatureCollection(filter).features();
		while (it.hasNext()) col.add(it.next());
		it.close();
		return col;
	}*/

	public void filter(Filter filter, String outPath, String outFile){
		SimpleFeatureCollection sfc = getFeatureCollection(filter);
		saveSHP(sfc, outPath, outFile);
	}


	public static void saveSHP(SimpleFeatureCollection sfs, String outPath, String outFile) {
		try {
			//create output file
			new File(outPath).mkdirs();
			File file = new File(outPath+outFile);
			if(file.exists()) file.delete();

			//create feature store
			HashMap<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", file.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);
			ShapefileDataStore ds = (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore(params);
			ds.createSchema(sfs.getSchema());
			String tn = ds.getTypeNames()[0];
			SimpleFeatureStore fst = (SimpleFeatureStore)ds.getFeatureSource(tn);

			//creation transaction
			Transaction tr = new DefaultTransaction("create");
			fst.setTransaction(tr);
			try {
				fst.addFeatures(sfs);
				tr.commit();
			} catch (Exception e) {
				e.printStackTrace();
				tr.rollback();
			} finally {
				tr.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/*/add feature to a shapefile
	public static void add(SimpleFeature f, String inFile) {
		try {
			Map<String,URL> map = new HashMap<String,URL>();
			map.put("url", new File(inFile).toURI().toURL());
			DataStore ds = DataStoreFinder.getDataStore(map);
			String typeName = ds.getTypeNames()[0];
			SimpleFeatureType ft = ds.getFeatureSource(typeName).getFeatures().getSchema();

			Transaction tr = new DefaultTransaction("create");
			String tn = ds.getTypeNames()[0];
			SimpleFeatureSource fs_ = ds.getFeatureSource(tn);

			if (fs_ instanceof SimpleFeatureStore) {
				SimpleFeatureStore fst = (SimpleFeatureStore) fs_;

				DefaultFeatureCollection objs = new DefaultFeatureCollection(null, ft);
				objs.add(f);

				fst.setTransaction(tr);
				try {
					fst.addFeatures(objs);
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
	}*/

	/*
	public static SimpleFeatureCollection get(SimpleFeature f) {
		SimpleFeatureType ft = f.getFeatureType();
		DefaultFeatureCollection sfc = new DefaultFeatureCollection(null, ft);
		//SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);
		return sfc;
	}*/

	private void findIntersections(String outputFile){
		//TODO
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
