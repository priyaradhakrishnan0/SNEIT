package com.salience.meij;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.salience.collect.Annotation;
import com.salience.collect.Concept;
import com.salience.collect.KBAnnotation;
import com.salience.collect.SeimpTrainingRow;
import com.salience.commons.AppGlobals;
import com.salience.meij.pojo.AnchorDBRow;
import com.salience.meij.pojo.AnchorPageRow;
import com.salience.meij.pojo.TagDefDef;
import com.salience.meij.pojo.TagDefMaster;
import com.salience.ml.RitterResponse;
import com.salience.util.Utilities;
import com.salience.util.mongo.MongoDbManager;

public class TrainingSetCreator {

	private static final int TOP_N_CONCEPTS = 5;

	public static void main(final String[] argv) throws Exception {
		final String tweet = "#sachin , #Tiger of #bangladesh roar in #indvsban #qfinal http://t.co/bjVHmpGpw7#577058480068132864";
		final List<String> masterKbList = Arrays.asList("Mashrafe_Mortaza",
				"Bangladesh_(producer)", "History_of_Bangladesh", "Bangladesh",
				"Bastian_Schweinsteiger", "James_H._Howard");
		final List<String> annKbList = Arrays.asList(
				"Mashrafe_Mortaza_(cricketer)", "Bangladesh");
		
		/*
		final BufferedReader reader=new BufferedReader(new FileReader("master_10938.txt"));
		String line=null;
		int count=0;
		while((line=reader.readLine())!=null){
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(line,
							"com.salience.collect.SeimpTrainingRow");
			BasicDBObject dbo=new BasicDBObject();
			dbo.put("_id", row.get_id());
			int size=MongoDbManager.getCollection(AppGlobals.MONGO_DB_NAME, "ritterResponse").find(dbo).size();
			if(size==0){
				final String response = Utilities.makeGetCall(
						AppGlobals.GET_RITTER_NER_ENDPOINT + URLEncoder.encode(row.getText()),
						null);
				RitterResponse resp = new RitterResponse(); 
				resp.set_id(row.get_id());
				resp.setTags(response);
				MongoDbManager.insertJSON(MongoDbManager.getCollection(AppGlobals.MONGO_DB_NAME,"ritterResponse"),resp);
				Thread.sleep(300);
				System.out.println(count++);
			}
		}
		reader.close();
		*/
		
		/*
		final BufferedReader reader=new BufferedReader(new FileReader("WCQ_1200.txt"));
		final PrintWriter writer=new PrintWriter("1200_Meij_14");
		String line=null;
		int tc=0,rc=0;
		while((line=reader.readLine())!=null){
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(line,
							"com.salience.collect.SeimpTrainingRow");
			if(!isValidRecord(row)) continue;
						
			final List<String> annKbEntries=computeAnnKBEntries(row);
			final List<String> masterKbEntries=computeMasterKBEntries(row);
			final List<String> cmnsConcepts=rankConcepts(genConcepts(tokenizeTweet(row.get_id())));
			if(masterKbEntries.size()==0 || cmnsConcepts.size()==0) continue;
			
			for(final String q:cmnsConcepts){
				for(final String c:masterKbEntries){
					StringBuffer buffer=new StringBuffer();
					int target=0;
					if(annKbEntries.indexOf(c)!=-1) target=1;
					buffer.append(target+",");
					
					/*
					 * Feature computation
					 */
					/*
					try {
						//N-gram features
						buffer.append(len(q)+",");
						buffer.append(linkProbability(q)+",");
						
						//Concept features					
						buffer.append(inlinks(c)+",");
						buffer.append(wlen(c)+",");
						buffer.append(clen(c)+",");						
						
						//N-gram + concept features
						buffer.append(spr(q,c)+",");
						buffer.append(nct(q,c)+",");
						buffer.append(tcn(q,c)+",");
						buffer.append(ten(q,c)+",");
						
						//Tweet features
						buffer.append(twct(q,row.getText())+",");
						buffer.append(tctw(q,row.getText())+",");
						buffer.append(tetw(q,row.getText())+",");
						buffer.append(tagDef(q,row.getText()));
						//buffer.append(url(q,row.getText()));
						
						writer.println(buffer.toString());
						
						++rc;
						System.out.println(tc+"-"+rc);
					} catch(Exception e) {
						e.printStackTrace();
					}				
				}
			}			
			++tc;
			
		}
		writer.close();
		reader.close();
		*/
	}
	
