package eu.ec.estat.bd.io;

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

/**
 * @author julien Gaffuri
 *
 */
public class XML {

	public static Document parseXMLfromURL(String urlString){
		try{
			InputStream in = new URL(urlString).openConnection().getInputStream();
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
			in.close();
			return doc;
		}
		catch(Exception e){
			e.printStackTrace();
		}       
		return null;
	}

}
