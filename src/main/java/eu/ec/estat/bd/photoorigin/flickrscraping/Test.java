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

		String year = "2014", month = "08";

		//"lat", "49.611622", "lon", "6.131935", "radius", "10", //luxembourg
		PhotoSearch ps = new PhotoSearch("lat", ""+43.695949, "lon", ""+7.271413, "radius", ""+5, "min_taken_date", year+"-"+month+"-01", "max_taken_date", year+"-"+month+"-31");
		ps.save("out/flickr/", year+"_"+month+".txt");

		/*
		Photo photo;
		photo = new Photo("19527571594", "36049946@N03", "7022d8922a");
		photo.retrieveInfo();
		System.out.println(photo);
		photo = new Photo("15679522111", "7898071@N04", "b8bf79ab32");
		photo.retrieveInfo();
		System.out.println(photo);
		photo = new Photo("15582927170", "99056719@N00", "8a65dcc990");
		photo.retrieveInfo();
		System.out.println(photo);
		photo = new Photo("15077256071", "50285818@N02", "fa2363b0f1");
		photo.retrieveInfo();
		System.out.println(photo);
		photo = new Photo("14884305219", "56857020@N04", "8a9dbe0a66");
		photo.retrieveInfo();
		System.out.println(photo);
		photo = new Photo("15481200116", "128591204@N04", "e46f9b6209");
		photo.retrieveInfo();
		System.out.println(photo);*/

		System.out.println("End");
	}

}
