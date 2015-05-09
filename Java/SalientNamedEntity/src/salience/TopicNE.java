package salience;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import Variables.Variables;
import salience.db.CognosDataset;
import salience.db.TweetIndexer;
import salience.pageRank.Node;
import utility.FileLoader;
import utility.Logger;

public class TopicNE {
	
	public static HashMap<String,Integer> NEcountMapMaster = new  HashMap<String, Integer>();//NE to numOfExperts who talked about the NE
	public static HashMap<String,Integer> NEadjacencyMapMaster = new  HashMap<String, Integer>();
	public static HashMap<String, ArrayList<String>> NEadjacencyListMap = new  HashMap<String, ArrayList<String>>();
	
	public static void main(String[] arg){
		TopicNE topicNE = new TopicNE();
		
		//NEcountMapMaster = topicNE.entriesSortedByValues(NEcountMapMaster);
		 //Logger.printOutMap(topicNE.prune(2));
		//topicNE.getAdjacentNE("cricketer");
		/*
		topicNE.getRepeatingNE("cricketer");
		HashMap<String,Integer> topicNEmap = prune(2);
		topicNE.getAdjacentNE("cricketer",topicNEmap); System.out.println("Adjacent pairs = "+NEadjacencyMapMaster.size());
		TweetIndexer tweetIndexer = new TweetIndexer();
		topicNE.generate(tweetIndexer.getExpertsOnTopic("cricketer").size(),topicNEmap);
		*/
		//FileLoader fileLoader = new FileLoader();
//		String repLabCategory = "automotive";//"university";//banking";
//		int topicRepetition = 2;
//		int expertCount = topicNE.getNEandAjacencyEval(repLabCategory, topicRepetition);
		String qTopic = "cricket";
		int expertCount = topicNE.getNEandAjacency(qTopic);
		System.out.println("FINAL SIZES :: NE size = "+NEcountMapMaster.size()+" Adj size = "+NEadjacencyMapMaster.size());
		HashMap<String,Integer> topicNEmap = prune(3);
		//Logger.storeNEMap(NEcountMapMaster);
		topicNE.generate(expertCount, topicNEmap);
//		ArrayList<String> equivalentCognosTopics = fileLoader.getCognosTopicsForReplab(repLabCategory);
//		System.out.println(repLabCategory+". Num of eq topics = "+equivalentCognosTopics.size());
//		int topicCount = 1;
//		for(String eqTopic : equivalentCognosTopics){
//			System.out.println(eqTopic);
//			topicNE.getRepeatingNE(eqTopic);
//			System.out.println("After prcessing "+topicCount+" size = "+NEcountMapMaster.size());
//			++topicCount;
//		}//for eqTopic 
	}//main

