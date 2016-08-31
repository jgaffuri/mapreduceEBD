/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import eu.ec.estat.bd.Config;

/**
 * @author Julien Gaffuri
 *
 */
public class Main {

	public static void main(String[] args) {
		System.out.println("Start");
		Config.init();

		//"lat", "49.611622", "lon", "6.131935", "radius", "10", //luxembourg

		PhotoSearch ps = new PhotoSearch("lat", ""+43.695949, "lon", ""+7.271413, "radius", ""+5, "min_taken_date", "2005-01");
		System.out.println(ps.getList().size());
		ps.save("out/flickr/", "nice.txt");

		/*
		Photo photo;
		photo = new Photo("19527571594", "36049946@N03", "7022d8922a");
		photo.retrieveInfo();
		System.out.println(photo);*/

		System.out.println("End");
	}

}
