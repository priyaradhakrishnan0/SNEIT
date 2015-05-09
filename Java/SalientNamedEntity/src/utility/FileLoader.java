package utility;

import java.awt.List;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Pattern;

import Variables.Variables;

public class FileLoader {
	public static HashMap<String, Double>PageRankMap = new HashMap<String, Double>();

	public void clearPgrk(){
		this.PageRankMap.clear();
	}
	
	public static void main(String[] args){
		FileLoader fileLoader = new FileLoader();
//		HashMap<String, String>  entities =fileLoader.loadReplabEntities();
//		System.out.println(entities.get("BBVA"));
//		fileLoader.loadPageRankScale("barclays");
//		System.out.println("pagerank value = "+PageRankMap.get("Paton"));
//		System.out.println(fileLoader.getCognosTopic("extra3"));
//		int exactMatchingCognosTopic = 0;
//		ArrayList<String> equalTopics = new ArrayList<String>();
//		for(String query : fileLoader.loadQueries("music")){
//			//query = query.replace(" ", "_");
//			if(fileLoader.isCognosTopic(query)){
//				exactMatchingCognosTopic++;
//				equalTopics.add(query);
//				System.out.println(query);
//			}
//		}
//		System.out.println("Exact matches = "+exactMatchingCognosTopic+"  "+equalTopics);
		//System.out.println("Size = "+fileLoader.loadCognosTopics().size());
		//System.out.println(fileLoader.loadQueries("banking"));
		//System.out.println(fileLoader.isCognosTopic("cricketer"));
		//System.out.println("Loaded file size = "+fileLoader.loadNEAdjacencyListMap().size());
		//HashMap<String, HashMap<String, Boolean>> replabGold = fileLoader.loadReplabGold();
		//System.out.println(replabGold.size());
		//for(String entityId : replabGold.keySet())System.out.println(entityId + "->"+replabGold.get(entityId));
		//System.out.println(replabGold.get("RL2013D04E151"));
		//System.out.println(fileLoader.loadCognosExperts("barclays").size());
		//fileLoader.loadScaleTweets("cricket.rep1");
		String topic = "cricket.old";
		fileLoader.loadPageRank(topic);
		System.out.println(topic + "--" +PageRankMap.size());
		PageRankMap.clear();
		topic = "cricket.rep1";
		fileLoader.loadPageRank(topic);
		System.out.println(topic + "--" +PageRankMap.size());
		PageRankMap.clear();
		topic = "cricket.rep2";
		fileLoader.loadPageRank(topic);
		System.out.println(topic +"--"+ PageRankMap.size());
		PageRankMap.clear();
		topic = "cricket.rep3";
		fileLoader.loadPageRank(topic);
		System.out.println(topic+"--"+PageRankMap.size());
		PageRankMap.clear();
		topic = "cricket.rep4";
		fileLoader.loadPageRank(topic);
		System.out.println(topic +"--"+PageRankMap.size());
		PageRankMap.clear();
		topic = "cricket.rep5";
		fileLoader.loadPageRank(topic);
		System.out.println(topic +"--"+PageRankMap.size());
		PageRankMap.clear();		
//		HashMap<String,Double> PageRank = fileLoader.readPageRank("cricket.old");
//		String[] names = new String[]{"Henry","Dravid","Sachin"};
//		for(String name: names){
//			if(PageRank.containsKey(name))
//				System.out.println("Found "+name);
//		}
		//fileLoader.getMappingCognosExperts("cricket");
		
		
		
	}//end main

