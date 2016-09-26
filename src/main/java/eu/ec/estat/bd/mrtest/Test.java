/**
 * 
 */
package eu.ec.estat.bd.mrtest;

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
public class Test {

	public static void main(String[] args) throws Exception {

		//build job
		Configuration conf = new Configuration();
		Job job = Job.getInstance(conf);
		job.setJarByClass(Test.class);
		job.setJobName("Count letters");

		job.setMapperClass(TestMapper.class);
		job.setCombinerClass(TestReducer.class);
		job.setReducerClass(TestReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		//set input path
		FileInputFormat.addInputPath(job, new Path("/user/gaffuju/test/test.txt"));

		//azure
		//wasbs:///example/data/gutenberg/davinci.txt
		//wasbs:///example/data/wordcountout
		
		//set output path
		Path outputPath = new Path("/user/gaffuju/test/output");
		FileSystem.get(conf).delete(outputPath, true);
		FileOutputFormat.setOutputPath(job, outputPath);

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}
