package salience.evaluation;

import Variables.Variables;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import salience.FeatureGenerator;
import salience.MentionDetector;
import salience.TweetClassifier;
import salience.db.TweetExtractor;
import salience.kb.KBResult;
import utility.FileLoader;
import utility.Logger;

public class ReplabData
{
  public static HashMap<String, String> entityMap = new HashMap();
  HashMap<String, HashMap<String, Boolean>> replabGold = new HashMap();

  public void loadReplabMap() {
    entityMap = FileLoader.loadReplabEntities();
    this.replabGold = FileLoader.loadReplabGold();
  }

  public ReplabData() {
    loadReplabMap();
  }

  public static void main(String[] args) {
    ReplabData replabData = new ReplabData();
//    System.out.println(replabData.getRelatedTweets("RL2013D01E022").size());
    System.out.println("RepLab entity size = " + entityMap.size() + ", Gold size = " + replabData.replabGold.size());
    try {
      replabData.evaluate(args[0]);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  //retrieved replab tweets that are RELATED
  public HashMap<String, String> getRelatedTweets(String entityId)
  {
    HashMap RelTweets = new HashMap();

    HashMap tweetRelMap = (HashMap)this.replabGold.get(entityId);

    String disamFile = Variables.EvalDir.concat(entityId).concat("_texts.tsv"); System.out.println("Loading file =" + disamFile);
    try {
      BufferedReader br = new BufferedReader(new FileReader(disamFile));
      String sCurrentLine = br.readLine();
      while ((sCurrentLine = br.readLine()) != null) {
        Scanner s = new Scanner(sCurrentLine).useDelimiter("\"\\s*\"");
        String tweetId = s.next(); tweetId = tweetId.replace('"', ' '); tweetId = tweetId.trim();
        s.next(); s.next();
        String tweetText = s.next();
        tweetText = tweetText.replace('"', ' '); tweetText = tweetText.trim();
        if ((tweetRelMap.containsKey(tweetId)) && 
          (((Boolean)tweetRelMap.get(tweetId)).booleanValue())) {
          RelTweets.put(tweetId, tweetText);
        }
      }
    }
    catch (FileNotFoundException e1)
    {
      e1.printStackTrace();
    } catch (IOException e2) {
      e2.printStackTrace();
    }

    return RelTweets;
  }//getRelatedTweets
  
  //retrieved RELATED and UNRELATED replab tweets on an entity
  public HashMap<String, String> getAllTweets(String entityId)
  {
    HashMap RelTweets = new HashMap();

    HashMap tweetRelMap = (HashMap)this.replabGold.get(entityId);

    String disamFile = Variables.EvalDir.concat(entityId).concat("_texts.tsv"); System.out.println("Loading file =" + disamFile);
    try {
      BufferedReader br = new BufferedReader(new FileReader(disamFile));
      String sCurrentLine = br.readLine();
      while ((sCurrentLine = br.readLine()) != null) {
        Scanner s = new Scanner(sCurrentLine).useDelimiter("\"\\s*\"");
        String tweetId = s.next(); tweetId = tweetId.replace('"', ' '); tweetId = tweetId.trim();
        s.next(); s.next();
        String tweetText = s.next();
        tweetText = tweetText.replace('"', ' '); tweetText = tweetText.trim();
        if (tweetRelMap.containsKey(tweetId)) {
          RelTweets.put(tweetId, tweetText);
        }
      }
    }
    catch (FileNotFoundException e1)
    {
      e1.printStackTrace();
    } catch (IOException e2) {
      e2.printStackTrace();
    }

    return RelTweets;
  }//getAllTweets

  //print RELATED and UNRELATED replab tweets on an entity
  public void printTweets(String entityId)
  {
    HashMap RelTweets = new HashMap();
    StringBuilder sb = new StringBuilder();
    HashMap tweetRelMap = (HashMap)this.replabGold.get(entityId);

    String disamFile = Variables.EvalDir.concat(entityId).concat("_texts.tsv"); System.out.println("Loading file =" + disamFile);
    try {
      BufferedReader br = new BufferedReader(new FileReader(disamFile));
      String sCurrentLine = br.readLine();
      while ((sCurrentLine = br.readLine()) != null) {
        Scanner s = new Scanner(sCurrentLine).useDelimiter("\"\\s*\"");
        String tweetId = s.next(); tweetId = tweetId.replace('"', ' '); tweetId = tweetId.trim();
        s.next(); s.next();
        String tweetText = s.next();
        tweetText = tweetText.replace('"', ' '); tweetText = tweetText.trim();
        if (tweetRelMap.containsKey(tweetId)) {
          sb.append(sCurrentLine).append("\n");
        }
      }//while
    }
    catch (FileNotFoundException e1)
    {
      e1.printStackTrace();
    } catch (IOException e2) {
      e2.printStackTrace();
    }
    utility.Logger.ReplabGoldOut(sb);
    
  }//printTweets


  public void evaluate(String repLabQuery) throws InterruptedException
  {
	  
	  
    int KbCutoff = 2;
    System.out.println("Evaluating replab query " + repLabQuery);
    String equivalentRepEid = null;
    if(repLabQuery.equalsIgnoreCase("Ford")){
    	equivalentRepEid = "RL2013D01E041";// Ford_Motor_Company
    } else if(repLabQuery.equalsIgnoreCase("Kia")){
    	equivalentRepEid = "RL2013D01E040"; // Kia_Motors
    } else if(repLabQuery.equalsIgnoreCase("MIT")){
    	equivalentRepEid = "RL2013D03E089";//MIT
    } else if(repLabQuery.equalsIgnoreCase("Britney Spears")){
    	equivalentRepEid = "RL2013D04E207";
    } else {
	    for (String eId : entityMap.keySet()) {
	    	String replabEntity = entityMap.get(eId); 
	    	replabEntity = replabEntity.replace("\\(.*\\)", "");
	    	replabEntity = replabEntity.replace("_", " ");
	        if (replabEntity.equalsIgnoreCase(repLabQuery)) {
			    System.out.println("Replab query with equivalent cognos topic");
		        equivalentRepEid = eId;
		        break;
		    }
	    }
    }
    System.out.println("Rep lab Id = " + equivalentRepEid);
    //FileLoader.printReplabGold(equivalentRepEid);
           
    HashMap<String, String> tweetMap = getAllTweets(equivalentRepEid);
    System.out.println("Evaluation tweet suite size = " + tweetMap.size());    

    String replabquery = repLabQuery.toLowerCase();
    int sneTweets = 0; int loopCount = 0;
    StringBuilder sb = new StringBuilder();

    FileLoader fileLoader = new FileLoader();
    fileLoader.loadPageRank(replabquery + ".pgrk");
    HashMap<String, Boolean> replabGold = fileLoader.loadReplabGold(equivalentRepEid);
    MentionDetector mentionDetector = new MentionDetector();
    //TweetClassifier tc = new TweetClassifier();
    FeatureGenerator featureGenerator = new FeatureGenerator();
    featureGenerator.setTopic(replabquery);

    int AccuracyCount = 0; int Pat1count = 0, TP=0, FP = 0, FN =0;
    double avgPat1 = 0.0D; double map = 0.0D;
    for (String tweetId : tweetMap.keySet()) {
      boolean relate = false;
      String tweet = (String)tweetMap.get(tweetId);

      loopCount++;
      tweet = tweet.replaceAll("\n", "");
      System.out.println(loopCount + "<TWEET>" + tweet + "</TWEET>");
      ArrayList<String> SNEs = new ArrayList<String>();

      if (tweet.length() > 0) {//if (tweet.length() > 0 && tweetExtractor.isEnglishTweet(tweetId)) {
        ArrayList<String> salMentions = mentionDetector.getSalientMention(tweet); System.out.println(" Salient mentions : " + salMentions);
        if (salMentions.size() > 0) {
          sneTweets++;

          featureGenerator.createFV(tweet, salMentions, KbCutoff);
          HashMap<String, ArrayList<KBResult>> WnFn = FeatureGenerator.snefeatureVectorMap;
          System.out.println("Sizes : Featmap = " + WnFn.size());
          String[] str;
          for(String sne : WnFn.keySet()){
        	  ArrayList<KBResult> wFn = WnFn.get(sne);
      
	          for (KBResult W : wFn) {
	            //double[] vals = new double[5];
	            //vals[0] = 0.0D;
	            String wikiTit = null;
//	            for (String f : W.getFeatures()) {
//	              str = f.split(":");
//	              int fNum = Integer.parseInt(str[0].trim());
//	              double value = Double.parseDouble(str[1].trim());
//	              vals[fNum] = value;
//	            }
//	            double[] prob_estimates = tc.evaluate(vals, TweetClassifier.liveModel);
//	            if (prob_estimates[2] == 1.0D) {
	            	wikiTit = W.getWikiTiltle(); System.out.println(repLabQuery+" = "+wikiTit);
	            	if (wikiTit.contains(repLabQuery) || repLabQuery.contains(wikiTit)) { System.out.println("HURRAY @ ");
		                relate = true;
		                AccuracyCount++;
		            }	            	
///	            }
	          }//for WikiEntity
          }//for sne	          
          featureGenerator.clearMaps();
//          StringBuilder repSysOut = new StringBuilder().append("\"").append(equivalentRepEid).append("\"")
//          		.append(" \"").append(tweetId).append("\"");
//          if(relate){
//        	  repSysOut.append(" \"").append("RELATED").append("\"\n");
//          } else {
//        	  repSysOut.append(" \"").append("UNRELATED").append("\"\n");
//          }
//          utility.Logger.ReplabSysOut(repSysOut);  
          if(relate){
        	  if(replabGold.get(tweetId)){//TP
        		  ++TP;
        	  } else {
        		  ++FP;
        	  }
          } else {
        	  if(replabGold.get(tweetId)){
        		  ++FN;
        	  }
          }
          
        }//if salMentions
      }//if tweet
      System.out.println(" TP = "+TP+" FP = "+FP+"  FN = "+FN);
    }//for tweet
    System.out.println("SCORES:"+repLabQuery+" TP = "+TP+" FP = "+FP+"  FN = "+FN+" relateds count "+AccuracyCount+" Detected NE for "+sneTweets);
    double P = 1.0*TP/(TP+FP);
    double R = 1.0*TP/(TP+FN);
    double F = 2 * P * R / (P + R);
    System.out.println(" P = "+ P + " R = "+R+" F = "+F+" Tweets with SNE " + sneTweets + " Accuracy = " + 1.0D * AccuracyCount / sneTweets );
    StringBuilder status = new StringBuilder().append("SCORES : ").append(repLabQuery).append(",")
    		.append(FileLoader.PageRankMap.size()).append(",").append(tweetMap.size()).append(",").append(sneTweets).append(",")
    		.append(AccuracyCount).append(",").append(1.0D * AccuracyCount / sneTweets).append(",")
    		.append(P).append(",").append(R).append(",").append(F).append("\n");				
    utility.Logger.ReplabSysOut(status);  
  }//evaluate
  
}//class

