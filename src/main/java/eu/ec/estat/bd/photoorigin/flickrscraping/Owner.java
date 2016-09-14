/**
 * 
 */
package eu.ec.estat.bd.photoorigin.flickrscraping;

import java.util.HashMap;

/**
 * @author julien Gaffuri
 *
 */
public class Owner {

	String id, locationText;
	double lon,lat;

	private final static HashMap<String,Owner> INDEX = new HashMap<String,Owner>();
	private Owner(){}

	public static Owner getOwner(String id){ return INDEX.get(id); }

	public static void setOwner(Photo p, String ownerId, String ownerlocationText){
		Owner ow = INDEX.get(ownerId);
		if(ow != null) { p.owner = ow; return; }
		ow = new Owner();
		ow.id = ownerId; ow.locationText = ownerlocationText;
		p.owner = ow;
		INDEX.put(ow.id, ow);
	}

	public static int getOwnerNumber(){ return INDEX.size(); }

	public static int getOwnerWithLocationTextNumber(){
		int nb=0;
		for(Owner ow : INDEX.values()) if(ow.locationText != null && !"".equals(ow.locationText)) nb++;
		return nb;
	}

}
