package eu.ec.estat.bd.geonames;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import eu.ec.estat.bd.photoorigin.flickrscraping.Owner;
import eu.ec.estat.bd.photoorigin.flickrscraping.Photo;

/**
 * @author julien gaffuri
 *
 */
public class SearchMapper extends Mapper<Text, Text, Text, Text> {

	@Override
	public void map(Text key, Text value, Context context) throws IOException, InterruptedException {
		//NB: key is the line offset within the file. It is ignored most of the time.

		String searchTerm = context.getConfiguration().get("searchTerm");

		String line = value.toString();
		//TODO parse line to retrieve location name
		//compute coherence name
		//https://en.wikipedia.org/wiki/Levenshtein_distance
		int score = 10;

		//if location name is coherent with searchTerm, write text with coherence score + line content (structured?)
		context.write(new Text(""+score), new Text(line));

	}


}
