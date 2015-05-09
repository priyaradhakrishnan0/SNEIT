package salience.ner;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import salience.AppGlobals;
import utility.WebUtility;

public class AlanRitter {
	
	public static void main(String[] args) throws IOException{
		AlanRitter ar = new AlanRitter();
		String tweet = "RT @SUcampus: The brothers of SAE are almost halfway to their fundraising goal through ShAvE a Brother to benefit @GiftofLife: http://t.co/…";
		System.out.println(ar.recognizeNE(tweet));
	}//main
	
	public static List<String> recognizeNE(String tweet) throws IOException {
		System.out.println(AppGlobals.RITTER_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"));
        final String response=WebUtility.makeGetCall(AppGlobals.RITTER_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"), null);
        if(response==null || response.startsWith("</empty>") || response.startsWith("ERROR")) return null;
        final List<String> neList=new ArrayList<String>();
        for(final String ne:response.split(","))
            neList.add(ne);
        return neList;
    }	
}//class
