/**
 * 
 */
package eu.ec.eurostat.geostat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;

import eu.ec.estat.java4eurostat.io.DicUtil;
import eu.ec.eurostat.ShapeFile;

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
			bw.write("id,area");
			//bw.write("id,number,area,length"/*+",area_density,length_density"*/);
			bw.newLine();

			//open statistical units and geo shapefiles
			ShapeFile statShp = new ShapeFile(statUnitsSHPFile).dispose();
			ShapeFile geoShp = new ShapeFile(statUnitsSHPFile);
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

			//go through statistical units
			FeatureIterator<SimpleFeature> itStat = statShp.getFeatures();
			while (itStat.hasNext()) {
				SimpleFeature statUnit = itStat.next();
				String statUnitId = statUnit.getAttribute(statUnitIdField).toString();
				System.out.println(statUnitId);

				//get all geo intersecting the stat unit (with spatial index)
				Geometry StatUnitGeom = (Geometry) statUnit.getDefaultGeometryProperty().getValue();
				FeatureIterator<SimpleFeature> itGeo = geoShp.getFeatures(statUnit.getBounds(), "the_geom", ff);

				//compute stat on geo: total area/volume, number, building size distribution
				int nbGeo=0; double totalArea=0 /*, totalLength=0*/;
				while (itGeo.hasNext()) {
					SimpleFeature geo = itGeo.next();

					//TODO add additional filter here?

					Geometry geoGeom = (Geometry) geo.getDefaultGeometryProperty().getValue();
					if(!geoGeom.intersects(StatUnitGeom)) continue;

					Geometry inter = geoGeom.intersection(StatUnitGeom);

					nbGeo++;
					totalArea += inter.getArea();
					//totalLength += inter.getLength();
				}
				itGeo.close();

				if(nbGeo == 0) continue;

				//store
				String line = statUnitId+","+totalArea;
				//String line = statUnitId+","+nbGeo+","+totalArea+","+totalLength/*+","+totalArea/StatUnitGeom.getArea()+","+totalLength/StatUnitGeom.getArea()*/;
				System.out.println(line);
				bw.write(line);
				bw.newLine();
			}
			itStat.close();
			bw.close();

		} catch (MalformedURLException e) { e.printStackTrace();
		} catch (IOException e) { e.printStackTrace(); }
	}


	public static void computeGeoStatValueFromStatUnitValue(String geoSHPFile, String geoIdField, String statUnitsSHPFile, String statUnitsIdField, String statUnitValuesPath, String statUnitGeoTotalAreaPath, String geoOutFile) {
		try {
			//create out file
			File outFile_ = new File(geoOutFile);
			if(outFile_.exists()) outFile_.delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_, true));

			//write header
			bw.write(geoIdField+",value,density,_nbStatUnitIntersecting");
			bw.newLine();

			//open geo and statistical units shapefiles
			ShapeFile geoShp = new ShapeFile(geoSHPFile).dispose();
			int nbGeo = geoShp.count();
			ShapeFile statShp = new ShapeFile(statUnitsSHPFile);
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

			//load stat unit population data
			HashMap<String, String> statUnitValue = DicUtil.load(statUnitValuesPath, ",");

			//get geo total area by SU
			HashMap<String, String> statUnitGeoTotalArea = DicUtil.load(statUnitGeoTotalAreaPath, ",");

			//go through geo - purpose is to compute geo pop/density
			FeatureIterator<SimpleFeature> itGeo = geoShp.getFeatures();
			int geoCounter = 0;
			while (itGeo.hasNext()) {
				SimpleFeature geoUnit = itGeo.next();
				String geoId = geoUnit.getAttribute(geoIdField).toString();
				System.out.println(geoId + " " + (geoCounter++) + "/" + nbGeo + " " + (Math.round(10000*geoCounter/nbGeo))*0.01 + "%");

				Geometry geoGeom = (Geometry) geoUnit.getDefaultGeometryProperty().getValue();

				//get all stat units intersecting the geo (with spatial index)
				FeatureIterator<SimpleFeature> itStat = statShp.getFeatures(geoUnit.getBounds(), "the_geom", ff);

				int nbStat = 0;
				double geoStatValue = 0;
				//geoStatValue = Sum on SUs intersecting of:  surf(geo inter su)/statUnitGeoTotalArea * statUnitValue
				while (itStat.hasNext()) {
					SimpleFeature stat = itStat.next();
					String statId = stat.getAttribute(statUnitsIdField).toString();

					//get stat unit geometry
					Geometry statUnitGeom = (Geometry) stat.getDefaultGeometryProperty().getValue();
					if(!geoGeom.intersects(statUnitGeom)) continue;

					//get stat unit value
					String statValue = statUnitValue.get(statId);
					if(statValue == null || Double.parseDouble(statValue) == 0) continue;

					//get stat unit geo total area
					String statGeoTot = statUnitGeoTotalArea.get(statId);
					if(statGeoTot == null || Double.parseDouble(statGeoTot) == 0) continue;

					nbStat++;
					geoStatValue += geoGeom.intersection(statUnitGeom).getArea() / Double.parseDouble(statGeoTot) * Double.parseDouble(statValue);
				}
				itStat.close();

				if(nbStat == 0) continue;

				//store
				String line = geoId+","+geoStatValue+","+geoStatValue/geoGeom.getArea()+","+nbStat;
				System.out.println(line);
				bw.write(line);
				bw.newLine();
			}
			itGeo.close();
			bw.close();
		} catch (Exception e) { e.printStackTrace(); }
	}



	public static void aggregateStatValueFomGeoValues(String statUnitsSHPFile, String statUnitsIdField, String geoSHPFile, String geoIdField, String geoValuesPath, String statUnitOutFile) {
		try {
			//create out file
			File outFile_ = new File(statUnitOutFile);
			if(outFile_.exists()) outFile_.delete();
			BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_, true));

			//write header
			bw.write(statUnitsIdField+",value,density,nbGeosIntersecting");
			bw.newLine();

			//open statistical units and geo shapefiles
			ShapeFile statShp = new ShapeFile(statUnitsSHPFile).dispose();
			int nbStats = statShp.count();
			ShapeFile geoShp = new ShapeFile(geoSHPFile);
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

			//load geo values
			HashMap<String, String> geoValues = DicUtil.load(geoValuesPath, ",");

			//go through stat units - purpose is to compute value from geos intersecting
			FeatureIterator<SimpleFeature> itStat = statShp.getFeatures();
			int statCounter = 0;
			while (itStat.hasNext()) {
				SimpleFeature statUnit = itStat.next();
				String statId = statUnit.getAttribute(statUnitsIdField).toString();
				System.out.println(statId + " " + (statCounter++) + "/" + nbStats + " " + (Math.round(10000*statCounter/nbStats))*0.01 + "%");

				Geometry statGeom = (Geometry) statUnit.getDefaultGeometryProperty().getValue();

				//get all geos intersecting the stat unit (with spatial index)
				FeatureIterator<SimpleFeature> itGeo = geoShp.getFeatures(statUnit.getBounds(), "the_geom", ff);

				int nbGeos = 0;
				double statValue = 0;
				//statValue = Sum on geos intersecting of:  surf(geo inter su)/surf(geo) * geoValue
				while (itGeo.hasNext()) {
					SimpleFeature geo = itGeo.next();
					String geoId = geo.getAttribute(geoIdField).toString();

					//get geo geometry
					Geometry geoGeom = (Geometry) geo.getDefaultGeometryProperty().getValue();
					if(!geoGeom.intersects(statGeom)) continue;

					//get geo value
					String geoValue = geoValues.get(geoId);
					if(geoValue == null || Double.parseDouble(geoValue) == 0) continue;

					nbGeos++;
					statValue += geoGeom.intersection(statGeom).getArea() / geoGeom.getArea() * Double.parseDouble(geoValue);
				}
				itGeo.close();

				if(nbGeos == 0) continue;

				//store
				String line = statId+","+statValue+","+statValue/statGeom.getArea()+","+nbGeos;
				System.out.println(line);
				bw.write(line);
				bw.newLine();
			}
			itStat.close();
			bw.close();
		} catch (Exception e) { e.printStackTrace(); }
	}

}
