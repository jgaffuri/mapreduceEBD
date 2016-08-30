/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang.ArrayUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.io.Files;

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

	private String url_;
	private Collection<PhotoInfo> list;

	public PhotoSearch(String... paramData){
		//build url template
		String[] paramData_ = (String[])ArrayUtils.addAll(paramData, new String[] {"api_key", Config.FLICKR_API_KEY, "method", "flickr.photos.search", "format", "rest", "content_type", "1", "has_geo", "1", "per_page", perpage});
		url_ = IOUtil.getURL(URL_BASE, paramData_);
	}


	public Collection<PhotoInfo> getList(){
		if(list == null) {
			list = new HashSet<PhotoInfo>();

			int pages = 1;
			for(int page=1; page<=pages; page++){

				String url = url_ + "&page="+page;
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
					list.add( new PhotoInfo(photoElt.getAttribute("id"), photoElt.getAttribute("owner"), photoElt.getAttribute("secret")) );
				}
			}
		}
		return list;
	}
	
	public void save(String path, String fileName){
		
	}
}
