/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.ec.estat.bd.Config;
import eu.ec.estat.bd.io.IOUtil;
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
	private String urlQueryBaseNoKey;
	private int startYear, startMonth, endYear, endMonth;

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
	public PhotoSearch(int startYear, int startMonth, int endYear, int endMonth, String... paramData){
		//build url template
		String[] paramData_ = (String[])ArrayUtils.addAll(paramData, new String[] {"method", "flickr.photos.search", "format", "rest", "content_type", "1", "has_geo", "1", "per_page", perpage});
		urlQueryBaseNoKey = IOUtil.getURL(URL_BASE, paramData_);
		this.startYear=startYear; this.startMonth=startMonth; this.endYear=endYear; this.endMonth=endMonth;
	}


	public void getAndSaveWithGenericScheduler(final String path, final String fileName, boolean deleteInitialFile){

		final ScrapingScheduler sch = new ScrapingScheduler(250, path, fileName, deleteInitialFile);

		for(int year=startYear; year<=endYear; year++){
			int startMonth_ = year==startYear?startMonth:1;
			int endMonth_ = year==endYear?endMonth:12;
			for(int month=startMonth_; month<=endMonth_; month++){
				final String min_taken_date = getDateStamp(year,month);
				final String max_taken_date = month==12? getDateStamp(year+1,1) : getDateStamp(year,month+1);

				String url = urlQueryBaseNoKey + "&min_taken_date="+min_taken_date+"&max_taken_date="+max_taken_date;
				sch.add(QueryType.XML, url, new Function(){
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
							//final int pagef=page;
							String url = urlQueryBaseNoKey + "&min_taken_date="+min_taken_date + "&max_taken_date="+max_taken_date + "&page="+page;
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
									//System.out.println("page: " + pagef + "/" + pages);

									for(int photoI=0; photoI<photoList.getLength(); photoI++) {
										//load next image
										Element photoElt = (Element) photoList.item(photoI);

										final String id = photoElt.getAttribute("id");
										final String owner = photoElt.getAttribute("owner");
										final String secret = photoElt.getAttribute("secret");

										final String url = IOUtil.getURL(URL_BASE, "method", "flickr.photos.getInfo", "format", "rest", "photo_id", id, "secret", secret);
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

												StringBuffer sb = new StringBuffer();
												sb.append(id);
												sb.append("|").append(owner);

												//ownerlocation information
												elt = (Element) mainElt.getElementsByTagName("owner").item(0);
												sb.append("|").append(elt.getAttribute("location"));

												sb.append("|").append(secret);

												//date
												elt = (Element) mainElt.getElementsByTagName("dates").item(0);
												sb.append("|").append(elt.getAttribute("taken"));

												//get location information
												elt = (Element) mainElt.getElementsByTagName("location").item(0);
												sb.append("|").append(elt.getAttribute("latitude"));
												sb.append("|").append(elt.getAttribute("longitude"));
												sb.append("|").append(elt.getAttribute("accuracy"));

												sb.append("\n");

												String photoSt = sb.toString();
												System.out.println(photoSt);

												sch.append(photoSt);
											}
										});
									}
								}
							});
						}
					}
				});

			}
		}

		for(int i=0; i<Config.FLICKR_API_KEYS.length; i++)
			sch.launchExecutorAtFixedRate(1000, "&api_key=" + Config.FLICKR_API_KEYS[i], true);
	}

	//private static String getMonth(int month){ return (month>=10?"":"0")+month; }
	private static String getDateStamp(int year, int month){ return year+"-"+((month>=10?"":"0")+month); }

	//find user location with https://www.flickr.com/services/api/flickr.places.placesForUser.html
	public static void getUserInfo(String photoInfoFile, String path, String fileName){

		//initialise output file
		new File(path).mkdirs();
		try {
			if(!new File(path+fileName).exists()) Files.createFile(Paths.get(path+fileName));
		} catch (Exception e) { e.printStackTrace(); }

		//load collection of users already in the file
		HashSet<String> usersIdsDone = new HashSet<String>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(path+fileName));
			String line;
			while ((line = br.readLine()) != null) usersIdsDone.add(line.split("\\|")[0]);
			br.close();
		} catch (IOException e) { e.printStackTrace(); } finally { try { if (br != null)br.close(); } catch (Exception ex) { ex.printStackTrace(); } }

		System.out.println(usersIdsDone.size() + " users located.");

		//get list of users to find
		HashSet<String> usersIds = new HashSet<String>();
		try {
			br = new BufferedReader(new FileReader(photoInfoFile));
			String line;
			while ((line = br.readLine()) != null) {
				String userId = line.split("\\|")[1];
				if(usersIdsDone.contains(userId)) continue;
				usersIds.add(userId);
			}
			br.close();
		} catch (IOException e) { e.printStackTrace(); } finally { try { if (br != null)br.close(); } catch (Exception ex) { ex.printStackTrace(); } }

		System.out.println(usersIds.size() + " users to locate.");

		/*
		final ScrapingScheduler sch = new ScrapingScheduler(250, path, fileName, false);
		for(String userId : usersIds) {
			String url = 
			sch.add(QueryType.XML, url, new Function(){
				
			}
		}*/

		/*for(int i=0; i<Config.FLICKR_API_KEYS.length; i++)
			sch.launchExecutorAtFixedRate(1000, "&api_key=" + Config.FLICKR_API_KEYS[i], true);*/

	}

}
