package salience;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import salience.db.AnchorIndexer;
import salience.db.TweetIndexer;
import salience.kb.DBPediaSearch;
import salience.kb.GCDsearch;
import salience.kb.KBResult;
import salience.kb.KBRetrieval;
import salience.kb.WikipediaSearch;

/*Create SNE feature vector. 
 * SNE features
 * f1 = Lucene Rank
 * f2 = Jaccard similarity score
 * f3 = Link Probability
 * f4 = Salience Probability */
public class FeatureGenerator {

	public static HashMap<String, ArrayList<KBResult>> snefeatureVectorMap = new LinkedHashMap<String, ArrayList<KBResult>>();
	
	public static ArrayList<String> rankList = new ArrayList<String>();
	static boolean DEBUG_FLAG = false;//arg[3] is debug flag
	static boolean EVAL_FLAG = false;//arg[4] is evaluation flag
	static boolean TIME_FLAG = false;//arg[5] is time variance feature flag
	String inPath = "/home/priya/Desktop/EntityRanking";//for local machine
	String topic = "cricketer";
	
	public void setTopic(String topic){
		this.topic = topic;
	}
	public String getTopic(){
		return this.topic;
	}	
	public void clearMaps(){
		snefeatureVectorMap.clear();
		rankList.clear();
	}//clearMap
	
	public static void main(String[] args) {
		FeatureGenerator featureGenerator = new FeatureGenerator();
//		if(args[3].equalsIgnoreCase("debug")) featureGenerator.DEBUG_FLAG = true;
//		if(args[4].equalsIgnoreCase("eval")) featureGenerator.EVAL_FLAG = true;
//		if(args[5].equalsIgnoreCase("time")) featureGenerator.TIME_FLAG = true;		

		String tweet = "Suresh Raina Wedding: MS Dhoni, Dwayne Bravo Among High-Profile Cricketers in Attendance: A stream of celebrit.";
		MentionDetector mentionDetector = new MentionDetector();
		//ArrayList<String> salMentions = mentionDetector.getSalientMention(tweet);
		ArrayList<String> salMentions = new ArrayList<String>();
		salMentions.add("MS Dhoni"); salMentions.add("Suresh Raina");

		featureGenerator.createFV(tweet, salMentions, 2);		
	}//End main

	public void createFV(String tweet, ArrayList<String> salMentions, int KBcutoff) {
		GCDsearch sc=new GCDsearch();	//DBPediaSearch sc = new DBPediaSearch();//WikipediaSearch sc = new WikipediaSearch();
		TweetIndexer tweetIndexer = new TweetIndexer();
		AnchorIndexer anchorIndexer = new AnchorIndexer();
		
		//f5:lexical ordering of mentions in the tweet
		ArrayList<String> lexicalOrder = new ArrayList<String>();		
		for(String a: tweet.split(" ")){
			for(String sNE:salMentions){
				if(sNE.contains(a) || a.contains(sNE)){
					lexicalOrder.add(sNE);	
				}			
			}
		}
		//f6 and f7 : presence in hashtag and userid
		double[] f6 = new double[salMentions.size()];
		double[] f7 = new double[salMentions.size()];
		for(String a: tweet.split(" ")){
			for(String ne : salMentions){
				if(ne.contains(a) || a.contains(ne)){
					if(a.startsWith("#")) f6[salMentions.indexOf(ne)]=1.0;
					if(a.startsWith("@")) f7[salMentions.indexOf(ne)]=1.0;
				}
			}
		}
	

		for(String salMen : salMentions){
			
			//Local features = GCD / DBpedia
			HashMap<String, Double> disambiguatedNEs = sc.entriesSortedByValues(sc.search(salMen, tweet, KBcutoff));
//			KBRetrieval kbRetrieval = new KBRetrieval();
//			kbRetrieval.setNE(salMen);
//			kbRetrieval.setRetrievals(disambiguatedNEs);
			if(disambiguatedNEs != null){
				ArrayList<KBResult> wfnList = new ArrayList<KBResult>();
				//f1 = Normalized GCD retrieval rank
				//f2 = Jac Sim b/w mention and dcument
				//Global features
				ArrayList<String> disList = new ArrayList<String>(disambiguatedNEs.keySet());
				//f3 = Link Probability
				double[] f3 = anchorIndexer.lp(disList);
				//f4. Salience Probability 
				double[] f4 = tweetIndexer.getNeSalProb(topic, disList);
				//Lexical features
				//f5 = Lexical position of NE in tweet OR Word Order of NE : 
				//f6 = Presence in HashTag : 
				//f7 = Preceded by @ :
				//f8 = topic word			
								
				int j = 0; //GCD retrieval rank -1
				//populate FV
				//for (int k=0; k < KBcutoff & k < disList.size() ; ++k){
				for (int k=0; k < disList.size() ; ++k){
					String wikiTitle = disList.get(k);
					ArrayList<String> featureValue = new ArrayList<String>();
					featureValue.add("1:"+1.0*(j+1)/disambiguatedNEs.size()); 
					featureValue.add("2:"+disambiguatedNEs.get(wikiTitle));
					featureValue.add("3:"+ Double.toString(f3[j]));
					featureValue.add("4:"+ Double.toString(f4[j]));
					featureValue.add("5:"+(double) lexicalOrder.indexOf(salMen)/lexicalOrder.size());
					featureValue.add("6:"+f6[salMentions.indexOf(salMen)]);
					featureValue.add("7:"+f7[salMentions.indexOf(salMen)]);
					if(wikiTitle.contains(topic) || topic.contains(wikiTitle)){
						featureValue.add("8:1.0");
					} else {
						featureValue.add("8:0.0");
					}
					KBResult Wn = new KBResult();
					Wn.setWikiTitle(wikiTitle);
					Wn.setFeatureValue(featureValue);
					wfnList.add(Wn);
					//nefeatureVectorMap.put(wikiTitle, featureValue);
					//rankList.add(wikiTitle);
					System.out.println(wikiTitle+"  "+featureValue);
					++j;
				}//for disambiguated wiki title
				snefeatureVectorMap.put(salMen, wfnList);
			}//if disambiguated
		}//for salMen
	}//createFV
}
