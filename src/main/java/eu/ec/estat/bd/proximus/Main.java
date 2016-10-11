/**
 * 
 */
package eu.ec.estat.bd.proximus;

import java.util.HashMap;

import eu.ec.estat.bd.io.DicUtil;

/**
 * @author julien Gaffuri
 *
 */
public class Main {
	public static String BASE_PATH = "A:/geodata/";

	//compute Eurostat population dataset from geostat grid and compare with published one
	//TODO: get pop data at LAU level from fabio
	public static void validateEurostatGeostat(){
		//test consistency between estat NUTS3/LAU data and geostat grid

		//load estat pop data
		HashMap<String, String> estatData = DicUtil.load(BASE_PATH+"eurobase/BE_pop_nuts3.csv", ",");

		//load estat dataset
		//go through regions
		//get grid cell intersecting it
		//compute intersection
		//add cell data for each cell, depending on intersection area
		//produce csv file with data + difference
	}

	public static void getBuildingStatByGridCell() {
		//intersection between geostat grid and building layer

		//go through grid cells
		//get all buildings intersecting the grid
		//compute stat on buildings: total area/volume, number, building size distribution
		//compute also building density from pop data. Asumption: should be +/- constant.
		//export as csv file
	}

	public static void getPopulationGridFromMobilePhoneData() {

		//go through voronoi cells
		//get all grid cells intersecting
		//compute value of cell: naive approach (based on cell area) and with building stats + difference
		//export as csv

	}

	public static void main(String[] args) {

		validateEurostatGeostat();
		//getBuildingStatByGridCell();
		//getPopulationGridFromMobilePhoneData();

	}

}
