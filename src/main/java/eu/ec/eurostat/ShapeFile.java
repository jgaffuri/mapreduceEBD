/**
 * 
 */
package eu.ec.eurostat;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.DataUtilities;
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
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * Various functions to ease shapefiles manipulation
 * 
 * @author julien Gaffuri
 *
 */
public class ShapeFile {
	private DataStore dataStore;
	private SimpleFeatureStore featureStore;
	//SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(ft);

	/**
	 * Open a shapefile
	 * 
	 * @param path
	 */
	public ShapeFile(String path){ open(path); }

	/**
	 * Create a shapefile
	 * 
	 * @param ft
	 * @param folderPath
	 * @param fileName
	 */
	public ShapeFile(SimpleFeatureType ft, String folderPath, String fileName){
		try {
			new File(folderPath).mkdirs();
			File f = new File(folderPath+fileName);
			if(f.exists()) f.delete();
			HashMap<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", f.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);
			ShapefileDataStore sfds =  (ShapefileDataStore) new ShapefileDataStoreFactory().createNewDataStore( params );
			sfds.createSchema(ft);

			//open shapefile
			open(folderPath+fileName);
		} catch (Exception e) { e.printStackTrace(); }
	}

	/**
	 * Create a shapefile
	 * 
	 * @param geomType
	 * @param epsgCode
	 * @param attributes
	 * @param folderPath
	 * @param fileName
	 */
	public ShapeFile(String geomType, int epsgCode, String attributes, String folderPath, String fileName){
		this(getFeatureType(geomType, epsgCode, attributes), folderPath, fileName);
	}


