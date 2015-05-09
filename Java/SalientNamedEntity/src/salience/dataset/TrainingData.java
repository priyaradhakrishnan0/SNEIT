package salience.dataset;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import salience.FeatureGenerator;
import salience.MentionDetector;
import salience.kb.KBResult;
import utility.FileLoader;
import utility.Logger;

public class TrainingData {
	
	public static void main(String[] args){
		TrainingData trainingData = new	TrainingData();
		trainingData.createTrainigData(Integer.parseInt(args[0]));
	}//main
	
	public void createTrainigData(int kbCutoff){
		
		//Read Seimp dataset
		DatasetRead datasetRead = new DatasetRead();
		HashMap<String, List<KBAnnotation>> trainingData = datasetRead.getTrainigData();
		int loopCount = 1, HurrayCount = 0, sampleCount = 0;
		
		boolean testRecord = false;
		FileLoader fileLoader =  new FileLoader();
		fileLoader.loadPageRank("cricket.old");// loads FileLoader.PageRankMap 
		MentionDetector mentionDetector = new MentionDetector();
		FeatureGenerator featureGenerator = new FeatureGenerator();
		featureGenerator.setTopic("cricketer");
		for(Entry<String, List<KBAnnotation>> trainRecord : trainingData.entrySet()){
			//get salient mention
			String tweet = trainRecord.getKey(); System.out.println(tweet);
			//ArrayList<String> salMentions = mentionDetector.getSalientMention(tweet);//full tweet classification
			ArrayList<String> salMentions = new ArrayList<String>(); //Salient mention alone classification
			for(KBAnnotation kbAnn : trainRecord.getValue()){
				String goldNe = kbAnn.getNe(); 
				goldNe = goldNe.replaceAll("\\(.*\\)", " ");
				goldNe = goldNe.trim();
				salMentions.add(goldNe);
			}
			if(salMentions.size() > 0){ System.out.println("Salient mentions "+salMentions);
				//get feature vector
				featureGenerator.createFV(tweet, salMentions,kbCutoff);
				HashMap<String, ArrayList<KBResult>> Wn = featureGenerator.snefeatureVectorMap;
				StringBuilder sb = new StringBuilder();
				//System.out.println("Wn size = "+Wn.size());
				for(Entry<String, ArrayList<KBResult>>salMention : Wn.entrySet()){
					boolean foundHumanAns = false, positivesample = false ;
					//find the human ans
					String humanGivenEntity = null;
					for(KBAnnotation kbAnn : trainRecord.getValue()){
						humanGivenEntity = kbAnn.getKbEntry(); 
						if(humanGivenEntity != null && humanGivenEntity != "none"){
							foundHumanAns=true;
							break;
						}//if humanGivenEntity is valid
					}
					//find the sys ans
					if(foundHumanAns){
						for(KBResult res : salMention.getValue()){							
							//Append features
							for(String feature : res.getFeatures()){
								sb.append(feature); sb.append(" ");
							}//for feature
							//Append classlabel
							if(res.getWikiTiltle().equalsIgnoreCase(humanGivenEntity)){
								System.out.println("Hurray");
								positivesample = true;
								++HurrayCount;
								sb.append("TRUE ");
							} else {
								sb.append("FALSE ");
							}
							sb.append("\n"); 
							++sampleCount; System.out.println("Valid sample count :"+sampleCount);
						}//for KBResult
					}	
				}//for salMention
				++loopCount;
				System.out.println("Iter = "+loopCount+" Positive sample = "+HurrayCount+" Total sample = "+sampleCount);
				Logger.trainsetOut(sb, false);
				featureGenerator.clearMaps();
			}//if sal mentions exist
		}//for trngRecord

		System.out.println("Hurray Count = "+HurrayCount);
		
	}//createTraining data	
	
}//class