	/* Creates nodes and edges map. returns number of experts processed*/
	public int getNEandAjacency(String qTopic){
		int expertCount = 0;
		FileLoader fileLoader = new FileLoader();
		TweetIndexer tweetIndexer = new TweetIndexer();

		 //HashSet<String> equivalentCognosExperts = new HashSet<String>();	equivalentCognosExperts.add("ImIshant"); equivalentCognosExperts.add("DaleSteyn62");
		HashSet<String> equivalentCognosExperts = fileLoader.getMappingCognosExperts(qTopic);
		System.out.println(qTopic+". Num of cognos expersts = "+equivalentCognosExperts.size());
		int topicCount = 1;
		for(String expert : equivalentCognosExperts){//expertUser

			HashSet<String> NEset = tweetIndexer.getNEbyExpert1(expert);
			if(NEset.size()>0){
				HashSet<String> repeatNE = new HashSet<String>();

				/*Populate NEs into NEcountMapMaster*/
				for(String NEstr:NEset){
					int currentCount = 1;
					if(NEcountMapMaster.containsKey(NEstr)){
						repeatNE.add(NEstr);
						currentCount = currentCount + NEcountMapMaster.get(NEstr);
					}
					NEcountMapMaster.put(NEstr, currentCount);
				}//for NE
				System.out.println("Expert : "+expert+" Repetition :"+(1.0* repeatNE.size() / NEset.size()));

				/*Populate NEcombinations into NEAdjacencyMapMaster*/
				for(String NE1 : repeatNE){
					for(String NE2 : repeatNE){
						if(!NE1.equalsIgnoreCase(NE2)){
							String neCombo = NE1+"##"+NE2;
							int count = 1;
							if(NEadjacencyMapMaster.containsKey(neCombo)){
								count = count + NEadjacencyMapMaster.get(neCombo);
							}
							NEadjacencyMapMaster.put(neCombo, count);
						}							
					}//for NE2
				}//for NE1
				
			}//if
			++expertCount;
		}//for expert		
		System.out.println("After prcessing "+expertCount+" experts, NE size = "+NEcountMapMaster.size()+" Adj size = "+NEadjacencyMapMaster.size());
		//check AdjacencyMap size
//			if(NEadjacencyListMap.size()>10000){
//				if(Logger.flushAdjacencyMap( NEadjacencyListMap))
//					NEadjacencyListMap.clear();
//			}
//		Logger.flushAdjacencyMap( NEadjacencyListMap);//complete flush
		return expertCount;
	}//getNEandAdjacency

	
	/*Evaluation. Creates nodes and edges map. returns number of experts processed*/
	public int getNEandAjacencyEval(String repLabCategory,int topicRepetition){
		int expertCount = 0;
		FileLoader fileLoader = new FileLoader();
		TweetIndexer tweetIndexer = new TweetIndexer();
		//String repLabCategory = "university";//banking";
		ArrayList<String> equivalentCognosTopics = fileLoader.getCognosTopicsForReplab(repLabCategory,topicRepetition);
		System.out.println(repLabCategory+". Num of eq topics = "+equivalentCognosTopics.size());
		int topicCount = 1;
		for(String eqTopic : equivalentCognosTopics){
			System.out.println("Current Topic = "+eqTopic);
			List<String> experts = tweetIndexer.getExpertsOnTopic(eqTopic);
			expertCount = expertCount + experts.size();
			for(String expert : experts){//expertUser
				HashSet<String> NEset = tweetIndexer.getNEbyExpert1(expert);
				if(NEset.size()>0){
					HashSet<String> repeatNE = new HashSet<String>();

					/*Populate NEs into NEcountMapMaster*/
					for(String NEstr:NEset){
						int currentCount = 1;
						if(NEcountMapMaster.containsKey(NEstr)){
							repeatNE.add(NEstr);
							currentCount = currentCount + NEcountMapMaster.get(NEstr);
						}
						NEcountMapMaster.put(NEstr, currentCount);
					}//for NE
					System.out.println("Expert : "+expert+" Repetition :"+(1.0* repeatNE.size() / NEset.size()));

					/*Populate NEcombinations into NEAdjacencyMapMaster*/
					for(String NE1 : repeatNE){
						for(String NE2 : repeatNE){
							if(!NE1.equalsIgnoreCase(NE2)){
								String neCombo = NE1+"##"+NE2;
								int count = 1;
								if(NEadjacencyMapMaster.containsKey(neCombo)){
									count = count + NEadjacencyMapMaster.get(neCombo);
								}
								NEadjacencyMapMaster.put(neCombo, count);
							}							
						}//for NE2
					}//for NE1
					
				}//if
			}//for expert		
			System.out.println("After prcessing "+topicCount+" NE size = "+NEcountMapMaster.size()+" Adj size = "+NEadjacencyMapMaster.size());
			++topicCount;
			//check AdjacencyMap size
//			if(NEadjacencyListMap.size()>10000){
//				if(Logger.flushAdjacencyMap( NEadjacencyListMap))
//					NEadjacencyListMap.clear();
//			}
		}//for eqTopic 
//		Logger.flushAdjacencyMap( NEadjacencyListMap);//complete flush
		return expertCount;
	}//getNEandAdjacency
	
	/*Populate NEs into NEcountMapMaster*/
	public void getRepeatingNE(String topic){
		TweetIndexer tweetIndexer = new TweetIndexer();
		//CognosDataset cognosDataset = new CognosDataset();		
		int repeatNE = 0;
		for(String expert : tweetIndexer.getExpertsOnTopic(topic)){//expertUser
			HashSet<String> NEcount = tweetIndexer.getNEbyExpert1(expert);
			if(NEcount.size()>0){
				for(String NEstr:NEcount){
					int currentCount = 1;
					if(NEcountMapMaster.containsKey(NEstr)){
						++repeatNE;
						currentCount = currentCount + NEcountMapMaster.get(NEstr);
					}
					NEcountMapMaster.put(NEstr, currentCount);
				}//for
				System.out.println("Expert : "+expert+" Repetition :"+(1.0* repeatNE / NEcount.size()));
				repeatNE = 0;
			}//if
		}//for		
	}//getRepeatingNEs
	
