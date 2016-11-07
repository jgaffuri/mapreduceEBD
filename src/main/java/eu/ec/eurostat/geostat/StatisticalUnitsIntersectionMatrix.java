package eu.ec.eurostat.geostat;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureIterator;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.FilterFactory2;

import com.vividsolutions.jts.geom.Geometry;

import eu.ec.eurostat.ShapeFile;

/**
 * @author julien Gaffuri
 *
 */
public class StatisticalUnitsIntersectionMatrix {

	/**
	 * Compute and store the intersection matrix between two statistical unit datasets of a same area of interest.
	 * This intersection matrix computes the share of a statistical unit which intersects another one of the other datasets.
	 * 
	 * NB: this operation commutes, with matrix transposition.
	 * 
	 * @param datasetName1 name of the first dataset (for labelling)
	 * @param shpFilePath1 path of the first dataset
	 * @param idField1 name of the first dataset id attribute
	 * @param datasetName2
	 * @param shpFilePath2
	 * @param idField2
	 * @param outFile The output file where to store the intersection matrix
	 * @throws ShapefileException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static void compute(String datasetName1, String shpFilePath1, String idField1, String datasetName2, String shpFilePath2, String idField2, String outFile) throws ShapefileException, MalformedURLException, IOException{
		//create out file
		File outFile_ = new File(outFile);
		if(outFile_.exists()) outFile_.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_, true));

		//write header
		bw.write(datasetName1+","+datasetName2+","+datasetName2+"_to_"+datasetName1+","+datasetName1+"_to_"+datasetName2+",intersectionArea");
		bw.newLine();

		//load shapefile 1
		FeatureIterator<SimpleFeature> itSu1 = new ShapeFile(shpFilePath1).dispose().getFeatures();

		//preload shapefile 2
		ShapeFile shpFile2 = new ShapeFile(shpFilePath2);
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

		//go through shapefile 1
		while (itSu1.hasNext()) {
			SimpleFeature f1 = itSu1.next();
			String id1 = f1.getAttribute(idField1).toString();

			System.out.println(datasetName1+" - "+id1);

			Geometry geom1 = (Geometry) f1.getDefaultGeometryProperty().getValue();
			double a1 = geom1.getArea();

			//get all su2 intersecting the su1 (with spatial index)
			FeatureIterator<SimpleFeature> itSu2 = shpFile2.getFeatures(f1.getBounds(), "the_geom", ff);

			while (itSu2.hasNext()) {
				SimpleFeature f2 = itSu2.next();
				Geometry geom2 = (Geometry) f2.getDefaultGeometryProperty().getValue();

				//check intersection
				if(!geom1.intersects(geom2)) continue;
				double interArea = geom1.intersection(geom2).getArea();
				if(interArea == 0) continue;

				//store relation data
				String id2 = f2.getAttribute(idField2).toString();
				String line = id1+","+id2+","+(interArea/geom2.getArea())+","+(interArea/a1)+","+interArea;
				System.out.println(datasetName2+" - "+id2+" - "+line);
				bw.write(line);
				bw.newLine();
			}
			itSu2.close();
		}
		itSu1.close();
		bw.close();

	}



	/*	public static void computeOld(String datasetName1, String shpFile1, String idField1, String datasetName2, String shpFile2, String idField2, String outFile) throws ShapefileException, MalformedURLException, IOException{

		//create out file
		File outFile_ = new File(outFile);
		if(outFile_.exists()) outFile_.delete();
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile_, true));

		//write header
		bw.write(datasetName1+","+datasetName2+","+datasetName2+"_to_"+datasetName1+","+datasetName1+"_to_"+datasetName2);
		bw.newLine();

		//load feature collections
		System.out.print("Loading...");
		Collection<SimpleFeature> fc1 = new ShapeFile(shpFile1).dispose().getFeatureCollectionF();
		Collection<SimpleFeature> fc2 = new ShapeFile(shpFile2).dispose().getFeatureCollectionF();
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
	 */

}
