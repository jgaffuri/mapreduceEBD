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

		/*PhotoSearch ps = new PhotoSearch("lat", ""+43.695949, "lon", ""+7.271413, "radius", ""+7, "min_taken_date", "2005-01");
		System.out.println(ps.getList().size());
		ps.save("out/flickr/", "nice.txt");*/

		/*PhotoInfo photo;
		photo = new PhotoInfo("19527571594", "36049946@N03", "7022d8922a");
		photo.retrieveInfo();
		System.out.println(photo);*/

		/*
		System.out.println("Load photo list");
		PhotoSearch ps = new PhotoSearch();
		ps.load("out/flickr/nice.txt");
		System.out.println(ps.getList().size());

		ps.retrievePhotoInfo("out/flickr/", "nice_.txt");*/

		//ps.save("out/flickr/", "nice_.txt");

		int radius = 3;
		PhotoSearch ps = new PhotoSearch(2014, 5, 2014, 10, "lat", "43.695949", "lon", "7.271413", "radius", ""+radius);
		ps.getAndSaveWithGenericScheduler("out/flickr/", "nice_"+radius+"km_"+"2000-1"+".txt", true);

		System.out.println("End");
	}

}
