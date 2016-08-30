/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import java.util.Collection;
import java.util.HashSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.ec.estat.bd.Config;
import eu.ec.estat.bd.io.IOUtil;
import eu.ec.estat.bd.io.XML;

/**
 * Flickr photo data
 * 
 * @author Julien Gaffuri
 *
 */
public class Photo {
	private static final String URL_BASE = "https://api.flickr.com/services/rest/";

	//information on the photo
	String id, secret, date, owner, ownerlocation;
	double lat, lon;

	public Photo(String id, String owner, String secret){
		this.id=id; this.owner=owner; this.secret=secret;
	}

	@Override
	public String toString() { return id+" "+owner+" "+ownerlocation+" "+" "+secret+" "+date+" "+lat+" "+lon; }

	/**
	 * Retrieve missing information on the photo based on its id+secret
	 */
	public void retrieveInfo() {
		//example:
		//https://api.flickr.com/services/rest/?api_key=ff1340afcb6f0bc7ba23f38eed2a1e17&method=flickr.photos.getInfo&format=rest&photo_id=19527571594&secret=7022d8922a

		String url = IOUtil.getURL(URL_BASE, "api_key", Config.FLICKR_API_KEY, "method", "flickr.photos.getInfo", "format", "rest", "photo_id", id, "secret", secret);
		//System.out.println(url);

		//parse xml
		Document xml = XML.parseXMLfromURL(url);

		//check status
		Element mainElt = (Element)xml.getChildNodes().item(0);
		String status = mainElt.getAttribute("stat");
		if(!status.equals("ok")){
			System.out.println("Could not get data from url: "+url);
			System.out.println("Status = "+status);
			return;
		}

		mainElt = (Element) mainElt.getElementsByTagName("photo").item(0);
		Element elt;

		//get location information
		elt = (Element) mainElt.getElementsByTagName("location").item(0);
		lat = Double.parseDouble(elt.getAttribute("latitude"));
		lon = Double.parseDouble(elt.getAttribute("longitude"));

		//get date information
		elt = (Element) mainElt.getElementsByTagName("dates").item(0);
		date = elt.getAttribute("taken");

		//get ownerlocation information
		elt = (Element) mainElt.getElementsByTagName("owner").item(0);
		ownerlocation = elt.getAttribute("location");

	}





	/**
	 * Get a collection of photos based on a query.
	 * 
	 * @return
	 */
	public static Collection<Photo> getPhotosList(double lat, double lon, int radiusKM, String minDate, String maxDate){
		String perpage = "250";
		int pages = 1;

		Collection<Photo> photos = new HashSet<Photo>();
		for(int page=1; page<=pages; page++){

			String url = IOUtil.getURL(URL_BASE, "api_key", Config.FLICKR_API_KEY, "method", "flickr.photos.search", "format", "rest", "content_type", "1", "has_geo", "1",
					"per_page", perpage, "page", ""+page,
					"lat", ""+lat, "lon", ""+lon, "radius", ""+radiusKM,
					"min_taken_date", minDate, "max_taken_date", maxDate
					);
			System.out.println(url);

			//parse xml
			Document xml = XML.parseXMLfromURL(url);

			//check status
			Element mainElt = (Element)xml.getChildNodes().item(0);
			String status = mainElt.getAttribute("stat");
			if(!status.equals("ok")){
				System.out.println("Could not get data from url: "+url);
				System.out.println("Status = "+status);
				continue;
			}

			mainElt = (Element) mainElt.getElementsByTagName("photos").item(0);

			//update pages count (if different from 1)
			if(pages == 1) pages = Integer.parseInt(mainElt.getAttribute("pages"));

			//create photo elements
			NodeList photoList = mainElt.getElementsByTagName("photo");
			for(int i=0; i<photoList.getLength(); i++){
				Element photoElt = (Element) photoList.item(i);
				photos.add( new Photo(photoElt.getAttribute("id"), photoElt.getAttribute("owner"), photoElt.getAttribute("secret")) );
			}
		}
		return photos;
	}

}
