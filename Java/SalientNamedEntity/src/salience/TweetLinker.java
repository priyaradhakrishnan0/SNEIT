package salience;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import Variables.Variables;
import salience.dataset.KBAnnotation;
import salience.kb.KBResult;
import utility.FileLoader;
import utility.Logger;
import utility.WebUtility;

public class TweetLinker {

	public static void main(String[] args){
		TweetLinker tweetLinker = new TweetLinker();
		String tweet = "Its a dream to drive an Fiat pushcart here in this rain";
		//String tweet = "Outside MCG, MS Dhoni fans throng stadium for #INDvsBAN QF http://t.co/9SFPq4ZHoI http://t.co/mNwgFrp0SS";
		//String tweet = "RT @cdixon: Good article on Samsung's super impressive \"fast follower\" strategy: http://t";
		tweetLinker.getSNEs(tweet,"fiat");
		//tweetLinker.loadtestData();
		
	}//main
	
	/*Get ranked list of SNEs*/
	public ArrayList<String> getSNEs(String tweet, String topic){
		ArrayList<String> SNEs = new ArrayList<String>();
		int loopCount = 1, HurrayCount = 0;
		StringBuilder sb = new StringBuilder();
		boolean testRecord = false;
		FileLoader fileLoader =  new FileLoader();
		fileLoader.loadPageRank(topic+".pgrk");// loads FileLoader.PageRankMap 
		MentionDetector mentionDetector = new MentionDetector();		
		//TweetClassifier tc = new TweetClassifier();

		//get salient mention
		ArrayList<String> salMentions = mentionDetector.getSalientMention(tweet);System.out.println(" Salient mentions : "+salMentions);
		//get feature vector
		FeatureGenerator featureGenerator = new FeatureGenerator();
		featureGenerator.setTopic(topic);
		featureGenerator.createFV(tweet, salMentions, 2); //TUNABLE PARAMETER kBCutoff
		HashMap<String, ArrayList<KBResult>> WnFnMap = featureGenerator.snefeatureVectorMap;
		double maxConfidence = 0.0; String maxConfLink = null;
		for(String salientNE : WnFnMap.keySet()){
			ArrayList<KBResult> WnFn = WnFnMap.get(salientNE);
			for(KBResult wFn : WnFn){
				StringBuilder featString = new StringBuilder();
				//double[] vals= new double[5];//TODO : set to features+1 
				//vals[0] = 0;//predicting label to be 0 always.
				//Append features
				for(String F:wFn.getFeatures()){ //	System.out.println("Feature string = "+f);
//					String[] str = F.split(":"); //System.out.println("After splitting "+str[0]+" and "+str[1]);
//					int fNum = Integer.parseInt(str[0].trim());
//					double value = Double.parseDouble(str[1].trim());
//					vals[fNum] = value;					
					String[] f = F.split(":");
					featString.append(f[1]).append(",");					
				}
				
				//System.out.println("Input featur len "+vals.length);
				//double[] prob_estimates = tc.evaluate(vals, tc.liveModel);
				boolean linked = link(featString.toString()); System.out.println("Linked "+linked);
				//if(prob_estimates[2]==1.0){
				if(linked){
					String wikiTitle = wFn.getWikiTiltle();
					SNEs.add(wikiTitle); System.out.println("FOUND SNE = "+wikiTitle);
				}
				
			}//for disambiguated wiki entity
		}//for salientNE
		return SNEs;
	}//getSNEs
	
	/*read test data*/
	public void loadtestData(){		
		//TweetClassifier tc = new TweetClassifier();
		String disamFile = Variables.LibDir.concat("TestVector.csv"); 	System.out.println("Loading file ="+disamFile);
		int TP=0, TN=0, FP=0, FN=0;
		try {
			BufferedReader br = new BufferedReader(new FileReader(disamFile));			
			String 	sCurrentLine = null;
			int totalTests = 1, totalLinked = 0;
			while((sCurrentLine = br.readLine()) != null) { System.out.println(" Running "+totalTests);
				++totalTests;
				String[] NEstrips = sCurrentLine.split(" ");
				String label=null;
				StringBuilder featString = new StringBuilder();				
				//double[] vals= new double[5];//TODO : set to features+1
				for(String NEstrip : NEstrips){					
					String[] splits = NEstrip.trim().split(":");
					if(splits.length==2){
						//System.out.println(" f = "+ splits[0].trim()+" value = "+splits[1].trim());
						//vals[Integer.parseInt(splits[0].trim())] = Double.parseDouble(splits[1].trim());
						featString.append(splits[0].trim()).append(",");
					} else{
						//vals[0] = Double.parseDouble(NEstrip); System.out.println(" label = "+vals[0]);
						//if(vals[0]==1.0)System.out.println("Positive sample at "+totalTests);
						label=NEstrip;
						if(NEstrip.trim().equalsIgnoreCase("TRUE"))System.out.println("Positive sample at "+totalTests);
					}
				}
 
				//System.out.println("Input featur len "+vals.length);
				//double[] prob_estimates = tc.evaluate(vals, tc.liveModel);
				boolean linked = link(featString.toString());
				//System.out.println(totalTests+" actual " +vals[0]+"  predicted "+prob_estimates[2]);
				//if(prob_estimates[2]==1.0){
				if(linked){
					totalLinked++; 
					if(label.equalsIgnoreCase("TRUE")){ //if(vals[0]==1.0){
						++TP;
					}else{
						++FN;
					}
				}else{
					//if(vals[0]==1.0){
					if(label.equalsIgnoreCase("TRUE")){
						++FP;
					}
				}				
			}//while line
			System.out.println("TOTAL tests = "+totalTests+" linked "+totalLinked);
			System.out.println(" TP = "+TP+"FP = "+FP+" FN = "+FN);
			double precision = 0.0, recall = 0.0, F = 0.0;
			precision =  1.0*TP/(TP+FP);
			System.out.println("P = "+ precision);
			recall = 1.0*TP/(TP+FN);
			System.out.println("R = "+ recall);
			F = 2.0 * precision * recall / (precision+recall) ;
			System.out.println("F = "+F);
			
		} catch (FileNotFoundException e1) {
			//Logger.logOut(disamFile+" File not found");
			e1.printStackTrace();
		} catch (IOException e2) {			//Logger.logOut("Reading error with "+disamFile);
			e2.printStackTrace();
		}
	}//End testData

	public boolean link(String featString){
		boolean linked = false;
		try {
			String response=WebUtility.makeGetCall(Variables.linkerServer+featString, null);
			if (response.equals("1")) linked = true;			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return linked;
	}
	
}//class
