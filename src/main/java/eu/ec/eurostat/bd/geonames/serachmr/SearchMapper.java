package eu.ec.eurostat.bd.geonames.serachmr;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import eu.ec.eurostat.bd.geonames.GeoNameEntry;
import eu.europa.ec.eurostat.jgiscotools.algo.matching.LevenshteinMatching;

/**
 * @author julien gaffuri
 *
 */
public class SearchMapper extends Mapper<LongWritable, Text, IntWritable, Text> {

	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		//NB: key is the line offset within the file. It is ignored most of the time.

		String searchTerm = context.getConfiguration().get("searchTerm");

		String line = value.toString();
		ArrayList<String> names = new GeoNameEntry(line).getNames(true);
		int dist = 99999;
		for(String name : names)
			dist = Math.min(dist, LevenshteinMatching.getLevenshteinDistance(searchTerm, name, true, true, true, true));

		if(dist>10) return;
		//TODO

		context.write(new IntWritable(0), new Text(dist+"\t"+line));

	}

}
