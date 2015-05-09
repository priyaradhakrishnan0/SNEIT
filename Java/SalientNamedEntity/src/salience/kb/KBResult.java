package salience.kb;

import java.util.ArrayList;

public class KBResult {
 String wikiTitle;
 ArrayList<String> featureValue ;
 
 public void setWikiTitle(String wikiTitle){
	 this.wikiTitle = wikiTitle;
 }
 
 public String getWikiTiltle(){
	 return wikiTitle;
 }
 
 public void setFeatureValue(ArrayList<String> featureList){
	 this.featureValue = featureList;
 }
 
 public ArrayList<String> getFeatures(){
	 return featureValue;
 }
}
