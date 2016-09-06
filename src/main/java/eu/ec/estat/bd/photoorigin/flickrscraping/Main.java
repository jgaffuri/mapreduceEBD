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

		/*
		int radius = 8;
		PhotoSearch ps = new PhotoSearch(2000, 1, 2016, 12, "lat", "43.695949", "lon", "7.271413", "radius", ""+radius);
		ps.getAndSaveWithGenericScheduler("out/flickr/", "nice_"+radius+"km_"+"2000-1"+".txt", true);
		 */

		PhotoSearch.getUserInfo("out/flickr/nice___.txt", "out/flickr/", "users.txt");

		System.out.println("End");
	}

}
