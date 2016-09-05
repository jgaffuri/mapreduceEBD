/**
 * 
 */
package eu.ec.estat.bd.scraping;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import eu.ec.estat.bd.io.IOUtil;
import eu.ec.estat.bd.io.XML;


/**
 * @author Julien Gaffuri
 *
 */
public class ScrapingScheduler {

	/**
	 * The queue of queries
	 */
	private PriorityQueue<Query> queries = new PriorityQueue<Query>();

	/**
	 * The list of query signatures currently in the queue. It is used to ensure a same query is not added twice
	 */
	private HashSet<String> querySignatures = new HashSet<String>();

	/**
	 * Add a query to the queue.
	 * 
	 * @param type
	 * @param url
	 * @param callback
	 * @return
	 */
	public boolean add(QueryType type, String url, Function callback){
		Query qu = new Query(type, url, callback);
		String sign = qu.getSignature();
		synchronized (queries) {
			if(querySignatures.contains(sign)) return false;
			return queries.add(qu);
		}
	}

	/**
	 * A query.
	 * 
	 * @author Julien Gaffuri
	 *
	 */
	private class Query {
		QueryType type;
		String url;
		Function callback;
		public Query(QueryType type, String url, Function callback){ this.type=type; this.url=url; this.callback=callback; }
		String getSignature(){ return url; }
	}
	public enum QueryType { STRING, XML }
	public interface Function { void execute(Object data); }

	/**
	 * Launch an executor "AtFixedRate".
	 * NB: several executors may be launched in parrallel in case several keys are available.
	 * 
	 * @param timeMilliSeconds
	 * @param urlKeyPart
	 * @param verbose
	 */
	public void launchExecutorAtFixedRate(int timeMilliSeconds, final String urlKeyPart, final boolean verbose){
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {

				Query qu = null;
				synchronized (queries) { qu = queries.poll(); }

				if(qu==null){
					//no more query to execute: exit
					System.out.println("Done");
					executor.shutdown();
					return;
				}

				//execture query
				String url = qu.url + urlKeyPart;
				if(verbose) System.out.println(url);

				Object data;
				if(qu.type == QueryType.XML) data = XML.parseXMLfromURL(url);
				else data = IOUtil.getDataFromURL(url);

				qu.callback.execute(data);
			}
		}, 0, timeMilliSeconds, TimeUnit.MILLISECONDS);

	}

}
