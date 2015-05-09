package salience;

import java.util.ArrayList;

import salience.db.TweetIndexer;
import utility.Logger;

public class NEGraph {

	public static void main(String[] arg){
		TweetIndexer tweetIndexer = new TweetIndexer();
		
		String expert = "KP24";
		//ArrayList<String> NE1 = new ArrayList(tweetIndexer.getNEbyExpert(expert));
				
		expert = "ShaneWarne";
		ArrayList<String> NE2 = new ArrayList(tweetIndexer.getNEbyExpert(expert));
		
		generate(NE2);
	}//main
	
	/*Generate Adjacency matrix from the given NElist*/
	public static void generate(ArrayList<String> NE1){
		//split into NElist and AdjacencyList
		ArrayList<String> NEList = new ArrayList();
		ArrayList<String> AdjList = new ArrayList();
		for(String s : NE1){
			if (s.contains("##")){
				AdjList.add(s);
			} else {
				NEList.add(s);
			}//if
		}//for
		System.out.println("Num of pairs = "+AdjList.size()); System.out.println("Num of comb NEs = "+NEList.size());
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<NEList.size(); i++){ //System.out.println("NE "+i+" "+NEList.get(i));
			for(int j=0; j<NEList.size();j++){
				
				if(AdjList.contains(NEList.get(i)+"##"+NEList.get(j))){
					sb.append("1 ");//System.out.print("1 ");
				} else {
					sb.append("0 ");//System.out.print("0 ");
				}
				
			}
			sb.append("\n"); //System.out.print("\n");
		}
		Logger.solutionOut(sb);System.out.println(sb.toString());
	}//generate
}
