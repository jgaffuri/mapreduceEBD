/**
 * 
 */
package eu.ec.eurostat.bd.proximus;

import java.sql.Connection;

import eu.ec.eurostat.bd.Config;
import eu.ec.eurostat.io.postgis.PGConnection;
import eu.ec.eurostat.io.postgis.PGUtil;

/**
 * @author gaffuju
 *
 */
public class PostGISManipulation {

	public static void main(String[] args) throws Exception {

		PGConnection pgConn = new PGConnection(Config.pg_db, Config.pg_host, Config.pg_port, Config.pg_user, Config.pg_pw);
		Connection c = pgConn.getConnection();

		PGUtil.printDBInfo(c, System.out);
		System.out.println(PGUtil.getTableSize(c, "nuts_rg"));

		c.close();
	}

}
