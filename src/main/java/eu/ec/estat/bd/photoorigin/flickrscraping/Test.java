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
 * @author Julien Gaffuri
 *
 */
public class Test {

	private static final String URL_BASE = "https://api.flickr.com/services/rest/";

	//Get information about a user
	//https://api.flickr.com/services/rest/?api_key=ff1340afcb6f0bc7ba23f38eed2a1e17&method=flickr.people.getInfo&user_id=36049946@N03


	public static void main(String[] args) {
		System.out.println("Start");
		Config.init();

		/*Collection<Photo> photos = getPhotosList(
				43.695949, 7.271413, 5, //nice
				//"lat", "49.611622", "lon", "6.131935", "radius", "10", //luxembourg
				"2015-07-15", "2015-08-01"
				);

		System.out.println(photos.size());
		for(Photo photo : photos)
			System.out.println(photo);
		System.out.println(photos.size());*/

		Photo photo = new Photo("19527571594", "36049946@N03", "7022d8922a");
		photo.retrieveInfo();
		System.out.println(photo);

		System.out.println("End");
	}

	public static Collection<Photo> getPhotosList(double lat, double lon, int radiusKM, String minDate, String maxDate){
		String perpage = "250";
		int pages = 1;

		Collection<Photo> photos = new HashSet<Photo>();
		for(int page=1; page<=pages; page++){

			String url = IOUtil.getURL(URL_BASE, "api_key", Config.API_KEY, "method", "flickr.photos.search", "format", "rest", "content_type", "1", "has_geo", "1",
					"per_page", perpage, "page", ""+page,
					"lat", ""+lat, "lon", ""+lon, "radius", ""+radiusKM,
					"min_taken_date", minDate, "max_taken_date", maxDate
					);
			System.out.println(url);

			//String data = IOUtil.getDataFromURL(url);
			//System.out.println(data);

			Document xml = XML.parseXMLfromURL(url);

			Element rspElt = (Element)xml.getChildNodes().item(0);
			String status = rspElt.getAttribute("stat");
			if(!status.equals("ok")){
				System.out.println("Could not get data from url: "+url);
				System.out.println("Status = "+status);
				continue;
			}

			Element photosElt = (Element) rspElt.getElementsByTagName("photos").item(0);
			if(pages == 1) pages = Integer.parseInt(photosElt.getAttribute("pages"));

			NodeList photoList = photosElt.getElementsByTagName("photo");
			for(int i=0; i<photoList.getLength(); i++){
				Element photoElt = (Element) photoList.item(i);
				photos.add( new Photo(photoElt.getAttribute("id"), photoElt.getAttribute("owner"), photoElt.getAttribute("secret")) );
			}
		}
		return photos;
	}


	public static class Photo {
		String id, owner, secret, date, ownerlocation;
		double lat, lon;

		public Photo(String id, String owner, String secret){
			this.id=id; this.owner=owner; this.secret=secret;
		}
		
		@Override
		public String toString() { return id+" "+owner+" "+ownerlocation+" "+" "+secret+" "+date+" "+lat+" "+lon; }

		public void retrieveInfo() {
			//https://api.flickr.com/services/rest/?api_key=ff1340afcb6f0bc7ba23f38eed2a1e17&method=flickr.photos.getInfo&format=rest&photo_id=19527571594&secret=7022d8922a

			String url = IOUtil.getURL(URL_BASE, "api_key", Config.API_KEY, "method", "flickr.photos.getInfo", "format", "rest", "photo_id", id, "secret", secret);
			//System.out.println(url);

			Document xml = XML.parseXMLfromURL(url);

			Element mainElt = (Element)xml.getChildNodes().item(0);
			String status = mainElt.getAttribute("stat");
			if(!status.equals("ok")){
				System.out.println("Could not get data from url: "+url);
				System.out.println("Status = "+status);
				return;
			}

			mainElt = (Element) mainElt.getElementsByTagName("photo").item(0);
			Element elt;

			//location
			elt = (Element) mainElt.getElementsByTagName("location").item(0);
			lat = Double.parseDouble(elt.getAttribute("latitude"));
			lon = Double.parseDouble(elt.getAttribute("longitude"));

			//date
			elt = (Element) mainElt.getElementsByTagName("dates").item(0);
			date = elt.getAttribute("taken");

			//ownerlocation
			elt = (Element) mainElt.getElementsByTagName("owner").item(0);
			ownerlocation = elt.getAttribute("location");

		}
	}

}
