package salience.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import Variables.Variables;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoURI;
/*Class - creates a DBpedia store of DBpedia title to abstract*/
public class DbpediaIndex {

	private static MongoClient mongoClient; 
	private static DB db;
	private static DBCollection table;
	/*Constructor*/
	public DbpediaIndex () {
		try {
			Mongo m1 = Mongo.Holder.singleton().connect(new MongoURI(Variables.currentDB));
			db = m1.getDB("dbpediaDB");
			table = db.getCollection("dbpedia");
		} catch (UnknownHostException uke){
			uke.printStackTrace();
		}
	}
	
	public void destroy(){
		mongoClient.close();
	}

	public void indexDoc (String title, String Abstract) {
		BasicDBObject doc = new BasicDBObject("title", title).append("abstract", Abstract);
		table.insert(doc);
	}
	
	/* Checks if 'anchor' occurs in 'title' field in dbpedia */
	public boolean isTitle (String anchor) {
		db.requestStart();
		boolean occurs = false;			
		BasicDBObject query = new BasicDBObject(); 
		query.put( "title", anchor.trim());
		BasicDBObject fields = new BasicDBObject("abstract",true).append("_id",false);		
		DBObject obj = table.findOne(query, fields); //System.out.println("num of results = "+curs.count());
		if(obj != null) {
			//System.out.println("Link Freq = "+obj.get("anchor_freq").toString());
			occurs = true;
		}		
		db.requestDone();
		return occurs;		
	}//End isTitle()	

	/* Returns abstract of title 'anchor' in DBpedia */
	public String getAbstract (String anchor) {
		db.requestStart();
		String Abstract = null;			
		BasicDBObject query = new BasicDBObject(); // create an empty query 
		query.put( "title", anchor.trim());
		BasicDBObject fields = new BasicDBObject("abstract",true).append("_id",false);		
		DBObject obj = table.findOne(query, fields);//System.out.println("num of results = "+curs.count());
		if(obj != null) { //System.out.println("Freq = "+o.get("totalFreq").toString());
			Abstract =  obj.get("abstract").toString();
		}		
		db.requestDone();
		return Abstract;
	}//End getAbstract()
			
	public static void main(String[] args) {
		DbpediaIndex dbpediaIndex = new DbpediaIndex();
		String title = "India:_Matri_Bhumi";
		String abs = " Matri Bhumi or mother land";
		if(!dbpediaIndex.isTitle(title)){
			dbpediaIndex.indexDoc(title, abs);			
		}
		if(dbpediaIndex.isTitle(title)){
			System.out.println("Ha");			
		}
	}//main

}//class
