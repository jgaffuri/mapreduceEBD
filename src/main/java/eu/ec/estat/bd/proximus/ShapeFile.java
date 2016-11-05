/**
 * 
 */
package eu.ec.estat.bd.proximus;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.simple.SimpleFeatureCollection;
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

	public ShapeFile(String path){
		try {
			this.path = path;
			Map<String, Object> mapStat = new HashMap<String, Object>(); mapStat.put("url", new File(this.path).toURI().toURL());
			dataStore = DataStoreFinder.getDataStore(mapStat);
			featureSource = dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
		} catch (Exception e) { e.printStackTrace(); }
	}
	public ShapeFile dispose(){
		dataStore.dispose();
		return this;
	}

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

	public static ShapeFile union(ShapeFile... shapefiles){
		//TODO
		return null;
	}

}