	private static boolean isValidRecord(final SeimpTrainingRow row) {
		/*
		 * Returns true if the record is valid one to process.
		 */
		if(row.getAnnotationList()==null || row.getAnnotationList().size()==0) return false;
		final Annotation ann=row.getAnnotationList().iterator().next();
		return (ann.getKbList()!=null && ann.getKbList().size()!=0);
	}
	
	private static List<String> computeMasterKBEntries(final SeimpTrainingRow row){
		/*
		 * Returns the list of potential KBEntries for a tweet. 
		 */
		final List<String> masterKBList=new ArrayList<String>();
		final List<Concept> masterConceptList=row.getConceptList();
		if(masterConceptList==null || masterConceptList.size()==0) return masterKBList;
		for(final Concept concept:masterConceptList){
			if(concept.getKbEntries()!=null) {
				final List<String> kbList=concept.getKbEntries();
				for(int i=0;(i<kbList.size() && i<2);i++)
					masterKBList.add(kbList.get(i).toLowerCase());
			}
		}
		return masterKBList;
	}
	
	private static List<String> computeAnnKBEntries(final SeimpTrainingRow row){
		/*
		 * Returns the list of user annotated KBEntries for a tweet. 
		 */
		final List<String> annKBList=new ArrayList<String>();
		final List<KBAnnotation> annConceptList=row.getAnnotationList().iterator().next().getKbList();
		if(annConceptList==null || annConceptList.size()==0) return annKBList;
		for(final KBAnnotation kbAnn:annConceptList){
			if(!kbAnn.getKbEntry().equalsIgnoreCase("none"))
				annKBList.add(kbAnn.getKbEntry().toLowerCase());
		}
		return annKBList;
	}

	/*
	 * List of meij features.
	 */
	private static int len(final String q) {
		/*
		 * Returns number of terms in the n-gram q
		 */
		if (q == null || q.trim().length() == 0)
			return 0;
		return q.split(" ").length;
	}

	private static Double linkProbability(final String q)
			throws ClassNotFoundException {
		/*
		 * Probability that q is used as an anchor text in Wikipedia
		 */
		if (q == null || q.trim().length() == 0)
			return 0.0;
		final AnchorDBRow row = getAnchorDbRow(q);
		return (double) row.getAnchor_freq()
				/ ((double) (row.getAnchor_freq() + row.getTotal_freq()));
	}
	
	/*
	 * CONCEPT FEATURES
	 */
	private static HashMap<String,Integer> inlinkMap=new HashMap<String,Integer>();
	private static int inlinks(final String c) throws IOException{
		/*
		 * Number of Wikipedia articles linking to c
		 */
		final String key="Category:"+c;
		if(inlinkMap.get(key)==null) {
			String response=null;
			try {
				response=Utilities.makeGetCall(AppGlobals.GET_WIKI+key, AppGlobals.HTTP_PROXY);
			} catch(Exception e){
				return 0;
			}
			int score=0;
			if(response!=null) {
				final Document doc=Jsoup.parse(response);
				boolean isFound=false;
				for(final Element divElement:doc.select("div")) {
					if(divElement.attr("id").equals("mw-pages")) {
						for(final Element pTag:divElement.select("p")) {
							if(pTag.text().startsWith("The following ")) {
								score=Integer.parseInt(pTag.text().split(" ")[2]);
								isFound=true;
							}
							if(isFound==true) break;
						}
					}
					if(isFound==true) break;
				}
			}
			inlinkMap.put(key,score);
		}
		return inlinkMap.get(key);
	}
	
	private static int wlen(final String c){
		/*
		 * Number of terms in the title of c
		 */
		if(c.trim().length()==0) return 0;
		return c.split("_").length;
	}
	
	private static int clen(final String c){
		/*
		 * Number of characters in the title of c
		 */
		return c.length();
	}
	
	/*
	 * N-gram + concept features
	 */
	
	private static int nct(final String q,final String c){
		/*
		 * Does q contain the title of c?
		 */		
		return q.toLowerCase().contains(c.toLowerCase())?1:0;
	}
	
	private static int tcn(final String q,final String c){
		/*
		 * Does the title of c contain q?
		 */		
		return c.toLowerCase().contains(q.toLowerCase())?1:0;
	}
	
	private static int ten(final String q,final String c){
		/*
		 * Does the title of c equal q?
		 */		
		return q.toLowerCase().equals(c.toLowerCase())?1:0;
	}
	
