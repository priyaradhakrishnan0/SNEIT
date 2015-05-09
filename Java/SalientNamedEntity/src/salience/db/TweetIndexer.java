package salience.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import Variables.Variables;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoURI;


public class TweetIndexer {


	/*Class - creates a mongoDB of tweet to NE index
	 *  Typical record tweetId | tweetText | topic | Named Entity1 | Named Entity2 ..
	 *  number of Named Entities >= 0.
	 *  */
		private static MongoClient mongoClient; 
		private static DB db;
		private static DBCollection table;
		/*Constructor*/
		public TweetIndexer () {
			try {
				Mongo m1 = Mongo.Holder.singleton().connect(new MongoURI(Variables.currentDB));
				db = m1.getDB("tweetDB");
				table = db.getCollection("tweets");
				BasicDBObject indexField = new BasicDBObject("topic", true);
				table.createIndex(indexField);
			} catch (UnknownHostException uke){
				uke.printStackTrace();
			}
		}
		
		public void destroy(){
			table.dropIndexes();
			mongoClient.close();
		}

		public void indexDoc (Long tweetId, String tweet, String topic, String expertUser, List<String> neList) {
			ArrayList<DBObject> neL = new ArrayList<DBObject>();
			if(neList.size()>0){
				for(String ne : neList){
					BasicDBObject ne_doc = new BasicDBObject("ne", ne);
					neL.add(ne_doc);
				}
			}
			BasicDBObject doc = new BasicDBObject("tweetId", tweetId).
					append("tweet", tweet).
					append("topic", topic).
					append("expert", expertUser).
					append("neList", neL);
			if(doc != null){
				table.insert(doc);
			} else {
				System.out.println("Unable to inser null");
			}
			//System.out.println("Indexed tweetId "+tweetId);
		}
		
	/* Returns list of experts in the topic */
	public List<String> getExpertsOnTopic (String topic) {
		db.requestStart();
		BasicDBObject query = new BasicDBObject(); 
		query.put( "topic", topic.toLowerCase());
		List<String> expertList = table.distinct("expert", query);
		db.requestDone();
		return expertList;		
	}//End getTweetCountOnTopic()	
			
		/* Returns number of tweets in the topic */
		public Long getTweetCountOnTopic (String topic) {
			db.requestStart();
			BasicDBObject query = new BasicDBObject(); 
			query.put( "topic", topic.toLowerCase());
			Long Ntc = table.count(query);//(query, fields); //System.out.println("num of results = "+curs.count());
			db.requestDone();
			return Ntc;		
		}//End getTweetCountOnTopic()	

		/* Returns number of tweets by the user */
		public Long getTweetCountByExpert (String expert) {
			db.requestStart();
			BasicDBObject query = new BasicDBObject(); 
			query.put( "expert", expert);
			Long Ntc = table.count(query);//(query, fields); //System.out.println("num of results = "+curs.count());
			db.requestDone();
			return Ntc;		
		}//End getTweetCountOnTopic()	

