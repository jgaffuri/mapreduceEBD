/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.ec.estat.bd.Config;
import eu.ec.estat.bd.io.IOUtil;
import eu.ec.estat.bd.io.XML;
import eu.ec.estat.bd.scraping.ScrapingScheduler;
import eu.ec.estat.bd.scraping.ScrapingScheduler.Function;
import eu.ec.estat.bd.scraping.ScrapingScheduler.QueryType;

/**
 * @author Julien Gaffuri
 *
 */
public class PhotoSearch {
	private static final String URL_BASE = "https://api.flickr.com/services/rest/";
	private static final String perpage = "250";

	//the URL pattern
	private String urlQueryBase;
	private String urlQueryBaseNoKey;
	//private List<PhotoInfo> list;

	//https://www.flickr.com/services/api/explore/flickr.photos.search
	//https://api.flickr.com/services/rest/?lat=43.695949&lon=7.271413&radius=5&min_taken_date=2010-01&api_key=ff1340afcb6f0bc7ba23f38eed2a1e17&method=flickr.photos.search&format=rest&content_type=1&has_geo=1&per_page=250&page=229
	//Please note that Flickr will return at most the first 4,000 results for any given search query. If this is an issue, we recommend trying a more specific query.
	//safe_search?
	//content_type=7?

	//woe_id
	//place_id
	//has_geo

	//find location coords from text: https://www.flickr.com/services/api/flickr.places.find.html
	//find user location with https://www.flickr.com/services/api/flickr.places.placesForUser.html


	/**
	 * A photo search with some query parameters
	 * See https://www.flickr.com/services/api/flickr.photos.search.html
	 * 
	 * @param paramData
	 */
	public PhotoSearch(String... paramData){
		//build url template
		String[] paramData_ = (String[])ArrayUtils.addAll(paramData, new String[] {"method", "flickr.photos.search", "format", "rest", "content_type", "1", "has_geo", "1", "per_page", perpage});
		urlQueryBaseNoKey = IOUtil.getURL(URL_BASE, paramData_);
		urlQueryBase = urlQueryBaseNoKey + "&api_key=" + Config.FLICKR_API_KEY;
	}

