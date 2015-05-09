package salience;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import salience.db.SnetIndexer;
import salience.kb.KBResult;
import utility.FileLoader;

public class Scaler {

	public static void main(String[] args) {
		String cognosTopic = args[0];
		int sneCutoff = Integer.parseInt(args[1]);
		int kbCutoff = 2;
		//load pagrank and test tweets for this topic
		FileLoader fiLoader = new FileLoader();
		HashMap<String, String> scTweets = fiLoader.loadScaleTweets(cognosTopic);
		fiLoader.loadPageRankScale(cognosTopic);
		//getSalientMentions
		
		HashMap<String, Integer> candidatePgrkNodes = new HashMap<String, Integer>();
		int totalPgrkMissCount= 0, totalMentionCout = 0, loopCount = 1;
		MentionDetector mentionDetector = new MentionDetector();
		FeatureGenerator featureGenerator = new FeatureGenerator();
		TweetClassifier tc = new TweetClassifier();
		SnetIndexer snetIndexer = new SnetIndexer();
	    int sneTweets = 0; int sneCount = 0;
	    Iterator it = scTweets.keySet().iterator();
		while(loopCount< 100){			
			String tweetId = it.next().toString();
			String tweet = scTweets.get(tweetId);
			if (tweet.length() > 0) {
				ArrayList<String> salMentions = new ArrayList<String>();
				ArrayList<String> SNEs = new ArrayList<String>();
				try {
					//find NEs using NER
					List<String> mentionList = mentionDetector.tokenizeMax(tweet); System.out.println(loopCount+" NE list :"+mentionList);
					totalMentionCout+=mentionList.size();
					List<String> tempList = new ArrayList<String>();
					tempList.addAll(mentionList);
					double maxRank = 0.0; String maxMention = null;
					if(mentionList.size()>0){					
						for(String m : mentionList){
							for(String mention : tempList){
								mention = mention.trim(); //System.out.println("Mention : " + mention);
								String camelCaseMention = mention.replaceAll(" ", "");
								if(FileLoader.PageRankMap.containsKey(mention)){
									double pgrk = FileLoader.PageRankMap.get(mention); //System.out.println("PageRank of mention "+mention+" = "+pgrk);
									if(pgrk > maxRank){
										maxRank = pgrk;
										maxMention = mention;
									}
								} else if(FileLoader.PageRankMap.containsKey(camelCaseMention)){
									double pgrk = FileLoader.PageRankMap.get(camelCaseMention); //System.out.println("PageRank of mention "+mention+" = "+pgrk);
									if(pgrk > maxRank){
										maxRank = pgrk;
										maxMention = camelCaseMention;
									}
								} else {//page rank does not have this NE
									++totalPgrkMissCount;
									int repeatCount = 1;
									if(candidatePgrkNodes.containsKey(mention)){
										repeatCount += candidatePgrkNodes.get(mention); System.out.println("candidate Pgrk node = "+mention);
									}
									candidatePgrkNodes.put(mention, repeatCount);
								}//if							
							}//for mention
							if(maxMention != null){
								salMentions.add(maxMention);
								tempList.remove(maxMention);
								maxRank = 0.0; maxMention = null;
							}
						}//for m
					}//if mentionList
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				System.out.println(" Salient mentions : " + salMentions);
		        if (salMentions.size() > 0) {
		            featureGenerator.createFV(tweet, salMentions, kbCutoff);
		            //HashMap<String, ArrayList<KBResult>> Wn = FeatureGenerator.snefeatureVectorMap;
		            //ArrayList<String> rankList = FeatureGenerator.rankList;
		            //boolean[] retrievalListArray = new boolean[rankList.size()];
		            //System.out.println("Sizes : Featmap = " + Wn.size() + " rankList = " + rankList.size());// + " retList " + retrievalListArray.length);
		            String[] str;		            
		    		HashMap<String, ArrayList<KBResult>> WnFnMap = featureGenerator.snefeatureVectorMap;
		    		int sneRank = 1;//as sneFeatureVectorMap is a linkedHashmap, the loops are in the ranked order
		    		for(String salientNE : WnFnMap.keySet()){
		    			ArrayList<KBResult> WnFn = WnFnMap.get(salientNE);
		    			for(KBResult wFn : WnFn){    
				              double[] vals = new double[5];
				              vals[0] = 0.0D;
				              for (String f : wFn.getFeatures()) {
					                str = f.split(":");
					                int fNum = Integer.parseInt(str[0].trim());
					                double value = Double.parseDouble(str[1].trim());
					                vals[fNum] = value;
				              }
				              double[] prob_estimates = tc.evaluate(vals, TweetClassifier.liveModel);
				              if (prob_estimates[2] == 1.0D) {
				            	  if(sneRank > sneCutoff) {
				            		  System.out.println("HURRAY @ " + sneRank);
				            		  SNEs.add(wFn.getWikiTiltle());
				                  }//if 		                
				              }//if
   		                }//for WFn
		    			++sneRank;
		    		}//for salientNE
		            if(SNEs.size() > 0){
		            	snetIndexer.indexDoc(Long.parseLong(tweetId.trim()), tweet, SNEs);
		            	++sneTweets;
		            	sneCount += SNEs.size();
		            }//if indexing		            
		            featureGenerator.clearMaps();
		        }//if salmentions		        
			}//if tweet not null
			++loopCount;
		}//while	tweet		
		System.out.println("SCORES: Tweet count = "+scTweets.size()+", Tweets with SNE = "+sneTweets+", Avg SNE per tweet = "+1.0*sneCount/sneTweets);
		StringBuilder status = new StringBuilder().append("SCALER STATUS :").append(cognosTopic)
				.append(", Page rank vector size = ").append(FileLoader.PageRankMap.size())
				.append(", Tweet count = ").append(scTweets.size()).append(", Tweets with SNE = ")
				.append(sneTweets).append(", Avg SNE per tweet = ").append(1.0*sneCount/sneTweets)
				.append(", Avg Pagerank Miss = ").append(1.0*totalPgrkMissCount/totalMentionCout)
				.append(", Pagerank candidates = ").append(candidatePgrkNodes.size()).append("\n");				
		utility.Logger.StatusOut(status);
	}//main

}//class



