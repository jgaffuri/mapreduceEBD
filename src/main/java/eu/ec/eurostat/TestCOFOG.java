package eu.ec.eurostat;

import eu.ec.estat.java4eurostat.base.Selection;
import eu.ec.estat.java4eurostat.base.Stat;
import eu.ec.estat.java4eurostat.base.StatsHypercube;
import eu.ec.estat.java4eurostat.io.EurobaseIO;
import eu.ec.estat.java4eurostat.io.EurostatTSV;

public class TestCOFOG {

	public static void main(String[] args) {
		System.out.println("Start");

		//download/update data
		EurobaseIO.update("H:/eurobase/", "gov_10a_exp");

		//load data with negative values
		System.out.println("Loading...");
		StatsHypercube hcNeg = EurostatTSV.load("H:/eurobase/gov_10a_exp.tsv", new Selection.ValueLowerThan(0));

		System.out.println("Filtering...");
		hcNeg = hcNeg.selectDimValueEqualTo("na_item","TE"); hcNeg.delete("na_item");
		hcNeg = hcNeg.selectDimValueEqualTo("sector","S13"); hcNeg.delete("sector");
		hcNeg = hcNeg.selectDimValueEqualTo("unit","PC_GDP"); hcNeg.delete("unit");
		//hcNeg.printInfo();

		for(Stat s : hcNeg.stats)
			System.out.println(s);

		System.out.println("End");
	}

}
