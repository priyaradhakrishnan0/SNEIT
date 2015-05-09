package com.salience.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;
import com.salience.client.Service;
import com.salience.shared.Annotation;
import com.salience.shared.KBAnnotation;
import com.salience.shared.SeimpTrainingRow;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ServiceImpl extends RemoteServiceServlet implements
		Service {
	
	public Integer getTweetCount(final String dbName,final String collection){
		//Returns the no. of rows in the supplied db.
		return MongoDbManager.getCollection(dbName, collection).find().size();
	}
	
	public SeimpTrainingRow getTweet(final String dbName,final String collection,final int row){
		//Returns the tweet corresponding to the row no. specified when sorted by createdAt.
				
		int countRows=getTweetCount(dbName, collection);
		//Check if the specified row is within the limits.
		//compute(dbName,collection,row);
		//final List<String> goodTweet=getGoodTweet();
		//final List<String> tweetList=new ArrayList<String>();		
		final List<String> outList=new ArrayList<String>();
		if(1<=row && row<=countRows) {			
			final DBCollection dbc=MongoDbManager.getCollection(dbName, collection);
			//Sort records by createdAt in ascending order.
			final DBCursor cursor=dbc.find().sort(new BasicDBObject("_id",1));
			//Iterate over the cursor.
			int curRow=1;
			while(cursor.hasNext()){
				final BasicDBObject dbo=(BasicDBObject)cursor.next();
				/*if(goodTweet.indexOf(""+curRow)!=-1)
					tweetList.add(dbo.toString());*/
				SeimpTrainingRow prow=null;
				try {
					prow=(SeimpTrainingRow)Utilities.convertToPOJO(dbo.toString(),"com.salience.shared.SeimpTrainingRow");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				outList.add(curRow+"="+prow.get_id());
				if(curRow==row){
					writeToDisk(outList);
					return prow;
				}
				curRow++;
			}
		}
		return null;		
	}
	
	
	private static void writeToDisk(final List<String> outList){
		PrintWriter writer;
		try {
			writer = new PrintWriter("master_index.txt");
			for(final String line:outList)
				writer.println(line);
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public List<String> getGoodTweet(){
		final List<String> goodTweets=new ArrayList<String>();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader("C:/Users/Ganesh/workspace/SEIMPAnnotator/goodKB"));
			String line=null;
			while((line=reader.readLine())!=null){
				goodTweets.add(line.trim());
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return goodTweets;
	}
	
	public void saveAll(final List<String> rowList){
		MongoClient mc=null;
		try {
			mc=new MongoClient(new ServerAddress("localhost",ServerGlobals.MONGO_DB_PORT));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DBCollection dbc=mc.getDB("seimp").getCollection("a1");
		for(final String row:rowList) {
			final DBObject dbo=(DBObject)JSON.parse(row);
			dbc.insert(dbo);			
		}
		dbc=mc.getDB("seimp").getCollection("a2");
		for(final String row:rowList) {
			final DBObject dbo=(DBObject)JSON.parse(row);
			dbc.insert(dbo);			
		}
	}
	
	public void compute(final String dbName,final String collection,final int row){
		try{
			PrintWriter writer=new PrintWriter("tweet_ids");
			final DBCollection dbc=MongoDbManager.getCollection(dbName, collection);
			//Sort records by createdAt in ascending order.
			final DBCursor cursor=dbc.find().sort(new BasicDBObject("_id",1));
			//Iterate over the cursor.
			int curRow=1;
			while(cursor.hasNext()){
				SeimpTrainingRow r=(SeimpTrainingRow)Utilities.convertToPOJO(cursor.next().toString(),"com.salience.shared.SeimpTrainingRow");
				writer.println(r.get_id());
				curRow++;
				if(curRow==row){
					break;
				}
			}
			writer.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void saveSNE(final String dbName,final String collection,final SeimpTrainingRow newRow){
		//Updates the tweet with SNE's and possible comments.
		final BasicDBList bdbList = new BasicDBList();
		if(newRow.getAnnotationList()!=null) {
			for (final Annotation ann:newRow.getAnnotationList()) {
				final BasicDBObject bdbo = new BasicDBObject();
				bdbo.put("comments",ann.getComments());
				bdbo.put("annotator",ann.getAnnotator());
				bdbo.put("sneList",ann.getSneList());
				
				//Save the kb annotations
				if(ann.getKbList()!=null) {
					final BasicDBList kbDbList=new BasicDBList();
					for(final KBAnnotation kbann:ann.getKbList()){
						final BasicDBObject kbdbo=new BasicDBObject();
						kbdbo.put("ne",kbann.getNe());
						kbdbo.put("kbEntry",kbann.getKbEntry());
						kbDbList.add(kbdbo);
					}
					bdbo.put("kbList",kbDbList);
				}
				bdbList.add(bdbo);
			}
		}
		
		final BasicDBObject dbo=new BasicDBObject();
		dbo.append("$set", new BasicDBObject().append("annotationList",bdbList));		
		MongoDbManager.getCollection(dbName, collection).update(new BasicDBObject().append("_id",newRow.get_id()),dbo);		
	}

	public List<String> getUnannotatedList(final String dbName,final String collection,final String user){
		//Get un-annotated tweet ids, group them and return.
		final List<String> unannotatedList=new ArrayList<String>();
		final DBCollection dbc=MongoDbManager.getCollection(dbName, collection);
		//Sort records by createdAt in ascending order.
		final DBCursor cursor=dbc.find().sort(new BasicDBObject("_id",1));
		//Iterate over the cursor.
		int curRow=1,left=-1,right=-1;
		while(cursor.hasNext()){
			SeimpTrainingRow row=null;
			try {
				row = (SeimpTrainingRow)Utilities.convertToPOJO(((BasicDBObject)cursor.next()).toString(),"com.salience.shared.SeimpTrainingRow");
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if(row!=null) {
				if(isUnAnnotated(row,user)==true){
					if(left==-1) left=curRow;
					right=curRow;
				} else {
					if(left!=-1) unannotatedList.add(left+"-"+right);
					left=right=-1;
				}
			}
			curRow++;
		}
		if(left!=-1) unannotatedList.add(left+"-"+right);
		return unannotatedList;
	}
	
	private boolean isUnAnnotated(final SeimpTrainingRow row,final String user){
		//Returns true if the row is unannotated by any user or by this user.
		if(row.getAnnotationList()==null || row.getAnnotationList().size()==0) return true;
		for(final Annotation ann:row.getAnnotationList())
			if(ann.getAnnotator().equals(user))
				return false;
		return true;
	}
}