	private static int spr(final String q,final String c){
		/*
		 * Distance between the first and last occurrence of q in c
		 */
		int first=c.toLowerCase().indexOf(q.toLowerCase());
		int last=c.toLowerCase().lastIndexOf(q.toLowerCase());
		if(first==-1 || last==-1) return -1;
		return last-first;
	}
	
	
	/*
	 * TWEET FEATURES
	 */
	private static int twct(final String c, final String Q) {
		/*
		 * Does Q contain the title of c?
		 */
		return Q.toLowerCase().contains(c.toLowerCase())?1:0;
	}
	
	private static int tctw(final String c, final String Q) {
		/*
		 * Does the title of c contain Q?
		 */
		return c.toLowerCase().contains(Q.toLowerCase())?1:0;
	}
	
	private static int tetw(final String c, final String Q) {
		/*
		 * Does the title of c equal Q?
		 */
		return Q.toLowerCase().equals(c.toLowerCase())?1:0;
	}	
	
	private static int tagDef(final String q, final String Q)
			throws IOException, ClassNotFoundException {
		/*
		 * Number of times q appears in the hashtag definition of any hashtag in
		 * tweet Q
		 */
		final List<String> hashTagsList = getHashTags(Q);
		if (hashTagsList.size() == 0)
			return 0;
		int count=0;
		for (final String hashTag : hashTagsList) {
			String response = null;
			try {
				response=Utilities.makeGetCall(
							AppGlobals.GET_TAG_DEF_API.replace("???", hashTag),
							AppGlobals.HTTP_PROXY);
			} catch(Exception e){
				
			}
			if(response!=null) {
				final TagDefMaster row = (TagDefMaster) Utilities.convertToPOJO(
						response, "com.salience.meij.pojo.TagDefMaster");
				if(row.getNum_defs()!=0) {
					final TagDefDef tdds=row.getDefs().getDef();
					for(final String word:tdds.getText().split(" "))
						if(word.toLowerCase().contains(q.toLowerCase()))
							++count;					
				}
			}
		}
		return count;
	}
	
	private static int url(final String q, final String Q){
		/*
		 * Number of times q appears in a webpage linked to by Q
		 */
		final List<String> urlList=getUrls(Q);
		if(urlList.size()==0) return 0;
		int count=0;
		for(final String url:urlList) {
			Document doc=null;
			try {
				doc=Jsoup.parse(Utilities.makeGetCallRedirectHandled(url, AppGlobals.HTTP_PROXY));
			} catch(Exception e) {
				e.printStackTrace();
				continue;
			}			
			String bodyText=doc.select("body").text();
			while(bodyText.contains(q)) {
				++count;
				bodyText=bodyText.substring(bodyText.indexOf(q)+q.length());
			}
		}
		return count;
	}

	private static String cleanTweet(final String text) {
		/*
		 * Removes the URLS, '@' before mention and '#' before hastags.
		 */
		final StringBuffer buffer = new StringBuffer();
		for (final String word : text.split(" ")) {
			if (!word.matches("^(http|https|ftp)://.*$")) {
				if (word.startsWith("@") || word.startsWith("#"))
					buffer.append(word.substring(1) + " ");
				else
					buffer.append(word + " ");
			}
		}
		return buffer.toString().trim();
	}

	private static List<String> getHashTags(final String tweet) {
		/*
		 * Extract all the hast-tags from the tweet.
		 */
		final List<String> hashTagList = new ArrayList<String>();
		if (tweet != null && tweet.trim().length() != 0) {
			for (final String word : tweet.split(" "))
				if (word.startsWith("#"))
					hashTagList.add(word.substring(1));
		}
		return hashTagList;
	}
	
	private static List<String> getUrls(final String tweet) {
		/*
		 * Extract all the URLs from the tweet.
		 */
		final List<String> urlList=new ArrayList<String>();
		for (final String word : tweet.split(" ")) {
			if (word.matches("^(http|https|ftp)://.*$"))
				urlList.add(word);
		}
		return urlList;		
	}

	private static List<String> tokenizeTweet(final long tweetId)
			throws IOException, ClassNotFoundException {
		/*
		 * Tokenize the tweet and return them.
		 */
		final String response = accessRitter(tweetId);
		final List<String> tokenList = new ArrayList<String>();
		if (response != null && response.trim().length() > 0) {
			for (final String tokenInfo : response.split(" "))
				tokenList.add(tokenInfo.split("/")[0]);
		}
		return tokenList;
	}
	
	private static String accessRitter(final long tweetId) throws ClassNotFoundException {
		/*
		 * Call ritter system.
		 */
		final BasicDBObject bdbo=new BasicDBObject();
		bdbo.put("_id",tweetId);
		final DBCursor cursor=MongoDbManager.getCollection(AppGlobals.MONGO_DB_NAME, "ritterResponse").find(bdbo);
		RitterResponse response=null;
		boolean isFound=false;
		while(cursor.hasNext()){
			response = (RitterResponse) Utilities.convertToPOJO(cursor.next().toString(),"com.salience.ml.RitterResponse");
			isFound=true;
		}
		if(!isFound) {
			System.out.println("Tweet with id "+tweetId+" does not exist.");
			System.exit(0);
		}
		return response.getTags();
	}

