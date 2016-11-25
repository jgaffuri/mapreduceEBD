package eu.ec.eurostat.io.postgis;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class PGUtil {

	public static void printDBInfo(Connection c, PrintStream ps){ printDBInfo(c,ps,true,true); }
	public static void printDBInfo(Connection c, PrintStream ps, boolean onlyPublicTables, boolean onlyBaseTables){
		try {
			Statement st = c.createStatement();
			try {
				ResultSet res = st.executeQuery("SELECT table_name FROM information_schema.tables WHERE 1=1"+(onlyPublicTables?" AND table_schema='public'":"")+(onlyBaseTables?" AND table_type='BASE TABLE'":""));
				while (res.next()) ps.println(res.getString(1));
			} finally { st.close(); }
		} catch (Exception e) { e.printStackTrace(); }
	}

	public static int getTableSize(Connection c, String tableName){
		int s = -1;
		try {
			Statement st = c.createStatement();
			try {
				ResultSet res = st.executeQuery("SELECT count(*) FROM "+tableName);
				res.next();
				s = res.getInt(1);
			} finally { st.close(); }
		} catch (Exception e) { e.printStackTrace(); }
		return s;
	}

	//type: integer,text,etc.
	public static boolean addColumn(Connection c, String tableName, String columnName, String columnType){
		boolean b = false;
		try {
			Statement st = c.createStatement();
			try {
				b = st.execute( "ALTER TABLE "+tableName+" ADD COLUMN "+columnName+" "+columnType+";" );
			} finally { st.close(); }
		} catch (Exception e) { e.printStackTrace(); }
		return b;
	}

	public static boolean createIndex(Connection c, String tabName, String columnName, String indexName){
		boolean b = false;
		try {
			Statement st = c.createStatement();
			try {
				b = st.execute("CREATE INDEX "+indexName+" ON "+tabName+" ("+columnName+" ASC NULLS LAST);");
			} finally { st.close(); }
		} catch (Exception e) { e.printStackTrace(); }
		return b;
	}

}
