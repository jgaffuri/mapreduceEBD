package eu.ec.estat.bd.test;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * @author julien gaffuri
 *
 */
public class TestMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

	@Override
	public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
		//NB: key is the line offset within the file. It is ignored most of the time.

		//count the number of some letters
		String[] letters = new String[]{"o","n","a","r"};

		String line = value.toString();
		for(String letter : letters){
			int count = line.length() - line.replace(letter, "").length();
			context.write(new Text(letter), new IntWritable(count));
		}

	}

}
