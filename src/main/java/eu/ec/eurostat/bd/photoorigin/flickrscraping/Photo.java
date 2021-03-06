/**
 * 
 */
package eu.ec.eurostat.bd.photoorigin.flickrscraping;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author julien Gaffuri
 *
 */
public class Photo {
	String id,secret,taken,place_id,woeid;
	double latitude, longitude;
	int accuracy;
	Owner owner;


	/**
	 * @param filePath
	 * @return
	 */
	public static Collection<Photo> load(String filePath){
		Collection<Photo> photos = new HashSet<Photo>();
		BufferedReader br = null;
		try {
			String line;
			br = new BufferedReader(new FileReader(filePath));
			while((line = br.readLine()) != null) {
				Photo p = new Photo();
				String[] data = line.split("\\|", -1);

				int i=0;
				p.id = data[i++];
				String ownerId = data[i++];
				String ownerlocationText = data[i++];
				p.secret = data[i++];
				p.taken = data[i++];
				p.latitude = Double.parseDouble(data[i++]);
				p.longitude = Double.parseDouble(data[i++]);
				p.accuracy = Integer.parseInt(data[i++]);
				p.place_id = data[i++];
				p.woeid = data[i++];

				Owner.setOwner(p, ownerId, ownerlocationText);

				photos.add(p);
			}
		} catch (IOException e) { e.printStackTrace();
		} finally { try { if(br!=null) br.close(); } catch (IOException ex) {  ex.printStackTrace(); } }
		return photos;
	}

}
