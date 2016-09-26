package eu.ec.estat.bd.geonames;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * @author julien gaffuri
 *
 */
public class SearchReducer extends Reducer<Text, Text, Text, Text> {

	@Override
	public void reduce(Text score, Iterable<Text> lines, Context context) throws IOException, InterruptedException {

		//String searchTerm = context.getConfiguration().get("searchTerm");
		//merge info of mappers
		for (Text line : lines) context.write(score, line);
	}

}