	private void open(String path){
		try {
			HashMap<String, Object> params = new HashMap<String, Object>(); params.put("url", new File(path).toURI().toURL());
			dataStore = DataStoreFinder.getDataStore(params);
			featureStore = (SimpleFeatureStore) dataStore.getFeatureSource(dataStore.getTypeNames()[0]);
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

	//get basic info on shp file

	public SimpleFeatureType getSchema() { return featureStore.getSchema(); }
	public String[] getAttributeNames(){
		return getAttributeNames(getSchema());
	}
	public CoordinateReferenceSystem getCRS(String shpFilePath){
		return getSchema().getCoordinateReferenceSystem();
	}
	public Envelope getBounds(String shpFilePath) {
		return getSimpleFeatures().getBounds();
	}


	public FeatureIterator<SimpleFeature> getFeatures() { return getFeatures(Filter.INCLUDE); }
	public FeatureIterator<SimpleFeature> getFeatures(BoundingBox intersectionBB, String geometryAttribute, FilterFactory2 ff) {
		//Filter filter = ff.intersects(ff.property(geometryAttribute), ff.literal(StatUnitGeom));
		return getFeatures(ff.bbox(ff.property(geometryAttribute), intersectionBB));
	}
	public FeatureIterator<SimpleFeature> getFeatures(Filter filter) {
		try {
			return ((SimpleFeatureCollection) featureStore.getFeatures(filter)).features();
		} catch (IOException e) { e.printStackTrace(); }
		return null;
	}


	public SimpleFeatureCollection getSimpleFeatures(){ return getSimpleFeatures(null); }
	public SimpleFeatureCollection getSimpleFeatures(Filter f){
		try { return DataUtilities.collection(featureStore.getFeatures(f)); } catch (Exception e) { e.printStackTrace(); }
		return null;
	}



	public int count(){ return count(Filter.INCLUDE); }
	public int count(Filter filter){
		try {
			return featureStore.getCount(new Query( featureStore.getSchema().getTypeName(), filter ));
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
			return (SimpleFeatureCollection) featureStore.getFeatures(filter);
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	//"NATUR_CODE = 'BAT'"
	/*public ShapeFile filter(String cqlString, String outPath, String outFile){
		Filter f = null;
		try { f = CQL.toFilter(cqlString); } catch (CQLException e) { e.printStackTrace(); }
		SimpleFeatureCollection sfc = getFeatureCollection(f);
		return new ShapeFile(sfc.getSchema(), outPath, outFile).add(sfc);
	}
	public ShapeFile filter(Filter filter, String outPath, String outFile){
		SimpleFeatureCollection sfc = getFeatureCollection(filter);
		return new ShapeFile(sfc.getSchema(), outPath, outFile).add(sfc);
	}*/


	public ShapeFile add(SimpleFeature f) {
		DefaultFeatureCollection fs = new DefaultFeatureCollection(null, f.getFeatureType());
		//new ListFeatureCollection(f.getFeatureType(), sfc)
		fs.add(f);
		return add(fs);
	}
	public ShapeFile add(SimpleFeatureCollection fs) {
		try {
			Transaction tr = new DefaultTransaction("create");
			featureStore.setTransaction(tr);
			try {
				featureStore.addFeatures(fs);
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

	/*public static boolean add(String path, SimpleFeature f) {
		DefaultFeatureCollection fs = new DefaultFeatureCollection(null, f.getFeatureType());
		fs.add(f);
		return add(path, fs);
	}
	public static boolean add(String path, SimpleFeatureCollection fs) {
		ShapefileDumper dp = new ShapefileDumper(new File(path));
		System.out.println(path);
		System.out.println(new File(path).exists());
		//dp.setCharset(Charset.forName("ISO-8859-15"));
		//int maxSize = 100 * 1024 * 1024; dp.setMaxDbfSize(maxSize); dp.setMaxDbfSize(maxSize);
		try { return dp.dump(fs); } catch (IOException e) { e.printStackTrace(); }
		return false;
	}*/




	//schema manipulation

	public static SimpleFeatureType getFeatureType(String geomType) {
		return getFeatureType(geomType, -1);
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode) {
		return getFeatureType(geomType, epsgCode, new String[]{});
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode, Collection<String> data) {
		return getFeatureType(geomType, epsgCode, data.toArray(new String[data.size()]));
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode, String[] data) {
		String datast = "";
		if(data!=null) for(String data_ : data) datast += ","+data_;
		return getFeatureType(geomType, epsgCode, datast.substring(1, datast.length()));
	}
	public static SimpleFeatureType getFeatureType(String geomType, int epsgCode, String data) {
		try {
			String st = "";
			st = "the_geom:"+geomType;
			if(epsgCode>0) st += ":srid="+epsgCode;
			if(data!=null) st += ","+data;
			return DataUtilities.createType("ep", st);
			//String,Integer,Double,Boolean,Date
			//DataUtilities.createType( "my", "geom:Point,name:String,age:Integer,description:String" );
		} catch (SchemaException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String[] getAttributeNames(SimpleFeatureType sch){
		Collection<String> atts = new HashSet<String>();
		for(int i=0; i<sch.getAttributeCount(); i++){
			String att = sch.getDescriptor(i).getLocalName();
			if("the_geom".equals(att)) continue;
			if("GEOM".equals(att)) continue;
			atts.add(att);
		}
		return atts.toArray(new String[atts.size()]);
	}


	/*

	public static void saveGeomsSHP(Collection<Geometry> geoms, int epsgCode, String outPath, String outFile) {
		try {
			ArrayList<Feature> fs = new ArrayList<Feature>();
			for(Geometry geom : geoms){
				Feature f = new Feature();
				f.setGeom(geom);
				f.setProjCode(epsgCode);
				fs.add(f);
			}
			saveSHP(SimpleFeatureUtil.get(fs), outPath, outFile);
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static void saveGeomsSHP(Collection<Geometry> geoms, String outPath, String outFile) {
		saveGeomsSHP(geoms, -1, outPath, outFile);
	}


	//remove empty or null geometries from collection
	public static void removeNullOrEmpty(Collection<SimpleFeature> fs, String geomAtt) {
		ArrayList<SimpleFeature> toRemove = new ArrayList<SimpleFeature>();
		for(SimpleFeature f:fs){
			Geometry g = (Geometry)f.getAttribute(geomAtt);
			if(g==null || g.isEmpty())
				toRemove.add(f);
		}
		fs.removeAll(toRemove);
	}

	//clean geometries of a shapefile
	public static void cleanGeometries(String inFile, String geomAtt, String outPath, String outFile){
		System.out.println("Load data from "+inFile);
		SHPData data = loadSHP(inFile);

		System.out.print("clean all geometries...");
		for(Feature f : data.fs)
			f.setGeom( JTSGeomUtil.toMulti(JTSGeomUtil.clean( f.getGeom() )));
		System.out.println(" Done.");

		System.out.println("Save data to "+outFile);
		saveSHP(SimpleFeatureUtil.get(data.fs), outPath, outFile);
	}

	//save the union of a shapefile into another one
	public static void union(String inFile, String geomAtt, String outPath, String outFile){
		try {
			//load input shp
			SHPData data = loadSHP(inFile);

			//build union
			ArrayList<Geometry> geoms = new ArrayList<Geometry>();
			for( Feature f : data.fs )
				geoms.add(f.getGeom());
			Geometry union = JTSGeomUtil.unionPolygons(geoms);

			System.out.println(union.getGeometryType());

			//build feature
			SimpleFeatureBuilder fb = new SimpleFeatureBuilder(DataUtilities.createType("ep", "the_geom:"+union.getGeometryType()));
			fb.add(union);
			SimpleFeature sf = fb.buildFeature(null);

			//save shp
			DefaultFeatureCollection outfc = new DefaultFeatureCollection(null,null);
			outfc.add(sf);
			saveSHP(SimpleFeatureUtil.get(data.fs), outPath, outFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//get geometrical difference of a shapefile
	public static Geometry getDifferenceGeom(String inFile, String geomAtt, double margin) {
		//load input shp
		SHPData data = loadSHP(inFile);

		//get envelope
		Envelope e=data.env;
		e.expandBy(margin, margin);
		Geometry diff = JTSGeomUtil.getGeometry(e);
		e=null;

		//get poly list
		ArrayList<Geometry> polys = new ArrayList<Geometry>();
		for( Feature f:data.fs )
			polys.add(f.getGeom());

		//get union
		Geometry union = JTSGeomUtil.unionPolygons(polys);
		polys=null;

		//compute difference
		diff = diff.difference(union);
		if(diff instanceof Polygon || diff instanceof MultiPolygon) return diff;
		union=null;
		return diff.buffer(0);
	}


	//get geometrical difference of a shapefile
	public static SimpleFeature getDifferenceFeature(String inFile, String geomAtt, double margin) {
		try {
			//build difference geometry
			Geometry comp = getDifferenceGeom(inFile, geomAtt, margin);

			//get feature type
			Map<String,URL> map = new HashMap<String,URL>();
			map.put("url", new File(inFile).toURI().toURL());
			DataStore ds = DataStoreFinder.getDataStore(map);
			String typeName = ds.getTypeNames()[0];
			SimpleFeatureType ft = ds.getFeatureSource(typeName).getFeatures().getSchema();

			//build feature
			SimpleFeatureBuilder fb;
			fb = new SimpleFeatureBuilder(ft);
			fb.add(comp);
			return fb.buildFeature(null);
		} catch (Exception e) { e.printStackTrace(); }
		return null;
	}

	public static void addDifference(String inFile, String geomAtt, double margin) {
		add(getDifferenceFeature(inFile,geomAtt,margin), inFile);
	}

	 */

	//TODO
	//add/remove column
	//See http://www.programcreek.com/java-api-examples/index.php?api=org.geotools.data.shapefile.dbf.DbaseFileHeader
	//compute column


	/*public static void main(String[] args) throws Exception {
		System.out.println("Start");

		//FilterFactory ff = CommonFactoryFinder.getFilterFactory();
		//Filter f = ff.propertyLessThan( ff.property( "AGE"), ff.literal( 12 ) );
		//Filter f = CQL.toFilter( "NATUR_CODE = 'BAT'" );
		//new ShapeFile("H:/geodata/merge.shp").filter(f, "H:/geodata/", "merge_BAT.shp");

		System.out.println("end");
	}*/

}
