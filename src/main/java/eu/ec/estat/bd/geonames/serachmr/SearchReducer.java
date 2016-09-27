package eu.ec.estat.bd.geonames.serachmr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

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

		ArrayList<String> scoredLines_ = new ArrayList<String>();
		for (Text scoredLine : scoredLines) scoredLines_.add(scoredLine.toString());

		//sort scoredlines
		scoredLines_.sort(new Comparator<String>(){
			public int compare(String s1, String s2) { return getScore(s1) - getScore(s2); }
		});

		int nb = Integer.parseInt(context.getConfiguration().get("nb"));
		int scoreRet = 999;
		if(scoredLines_.size() >= nb) scoreRet = getScore(scoredLines_.get(nb-1));

		//merge info of mappers
		for (String scoredLine : scoredLines_) {
			int score = getScore(scoredLine);
			if(score > scoreRet) break;
			context.write(key, new Text(scoredLine));
		}
	}

	private static int getScore(String line){
		return Integer.parseInt(line.split("\t", -1)[0]);
	}

}
