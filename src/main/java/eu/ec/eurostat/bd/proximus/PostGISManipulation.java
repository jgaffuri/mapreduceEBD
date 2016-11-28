/**
 * 
 */
package eu.ec.eurostat.bd.proximus;

import java.sql.Connection;
import java.util.Collection;

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

		Collection<String> tables = PGUtil.getTableNames(c);
		for(String table : tables)
			System.out.println(table+" - "+PGUtil.getTableSize(c, table) + " rows"
					//+ " - SRID:" + PGUtil.getSRID(c, table, "geom")
					);

		//remove duplicates in wa
		//removeWallonyDuplicates(c);

		//add spatial index
		//if (PGUtil.createSpatialIndex(c, "bu_be")) System.out.println("Spatial index created"); else System.err.println("Spatial index NOT created");

		//TODO handle intersecting buildings
		//TODO test on intersecting buildings
		//TODO export shp or adapt script
		//TODO run
		//TODO write report

		/*
		for(String table : tables){
			System.out.println(table);
			PGUtil.createSpatialIndex(c, "bu_be_wa_bw");
		}
		 */
		c.close();

		System.out.println("End");
	}

	public static void handleIntersectingBuildings(Connection c){
		//should be the same
		System.out.println(PGUtil.getValues(c, "bu_be", "gid", false).size());
		System.out.println(PGUtil.getValues(c, "bu_be", "gid", true).size());

		//ArrayList<String> gids = PGUtil.getValues(c, "bu_be", "gid", false);
	}

	public static void removeWallonyDuplicates(Connection c){

		/*/check ids
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
	}

}
