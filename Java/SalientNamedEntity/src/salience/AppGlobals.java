package salience;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

import Variables.Variables;
import edu.berkeley.nlp.bp.Variable;

public class AppGlobals {
	
	public final static Proxy HTTP_PROXY=new Proxy(Proxy.Type.HTTP, new InetSocketAddress(HTTP_PROXY_HOST, HTTP_PROXY_PORT));
	public static boolean IS_DEBUG = true;
	/*
	 * Twitter configuration parameters.
	 */
	public final static String CONSUMER_KEY="";//populate
	public final static String CONSUMER_SECRET="";//populate
	public final static String OAUTH_ACCESS_TOKEN="";//populate
	public final static String OAUTH_ACCESS_TOKEN_SECRET="";//populate
	public final static String HTTP_PROXY_HOST="";//populate
	public final static String HTTP_PROXY_PORT="";//populate
	

/*
* NER parameters
*/
public final static String STOP_WORD_LIST_FILE = Variables.LibDir+Variables.stopwordList;
public final static String ARK_TWEET_TAGGER_TRAINING_MODEL = "ner/arkTweetModel.20120919";
public final static String RITTER_NER_RECOGNITION_ENDPOINT = "uow/";//"http://10.2.4.21:5050/extract?tweet=";//"http://10.2.4.192:5050/extract?tweet=";
public final static String ARK_TWEET_NER_RECOGNITION_ENDPOINT = "cmu/";//"http://10.2.4.192:8080/ELServers/process?tweet=";
public final static String STANFORD_NER_RECOGNITION_ENDPOINT = "stanford/" ;//"http://10.2.4.192:8080/ELServers/process?tweet=";
public static List<String> STOP_WORD_LIST = null;
public static enum NER {
	ALAN_RITTER, ARK_TWEET	
}

	static {
		STOP_WORD_LIST = new ArrayList<String>();
		//load the stop words.
		try {
		final BufferedReader br = new BufferedReader(new FileReader(AppGlobals.STOP_WORD_LIST_FILE));
		String line = "";
		while ((line = br.readLine()) != null)
			STOP_WORD_LIST.add(line.trim());
		br.close();
	} catch (final IOException ie) {
		ie.printStackTrace();
	}
}
}
