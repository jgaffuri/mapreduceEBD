/**
 * 
 */
package eu.ec.estat.bd.proximus;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Various function on geotools features.
 * 
 * @author julien Gaffuri
 *
 */
public class FeatureUtil {

	public static SimpleFeatureCollection getFeatureCollection(String shpFilePath, Geometry geomIntersects, String geometryAttribute){
		//ECQL.toFilter("BBOX(THE_GEOM, 10,20,30,40)")
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
		Filter filter = ff.intersects(ff.property(geometryAttribute), ff.literal(geomIntersects));
		return getFeatureCollection(shpFilePath, filter);
	}
	public static SimpleFeatureCollection getFeatureCollection(String shpFilePath){ return getFeatureCollection(shpFilePath, Filter.INCLUDE); }
	public static SimpleFeatureCollection getFeatureCollection(String shpFilePath, Filter filter){
		try {
			File file = new File(shpFilePath);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("url", file.toURI().toURL());

			DataStore dataStore = DataStoreFinder.getDataStore(map);
			String typeName = dataStore.getTypeNames()[0];
			FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
			dataStore.dispose();
			return (SimpleFeatureCollection) source.getFeatures(filter);
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


}
