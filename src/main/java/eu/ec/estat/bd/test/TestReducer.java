/**
 * 
 */
package eu.ec.estat.bd.test;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/**
 * @author julien gaffuri
 *
 */
public class TestReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

	@Override
	public void reduce(Text letter, Iterable<IntWritable> counts, Context context) throws IOException, InterruptedException {

		//sum the counts of the mappers
		int sCounts = 0;
		for (IntWritable count : counts)
			sCounts += count.get();

		context.write(letter, new IntWritable(sCounts));
	}

}
