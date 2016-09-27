package eu.ec.estat.bd.geonames;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * @author julien gaffuri
 *
 */
public class SearchReducer extends Reducer<IntWritable, Text, IntWritable, Text> {

	@Override
	public void reduce(IntWritable key, Iterable<Text> scoredLines, Context context) throws IOException, InterruptedException {

		int nb = Integer.parseInt(context.getConfiguration().get("nb"));

		//sort scoredlines
		ArrayList<String> scoredLines_ = new ArrayList<String>();
		
		
		//merge info of mappers
		/*for (IntWritable score : scores) {
			context.write(line, score);
		}*/
	}

}
