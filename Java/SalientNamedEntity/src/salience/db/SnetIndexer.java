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


public class SnetIndexer {


	/*Class - creates a mongoDB of tweet to SNE 
	 *  Typical record tweetText | ranked list of SNEs
	 *  */
		private static MongoClient mongoClient; 
		private static DB db;
		private static DBCollection table;
		/*Constructor*/
		public SnetIndexer () {
			try {
				Mongo m1 = Mongo.Holder.singleton().connect(new MongoURI(Variables.currentDB));
				db = m1.getDB("snetDB");
				table = db.getCollection("snet");
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

		public void indexDoc (Long tweetId, String tweet, List<String> sneList) {
			ArrayList<DBObject> neL = new ArrayList<DBObject>();
			if(sneList.size()>0){
				for(String ne : sneList){
					BasicDBObject ne_doc = new BasicDBObject("ne", ne);
					neL.add(ne_doc);
				}
			}
			BasicDBObject doc = new BasicDBObject("tweetId", tweetId).
					append("tweet", tweet).
					append("neList", neL);
			if(doc != null){
				table.insert(doc);
			} else {
				System.out.println("Unable to inser null");
			}
			//System.out.println("Indexed tweetId "+tweetId);
		}
		
	
		/* Returns List of NEs in the first tweet of tweetId */
		public ArrayList<String> getSNE (Long tweetId) {
			db.requestStart();
			ArrayList<String> collection = new ArrayList<String>();			
			BasicDBObject query = new BasicDBObject(); 
			query.put( "tweetId", tweetId);
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
		

		public static void main(String[] args) {
			SnetIndexer snetIndexer = new SnetIndexer();
		}//main

	}//class