	/*Populate NEcombinations into NEAdjacencyMapMaster
	 * Two NEs are adjacent if the same expert talks about both of them*/
	public void getAdjacentNE(String topic,HashMap<String,Integer> TopNEcountMap){
		TweetIndexer tweetIndexer = new TweetIndexer();
		for(String expert : tweetIndexer.getExpertsOnTopic(topic)){//expertUser
			HashSet<String> NEset = tweetIndexer.getNEbyExpert1(expert);
			for(Entry<String,Integer> oEntry:TopNEcountMap.entrySet()) {
				if(NEset.contains(oEntry.getKey())){
					for(Entry<String,Integer> iEntry : TopNEcountMap.entrySet()){
						if(!iEntry.getKey().equalsIgnoreCase(oEntry.getKey())){
							if(NEset.contains(iEntry.getKey())){							
								String neComb = oEntry.getKey()+"##"+iEntry.getKey();
								int currentCount = 1;
								if(NEadjacencyMapMaster.containsKey(neComb)){
									currentCount = currentCount + NEadjacencyMapMaster.get(neComb);
								}
								NEadjacencyMapMaster.put(neComb, currentCount);
							}//if i
						}//if not self
					}//for i					
				}//if o
			}//for o			
		}//for expert		
	}//getAdjacentNE
	
	
    private HashMap<String, Integer> entriesSortedByValues(Map<String,Integer> map) {

		List<Entry<String,Integer>> sortedEntries = new ArrayList<Entry<String,Integer>>(map.entrySet());
		
		Collections.sort(sortedEntries, new Comparator<Map.Entry<String,Integer>>() {
			@Override
			public int compare(Entry<String,Integer> o1, Entry<String,Integer> o2) {
				return - ((Map.Entry<String,Integer>) (o1)).getValue().compareTo(((Map.Entry<String,Integer>) (o2)).getValue());
			}
		});
		
	    HashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
	    for (Iterator<Entry<String,Integer>> it = sortedEntries.iterator(); it.hasNext();) {
	        Map.Entry<String,Integer> entry = it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }
		
		return result;
	}//entriesSortedByValues
	
    /*Chooses top most repeated NEs from NEcountMapMaster based on repetition */
	public static HashMap<String, Integer> prune(int lowestRepetionAllowed){
		HashMap<String, Integer> pruned = new HashMap<>();
		System.out.println(" initial "+ NEcountMapMaster.size());
		for(String ne: NEcountMapMaster.keySet()){
			if(NEcountMapMaster.get(ne) >= lowestRepetionAllowed){
				pruned.put(ne, NEcountMapMaster.get(ne));
			} 
		}//for
		System.out.println(" now "+ pruned.size());
		return pruned;
	}//End prune()


	/*Generate Adjacency matrix from the given NElist*/
	public static void generate(ArrayList<String> NE1){
		HashMap<String,Integer> topicNEmap = prune(2);
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
	
	/*Generate Adjacency matrix from the given NElist*/
	public static void generate(int numOfExperts, HashMap<String,Integer> topicNEmap){
		StringBuilder sb = new StringBuilder();
		for(String ne1 : topicNEmap.keySet()){ //System.out.println("NE "+i+" "+NEList.get(i));
			String neInPgRk = ne1.replaceAll(" ", "_");
			sb.append(neInPgRk).append(" ");//PageRank representation has space replaced with _
			for(String ne2 : topicNEmap.keySet()){
				
				if( NEadjacencyMapMaster.containsKey(ne1+"##"+ne2) ){
					double pmi_num = 1.0 * NEadjacencyMapMaster.get(ne1+"##"+ne2) * numOfExperts ; 
					double pmi_den = 1.0 * NEcountMapMaster.get(ne1) * NEcountMapMaster.get(ne2);
					double pmi = Math.log10(1.0 * pmi_num/pmi_den);
					sb.append(pmi).append(" ");  System.out.println(ne1+"##"+ne2+" "+pmi);//System.out.println(ne1+"##"+ne2+" num = "+pmi_num+" den = "+pmi_den+" pmi = "+pmi );
				} else if( NEadjacencyMapMaster.containsKey(ne2+"##"+ne1) ){
					double pmi_num = 1.0 * NEadjacencyMapMaster.get(ne2+"##"+ne1) * numOfExperts ;
					double pmi_den = 1.0 * NEcountMapMaster.get(ne1) * NEcountMapMaster.get(ne2);
					double pmi = Math.log10(1.0 * pmi_num/pmi_den);
					sb.append(pmi).append(" ");   System.out.println(ne1+"##"+ne2+" "+pmi);//System.out.println(ne2+"##"+ne1+" num = "+pmi_num+" den = "+pmi_den+" pmi = "+pmi);
				} else {
					sb.append("0 ");//System.out.print("0 ");
				}					
			}
			sb.append("\n"); //System.out.print("\n");
			//check AdjacencyMap size
			if(sb.length()>1000){
				if(Logger.solutionOut(sb))
					sb = new StringBuilder();
			}

		}
		Logger.solutionOut(sb);//System.out.println(sb.toString());
	}//generate


}//class
