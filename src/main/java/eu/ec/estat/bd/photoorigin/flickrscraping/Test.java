/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import eu.ec.estat.bd.Config;

/**
 * @author Julien Gaffuri
 *
 */
public class Test {

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

}