	/*read ReplabEntities*/
	public static HashMap<String, String> loadReplabEntities(){
		HashMap<String, String> replabEntities = new HashMap<String, String>(); 
		String disamFile = Variables.LibDir.concat(Variables.repLabEntities); 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine = br.readLine(); //"entity_id"     "query" "entity_name" "category" "homepage" "wikipedia_en"  "wikipedia_es"  "md5_homepage"  "md5_wikipedia_en"      "md5_wikipedia_es"
			while ((sCurrentLine = br.readLine()) != null) {
				Scanner s = new Scanner(sCurrentLine).useDelimiter("\"\\s*\""); 
				String entityId = s.next(); entityId = entityId.replace('"',' '); entityId = entityId.trim(); 
				/*query*/s.next(); s.next();s.next(); s.next(); 
				String wiki_title = s.next().replace("http://en.wikipedia.org/wiki/", "");
				replabEntities.put(entityId, wiki_title.trim()); //System.out.println(entityId +" -> "+wiki_title);
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return replabEntities;
	}//End read ReplabEntities
	
	/*read ReplabGold*/
	public static HashMap<String, HashMap<String, Boolean>> loadReplabGold(){
		HashMap<String, HashMap<String, Boolean>> replabGold = new HashMap<String, HashMap<String, Boolean>>();		
		String disamFile = Variables.LibDir.concat(Variables.repLabGold); 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine = br.readLine(); //"entity_id"	"tweet_id"	"filtering"
			while ((sCurrentLine = br.readLine()) != null) {
				Scanner s = new Scanner(sCurrentLine).useDelimiter("\"\\s*\"");
				String entityId = s.next().replace('"',' ');
				entityId = entityId.trim();				
				String tweetId = s.next().trim(); //System.out.println(entityId+" -- "+tweetId);
				boolean related = (s.next().startsWith("RE") ? true: false);
				if(replabGold.containsKey(entityId)){
					replabGold.get(entityId).put(tweetId, related);
				} else {
					HashMap<String, Boolean> tweetRelunrelMap = new HashMap<String, Boolean>(); 
					tweetRelunrelMap.put(tweetId, related);
					replabGold.put(entityId, tweetRelunrelMap);
				}
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return replabGold;
	}//End read ReplabGold

	/*read ReplabGold*/
	public static void printReplabGold(String repLabId){
		//HashMap<String, HashMap<String, Boolean>> replabGold = new HashMap<String, HashMap<String, Boolean>>();		
		String disamFile = Variables.LibDir.concat(Variables.repLabGold); 	System.out.println("Loading file ="+disamFile);
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine = br.readLine(); //"entity_id"	"tweet_id"	"filtering"
			while ((sCurrentLine = br.readLine()) != null) {
				Scanner s = new Scanner(sCurrentLine).useDelimiter("\"\\s*\"");
				String entityId = s.next().replace('"',' ');
				entityId = entityId.trim();				
				if(entityId.equalsIgnoreCase(repLabId)){
					sb.append(sCurrentLine).append("\n");
				}
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		utility.Logger.ReplabGoldOut(sb);
	}//End print ReplabGold

	/*read ReplabGold*/
	public HashMap<String, Boolean> loadReplabGold(String repLabId){
		HashMap<String, Boolean> replabGold = new HashMap<String, Boolean>();		
		String disamFile = Variables.LibDir.concat(Variables.repLabGold); 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine = br.readLine(); //"entity_id"	"tweet_id"	"filtering"
			while ((sCurrentLine = br.readLine()) != null) {
				Scanner s = new Scanner(sCurrentLine).useDelimiter("\"\\s*\"");
				String entityId = s.next().replace('"',' ');
				entityId = entityId.trim();
				String tweetId = s.next().trim(); //System.out.println(entityId+" -- "+tweetId);
				boolean related = (s.next().startsWith("RE") ? true: false);
				if(repLabId.equalsIgnoreCase(entityId)){
					replabGold.put(tweetId, related);
				}				
			}//while			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return replabGold;
	}//End load ReplabGold

	
	/*read existingUsers*/
	public HashSet<String> loadExistingUsers(){
		HashSet<String> existingUsers = new HashSet<String>(); 
		String disamFile = Variables.OutputDir.concat("existingUsers"); 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine;		 
			while((sCurrentLine = br.readLine()) != null) {
				sCurrentLine = sCurrentLine.replace(']', ' ');
				existingUsers.add(sCurrentLine.trim());//System.out.println(sCurrentLine);
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return existingUsers;
	}//End readExistingUsers

	/*read pgRank.topic*/
	public void loadPageRank(String topic){
		String disamFile = Variables.PgrkDir.concat(topic); 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine = br.readLine();		 
			if(sCurrentLine != null) { //System.out.println(sCurrentLine);
				sCurrentLine = sCurrentLine.replaceAll("\\}", "").replaceAll("\\{", "").replaceAll("u'", "").replaceAll("'", "");
				String[] NEstrips = sCurrentLine.split(",");
				for(String NEstrip : NEstrips){
					
					String[] splits = NEstrip.trim().split(":");
					if(splits.length==2){
						PageRankMap.put(splits[0], Double.valueOf(splits[1]));
					}
				}
				System.out.println(" Size of PageRank loaded = "+PageRankMap.size());
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
	}//End loadPagerank
	
	/*read topic.tweets*/
	public HashMap<String, String> loadScaleTweets(String topic){
		HashMap<String,String> tweetList = new HashMap<String,String>();
		String disamFile = Variables.scDir.concat(topic).concat(".tweets"); 	System.out.println("Loading file ="+disamFile);
		try {
			File fileDir = new File(disamFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileDir), "UTF-8"));

			String sCurrentLine = br.readLine();		 
			while((sCurrentLine  = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.split(" ");
				String tweetId = splits[0];
				//if(Pattern.matches("[a-zA-Z]+", tweetId) == false && tweetId.length() > 2){
				if(Pattern.matches("[0-9]+", tweetId)  && tweetId.length() ==18){
					String tweet = sCurrentLine.replace(tweetId, ""); //System.out.println(tweetId);
					tweet = tweet.replaceAll("\\s+", " ");
					tweetList.put(tweetId, tweet); //System.out.println(" Size of Scaling tweets = "+tweetList.size());
				}
			}	
			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return tweetList;
	}//End loadScaleTweets
	
	/*read pgRank.topic*/
	public void loadPageRankScale(String topic){
		String disamFile = Variables.scDir.concat(topic).concat(".pgrk"); 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine = br.readLine();		 
			if(sCurrentLine != null) { //System.out.println(sCurrentLine);
				sCurrentLine = sCurrentLine.replaceAll("\\}", "").replaceAll("\\{", "").replaceAll("u'", "").replaceAll("'", "").replaceAll("u\"", "").replaceAll("\"", "");
				String[] NEstrips = sCurrentLine.split(",");
				for(String NEstrip : NEstrips){
					
					String[] splits = NEstrip.trim().split(":");
					if(splits.length==2){ //System.out.println(splits[0]+"->"+Double.parseDouble(splits[1].trim()));
						PageRankMap.put(splits[0].trim(), Double.parseDouble(splits[1].trim()));
					}
				}
				System.out.println(" Size of pgrk read = "+PageRankMap.size());
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}

	}//End loadPagerankSc

	
	public ArrayList<String> getCognosTopicsForReplab(String repLabCategory, int repetitionFactor){
		HashMap<String, Integer> topicRepetitionMap = new  HashMap<String, Integer>();
		HashMap<String, HashSet<String>> expertTopicSetMap = getCognosTopic(loadQueries(repLabCategory));
		for(String expert : expertTopicSetMap.keySet()){				
			for(String topic: expertTopicSetMap.get(expert)){
				int count = 1;
				if(topicRepetitionMap.containsKey(topic)){
					count = topicRepetitionMap.get(topic) + count;
				}
				topicRepetitionMap.put(topic, count);
			}//for
		}//for
		return prune(topicRepetitionMap, repetitionFactor); //TODO : Tunable parameter = number of cognos repeating categories in a replab category topic NE. 2-automobiles and music, 1-banking and university
	}//getCognostopicsForReplab
	
	/*read Cognos topics*/
	public ArrayList<String> loadCognosTopics(){
		ArrayList<String> topics = new ArrayList<String>();
		String disamFile = Variables.LibDir+Variables.cognosTopics; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine;			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				topics.add(sCurrentLine.trim());
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return topics;
	}//End loadCognosTopics()

	/*Append Cognos experts to topic-expert.txt*/
	public static boolean storeCognosExpert(String topic, HashSet<String> experts) {
    	boolean writting = false;
		try {
			File file = new File(Variables.LibDir+Variables.cognosExperts);
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);
			String content = topic + experts.toString()+'\n'; System.out.println(content);
			if(content.length()>0){
				writting = true;
			}
			bw.write(content);
			bw.close(); //System.out.println("Done");
 		} catch (IOException e) {
			e.printStackTrace();
		}
		return writting;
    }//End storeCognosExpert
	
	/*read Cognos experts*/
	public ArrayList<String> loadCognosExperts(String topic){
		ArrayList<String> experts = new ArrayList<String>();
		String disamFile = Variables.LibDir+Variables.cognosExperts; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("\\[");
				String topicString = splits[0];
				if(topicString.equalsIgnoreCase(topic)){
					String expertString = splits[1].replace("]", "");
					for(String expert : expertString.split(",")){
						experts.add(expert);
					}
					break;
				}
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return experts;
	}//End loadCognosExperts(topic)

	/*Check if this is a Cognos topic */
	public boolean isCognosTopic(String qtopic){
		boolean foundTopic = false;
		String disamFile = Variables.LibDir+Variables.cognosExperts; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("\\[");
				String topicString = splits[0];
				if(topicString.trim().toLowerCase().equalsIgnoreCase(qtopic.toLowerCase())){
					foundTopic = true; System.out.println(topicString);
					break;
				}
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return foundTopic;
	}//End isCognosTopic
	
	/*Find Cognos topic of a expert*/
	public HashSet<String> getCognosTopic(String qExpert){
		HashSet<String> topics = new HashSet<String>();
		String disamFile = Variables.LibDir+Variables.cognosExperts; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("\\[");
				String topicString = splits[0];
				String expertString = splits[1].replace("]", ""); 
				for(String expert : expertString.split(",")){//System.out.println(topicString+" "+expert);
					if(expert.contains(qExpert)){						
						topics.add(topicString);
					}
				}
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return topics;
	}//End getCognosTopic

	/*Find Cognos topic of experts*/
	public HashMap<String, HashSet<String>> getCognosTopic(ArrayList<String> qExperts){
		HashMap<String, HashSet<String>> expertTopicsetMap = new HashMap<String, HashSet<String>>(); 
		
		String disamFile = Variables.LibDir+Variables.cognosExperts; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("\\[");
				String topicString = splits[0];
				String expertString = splits[1].replace("]", ""); 
				for(String expert : expertString.split(",")){//System.out.println(topicString+" "+expert);
					for(String qExpert : qExperts){
						if(expert.contains(qExpert)){
							HashSet<String> topics = new HashSet<String>();
							if(expertTopicsetMap.containsKey(qExpert)){
								topics = expertTopicsetMap.get(qExpert);
							}
							topics.add(topicString);
							expertTopicsetMap.put(qExpert, topics);
						}
					}
				}
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return expertTopicsetMap;
	}//End getCognosTopic

	/*Find Cognos experts mapping to give topic*/
	public  HashSet<String> getMappingCognosExperts(String qTopic){
		HashSet<String> cognosExpertSet = new HashSet<String>(); 
		
		String disamFile = Variables.LibDir+Variables.cognosExperts; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("\\[");
				String topicString = splits[0].trim();
				String expertString = splits[1].replace("]", "");
				String[] experts = expertString.split(",");
				if(topicString.contains(qTopic)){
					for(String expert: experts){
						cognosExpertSet.add(expert.trim());	
					}
					break;
				}
				for(String expert : experts){//System.out.println(topicString+" "+expert);
					if(expert.contains(qTopic)){
						cognosExpertSet.add(expert.trim());						
					}
				}
			}//while
			System.out.println("Mapping cognos experts = "+cognosExpertSet.size());
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return cognosExpertSet;
	}//End getMappingCognosExperts


	
	/*read adjacency links*/
	public HashMap<String, Integer> loadAdjacencyMap(){
		
		HashMap<String, Integer> adjLinkMap = new HashMap<String, Integer>();
		String disamFile = Variables.OutputDir+Variables.adjacencyMap; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("=");
				String neCombo = splits[0].trim();
				Integer occurances = Integer.getInteger( splits[1].trim());
				adjLinkMap.put(neCombo, occurances);
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return adjLinkMap;
	}//End loadAdjacencyMap()

	/*read NEmap*/
	public HashMap<String, Integer> loadNEMap(){
		
		HashMap<String, Integer> adjLinkMap = new HashMap<String, Integer>();
		String disamFile = Variables.OutputDir+Variables.neMap; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("=");
				String ne = splits[0].trim();
				Integer occurances = Integer.getInteger( splits[1].trim());
				adjLinkMap.put(ne, occurances);
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return adjLinkMap;
	}//End loadneMap
	
	/*read AdjListmap*/
	public void loadNEAdjacencyListMap_R(){
		
		ArrayList<String> adjacentNEs = new ArrayList<String>();
		String disamFile = Variables.OutputDir+Variables.adjacencyMap; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String currentLine; 			 
			while ((currentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = currentLine.trim().split("=");
				String ne = splits[0].trim();
				adjacentNEs.add(ne);
			}
			br.close();
			System.out.println("Loaded size ="+adjacentNEs.size());

			BufferedReader br2 = new BufferedReader(new FileReader(disamFile));
			File file = new File(Variables.LibDir+Variables.adjacencyMapR);
			FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);//open in append mode
			BufferedWriter bw = new BufferedWriter(fw);

			String sCurrentLine; 			 
			while ((sCurrentLine = br2.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("=");

				String adjList = splits[1].trim().replace("[\\[]", "").replace("[\\]]", "");
				StringBuilder content = new StringBuilder();
				for(String adj : adjList.split(",")){
					content.append(adjacentNEs.indexOf(adj)).append(" ");
				}
				bw.write(content.toString());
			}			
			bw.close(); //System.out.println("Done");
			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}

	}//End loadadjList_R
	

	/*read AdjListmap*/
	public HashMap<String, ArrayList<String>> loadNEAdjacencyListMap(){
		ArrayList<String> adjacentNEs = new ArrayList<String>();
		HashMap<String, ArrayList<String>> adjLinkMap = new HashMap<String, ArrayList<String>>();
		String disamFile = Variables.OutputDir+Variables.adjacencyMap; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("=");
				String ne = splits[0].trim();
				String adjList = splits[1].trim().replace("[\\[]", "").replace("[\\]]", "");
				for(String adj : adjList.split(",")){
					adjacentNEs.add(adj);
				}
				adjLinkMap.put(ne, adjacentNEs);
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return adjLinkMap;
	}//End loadadjList
	
	
	
	/*read Cognos experts*/
	public HashMap<String, ArrayList<String>> loadCognosExperts(){
		ArrayList<String> experts = new ArrayList<String>();
		HashMap<String, ArrayList<String>> topicExpertlistMap = new HashMap<String, ArrayList<String>>();
		String disamFile = Variables.LibDir+Variables.cognosExperts; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) { //System.out.println(sCurrentLine);
				String[] splits = sCurrentLine.trim().split("[\\[]");
				String topic = splits[0].trim();
				String expertString = splits[1].trim().replace("[\\]]", "");
				for(String expert : expertString.split(",")){
					experts.add(expert.trim());
				}
				topicExpertlistMap.put(topic, experts);
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return topicExpertlistMap;
	}//End loadCognosExperts()
	
	/*calculate statistics*/
	public void getNEstatats(){
		String disamFile = "/home/priya/Desktop/SalientNE/Output/stat"; 	System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine; 			 
			while ((sCurrentLine = br.readLine()) != null) {				
				String[] expStr = sCurrentLine.trim().split("NEbyexperts = ");
				String exp = expStr[1].replace("[\\[]", "");
				exp = exp.replace("[\\]]", ""); //System.out.println(exp);
				String[] NEs = exp.split(",");
				System.out.println(expStr[0]+" NEbyexperts = "+NEs.length);
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
	}//End getNEstatats()

	
	/*read repLab entities.tsv, Return query words for a domain*/
	public ArrayList<String> loadQueries(String domain){
		ArrayList<String> queries = new ArrayList<String>();
		String disamFile = Variables.LibDir+Variables.repLabEntities; 	//System.out.println("Loading file ="+disamFile);
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String sCurrentLine = br.readLine(); //"entity_id"     "query" "entity_name"   "category"      "homepage"      "wikipedia_en"  "wikipedia_es"  "md5_homepage"  "md5_wikipedia_en"      "md5_wikipedia_es"
			while ((sCurrentLine = br.readLine()) != null) {
				Scanner s = new Scanner(sCurrentLine).useDelimiter("\"\\s*\""); 
				s.next(); String query = s.next();
				s.next(); String category = s.next();
				if(category.equalsIgnoreCase(domain)){
					queries.add(query);//System.out.println(query +"  "+ category);
				}
			}			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
		return queries;
	}//End loadQueries(topic)
	
    /*Chooses top most repeated NEs from NEcountMapMaster based on repetition */
	public static ArrayList<String> prune(HashMap<String, Integer>topicMap, int lowestRepetionAllowed){
		ArrayList<String> pruned = new ArrayList<String>();
		System.out.println(" initial "+ topicMap.size());
		for(String topic:topicMap.keySet()){
			if(topicMap.get(topic) >= lowestRepetionAllowed){
				pruned.add(topic);
			} 
		}//for
		System.out.println(" now "+ pruned.size());
		return pruned;
	}//End prune()
	
}//class
