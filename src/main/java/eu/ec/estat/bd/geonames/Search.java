package eu.ec.estat.bd.geonames;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/**
 * @author julien gaffuri
 *
 */
public class Search {

	public static void main(String[] args) throws Exception {

		//build job
		Configuration conf = new Configuration();
		conf.set("searchTerm", args[1]);
		Job job = Job.getInstance(conf);

		job.setJarByClass(Search.class);
		job.setJobName("Search in place position (based on geoname)");

		job.setMapperClass(SearchMapper.class);
		job.setCombinerClass(SearchReducer.class);
		job.setReducerClass(SearchReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		//set input path
		FileInputFormat.addInputPath(job, new Path(args[0]));

		//set output path
		Path outputPath = new Path(args[2]);
		FileSystem.get(conf).delete(outputPath, true);
		FileOutputFormat.setOutputPath(job, outputPath);

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}