		/* Returns count of tweets in the topic, containing qNE pair*/
		public int getNETweetCountOnTopic (String topic, String qNE1, String qNE2) {
			boolean nePair = false;
			int NEpairCount = 0;
			db.requestStart();
			BasicDBObject query = new BasicDBObject();  
			query.put( "topic", topic.toLowerCase());
			BasicDBObject fields = new BasicDBObject("neList",true).append("_id",false);		
			DBCursor cursor = table.find(query, fields);
			while( cursor.hasNext() ){
			    DBObject obj = cursor.next();
			    JSONParser jp = new JSONParser();
			    JSONArray jo = null;
				try {	//System.out.println(obj.get("neList"));
					jo = (JSONArray) jp.parse(obj.get("neList").toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				for(int i = 0; i < jo.size(); i++)
				{
				    JSONObject object = (JSONObject) jo.get(i);
					String NE = object.get("ne").toString();
					if(NE.equalsIgnoreCase(qNE1) || NE.equalsIgnoreCase(qNE2)){ //System.out.println(obj.get("neList"));
						if(nePair){
							NEpairCount++;
							nePair = false;				
						} else {
							nePair = true;							
						}
					}
				}//for ne
				nePair = false;
			}//while tweet
			db.requestDone();
			return NEpairCount;
		}//End getNETweetCountOnTopic

		/* Returns count of tweets in the topic, containing qNE */
		public int getNETweetCountOnTopic (String topic, String qNE) {
			int NETweetCountOnTopic = 0;
			db.requestStart();
			BasicDBObject query = new BasicDBObject();  
			query.put( "topic", topic.toLowerCase());
			BasicDBObject fields = new BasicDBObject("neList",true).append("_id",false);		
			DBCursor cursor = table.find(query, fields);
			while( cursor.hasNext() ){
			    DBObject obj = cursor.next();
			    JSONParser jp = new JSONParser();
			    JSONArray jo = null;
				try {	//System.out.println(obj.get("neList"));
					jo = (JSONArray) jp.parse(obj.get("neList").toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				for(int i = 0; i < jo.size(); i++)
				{
				    JSONObject object = (JSONObject) jo.get(i);
					String NE = object.get("ne").toString();
					if(NE.equalsIgnoreCase(qNE)){ //System.out.println(obj.get("neList"));
						++NETweetCountOnTopic;
						break;
					}
				}//for ne
			}//while tweet
			db.requestDone();
			return NETweetCountOnTopic;
		}//End getNETweetCountOnTopic
		
		/* Returns Probability of tweets in the topic, containing qNE */
		public double getProbNETweetOnTopic (String topic, String qNE) {
			int NETweetCountOnTopic = 0, TweetCountOnTopic = 0;
			db.requestStart();
			BasicDBObject query = new BasicDBObject();  
			query.put( "topic", topic.toLowerCase());
			BasicDBObject fields = new BasicDBObject("neList",true).append("_id",false);		
			DBCursor cursor = table.find(query, fields);
			while( cursor.hasNext() ){
				++TweetCountOnTopic;
			    DBObject obj = cursor.next();
			    JSONParser jp = new JSONParser();
			    JSONArray jo = null;
				try {	//System.out.println(obj.get("neList"));
					jo = (JSONArray) jp.parse(obj.get("neList").toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				for(int i = 0; i < jo.size(); i++)
				{
				    JSONObject object = (JSONObject) jo.get(i);
					String NE = object.get("ne").toString();
					if(NE.equalsIgnoreCase(qNE)){ //System.out.println(obj.get("neList"));
						++NETweetCountOnTopic;
						break;
					}
				}//for ne
			}//while tweet
			db.requestDone();
			System.out.println("NETweetCountOnTopic = "+ NETweetCountOnTopic +" TweetCountOnTopic = " + TweetCountOnTopic);
			return (double)(1.0 * NETweetCountOnTopic/TweetCountOnTopic);
		}//End getProbNETweetOnTopic
		
		/* Returns Salience Probability of qNE, given topic */
		public double getSalienceProb (String qNE, String topic) {
			double SalienceProb = 0;
			SalienceProb = 1.0 * getNETweetCountOnTopic(topic, qNE)/Variables.totalTweets; //System.out.println("Ps = "+SalienceProb);
			return SalienceProb;
		}//getSalienceProb
	
		/* Returns salience prob of NEs in a tweet */
		public double[] getNeSalProb (String topic, ArrayList<String> qNE) {
			double[] PS = new double[qNE.size()];
			int[] count = new int[qNE.size()];
			db.requestStart();
			BasicDBObject query = new BasicDBObject();  
			query.put( "topic", topic.toLowerCase());
			BasicDBObject fields = new BasicDBObject("neList",true).append("_id",false);		
			DBCursor cursor = table.find(query, fields);
			while( cursor.hasNext() ){
			    DBObject obj = cursor.next();
			    JSONParser jp = new JSONParser();
			    JSONArray jo = null;
				try {	//System.out.println(obj.get("neList"));
					jo = (JSONArray) jp.parse(obj.get("neList").toString());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				for(int i = 0; i < jo.size(); i++)
				{
				    JSONObject object = (JSONObject) jo.get(i);
					String NE = object.get("ne").toString();
					for(int j=0; j<qNE.size(); ++j){
						String qN = qNE.get(j).replaceAll("_", " ").toLowerCase();
						if(NE.equalsIgnoreCase(qN)){
							count[j]++;
						}
					}
				}//for ne
			}//while tweet
			db.requestDone();
			for(int i =0; i<qNE.size();++i){
				PS[i] = 1.0 * count[i]/Variables.totalTweets;
			}
			return PS;
		}//End getNETweetCountOnTopic
	
		/* Returns List of NEs in the first tweet on the topic. element[0] = tweet,element[1 to n] = NEs */
		public ArrayList<String> getNEbyTopic (String topic) {
			db.requestStart();
			ArrayList<String> collection = new ArrayList<String>();			
			BasicDBObject query = new BasicDBObject(); 
			query.put( "topic", topic.toLowerCase());
			BasicDBObject fields = new BasicDBObject("neList",true).append("tweet", true).append("_id",false);		
			DBObject obj = table.findOne(query, fields); 
			if(obj != null) { collection.add(obj.get("tweet").toString());;
				JSONParser jp = new JSONParser();
				JSONArray jarr = null;
				try {
					jarr = (JSONArray) jp.parse(obj.get("neList").toString());
				} catch (ParseException e) {
					jarr = new JSONArray();
				}
				//System.out.println("Link Freq = "+o.get("anchPageFreq").toString());				
				for(int i = 0; i < jarr.size(); i++)
				{
				    JSONObject object = (JSONObject) jarr.get(i);
					collection.add(object.get("ne").toString());
				}//for ne				
			}		
			db.requestDone();
			return collection;		
		}//End getNEs()
		
		/* Returns List of unique NEs and NEcombinations an expert mentioned in his tweets */
		public HashSet<String> getNEbyExpert (String expert) {
			db.requestStart();
			HashSet<String> collection = new HashSet<String>();			
			HashSet<String> combiningNE = new HashSet<String>();
			BasicDBObject query = new BasicDBObject(); 
			query.put( "expert", expert);
			BasicDBObject fields = new BasicDBObject("neList",true).append("_id",false);		
			DBCursor curr = table.find(query,fields);
			while(curr.hasNext()){ //System.out.println("Results = "+curr.count());
				DBObject obj = curr.next();
				if(obj != null) { 	//System.out.println("NE list = "+obj.get("neList").toString());
					JSONParser jp = new JSONParser();
					JSONArray jarr = null;
					try {
						jarr = (JSONArray) jp.parse(obj.get("neList").toString());
					} catch (ParseException e) {
						jarr = new JSONArray();
					}
	
					//if num of NEs is more than one, create adjacency matrix
					if(jarr.size()>1){
						for(int i = 0; i < jarr.size(); i++)
						{
							JSONObject object1 = (JSONObject) jarr.get(i);
							String NE1 = object1.get("ne").toString();
							combiningNE.add(NE1);
							for(int j = i+1 ; j < jarr.size(); j++)
							{
								JSONObject object2 = (JSONObject) jarr.get(j);
								String NE2 = object2.get("ne").toString();
						    	collection.add(NE1+"##"+NE2); //System.out.println(NE1+"##"+NE2);
							}
						}//for co-occuring ne
					} /*else {
					    JSONObject object = (JSONObject) jarr.get(0);
						collection.add(object.get("ne").toString());
					}*/
				}
			}//while
			System.out.println("Expert "+expert+" num of pairs = "+collection.size());
			System.out.println("Expert "+expert+" num of combining NEs = "+combiningNE.size());
			collection.addAll(combiningNE);
			db.requestDone();
			return collection;		
		}//End getNEs()

		/* Returns List of unique NEs an expert mentioned in his tweets */
		public HashSet<String> getNEbyExpert1 (String expert) {
			db.requestStart();
			HashSet<String> collection = new HashSet<String>();			
			BasicDBObject query = new BasicDBObject(); 
			query.put( "expert", expert);
			BasicDBObject fields = new BasicDBObject("neList",true).append("_id",false);		
			DBCursor curr = table.find(query,fields);
			while(curr.hasNext()){ //System.out.println("Results = "+curr.count());
				DBObject obj = curr.next();
				if(obj != null) { 	//System.out.println("NE list = "+obj.get("neList").toString());
					JSONParser jp = new JSONParser();
					JSONArray jarr = null;
					try {
						jarr = (JSONArray) jp.parse(obj.get("neList").toString());
					} catch (ParseException e) {
						jarr = new JSONArray();
					}	
					for(int i = 0; i < jarr.size(); i++)
					{
						JSONObject object1 = (JSONObject) jarr.get(i);
						String NE1 = object1.get("ne").toString();
						NE1 = NE1.trim();
						if(NE1!=null)collection.add(NE1);
					}//for co-occuring ne				 
				}
			}//while
			//System.out.println("Expert "+expert+" num of pairs = "+collection.size());
			db.requestDone();
			return collection;		
		}//End getNEs()
		
		/* Returns Map of unique NEs to the Occurence count by an expert */
		public HashMap<String, Integer> getNEbyExpert2 (String expert) {
			db.requestStart();
			HashMap<String, Integer> collection = new HashMap<String, Integer>();			
			BasicDBObject query = new BasicDBObject(); 
			query.put( "expert", expert);
			BasicDBObject fields = new BasicDBObject("neList",true).append("_id",false);		
			DBCursor curr = table.find(query,fields);
			while(curr.hasNext()){ //System.out.println("Results = "+curr.count());
				DBObject obj = curr.next();
				if(obj != null) { 	//System.out.println("NE list = "+obj.get("neList").toString());
					JSONParser jp = new JSONParser();
					JSONArray jarr = null;
					try {
						jarr = (JSONArray) jp.parse(obj.get("neList").toString());
					} catch (ParseException e) {
						jarr = new JSONArray();
					}	
					for(int i = 0; i < jarr.size(); i++)
					{
						JSONObject object1 = (JSONObject) jarr.get(i);
						String NE1 = object1.get("ne").toString();
						int currentCount = 1;
						if(collection.containsKey(NE1)){
							currentCount = collection.get(NE1);
						}
						collection.put(NE1, currentCount);
					}
				}
			}//while
			System.out.println(expert+" : Num of NEs = "+collection.size());
			db.requestDone();
			return collection;		
		}//End getNEbyExpert2()

		/* Returns Map of unique NEs and NEcombinations to their occurence count as from expert's tweet */
		public HashMap<String, Integer> getAdjacency (String expert) {
			db.requestStart(); //int repCount = 0;
			HashMap<String, Integer> collection = new HashMap<String, Integer>();		
			//HashSet<String> combiningNE = new HashSet<String>();
			BasicDBObject query = new BasicDBObject(); 
			query.put( "expert", expert);
			BasicDBObject fields = new BasicDBObject("neList",true).append("_id",false);		
			DBCursor curr = table.find(query,fields);
			while(curr.hasNext()){ //System.out.println("Results = "+curr.count());
				DBObject obj = curr.next();
				if(obj != null) { 	//System.out.println("NE list = "+obj.get("neList").toString());
					JSONParser jp = new JSONParser();
					JSONArray jarr = null;
					try {
						jarr = (JSONArray) jp.parse(obj.get("neList").toString());
					} catch (ParseException e) {
						jarr = new JSONArray();
					}
	
					//if num of NEs is more than one, create adjacency matrix
					if(jarr.size()>1){
						for(int i = 0; i < jarr.size(); i++)
						{
							JSONObject object1 = (JSONObject) jarr.get(i);
							String NE1 = object1.get("ne").toString();
							int currentCount = 1;
//							if(collection.containsKey(NE1)){
//								currentCount = collection.get(NE1)+1;
//							}
//							collection.put(NE1, currentCount);							
//							
							for(int j = i+1 ; j < jarr.size(); j++)
							{
								JSONObject object2 = (JSONObject) jarr.get(j);
								String NE2 = object2.get("ne").toString();
								String nePair = NE1+"##"+NE2;
								currentCount = 1;
								if(collection.containsKey(nePair)){
									currentCount = collection.get(nePair)+1;
								}
								collection.put(nePair, currentCount);//System.out.println(NE1+"##"+NE2);
								//++repCount;
							}
						}//for co-occuring ne
					} /*else {
					    JSONObject object = (JSONObject) jarr.get(0);
						String NE = object.get("ne").toString();
						int currentCount = 1;
						if(collection.containsKey(NE)){
							currentCount = collection.get(NE)+1;
						}
						collection.put(NE, currentCount);
					} */
				}
			}//while
			//System.out.println(" num of element = "+collection.size()+" pairs = "+repCount);
			db.requestDone();
			return collection;		
		}//End getNEs()


//		/* Returns map of Wikipedia page-ids to number of inlinks to those pages. Page ids are pages the string 'anchor' points to in Wikipedia */
//		public Map<Long, Integer> getPagesMap (String anchor) {
//			db.requestStart();
//			Map<Long, Integer> PageCollection = new HashMap<Long, Integer>();			
//			BasicDBObject query = new BasicDBObject(); 
//			query.put( "anchor", anchor.toLowerCase());		
//			BasicDBObject fields = new BasicDBObject("page_id",true).append("pages", true).append("page_freq", true).append("anchor_freq", true).append("_id",false);		
//			DBObject ans = table.findOne(query, fields);//System.out.println("num of results = "+curs.count());
//			db.requestDone();
//			if(ans != null) {
//				JSONParser jp = new JSONParser();
//				JSONArray jo = null;
//				try {	//System.out.println(ans.get("pages"));
//					jo = (JSONArray) jp.parse(ans.get("pages").toString());
//				} catch (ParseException e) {
//					e.printStackTrace();
//				}//System.out.println("Link Freq = "+o.get("anchPageFreq").toString());
//				for(int i = 0; i < jo.size(); i++)
//				{
//				    JSONObject object = (JSONObject) jo.get(i);
//					Long pId = (long)(object.get("page_id"));
//					Long pValue0 = (long)object.get("page_freq");
//					int pValue = pValue0.intValue();
//					if(PageCollection.containsKey(pId)){
//						pValue = PageCollection.get(pId)+ pValue;
//					} 
//					PageCollection.put(pId, pValue);
//				}
//			}
//			return PageCollection;		
//		}//End getPagesMap()
//		
//		/* Returns map of Wikipedia page-ids to number of inlinks to those pages. 
//		 * Only those pages whose inlinks contribute to 'restriction' % of the total inlinks are returned.
//		 * Page ids are pages the string 'anchor' points to in Wikipedia. */
//		public Map<Long, Integer> getPagesMap (String anchor, double restriction) {
//			db.requestStart();
//			Map<Long, Integer> PageCollection = new HashMap<Long, Integer>();			
//			BasicDBObject query = new BasicDBObject(); 
//			query.put( "anchor", anchor.toLowerCase());		
//			BasicDBObject fields = new BasicDBObject("page_id",true).append("pages", true).append("page_freq", true).append("anchor_freq", true).append("_id",false);		
//			DBObject ans = table.findOne(query, fields);
//			db.requestDone();
//			if(ans != null) {
//				JSONParser jp = new JSONParser();
//				JSONArray jo = null;
//				int freq = (int)ans.get("anchor_freq");
//				try {
//					jo = (JSONArray) jp.parse(ans.get("pages").toString());
//				} catch (ParseException e) {				
//					e.printStackTrace();
//				}
//				for(int i = 0; i < jo.size(); i++)
//				{
//				    JSONObject object = (JSONObject) jo.get(i);
//					Long pId = (long)(object.get("page_id"));
//					Long pValue0 = (long)object.get("page_freq");
//					int pValue = pValue0.intValue();
//					if(PageCollection.containsKey(pId)){
//						pValue = PageCollection.get(pId)+ pValue;
//					}
//					if(pValue/(1.0*freq)>restriction){
//						PageCollection.put(pId, pValue);
//					}
//				}
//			}
//
//			return PageCollection;		
//		}//End getPagesMap()
//		
//		
//		/* Returns two member integer array. 
//		 * member 0 = total number of inlinks for the string anchor.
//		 * member 1 = number of inlinks to given PageId from the String anchor.*/
//		public int[] getPageCountInPages (String anchor, long PageId) {
//			db.requestStart();
//			int[] PageCountResults  = new int[2];;
//			int pageCount = 0;
//			int totalCount = 0;
//			BasicDBObject query = new BasicDBObject(); 
//			query.put("anchor", anchor.toLowerCase());
//			BasicDBObject fields = new BasicDBObject("pages",true).append("anchor_freq",true).append("total_freq",true).append("_id",false);		
//			DBObject obj = table.findOne(query, fields);//System.out.println("Pages Total = "+curs.count());
//			db.requestDone();
//			if(obj!=null) {
//				//System.out.println("Obj = "+o.get("pageId").toString());	
//				JSONParser jp = new JSONParser();
//				JSONArray jarr = null;
//				try {
//					jarr = (JSONArray) jp.parse(obj.get("pages").toString());
//				} catch (ParseException e) {
//					jarr = new JSONArray();
//				}
//				//System.out.println("Link Freq = "+o.get("anchPageFreq").toString());
//				for(int i = 0; i < jarr.size(); i++)
//				{
//					JSONObject jo = (JSONObject) jarr.get(i);
//					if(PageId == (long)jo.get("page_id")){
//						 Long pageCount0 = (long)jo.get("page_freq"); //++pageCount;
//						 pageCount += pageCount0.intValue();
//					} 
//				}
//				totalCount += (int)obj.get("anchor_freq");
//			}		
//			PageCountResults[1] = pageCount; //System.out.println("Pages matching = "+pageCount);
//			PageCountResults[0] = totalCount;
//			return PageCountResults;		
//		}//End getPageCountInPages()
//
//		/*link-probability - Probability that an occurrence of a is an anchor pointing to some Wikipedia page*/
//		public double lp(String anchor){
//			int totalFreq = getTotalFreq(anchor);
//			int totalLinkFreq = getTotalLinkFreq(anchor);
//			if(totalFreq+totalLinkFreq > 0){
//				//System.out.println("totFr = "+totalFreq+" totLinkFr = "+totalLinkFreq);
//				return totalLinkFreq*1.0/(totalFreq+totalLinkFreq);
//			} else {
//				return 0;
//			}
//		}
//
//		/*Get entity mentions of a query string*/
//		public ArrayList<String> mentions(String text){
//			text = text.toLowerCase();
//			text = text.replaceAll("[,.;]"," ").replaceAll("\\s+", " ");
//			ArrayList<String> mentions = new ArrayList<String>();
//			String split[] = text.split("[ _:.,]");
//			int length = split.length;
//			for(int i=0;i<length;i++){
//				double currLP = 0;
//				for(int j=6;j>0;j--){
//					if(i+j>length)
//						continue;
//					String cMention = "";
//					for(int k=i;k<i+j&&i+j<=length;k++){
//						cMention += split[k].toLowerCase().trim() + " ";
//					}
//					cMention = cMention.trim();
//					//System.out.println(cMention+ " " + lp(cMention));
//					if(lp(cMention)>0.01 && !cMention.equals("") && lp(cMention)>currLP){
//						mentions.add(cMention.trim());
//						currLP = lp(cMention);
//						//System.out.println(i+" "+j);
//						i = i + j - 1 ;
//						break;
//					}
//				}
//			}
//			
//			String curr = "";
//			if(mentions.size()>0)
//				curr = mentions.get(0);
//			for(int i = 1;i < mentions.size();i++){
//				if(curr.contains(mentions.get(i))){
//					System.err.println(curr+","+lp(curr) + " " +mentions.get(i)+","+ lp(mentions.get(i)));
//					if(lp(curr)*0.5 >= lp(mentions.get(i))){
//						//pruneMentions.add(i);
//						mentions.remove(i);
//						i = i-1;
//					}
//					else{
//						mentions.remove(curr);
//					}
//				}
//				else{
//					curr = mentions.get(i);
//				}
//			}
//			return mentions;
//		}

		public static void main(String[] args) {
			TweetIndexer tweetIndexer = new TweetIndexer();
			String[] liist = new String[]{	"automobilismo",
			                	"automotive",
			                	"automotriz",
			                	"boyband",
			                	"car dealer",
			                	"client",
			                	"cricketer",
			                	"fuel",
			                	"kuis",
			                	"lbc",
			                	"manufacturers",
			                	"marca",
			                	"mazda",
			                	"motorbikes",
			                	"musictech",
			                	"otomotif",
			                	"penske",
			                	"porsche",
			                	"sponsor",
			                	"vehicle"};
			//for(String topic:liist)
			//System.out.println(topic+"  -> "+tweetIndexer.getTweetCountOnTopic(topic));
				
			System.out.println(tweetIndexer.getTweetCountByExpert("DaleSteyn62"));
			
//			ArrayList<String> neLiist = new ArrayList<String>();
//			neLiist.add("Mahendra_Singh_Dhoni"); neLiist.add("Rahul_Dravid");
//			for(double d : tweetIndexer.getNeSalProb("cricketer", neLiist)){
//				System.out.println(d);
//			}
			//System.out.println("NE count of t in c = "+tweetIndexer.getNETweetCountOnTopic("suzany","Hace"));
			//System.out.println("NE neighbour " + tweetIndexer.getNEbyExpert("ShaneWarne"));//byTopic("cricketer"));//
			//System.out.println("NEs " + tweetIndexer.getNEbyTopic("cricketer"));//
			//System.out.println(tweetIndexer.getAdjacency("priya"));
			
			
			//String anchor = "Colette Avital"; //Israel";		
			//System.out.println("#pages for "+anchor+" = "+anchorIndexer.getTotalLinkFreq(anchor));
			
			/*Store index file in mongodb*/
			//		String sortedFile = "/path/index/xet_synonsm";
			//		anchorIndexer.indexSortedFile(sortedFile);

			/*absolute freq i.e number of times anchor a occurs in Wikipedia not as an anchor*/
			//		String anchor = "samsung galaxy";
			//		System.out.println("Anchor : "+anchor+", Total freq : "+anchorIndexer.getTotalFreq(anchor));

			/*link(a) = Link freq i.e number of times a occurs in Wikipedia as an anchor*/
			//		String anchor = args[1];
			//		System.out.println("Anchor : "+anchor+", Total anchor freq : "+anchorIndexer.getTotalLinkFreq(anchor));

			/*freq(a) = freq i.enumber of times a occurs in Wikipedia (as an anchor or not)*/
			//		System.out.println("Anchor : "+anchor+", Freq : "+(anchorIndexer.getTotalFreq(anchor)+anchorIndexer.getTotalLinkFreq(anchor)));

			/*Pg(a) = Pages pointed to by anchor a*/
			
//			ArrayList<String> candList = new ArrayList<String>();
//			candList.add("Paris Abbott");
//			candList.add("The World of Abbott and Costello");
//			candList.add("L. B. Abbott");
//			wikiPageIndex wikiIndex = new wikiPageIndex();
//			for(String candidate:candList){
//				List<Long> candPagIds = anchorIndexer.getPages(candidate.toLowerCase());
//				ArrayList<String> candVectorList = new ArrayList<String>();
//				for(Long candPgId : candPagIds){
//					String pg = wikiIndex.getTitle(candPgId);
//					pg = pg.replace('_', ' ');
//					System.out.println("Adding page "+pg);
//					candVectorList.add(pg);
//				}
//				System.out.println("Cand vector list "+candVectorList);
//			}
			
//					System.out.println("Anchor : "+anchor+", Pages returned : "+(anchorIndexer.getPages(anchor).size()));
//					for(Long pgId:anchorIndexer.getPages(anchor.toLowerCase())){
//						System.out.println("Page : "+pgId);
//					}

			/*Unique Pages pointed to by anchor a*/
			//		String anchor = "conference";
			//		System.out.println("Anchor : "+anchor+", Pages returned : "+(anchorIndexer.getPages(anchor).size()));
			//
			//		Map<Integer, Integer> Pb = anchorIndexer.getPagesMap(anchor);
			//		System.out.println("Anchor : "+anchor+", Unique Pages returned : "+(Pb.size()));
			//		for(int pgId:Pb.keySet()){
			//			System.out.println("Page : "+pgId+" num : "+Pb.get(pgId));
			//		}

			/*Prior Pr(p/a) = (pages in Pg(a) that is p) / ( Pg(a) )*/
			//		int pgId = Integer.parseInt(args[2]);		
			//		int[] ans = anchorIndexer.getPageCountInPages(anchor, 33364019);
			//		System.out.println("Prior Pr(p/a) = "+((1.0*ans[1]) / ans[0]));
		}//main

	}//class