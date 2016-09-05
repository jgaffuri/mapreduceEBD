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

		PhotoSearch ps = new PhotoSearch("lat", ""+43.695949, "lon", ""+7.271413, "radius", ""+5, "min_taken_date", "2016-01");
		//ps.getAndSave("out/flickr/", "nice.txt");
		ps.getAndSaveWithGenericScheduler("out/flickr/", "nice.txt");


		/*PriorityQueue <Query>  prq = new PriorityQueue<Query> ();
		for ( int i=5; i<10; i++ ) prq.add( new Query(QueryType.XML, "url"+i, null) );
		System.out.println ("Init: "+ prq);
		Query head = prq.poll();
		System.out.println ( "Head: "+ head);
		System.out.println ( "After: "+ prq);*/

		System.out.println("End");
	}

}
