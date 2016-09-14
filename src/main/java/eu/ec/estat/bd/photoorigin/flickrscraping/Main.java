/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import java.util.Collection;

/**
 * @author Julien Gaffuri
 *
 */
public class Main {

	public static void main(String[] args) {
		System.out.println("Start");

		/*
		Config.init();
		//"lat", "49.611622", "lon", "6.131935", "radius", "10", //luxembourg

		int radius = 8;
		PhotoSearch ps = new PhotoSearch(2000, 1, 2016, 12, "lat", "43.695949", "lon", "7.271413", "radius", ""+radius);
		ps.getAndSaveWithGenericScheduler("out/flickr/", "nice_"+radius+"km_"+"2000-01"+".txt", true, true);
		 */

		Collection<Photo> photos = Photo.load("out/flickr/nice_8km_2000-01.txt");
		System.out.println("Number of photos: "+photos.size());
		System.out.println("Number of users: "+Owner.getOwnerNumber());
		System.out.println("Number of users with location text: "+Owner.getOwnerWithLocationTextNumber());

		System.out.println("End");
	}

}
