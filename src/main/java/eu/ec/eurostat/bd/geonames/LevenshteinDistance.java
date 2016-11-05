package eu.ec.eurostat.bd.geonames;

/**
 * @author Julien Gaffuri
 *
 */
public class LevenshteinDistance {

	//Levenshtein distance, from https://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
	public static int get(String lhs, String rhs) {
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


/*
	public static void main(String[] args) {
		System.out.println(get("senningerbergguygyugyugyugyuguyg","senningerberg"));
		System.out.println(get("senningerberg","senningerbergguygyugyugyugyuguyg"));
		System.out.println(get("senningerberg","aaa"));
		System.out.println(get("senningerberg","aaaaaa"));
		System.out.println(get("senningerberg","aaaaaaaaa"));
		System.out.println(get("senningerberg","aaaaaaaaaaaa"));

		System.out.println(get("senningerberg","senningerberg"));
		System.out.println(get("senningerberg","Senningerberg"));
		System.out.println(get("senningerberg","fdgbjdb"));
		System.out.println(get("senningerberg","aaaaaa"));
		System.out.println(get("senningerberg","aaaa"));
		System.out.println(get("senningerberg","sennin"));
		System.out.println(get("senningerberg","gerber"));
		System.out.println(get("senningerberg","senningerberg, luxembourg"));
		System.out.println(get("senningerberg","seningerberg"));
		System.out.println(get("senningerberg","sieningerberg"));
	}
*/
}
