package com.salience.commons;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;

public class AppGlobals {

	public final static boolean IS_DEBUG = true;
	public final static String HTTP_HOST="proxy.iiit.ac.in";
	public final static int HTTP_PORT=8080;
	public final static Proxy HTTP_PROXY=new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy.iiit.ac.in", 8080));

	/*
	 * Twitter configuration parameters.
	 */
	public final static int GET_TWEETS_FROM_USER_TIME_LINE_COUNT = 20;
	public final static int MAX_PAGE_PER_USER_CHECK = 100;

	/*
	 * Mongo DB Parameters.
	 */
	public final static String MONGO_DB_SERVER_IP = "localhost";
	public final static int MONGO_DB_PORT = 27017;
	public final static String MONGO_DB_NAME = "seimp";
	public final static String MEIJ_TRAINING_SET_COLLECTION_NAME = "meijtrainingset";
	public final static String SMALL_SEIMP_TRAINING_SET_COLLECTION_NAME = "smallseimptrainingset";
	public final static String LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME = "largeseimptrainingset";
	public final static String INTER_ANNOTATION_SET_COLLECTION_NAME="interannotationset";
	public final static String GOOGLE_WIKI_CONCEPTS_COLLECTION_NAME="googlewikiconcepts";
	public final static String COMPLETE_DATASET_COLLECTION_NAME="completeDataset";

	/*
	 * NER parameters
	 */
	private final static String STOP_WORD_LIST_FILE = "data/ner/stopwordslist.txt";
	public final static String ARK_TWEET_TAGGER_TRAINING_MODEL = "data/ner/arkTweetModel.20120919";
	public static List<String> STOP_WORD_LIST = null;
	public final static String GET_RITTER_NER_ENDPOINT = "http://10.2.4.97:5050/extract?tweet=";
	public final static String TWINER_WIKI_KEYPHRASENESS_FILE="data/ner/WikiQsEng.txt";
	public final static String POST_MICROSOFT_WEB_NGRAM_ACCESS_ENDPOINT="";
	public final static int TWINER_MAX_RES_SIZE=2;
	
	/*
	 * Knowledge base Parameters
	 */
	public final static String LUCENE_WIKI_INDEX="data/kb/wikiIndex/";
	public final static int LUCENE_HITS_TO_PROCESS=10;
	public final static String DBPEDIA_SPARQL_ENDPOINT="http://dbpedia.org/sparql?default-graph-uri=http://dbpedia.org&query=";
	public final static String DBPEDIA_SPARQL_QUERY_FILE="data/kb/dbpediaSparqlQuery";
	public final static String GOOGLE_WIKI_CONCEPTS_FILE="data/kb/dictionary/dictionary";
	public final static String GOOGLE_WIKI_CONCEPTS_INDEX="data/kb/ConceptsUncleaned";
	
	/*
	 * Meij
	 */
	public final static String ANCHOR_DB_IP="10.2.4.210";
	public final static int ANCHOR_DB_PORT=27017;
	public final static String ANCHOR_DB_COLLECTION_NAME="anchors";
	public final static String ANCHOR_DB_NAME="anchorDB";
	public final static String WIKI_PAGE_TITLE_COLLECTION="wikiPageTitle";
	public final static String GET_TAG_DEF_API="https://api.tagdef.com/one.???.json";
	public final static String GET_WIKI="http://en.wikipedia.org/wiki/";

	public static enum NER {
		ALAN_RITTER, ARK_TWEET, STANFORD_CRF
	}

	/*
	 * Meij parameters
	 */
	public final static String MEIJ_WSDM_2012_ANNOTATIONS = "data/meij_wsdm_2012/wsdm2012_annotations.txt";

	static {
		// load the stop words.
		STOP_WORD_LIST = new ArrayList<String>();
		try {
			final BufferedReader br = new BufferedReader(new FileReader(
					AppGlobals.STOP_WORD_LIST_FILE));
			String line = "";
			while ((line = br.readLine()) != null)
				STOP_WORD_LIST.add(line.trim());
			br.close();
		} catch (final IOException ie) {
			ie.printStackTrace();
		}
	}

}
