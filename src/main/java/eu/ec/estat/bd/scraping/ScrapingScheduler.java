/**
 * 
 */
package eu.ec.estat.bd.scraping;

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
	private PriorityQueue<Query> queries = new PriorityQueue<Query>();
	public boolean add(Query qu){ return queries.add(qu); }

	public class Query {
		QueryType type;
		String url;
		Function callback;
		public Query(QueryType type, String url, Function callback){ this.type=type; this.url=url; this.callback=callback; }
		public Query(String url, Function callback){ this(QueryType.STRING,url,callback); }
	}
	public enum QueryType { STRING, XML }
	public interface Function { void execute(Object data); }

	public void launch(int timeMilliSeconds, final String urlKeyPart, final boolean verbose){
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
