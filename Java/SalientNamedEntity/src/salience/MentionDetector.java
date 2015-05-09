package salience;

import java.io.IOException;
import java.io.ObjectInputStream.GetField;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Map.Entry;

import salience.dataset.DatasetRead;
import salience.dataset.KBAnnotation;
import salience.ner.AlanRitter;
import salience.ner.CloudNER;
import salience.ner.Gimpel;
import salience.pageRank.Node;
import utility.FileLoader;
import utility.Logger;

public class MentionDetector {
	private HashMap<String, Double>PageRankMap = new HashMap<String, Double>();
	
	
	public static void main(String[] args) throws IOException{
		MentionDetector mentionDetector = new MentionDetector();
//		FileLoader fileLoader =  new FileLoader();
//		fileLoader.loadPageRank("cricket.old");// loads FileLoader.PageRankMap 
//		String tweet = "RT @Leo_Tweets I am going to visit Sachin,Dravid";			
//		System.out.println(mentionDetector.getSalientMention(tweet));
		try {
			mentionDetector.evaluatePgrkMentionDetection();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//main
	
	/*Tokenize the tweet using ARK-tweet and TwiCal(A.Ritter) and return named entities by Union method.i.e MAX num of results*/
	public ArrayList<String> tokenizeMax(String tweet, int method) throws IOException {
		/*
		 * Get Mentions Using Twical
		 * 0 = Both; 1 = Twical; 2 = ARK
		 */
		tweet = tweet.replace("&", "and").replaceAll(";", ":");
		List<String> twical = AlanRitter.recognizeNE(tweet);		
		System.out.println("Twical: "+twical);		
		if(method==1)
			return new ArrayList(twical);

		/*
		 * Get Mentions using ARK
		 * 
		 */
		//tweet = tweet.replaceAll("#", " ");
		List<String> arrARK =  Gimpel.recognizeNE(tweet);
		//System.out.println(tweet);
		System.out.println("arrARK: "+arrARK);
		if(method==2)
			return new ArrayList(arrARK);
		/*
		 * Collecting mentions from 2 systems
		 */
		List<String> maxlist = new ArrayList<>();
		maxlist.addAll(twical); //System.out.println("size after twical "+maxlist.size());
		maxlist.addAll(arrARK); //System.out.println("size after arr "+ maxlist.size());
		return new ArrayList<String>(maxlist); 

	}//tokenizeMax

	/*Tokenize the tweet using ARK-tweet,TwiCal(A.Ritter) and stanford NER; return named entities by Union method.i.e MAX num of results*/
	public static List<String> tokenizeMax(String tweet) throws IOException {
		Set<String> set = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		//System.out.println("TWEET = "+tweet);
		/*
		 * Get Mentions Using Twical
		 */
		//tweet = tweet.replace("\n", "");
		tweet = tweet.replace("&", "and").replaceAll(";", ":").replaceAll("\\(", "").replaceAll("\\)", "").replaceAll("\\[", " ").replaceAll("\\]", " ");
		tweet = tweet.replaceAll("\\s+", " ");
		tweet = tweet.replaceAll("[^\\x00-\\x7F]", "");
		tweet = tweet.replaceAll("/", "").replaceAll("|", "");
		for(String w:tweet.split(" ")){
			if(w.matches("^(http|https|ftp)://.*$")){//if(w.startsWith("http:")){
				tweet = tweet.replaceAll(w, "");
			}
		}
		System.out.println("CLEANED TWEET = #####"+tweet+"#####");
		List<String> twical = CloudNER.recognizeNE(tweet, "ar");		
		//System.out.println("Twical: "+twical);		
		/*
		 * Get Mentions using ARK
		 * 
		 */
		List<String> arrARK = CloudNER.recognizeNE(tweet, "at");
		//System.out.println("arrARK: "+arrARK);
		/*
		 * Get Mentions using Stanford
		 * 
		 */
		List<String> arrSF = CloudNER.recognizeNE(tweet, "st");
		//System.out.println("arrSF: "+arrSF);
		
		/*
		 * Collecting mentions from 3 systems
		 */
		List<String> maxlist = new ArrayList<>();
		if(twical!=null){
			maxlist.addAll(twical); //System.out.println("size after twical "+maxlist.size());
		} 
		if(arrARK!=null){
			maxlist.addAll(arrARK); //System.out.println("size after arr "+ maxlist.size());
		}
		if(arrSF != null){
			maxlist.addAll(arrSF);	
		}
		set.addAll(maxlist);
		maxlist = new ArrayList<String>(set);
		//System.out.println("NERs: "+maxlist);
		return maxlist;
	}//tokenizeMax

	/*Get mentions ordered by pagerank*/
	public ArrayList<String> getSalientMention(String tweet){
		ArrayList<String> salientMentions = new ArrayList<String>();

		try {
			//find NEs using NER
			List<String> mentionList = tokenizeMax(tweet); System.out.println("NE list :"+mentionList);
			List<String> tempList = new ArrayList<String>();
			tempList.addAll(mentionList);
			double maxRank = 0.0; String maxMention = null; Node checkNode;
			if(mentionList.size()>0){
				
				for(String m : mentionList){
					for(String mention : tempList){
						mention = mention.trim(); //System.out.println("Mention : " + mention);

						if(FileLoader.PageRankMap.containsKey(mention)){
							double pgrk = FileLoader.PageRankMap.get(mention); //System.out.println("PageRank of mention "+mention+" = "+pgrk);
							if(pgrk > maxRank){
								maxRank = pgrk;
								maxMention = mention;
							}
						} else {
							String camelCaseMention = mention.replaceAll(" ", "");
							if(FileLoader.PageRankMap.containsKey(camelCaseMention)){
								double pgrk = FileLoader.PageRankMap.get(camelCaseMention); //System.out.println("PageRank of mention "+mention+" = "+pgrk);
								if(pgrk > maxRank){
									maxRank = pgrk;
									maxMention = camelCaseMention;
								}
							}
						}
					}//for
					if(maxMention != null){
						salientMentions.add(maxMention);
						tempList.remove(maxMention);
						maxRank = 0.0; maxMention = null;
					}
				}//for
			}			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return salientMentions;
	}//getSalientMention
	
	
	public void evaluatePgrkMentionDetection() throws InterruptedException, IOException{
		
		//Read Seimp dataset
		DatasetRead datasetRead = new DatasetRead();
		HashMap<String, List<KBAnnotation>> trainingData = datasetRead.getTrainigData();
		int loopCount = 1, truePositive = 0, falsePositive = 0, falseNegative = 0;
		StringBuilder sb = new StringBuilder();
		boolean testRecord = false;
		FileLoader fileLoader =  new FileLoader();
		//fileLoader.loadPageRank("cricket.rep5");// loads FileLoader.PageRankMap 

		for(Entry<String, List<KBAnnotation>> trainRecord : trainingData.entrySet()){
			Thread.currentThread().sleep(500);
			
			//get salient mention by pagerank
			String tweet = trainRecord.getKey();
			//ArrayList<String> salMentions = getSalientMention(tweet);//for pgrk=2 and more 
			List<String> salMentions = tokenizeMax(tweet); //for pgrk = none
			System.out.println("mentions = "+salMentions);
			if(salMentions.size()==0)System.out.println("No sal mentions for = "+tweet);
			ArrayList<String> goldMentions = new ArrayList<String>(); 

			for(KBAnnotation kbAnn : trainRecord.getValue()){
				String goldNe = kbAnn.getNe(); 
				goldNe = goldNe.replaceAll("\\(.*\\)", " ");
				goldNe = goldNe.trim();
				goldMentions.add(goldNe);					
			}
			System.out.println("Gold Mentions = "+goldMentions);
			
			ArrayList<String> trueMentions = new ArrayList<String>();
			for(String goldMention : goldMentions){
				String salMen=null;
				for(String salMention: salMentions){
					if(salMention.contains(goldMention) || goldMention.contains(salMention)){
						truePositive++;
						trueMentions.add(salMention);
						trueMentions.add(goldMention);
						break;
					}
				}//for goldMention	
				if(salMen != null)salMentions.remove(salMen);
			}//for salMention
			salMentions.removeAll(trueMentions);
			falsePositive += salMentions.size(); 			//sal mention not present in gold is false positive
			goldMentions.removeAll(trueMentions);
 			falseNegative += goldMentions.size();			//gold mention not detected by system are false negatives	
			
			System.out.println("Iter = "+loopCount+" Tp ="+truePositive+" Fp ="+falsePositive+" Fn ="+falseNegative);
			System.out.println();
			++loopCount;
		}//for trngRecord
		Logger.trainsetOut(sb, true);
		System.out.println(" TP = "+truePositive+"FP = "+falsePositive+" FN = "+falseNegative);
		double precision = 0.0, recall = 0.0, F = 0.0;
		precision =  1.0*truePositive/(truePositive+falsePositive);
		System.out.println("P = "+ precision);
		recall = 1.0*truePositive/(truePositive+falseNegative);
		System.out.println("R = "+ recall);
		F = 2.0 * precision * recall / (precision+recall) ;
		System.out.println("F = "+F);
		
	}//evaluatePgrkMentionDetection

}//class
