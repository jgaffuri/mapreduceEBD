/**
 * 
 */
package eu.ec.estat.bd.geonames;

import java.util.ArrayList;

/**
 * @author Julien Gaffuri
 *
 */
public class GeoNameEntry {
	/*
NB=19
geonameid         : integer id of record in geonames database
name              : name of geographical point (utf8) varchar(200)
asciiname         : name of geographical point in plain ascii characters, varchar(200)
alternatenames    : alternatenames, comma separated, ascii names automatically transliterated, convenience attribute from alternatename table, varchar(10000)
latitude          : latitude in decimal degrees (wgs84)
longitude         : longitude in decimal degrees (wgs84)
feature class     : see http://www.geonames.org/export/codes.html, char(1)
feature code      : see http://www.geonames.org/export/codes.html, varchar(10)
country code      : ISO-3166 2-letter country code, 2 characters
cc2               : alternate country codes, comma separated, ISO-3166 2-letter country code, 200 characters
admin1 code       : fipscode (subject to change to iso code), see exceptions below, see file admin1Codes.txt for display names of this code; varchar(20)
admin2 code       : code for the second administrative division, a county in the US, see file admin2Codes.txt; varchar(80) 
admin3 code       : code for third level administrative division, varchar(20)
admin4 code       : code for fourth level administrative division, varchar(20)
population        : bigint (8 byte int) 
elevation         : in meters, integer
dem               : digital elevation model, srtm3 or gtopo30, average elevation of 3''x3'' (ca 90mx90m) or 30''x30'' (ca 900mx900m) area in meters, integer. srtm processed by cgiar/ciat.
timezone          : the iana timezone id (see file timeZone.txt) varchar(40)
modification date : date of last modification in yyyy-MM-dd format

10127228	Dubai International Terminal Hotel	Dubai International Terminal Hotel		25.24847	55.35222	S	HTL	AE		03				0		1	Asia/Dubai	2015-03-05
1114935	Spin Tangi	Spin Tangi	Spin Tangi	33.45056	70.24806	T	PASS	AF		00				0		1215	Asia/Kabul	2012-01-16
	 */

	public String name, asciiname, alternatenames;
	double latitude, longitude;
	public GeoNameEntry(String line){
		String[] data = line.split("\t", -1);
		name = data[1];
		asciiname = data[2];
		alternatenames = data[3];
		if(alternatenames.length()>0) System.out.println(alternatenames);
		latitude = Double.parseDouble(data[4]);
		longitude = Double.parseDouble(data[5]);
	}

	public ArrayList<String> getNames(boolean forceLowerCase){
		ArrayList<String> names = new ArrayList<String>();
		names.add(forceLowerCase? name.toLowerCase() : name);
		names.add(forceLowerCase? asciiname.toLowerCase() : asciiname);
		for(String name : alternatenames.split(",", -1)) if(name!=null && !"".equals(name)) names.add(forceLowerCase? name.toLowerCase() : name);
		return names;
	}


	/*public static void main(String[] args) {
		System.out.println("Start");
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader("H:/desktop/geo/geonames/allCountries.txt"));
			while((line = br.readLine()) != null) {
				//System.out.println(line);
				//String[] data = line.split("\t", -1);
				//if(data.length != 19) System.out.println(line);
				System.out.println(new GeoNameEntry(line).getNames());
			}
		} catch (IOException e) { e.printStackTrace();
		} finally { try { if(br!=null) br.close(); } catch (IOException ex) {  ex.printStackTrace(); } }
		System.out.println("End");
	}*/

}