	private static List<String> rankConcepts(final List<String> conceptList)
			throws ClassNotFoundException {
		/*
		 * Returns a list of concepts sorted by decreasing order of rank.
		 */
		final List<ConceptRow> sortedList = new ArrayList<ConceptRow>();
		for (final String concept : conceptList) {
			final Double score = computeCommonness(concept);
			if (score != 0.0) {
				final ConceptRow row = new ConceptRow(concept,
						computeCommonness(concept));
				sortedList.add(row);
			}
		}
		Collections.sort(sortedList);

		final List<String> finalConceptList = new ArrayList<String>();
		for (int i = 0; (i < sortedList.size() && i < TOP_N_CONCEPTS); i++)
			finalConceptList.add(sortedList.get(i).concept.toLowerCase());
		return finalConceptList;
	}

	private static List<String> genConcepts(final List<String> tokenList) {
		/*
		 * Returns all possible n-grams from the set of tokens.
		 */
		if (tokenList == null || tokenList.size() == 0)
			return null;
		final List<String> conceptList = new ArrayList<String>();
		for (int i = 1; i <= tokenList.size(); i++) {
			for (int j = 0; j <= tokenList.size() - i; j++) {
				final StringBuffer buffer = new StringBuffer();
				for (int k = j; k < j + i; k++) {
					buffer.append(tokenList.get(k) + " ");
				}
				conceptList.add(buffer.toString().trim());
			}

		}
		return conceptList;
	}

	private static MongoClient mongoClient = null;

	private static Double computeCommonness(final String q)
			throws ClassNotFoundException {
		/*
		 * Returns the commonness value for the string q.
		 */
		final AnchorDBRow row = getAnchorDbRow(q);
		if (row == null || row.getAnchor_freq() == 0 || row.pages.size() == 0)
			return 0.0;

		// Compute the commonness score
		int maxScore = Integer.MIN_VALUE;
		for (final AnchorPageRow pRow : row.getPages())
			if (pRow.getPage_freq() > maxScore)
				maxScore = pRow.getPage_freq();
		return ((double) maxScore / (double) row.getAnchor_freq());
	}
	
	private static Double computeCommonness(final String q,final String c)
			throws ClassNotFoundException {
		/*
		 * Returns the commonness value for the string q.
		 */
		final AnchorDBRow row = getAnchorDbRow(q);
		if (row == null || row.getAnchor_freq() == 0 || row.pages.size() == 0)
			return 0.0;

		// Compute the commonness score
		int maxScore = Integer.MIN_VALUE;
		for (final AnchorPageRow pRow : row.getPages())
			if (pRow.getPage_freq() > maxScore)
				maxScore = pRow.getPage_freq();
		return ((double) maxScore / (double) row.getAnchor_freq());
	}

	private static AnchorDBRow getAnchorDbRow(final String q)
			throws ClassNotFoundException {
		/*
		 * Returns the matching anchor row if any.
		 */
		// Compose the mongo query.
		final BasicDBObject whereDBO = new BasicDBObject();
		whereDBO.put("anchor", q.toLowerCase());
		final DBCursor cursor = mongoClient.getDB(AppGlobals.ANCHOR_DB_NAME)
				.getCollection(AppGlobals.ANCHOR_DB_COLLECTION_NAME)
				.find(whereDBO);
		AnchorDBRow row = null;
		while (cursor.hasNext()) {
			row = (AnchorDBRow) Utilities.convertToPOJO(cursor.next()
					.toString(), "com.salience.meij.pojo.AnchorDBRow");
		}
		return row;
	}

	static {
		try {
			mongoClient = new MongoClient(new ServerAddress(
					AppGlobals.ANCHOR_DB_IP, AppGlobals.ANCHOR_DB_PORT));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}

}

class ConceptRow implements Comparable<ConceptRow> {

	public String concept;
	public Double rank;

	public ConceptRow() {
	}

	public ConceptRow(String concept, Double rank) {
		this.concept = concept;
		this.rank = rank;
	}

	@Override
	public int compareTo(ConceptRow doc) {
		int val = this.rank.compareTo(doc.rank);
		if (val > 0)
			return -1;
		if (val < 0)
			return 1;
		return 0;
	}
}