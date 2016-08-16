/**
 * 
 */
package eu.ec.estat.bd.test;

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

		//Job job = new Job();
		Job job = Job.getInstance();
		job.setJarByClass(Test.class);
		job.setJobName("Count letters");

		FileInputFormat.addInputPath(job, new Path("/user/gaffuju/test/test.txt"));
		FileOutputFormat.setOutputPath(job, new Path("/user/gaffuju/test/output.txt"));

		job.setMapperClass(TestMapper.class);
		//job.setCombinerClass(TestReducer.class);
		job.setReducerClass(TestReducer.class);

		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);

		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}
