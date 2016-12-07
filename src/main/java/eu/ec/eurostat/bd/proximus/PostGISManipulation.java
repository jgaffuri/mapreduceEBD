/**
 * 
 */
package eu.ec.eurostat.bd.proximus;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;

import org.postgresql.util.PSQLException;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import eu.ec.eurostat.bd.Config;
import eu.ec.eurostat.io.postgis.PGConnection;
import eu.ec.eurostat.io.postgis.PGUtil;

/**
 * @author gaffuju
 *
 */
public class PostGISManipulation {

	public static void main(String[] args) throws Exception {
		System.out.println("Start");

		PGConnection pgConn = new PGConnection(Config.pg_db, Config.pg_host, Config.pg_port, Config.pg_user, Config.pg_pw);
		Connection c = pgConn.getConnection();

		/*
		Collection<String> tables = PGUtil.getTableNames(c);
		for(String table : tables)
			System.out.println(table+" - "+PGUtil.getTableSize(c, table) + " rows"
					//+ " - SRID:" + PGUtil.getSRID(c, table, "geom")
					);*/

		//remove duplicates in wa
		//removeWallonyDuplicates(c);

		//add spatial index
		//PGUtil.createSpatialIndex(c, "bu_be_housing");
		//PGUtil.createSpatialIndex(c, "bu_be");
		//PGUtil.createIndex(c,"bu_be_housing","gid");

		//handle intersecting buildings
		//handleIntersectingBuildings(c, "bu_be_housing");
		//handleIntersectingBuildings(c, "bu_be");
		//DELETE FROM bu_be_housing WHERE gid NOT IN (select max(dup.gid) from bu_be_housing as dup group by geom);
		//DELETE FROM bu_be WHERE gid NOT IN (select max(dup.gid) from bu_be as dup group by geom);

		/*
		for(String table : tables){
			System.out.println(table);
			PGUtil.createSpatialIndex(c, "bu_be_wa_bw");
		}
		 */
		c.close();

		System.out.println("End");
	}

	public static void handleIntersectingBuildings(Connection c, String tableName){
		//9479080
		//housing: 4576608
		/*/should be the same
		System.out.println(PGUtil.getValues(c, tableName, "gid", false).size());
		System.out.println(PGUtil.getValues(c, tableName, "gid", true).size());*/

		WKTReader wkt = new WKTReader();

		//get all ids
		ArrayList<String> gids = PGUtil.getValues(c, tableName, "gid", false);
		Collections.shuffle(gids);
		int nb = gids.size();
		System.out.println(tableName + ": " + nb);

		//go through list of features
		int i=1;
		for(String gid1 : gids){

			//get geometry of object
			i++;
			Object geomS1_ = null;
			String geomS1 = null;
			try {
				Statement st = c.createStatement();
				try {
					ResultSet res = st.executeQuery("SELECT geom,ST_AsText(ST_Force2D(geom)) FROM "+tableName+" WHERE gid='"+gid1+"';");
					//System.out.println("SELECT geom,ST_AsText(ST_Force2D(geom)) FROM "+tableName+" WHERE gid='"+gid1+"';");
					if(res.next()){
						geomS1_ = res.getObject(1);
						geomS1 = res.getString(2);
					}
				} finally { st.close(); }
			} catch (Exception e) { e.printStackTrace(); }
			if(geomS1 == null) continue;
			Geometry geom1 = null;
			try { geom1 = wkt.read(geomS1); } catch (ParseException e) {
				System.out.println("Could not parse "+geomS1);
				e.printStackTrace();
			}

			//System.out.println(gid1);
			//System.out.println(geomS1_);
			//System.out.println(geom1);

			if(geom1 == null) {
				System.err.println("Null geometry for gid="+gid1);
				continue;
			}

			//clean geometry
			geom1 = geom1.buffer(0);

			try {
				Statement st = c.createStatement();
				try {
					//get all objects intersecting geom1
					ResultSet res = st.executeQuery("SELECT gid,ST_AsText(ST_Force2D(geom)) FROM "+tableName+" WHERE ST_Intersects(geom,'"+geomS1_+"') AND gid!='"+gid1+"';");
					//System.out.println("SELECT gid,ST_AsText(ST_Force2D(geom)) FROM "+tableName+" WHERE ST_Intersects(geom,'"+geomS1_+"') AND gid!='"+gid1+"';");

					while (res.next()) {
						//get geometry of intersecting object
						int gid2 = res.getInt(1);
						String geomS2 = res.getString(2);
						try {
							if(geomS2 == null) continue;
							Geometry geom2 = wkt.read(geomS2);
							if(geom2 == null) continue;

							//clean geometry
							geom2 = geom2.buffer(0);

							//compute intersection area
							double interArea = geom1.intersection(geom2).getArea();

							//if intersection area too small, skip
							if(interArea<0.01) continue;

							double a1 = geom1.getArea(), r1 = interArea/a1;
							double a2 = geom2.getArea(), r2 = interArea/a2;
							if(r2>r1 && r2>0.75){
								//delete gid2
								System.out.println("delete 2 "+gid2+"   "+((Math.round(10000.0*i/(nb*1.0)))*0.01)+"% done.");
								PGUtil.executeStatement(c, "DELETE FROM "+tableName+" WHERE gid='"+gid2+"'");
								continue;
							}
							if(r1>r2 && r1>0.75){
								//delete gid1
								System.out.println("delete 1 "+gid1+"   "+((Math.round(10000.0*i/(nb*1.0)))*0.01)+"% done.");
								PGUtil.executeStatement(c, "DELETE FROM "+tableName+" WHERE gid='"+gid1+"'");
								continue;
							}
						} catch (ParseException e1) {
							System.out.println("Could not parse "+geomS2);
							//e1.printStackTrace();
						} catch (TopologyException e1) {
							System.out.println("Topology problem");
							//e1.printStackTrace();
						}
					}
				} catch (PSQLException e) {
					e.printStackTrace();
				} finally { st.close(); }
			} catch (Exception e) { e.printStackTrace(); }
		}
	}

	//public static void removeWallonyDuplicates(Connection c){

	/*
		//check ids
		System.out.println(PGUtil.getValues(c, "bu_be_wa", "objectid", true).size());
		System.out.println(PGUtil.getValues(c, "bu_be_wa", "objectid", false).size());
		System.out.println(PGUtil.getValues(c, "bu_be_wa", "gid", true).size());
		System.out.println(PGUtil.getValues(c, "bu_be_wa", "gid", false).size());*/

	//remove duplicates of objectid - use gid as true id

	//create indexes
	//System.out.println( PGUtil.createIndex(c, "bu_be_wa", "objectid") );
	//System.out.println( PGUtil.createIndex(c, "bu_be_wa", "gid") ); useless: there is already an index for this column

	/*
		ArrayList<String> objectids = PGUtil.getValues(c, "bu_be_wa", "objectid", true);
		System.out.println(objectids.size());
		for(String objectid : objectids){
			ArrayList<String> gids = PGUtil.getValues(c, "bu_be_wa", "gid", false, "objectid='"+objectid+"'");
			//case where no duplicate
			if(gids.size()==1) continue;
			//remove duplicates
			//System.out.println(gids);
			gids.remove(0); //to ensure one is kept
			for(String gid : gids) PGUtil.executeStatement(c, "DELETE FROM bu_be_wa WHERE gid='"+gid+"'");
		}*/
	//}

}
