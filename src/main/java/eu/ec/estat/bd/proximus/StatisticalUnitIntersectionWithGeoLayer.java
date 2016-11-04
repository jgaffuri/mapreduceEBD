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
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.factory.CommonFactoryFinder;
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
public class StatisticalUnitIntersectionWithGeoLayer {


	/**
	 * Compute intersection between statistical units layer and geographic layer
	 * 
	 * @param statUnitsSHPFile
	 * @param statUnitIdField
	 * @param geoSHPFile
	 */
	public static void compute(String statUnitsSHPFile, String statUnitIdField, String geoSHPFile, String statUnitOutFile) {
		try {
			//create out file
			File outFile_ = new File(statUnitOutFile);
			if(outFile_.exists()) outFile_.delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_, true));

			//write header
			bw.write("id,number,area,length"/*+",area_density,length_density"*/);
			bw.newLine();

			//open statistical units dataset
			Map<String, Object> mapStat = new HashMap<String, Object>(); mapStat.put("url", new File(statUnitsSHPFile).toURI().toURL());
			DataStore dataStoreStat = DataStoreFinder.getDataStore(mapStat);
			FeatureSource<SimpleFeatureType, SimpleFeature> sourceStat = dataStoreStat.getFeatureSource(dataStoreStat.getTypeNames()[0]);
			dataStoreStat.dispose();

			//open geo dataset
			Map<String, Object> mapGeo = new HashMap<String, Object>(); mapGeo.put("url", new File(geoSHPFile).toURI().toURL());
			DataStore dataStoreGeo = DataStoreFinder.getDataStore(mapGeo);
			FeatureSource<SimpleFeatureType, SimpleFeature> sourceGeo = dataStoreGeo.getFeatureSource(dataStoreGeo.getTypeNames()[0]);
			//dataStoreGeo.dispose();
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

			//go through statistical units
			FeatureIterator<SimpleFeature> itStat = ((SimpleFeatureCollection) sourceStat.getFeatures(Filter.INCLUDE)).features();
			while (itStat.hasNext()) {
				SimpleFeature statUnit = itStat.next();
				String statUnitId = statUnit.getAttribute(statUnitIdField).toString();
				System.out.println(statUnitId);

				//get all geo intersecting the stat unit (with spatial index)
				Geometry StatUnitGeom = (Geometry) statUnit.getDefaultGeometryProperty().getValue();
				//Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(StatUnitGeom));
				Filter filter = ff.bbox(ff.property("the_geom"), statUnit.getBounds());
				FeatureIterator<SimpleFeature> itGeo = ((SimpleFeatureCollection) sourceGeo.getFeatures(filter)).features();

				//compute stat on geo: total area/volume, number, building size distribution
				int nbGeo=0; double totalArea=0, totalLength=0;
				while (itGeo.hasNext()) {
					SimpleFeature geo = itGeo.next();

					//TODO add additional filter here?

					Geometry geoGeom = (Geometry) geo.getDefaultGeometryProperty().getValue();
					if(!geoGeom.intersects(StatUnitGeom)) continue;

					Geometry inter = geoGeom.intersection(StatUnitGeom);

					nbGeo++;
					totalArea += inter.getArea();
					totalLength += inter.getLength();
				}
				itGeo.close();

				if(nbGeo == 0) continue;

				//store
				String line = statUnitId+","+nbGeo+","+totalArea+","+totalLength/*+","+totalArea/StatUnitGeom.getArea()+","+totalLength/StatUnitGeom.getArea()*/;
				System.out.println(line);
				bw.write(line);
				bw.newLine();
			}
			itStat.close();
			bw.close();

		} catch (MalformedURLException e) { e.printStackTrace();
		} catch (IOException e) { e.printStackTrace(); }
	}

	public static void computeGeoDensity(String geoSHPFile, String geoIdField, String statUnitsSHPFile, String statUnitsIdField, String statUnitsPopFilePath, String geoOutFile) {
		try {

			//create out file
			File outFile_ = new File(geoOutFile);
			if(outFile_.exists()) outFile_.delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_, true));

			//write header
			bw.write("id,population,density,_nbStatUnitIntersecting");
			bw.newLine();

			//open geo dataset
			Map<String, Object> mapGeo = new HashMap<String, Object>(); mapGeo.put("url", new File(geoSHPFile).toURI().toURL());
			DataStore dataStoreGeo = DataStoreFinder.getDataStore(mapGeo);
			FeatureSource<SimpleFeatureType, SimpleFeature> sourceGeo = dataStoreGeo.getFeatureSource(dataStoreGeo.getTypeNames()[0]);
			dataStoreGeo.dispose();

			//open statistical units dataset
			Map<String, Object> mapStat = new HashMap<String, Object>(); mapStat.put("url", new File(statUnitsSHPFile).toURI().toURL());
			DataStore dataStoreStat = DataStoreFinder.getDataStore(mapStat);
			FeatureSource<SimpleFeatureType, SimpleFeature> sourceStat = dataStoreStat.getFeatureSource(dataStoreStat.getTypeNames()[0]);
			//dataStoreStat.dispose();
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

			//go through geo - purpose is to compute geo pop/density
			FeatureIterator<SimpleFeature> itGeo = ((SimpleFeatureCollection) sourceGeo.getFeatures(Filter.INCLUDE)).features();
			while (itGeo.hasNext()) {
				SimpleFeature geoUnit = itGeo.next();
				String geoId = geoUnit.getAttribute(geoIdField).toString();
				System.out.println(geoId);

				//get all stat units intersecting the geo (with spatial index)
				Geometry geoGeom = (Geometry) geoUnit.getDefaultGeometryProperty().getValue();
				//Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(StatUnitGeom));
				Filter filter = ff.bbox(ff.property("the_geom"), geoUnit.getBounds());
				FeatureIterator<SimpleFeature> itStat = ((SimpleFeatureCollection) sourceStat.getFeatures(filter)).features();

				//TODO 1
				//get pop of all SUs intersecting (popSU) + geo quantity of SU (geoQSU)

				int nbStat = 0;
				double geoPop = 0;
				while (itStat.hasNext()) {
					SimpleFeature stat = itStat.next();

					Geometry StatUnitGeom = (Geometry) stat.getDefaultGeometryProperty().getValue();
					if(!geoGeom.intersects(StatUnitGeom)) continue;
					Geometry inter = geoGeom.intersection(StatUnitGeom);

					nbStat++;
					//TODO 2
					//geoPop = surf(geo)/geoQSU * popSU (case of single intersection)
					//geoPop = Sum on SUs interecting of:  surf(geo inter su)/geoQSU * popSU  (general case)
				}
				itStat.close();

				if(nbStat == 0) continue;

				//store
				String line = geoId+","+geoPop+","+geoPop/geoGeom.getArea()+","+nbStat;
				System.out.println(line);
				bw.write(line);
				bw.newLine();
			}
			itGeo.close();
			bw.close();
		} catch (Exception e) { e.printStackTrace(); }
	}

}
