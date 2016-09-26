package eu.ec.estat.bd.geonames;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

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


	/*
	public static void main(String[] args) {
		System.out.println(levenshteinDistance("senningerberg","senningerberg"));
		System.out.println(levenshteinDistance("senningerberg","Senningerberg"));
		System.out.println(levenshteinDistance("senningerberg","fdgbjdb"));
		System.out.println(levenshteinDistance("senningerberg","aaaaaa"));
		System.out.println(levenshteinDistance("senningerberg","aaaa"));
		System.out.println(levenshteinDistance("senningerberg","sennin"));
		System.out.println(levenshteinDistance("senningerberg","gerber"));
		System.out.println(levenshteinDistance("senningerberg","senningerberg, luxembourg"));
		System.out.println(levenshteinDistance("senningerberg","seningerberg"));
		System.out.println(levenshteinDistance("senningerberg","sieningerberg"));
	}*/


	//Levenshtein distance, from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
	private static int levenshteinDistance(String lhs, String rhs) {
		int len0 = lhs.length() + 1;                                                     
		int len1 = rhs.length() + 1;                                                     

		// the array of distances                                                       
		int[] cost = new int[len0];                                                     
		int[] newcost = new int[len0];                                                  

		// initial cost of skipping prefix in String s0                                 
		for (int i = 0; i < len0; i++) cost[i] = i;                                     

		// dynamically computing the array of distances                                  

		// transformation cost for each letter in s1                                    
		for (int j = 1; j < len1; j++) {                                                
			// initial cost of skipping prefix in String s1                             
			newcost[0] = j;                                                             

			// transformation cost for each letter in s0                                
			for(int i = 1; i < len0; i++) {                                             
				// matching current letters in both strings                             
				int match = (lhs.charAt(i - 1) == rhs.charAt(j - 1)) ? 0 : 1;             

				// computing cost for each transformation                               
				int cost_replace = cost[i - 1] + match;                                 
				int cost_insert  = cost[i] + 1;                                         
				int cost_delete  = newcost[i - 1] + 1;                                  

				// keep minimum cost                                                    
				newcost[i] = Math.min(Math.min(cost_insert, cost_delete), cost_replace);
			}                                                                           

			// swap cost/newcost arrays                                                 
			int[] swap = cost; cost = newcost; newcost = swap;                          
		}                                                                               

		// the distance is the cost for transforming all letters in both strings        
		return cost[len0 - 1];                                                          
	}

}
