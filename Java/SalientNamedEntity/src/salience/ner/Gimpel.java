package salience.ner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
//import java.util.StringTokenizer;



import java.util.regex.Pattern;

import utility.WebUtility;
import Variables.Variables;
//import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;
import salience.AppGlobals;

public class Gimpel {
	
	static ArrayList<String> stopwords;	
	private static Tagger t1;	
	private static Gimpel obj = null;

	public static Gimpel getInstance(){
		if(obj==null){
		Gimpel object = new Gimpel();
		obj = object;
		}
		return obj;
	}
	public static String getTags(String tweet){
		String response = "";
		List<TaggedToken> pos_list = t1.tokenizeAndTag(tweet);
		for(int i=0;i<pos_list.size();i++){
			String tag1=pos_list.get(i).tag;
			String word1=pos_list.get(i).token;
			//response += word1+"#$%^"+tag1+"#$%^";
			response += word1+" "+tag1+" ";
		}
		return response;
	}


	static{
		
		t1 = new Tagger();
		try {
			t1.loadModel(Variables.modelPath);///home/romil/Documents/jars/ark-tweet-nlp-0.3.2/model.20120919");
		} catch (IOException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		stopwords = new ArrayList<>();
		try{
			BufferedReader br = new BufferedReader(new FileReader(Variables.stopwordList));
			String line = "";
			while((line = br.readLine())!=null){
				stopwords.add(line.trim());
			}
		}
		catch(Exception e){
		
		}
	}//costructor


	public static void main(String[] args) throws IOException{
		//Gimpel gimpel;
		try {
			//gimpel = new Gimpel();		
			//String tweet = "RT @SUcampus: The brothers of SAE 56 are almost halfway to their fundraising goal through ShAvE a Brother to benefit @GiftofLife: http://t.co/…";
			String tweet = "RT @Leo_Tweets I am going to visit Sachin,Dravid";
			System.out.println(recognizeNE(tweet));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}//main
	
	public static List<String> recognizeNE(String tweet) throws IOException {
        final String response=WebUtility.makeGetCall(AppGlobals.ARK_TWEET_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"), null);
        if(response==null || response.startsWith("</empty>") || response.startsWith("ERROR")) return null;
        final List<String> neList=new ArrayList<String>();
        for(final String ne:response.split(","))
            neList.add(ne.trim());
        return neList;
    }//recogniseNE
	
}//class

