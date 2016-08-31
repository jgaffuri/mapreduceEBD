/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

/**
 * @author Julien Gaffuri
 *
 */
public class PhotoSearch {
	private static final String URL_BASE = "https://api.flickr.com/services/rest/";
	private static final String perpage = "250";

	//the 
	private String urlQueryBase;
	private List<PhotoInfo> list;

	/**
	 * A photo search with some query parameters
	 * See https://www.flickr.com/services/api/flickr.photos.search.html
	 * 
	 * @param paramData
	 */
	public PhotoSearch(String... paramData){
		//build url template
		String[] paramData_ = (String[])ArrayUtils.addAll(paramData, new String[] {"api_key", Config.FLICKR_API_KEY, "method", "flickr.photos.search", "format", "rest", "content_type", "1", "has_geo", "1", "per_page", perpage});
		urlQueryBase = IOUtil.getURL(URL_BASE, paramData_);
	}


	/**
	 * @return The list of photos for the query
	 */
	public List<PhotoInfo> getList(){
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
	}

	/**
	 * Retrieve some information on the photo (with one query per second)
	 */
	private int i__=0;
	public void retrievePhotoInfo(){
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
				System.out.println(photo);
				i__++;
			}
		}, 0, 1, TimeUnit.SECONDS);

		/*int i=1;
		for(PhotoInfo photo : getList()){
			System.out.println((i++)+"/"+getList().size());
			photo.retrieveInfo();
			System.out.println(photo);
			try { TimeUnit.SECONDS.sleep(1); } catch(InterruptedException e) { Thread.currentThread().interrupt(); }
		}*/
	}

	/**
	 * Save the list.
	 * 
	 * @param path
	 * @param fileName
	 */
	public void save(String path, String fileName){
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
	}


	public void load(String filePath) {
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

	}
}