	/**
	 * @return Retrieve and save image data
	 */
	private int page=0, pages=99999999, photoNb=0, photoI=0;
	NodeList photoList = null;
	StringBuffer sb = new StringBuffer();
	public void getAndSave(final String path, final String fileName){

		//initialise output file
		new File(path).mkdirs();
		File outFile_ = new File(path+fileName);
		try {
			if(outFile_.exists()) outFile_.delete();
			Files.createFile(Paths.get(path+fileName));
		} catch (Exception e) { e.printStackTrace(); }

		//launch queries with scheduler - one query every second
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {

				//new page needed to be loaded
				if(photoI==photoNb){

					//save
					System.out.println("save...");
					try {
						Files.write(Paths.get(path+fileName), sb.toString().getBytes(), StandardOpenOption.APPEND);
					} catch (IOException e) { e.printStackTrace(); }
					sb = new StringBuffer();

					page++;

					if(page>pages){
						//no more page to load: exit
						System.out.println("Done");
						executor.shutdown();
						return;
					}

					//load new page
					String url = urlQueryBase + "&page="+page;
					System.out.println(url);

					//parse xml
					Document xml = XML.parseXMLfromURL(url);

					//check status
					Element mainElt = (Element)xml.getChildNodes().item(0);
					String status = mainElt.getAttribute("stat");
					if(!status.equals("ok")){
						System.out.println("Could not get data from url: "+url);
						System.out.println("Status = "+status);
						return;
					}

					mainElt = (Element) mainElt.getElementsByTagName("photos").item(0);

					//initialise pages count (if not already done)
					if(pages == 99999999) pages = Integer.parseInt(mainElt.getAttribute("pages"));

					System.out.println(page+"/"+pages);

					//get photo elements
					photoList = mainElt.getElementsByTagName("photo");

					photoI=0; photoNb=photoList.getLength();
					return;
				}

				//load next image
				Element photoElt = (Element) photoList.item(photoI);
				PhotoInfo photo = new PhotoInfo(photoElt.getAttribute("id"), photoElt.getAttribute("owner"), photoElt.getAttribute("secret"));

				photo.retrieveInfo();
				String photoSt = photo.toString();
				System.out.println(photoSt);

				sb.append(photoSt);
				sb.append("\n");

				photoI++;
			}
		}, 0, 1, TimeUnit.SECONDS);

		/*
		int pages = 1;
		for(int page=1; page<=pages; page++){

			String url = urlQueryBase + "&page="+page;
			System.out.println(url);

			//parse xml
			Document xml = XML.parseXMLfromURL(url);

			//check status
			Element mainElt = (Element)xml.getChildNodes().item(0);
			String status = mainElt.getAttribute("stat");
			if(!status.equals("ok")){
				System.out.println("Could not get data from url: "+url);
				System.out.println("Status = "+status);
				continue;
			}

			mainElt = (Element) mainElt.getElementsByTagName("photos").item(0);

			//update pages count (if different from 1)
			if(pages == 1) pages = Integer.parseInt(mainElt.getAttribute("pages"));

			System.out.println(page+"/"+pages);

			//focus on photo elements
			final NodeList photoList = mainElt.getElementsByTagName("photo");
			i__=0;
			final StringBuffer data_ = new StringBuffer();

			final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
			executor.scheduleAtFixedRate(new Runnable() {
				public void run() {

					if(i__==photoList.getLength()){
						//save
						System.out.println("save...");
						try {
							Files.write(Paths.get(path+fileName), data_.toString().getBytes(), StandardOpenOption.APPEND);
						} catch (IOException e) { e.printStackTrace(); }

						executor.shutdown();
						return;
					}

					Element photoElt = (Element) photoList.item(i__);
					PhotoInfo photo = new PhotoInfo(photoElt.getAttribute("id"), photoElt.getAttribute("owner"), photoElt.getAttribute("secret"));

					photo.retrieveInfo();
					System.out.println(photo);

					data_.append(photo.toString());
					data_.append("\n");
					//photo.appendToFile(path, outFile);
					i__++;
				}
			}, 0, 1, TimeUnit.SECONDS);


		}*/
	}

	public void getAndSaveWithGenericScheduler(final String path, final String fileName){
		final ScrapingScheduler sch = new ScrapingScheduler();

		sch.add(QueryType.XML, urlQueryBaseNoKey, new Function(){
			public void execute(Object data) {
				Document xml = (Document) data;

				//check status
				Element mainElt = (Element)xml.getChildNodes().item(0);
				String status = mainElt.getAttribute("stat");
				if(!status.equals("ok")){
					System.out.println("Could not get data from url: "+urlQueryBaseNoKey);
					System.out.println("Status = "+status);
					return;
				}

				mainElt = (Element) mainElt.getElementsByTagName("photos").item(0);
				final int pages = Integer.parseInt(mainElt.getAttribute("pages"));
				System.out.println("pages="+pages);

				for(int page=1; page<=pages; page++){
					final int pagef=page;
					String url = urlQueryBaseNoKey+"&page="+page;
					sch.add(QueryType.XML, url, new Function(){
						public void execute(Object data) {
							Document xml_ = (Document) data;

							//check status
							Element mainElt = (Element)xml_.getChildNodes().item(0);
							String status = mainElt.getAttribute("stat");
							if(!status.equals("ok")){
								System.out.println("Could not get data from url: "+urlQueryBaseNoKey);
								System.out.println("Status = "+status);
								return;
							}

							//get photo elements
							NodeList photoList = mainElt.getElementsByTagName("photo");
							System.out.println(pagef + "/" + pages + "    photonb="+photoList.getLength());

							for(int photoI=0; photoI<photoList.getLength(); photoI++) {
								//load next image
								Element photoElt = (Element) photoList.item(photoI);
								final PhotoInfo photo = new PhotoInfo(photoElt.getAttribute("id"), photoElt.getAttribute("owner"), photoElt.getAttribute("secret"));

								final String url = IOUtil.getURL(URL_BASE, "method", "flickr.photos.getInfo", "format", "rest", "photo_id", photo.id, "secret", photo.secret);
								sch.add(QueryType.XML, url, new Function(){
									public void execute(Object data) {
										Document xml_ = (Document) data;

										//check status
										Element mainElt = (Element)xml_.getChildNodes().item(0);
										String status = mainElt.getAttribute("stat");
										if(!status.equals("ok")){
											System.out.println("Could not get data from url: "+url);
											System.out.println("Status = "+status);
											return;
										}

										mainElt = (Element) mainElt.getElementsByTagName("photo").item(0);
										Element elt;

										//get location information
										elt = (Element) mainElt.getElementsByTagName("location").item(0);
										photo.lat = Double.parseDouble(elt.getAttribute("latitude"));
										photo.lon = Double.parseDouble(elt.getAttribute("longitude"));

										//get date information
										elt = (Element) mainElt.getElementsByTagName("dates").item(0);
										photo.date = elt.getAttribute("taken");

										//get ownerlocation information
										elt = (Element) mainElt.getElementsByTagName("owner").item(0);
										photo.ownerlocation = elt.getAttribute("location");

										String photoSt = photo.toString();
										System.out.println(photoSt);
									}
								});
							}
							//TODO save from time to time
						}
					});
				}
			}
		});

		sch.launchExecutorAtFixedRate(1000, "&api_key=" + Config.FLICKR_API_KEY, true);
	}


	/**
	 * @return The list of photos for the query
	 */
	/*public List<PhotoInfo> getList(){
		if(list == null) {
			list = new ArrayList<PhotoInfo>();

			int pages = 1;
			for(int page=1; page<=pages; page++){

				String url = urlQueryBase + "&page="+page;
				System.out.println(url);

				//parse xml
				Document xml = XML.parseXMLfromURL(url);

				//check status
				Element mainElt = (Element)xml.getChildNodes().item(0);
				String status = mainElt.getAttribute("stat");
				if(!status.equals("ok")){
					System.out.println("Could not get data from url: "+url);
					System.out.println("Status = "+status);
					continue;
				}

				mainElt = (Element) mainElt.getElementsByTagName("photos").item(0);

				//update pages count (if different from 1)
				if(pages == 1) pages = Integer.parseInt(mainElt.getAttribute("pages"));

				//create photo elements
				NodeList photoList = mainElt.getElementsByTagName("photo");
				for(int i=0; i<photoList.getLength(); i++){
					Element photoElt = (Element) photoList.item(i);
					PhotoInfo photo = new PhotoInfo(photoElt.getAttribute("id"), photoElt.getAttribute("owner"), photoElt.getAttribute("secret"));
					//photo.retrieveInfo();
					list.add(photo);
				}
			}
		}
		return list;
	}*/




	/**
	 * Retrieve some information on the photo (with one query per second)
	 */
	/*
	public void retrievePhotoInfo(){ retrievePhotoInfo(null,null);}
	public void retrievePhotoInfo(final String path, final String outFile){
		i__=0;
		final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
		executor.scheduleAtFixedRate(new Runnable() {
			public void run() {
				System.out.println(i__+"/"+getList().size());
				if(i__==getList().size()){
					executor.shutdown();
					return;
				}
				PhotoInfo photo = getList().get(i__);
				photo.retrieveInfo();
				if(path != null && outFile != null) photo.appendToFile(path, outFile);
				System.out.println(photo);
				i__++;
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
	 */


	/**
	 * Save the list.
	 * 
	 * @param path
	 * @param fileName
	 */
	/*public void save(String path, String fileName){
		//populate
		getList();

		BufferedWriter bw = null;
		try {
			new File(path).mkdirs();
			File outFile_ = new File(path+fileName);
			if(outFile_.exists()) outFile_.delete();

			bw = new BufferedWriter(new FileWriter(outFile_, true));

			for(PhotoInfo photo : list){
				bw.write(photo.toString());
				bw.newLine();
			}
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (bw != null) bw.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}
	}*/


	/*public void load(String filePath) {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));

			list = new ArrayList<PhotoInfo>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] data = line.split("\\|", -1);
				//0id+"|"+1owner+"|"+2ownerlocation+"|"+3secret+"|"+4date+"|"+5lat+"|"+6lon
				PhotoInfo photo = new PhotoInfo(data[0],data[1],data[3]);
				photo.ownerlocation = data[2];
				photo.date = data[4];
				photo.lat = Double.parseDouble(data[5]);
				photo.lon = Double.parseDouble(data[6]);
				list.add(photo);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try { if (br != null)br.close(); } catch (Exception ex) { ex.printStackTrace(); }
		}

	}*/
}
