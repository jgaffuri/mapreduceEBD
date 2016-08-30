/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import eu.ec.estat.bd.Config;
import eu.ec.estat.bd.io.IOUtil;

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

		String url = IOUtil.getURL(URL_BASE, "api_key", Config.API_KEY, "method", "flickr.photos.search", "format", "rest", "content_type", "1", "has_geo", "1",
				"per_page", "20", "page", "1",
				"lat", "49.611622", "lon", "6.131935", "radius", "10",
				"min_taken_date", "2016-07", "max_taken_date", "2016-08"
				);

		String data = IOUtil.getDataFromURL(url);
		System.out.println(data);
	}

}
