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

	/*
//search photo
https://api.flickr.com/services/rest/?api_key=ff1340afcb6f0bc7ba23f38eed2a1e17&method=flickr.photos.search
&format=json&content_type=1&has_geo=1
&content_type=1
&has_geo=1
&min_taken_date
&max_taken_date
&bbox=minimum_longitude,minimum_latitude,maximum_longitude,maximum_latitude
&lat=
&lon=
&radius= (in km, max=32km)
&woe_id= A 32-bit identifier that uniquely represents spatial entities. (not used if bbox argument is present).
&place_id=A Flickr place id
&accuracy=World level is 1,Country is ~3,Region is ~6,City is ~11,Street is ~16
&text= free text
&tags=paris,france
&tag_mode=any|all
&sort=date-posted-asc|date-posted-desc|date-taken-asc|date-taken-desc|interestingness-desc|interestingness-asc|relevance
&per_page=500
&page=1

//get info on photo
https://api.flickr.com/services/rest/?api_key=ff1340afcb6f0bc7ba23f38eed2a1e17&method=flickr.photos.getInfo&photo_id=29238337811&secret=c2466316c5

//Get information about a user
https://api.flickr.com/services/rest/?api_key=ff1340afcb6f0bc7ba23f38eed2a1e17&method=flickr.people.getInfo&user_id=98614865@N05

	 *
	 */



	public static void main(String[] args) {
		System.out.println("Start");
		Config.init();

		Collection<Photo> photos = getPhotosList(
				43.695949, 7.271413, 5, //nice
				//"lat", "49.611622", "lon", "6.131935", "radius", "10", //luxembourg
				"2016-07-15", "2016-08-01"
				);

		System.out.println(photos.size());
		for(Photo photo : photos)
			System.out.println(photo);
		System.out.println(photos.size());

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
				Photo photo = new Photo();
				photo.id = photoElt.getAttribute("id");
				photo.owner = photoElt.getAttribute("owner");
				photo.secret = photoElt.getAttribute("secret");
				photos.add(photo);
			}
		}
		return photos;
	}


	public static class Photo {
		String id, owner, secret;

		@Override
		public String toString() { return id+" "+owner+" "+" "+secret; }
	}

}
