/**
 * 
 */
package eu.ec.eurostat.bd.photoorigin.flickrscraping;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author julien Gaffuri
 *
 */
public class Owner implements Comparable<Owner>{

	String id, locationText;
	double lon,lat;
	HashSet<Photo> photos = new HashSet<Photo>();

	public boolean hasLocationText(){ return this.locationText != null && !"".equals(this.locationText); }

	private final static HashMap<String,Owner> INDEX = new HashMap<String,Owner>();
	private Owner(){}

	public static Owner getOwner(String id){ return INDEX.get(id); }
	public static Collection<Owner> get(){ return INDEX.values(); }

	public static void setOwner(Photo p, String ownerId, String ownerlocationText){
		Owner ow = INDEX.get(ownerId);
		if(ow == null) {
			ow = new Owner();
			ow.id = ownerId; ow.locationText = ownerlocationText;
			INDEX.put(ow.id, ow);
		}
		p.owner=ow; ow.photos.add(p);
	}

	public static int getOwnerWithLocationTextNumber(){
		int nb=0;
		for(Owner ow : get()) if(ow.hasLocationText()) nb++;
		return nb;
	}
	public static void printLocationTexts(PrintStream out){
		for(Owner ow : get()) if(ow.hasLocationText()) out.println(ow.locationText);
	}

	public static void printOwnerInfos(PrintStream out){
		ArrayList<Owner> list = new ArrayList<Owner>(); list.addAll(get());
		Collections.sort(list);
		for(Owner ow : list) out.println(ow.photos.size()+","+ow.id+","+ow.locationText);
	}
	public int compareTo(Owner ow) { return this.photos.size() - ow.photos.size(); }


	public static HashSet<String> getOwnerLocationText() {
		HashSet<String> lts = new HashSet<String>();
		for(Owner ow : get()) lts.add(ow.locationText);
		return lts;
	}

}
