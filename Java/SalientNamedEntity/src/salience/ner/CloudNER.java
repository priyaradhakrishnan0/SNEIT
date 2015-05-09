package salience.ner;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import Variables.Variables;
import salience.AppGlobals;
import utility.WebUtility;

public class CloudNER {

	public static List<String> recognizeNE(String tweet, String ner) throws IOException {
		String response = null;
//		if(Variables.isProxy){ //ext Cloud
//	        if(ner.equalsIgnoreCase("ar")){
//	        	response=WebUtility.makeGetCall(Variables.nerServer+AppGlobals.RITTER_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"), AppGlobals.HTTP_PROXY);
//	        } else if(ner.equalsIgnoreCase("at")){//cmu
//	        	response=WebUtility.makeGetCall(Variables.nerServer+AppGlobals.ARK_TWEET_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"), AppGlobals.HTTP_PROXY);
//	        } else if(ner.equalsIgnoreCase("st")){
//	        	response=WebUtility.makeGetCall(Variables.nerServer+AppGlobals.STANFORD_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"), AppGlobals.HTTP_PROXY);        	
//	        }
//		} else { //local cloud
	        if(ner.equalsIgnoreCase("ar")){
	        	response=WebUtility.makeGetCall(Variables.nerServer+AppGlobals.RITTER_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"), null);
	        } else if(ner.equalsIgnoreCase("at")){//cmu
	        	response=WebUtility.makeGetCall(Variables.nerServer+AppGlobals.ARK_TWEET_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"), null);
	        } else if(ner.equalsIgnoreCase("st")){
	        	response=WebUtility.makeGetCall(Variables.nerServer+AppGlobals.STANFORD_NER_RECOGNITION_ENDPOINT+URLEncoder.encode(tweet,"UTF-8"), null);        	
	        }			
//		}
        if(response==null || response.startsWith("</empty>") || response.startsWith("ERROR")) return null;
        final List<String> neList=new ArrayList<String>();
        for(final String ne:response.split(","))
            neList.add(ne.trim());
        return neList;
    }
	
}
