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

		//TODO import remaining wa shapfiles (liege and hainault)
		//TODO remove duplicates wa
		//TODO import buildings bruxelles
		//TODO harmonise sheamses
		//TODO merge tables
		//TODO handle intersecting buildings
		//TODO adapt script
		//TODO run script
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

}
