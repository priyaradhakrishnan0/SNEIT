
package com.salience.util.mongo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.HashMap;

import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;
import com.salience.collect.SeimpTrainingRow;
import com.salience.commons.AppGlobals;
import com.salience.util.TwitterManager;
import com.salience.util.Utilities;

public class MongoUtil {

	public static void main(final String[] argv) throws Exception {
		/*
		String db = "seimp";
		String col = "completeDataset";
		MongoClient mongoClient = new MongoClient(new ServerAddress(
				"localhost", 27017));
		DBCollection dbc = mongoClient.getDB(db).getCollection(col);

		String[] keywords = new String[] { "#NZvsWI", "#WIvsNZ", "#PakvsAus",
				"#AUSvsPAK", "#INDvsBAN", "#BANvsIND", "#SAvSL", "#SLvSA",
				"#IndvsAus", "#AUSvsIND", "#AUSvsIND", "#nzvssa", "#NZvsAus",
				"#AUSvsNZ", "#NZvWI", "#WIvNZ", "#PakvAus", "#AUSvPAK",
				"#INDvBAN", "#BANvIND", "#SAvL", "#SLvA", "#IndvAus",
				"#AUSvIND", "#AUSvIND", "#nzvsa", "#NZvAus", "#AUSvNZ" };
		writer = new PrintWriter("out");
		for (String key : keywords)
			createBigCollection(dbc, key);

		writer.close();
		*/
		/*
		final PrintWriter writer = new PrintWriter("dataset.txt");
		final DBCollection dbc = MongoDbManager.getCollection("seimp",AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			if(row.getAnnotationList()!=null && !row.getAnnotationList().iterator().next().getComments().equalsIgnoreCase("X")){
				final Annotation ann=row.getAnnotationList().iterator().next();
				writer.println(row.getText().replaceAll("\n"," ")+"\t"+Utilities.toStr(row.getMergedNeList())+"\t"+Utilities.toStr(ann.getSneList()));
			}
		}
		writer.close();		
		*/
		
		/*final PrintWriter writer = new PrintWriter("CricketWordldCup2015Dataset.txt");
		final DBCollection dbc = MongoDbManager.getCollection("seimp",AppGlobals.COMPLETE_DATASET_COLLECTION_NAME);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		int count=0;
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			writer.println(row.getText().replaceAll("\n"," "));
			System.out.println(count++);
		}
		writer.close();*/
		
		
		//loadToMongo("localhost","10.2.4.21",27017,AppGlobals.MONGO_DB_NAME,"a1");
		//loadToMongo("localhost","10.2.4.21",27017,AppGlobals.MONGO_DB_NAME,"a2");
		//writeToDisk("10.2.4.21",27017,"seimp", "a1", "a1.txt");
		//writeToDisk("localhost",27017,"seimp", "largeseimptrainingset", "4500-5000.txt");
		//loadToMongo("localhost", "10.2.4.249", 27017, "seimp", "largeseimptrainingset");
		//collate();
		
		final BufferedReader br=new BufferedReader(new FileReader("master_10938.txt"));
		final PrintWriter writer=new PrintWriter("cwc15_10938.json");
		String line=null;
		final Gson gson=new Gson();
		while((line=br.readLine())!=null){
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(line,
							"com.salience.collect.SeimpTrainingRow");
			row.setText("");
			writer.println(gson.toJson(row));
		}
		writer.close();
		br.close();
	}
	
	public static void collate() throws Exception{
		final BufferedReader reader=new BufferedReader(new FileReader("master_index.txt"));
		String line=null;
		HashMap<String,DBCollection> map=new HashMap<String,DBCollection>();
		MongoClient mc1=new MongoClient(new ServerAddress("localhost",AppGlobals.MONGO_DB_PORT));
		map.put("localhost",mc1.getDB("seimp").getCollection("largeseimptrainingset"));
		MongoClient mc2=new MongoClient(new ServerAddress("10.2.4.21",AppGlobals.MONGO_DB_PORT));
		map.put("10.2.4.21",mc2.getDB("seimp").getCollection("largeseimptrainingset"));
		MongoClient mc3=new MongoClient(new ServerAddress("10.2.4.249",AppGlobals.MONGO_DB_PORT));
		map.put("10.2.4.249",mc3.getDB("seimp").getCollection("largeseimptrainingset"));
		final PrintWriter writer=new PrintWriter("master_10938.txt");
		while((line=reader.readLine())!=null){
			String[] content=line.split("=");
			DBCollection coll=map.get(ipAssign(Integer.parseInt(content[0])));
			BasicDBObject bdbo=new BasicDBObject();
			bdbo.put("_id", Long.parseLong(content[1]));
			DBCursor cursor=coll.find(bdbo);
			while(cursor.hasNext()){
				writer.println(cursor.next().toString());				
			}		
		}
		writer.close();
		reader.close();		
	}
	
	
	public static String ipAssign(final int no){
		System.out.println(no);
		if(1<=no && no<=3000) return "localhost";
		if(3001<=no && no<=4500) return "10.2.4.21";
		if(4501<=no && no<=5000) return "10.2.4.249";
		if(5000<=no && no<=6999) return "10.2.4.21";
		if(7000<=no && no<=10938) return "localhost";
		return null;
	}

	static PrintWriter writer = null;

	public static void createBigCollection(final DBCollection dbc,
			final String keyword) throws TwitterException, InterruptedException {
		// Creates a tweet collection obtained from supplied keyword.
		final Twitter twitter = TwitterManager.getTwitterInstance();

		// Compose the query.
		final Query query = new Query(keyword + " -filter:retweets");
		query.setLang("en");
		query.setCount(100);

		long lastID = Long.MAX_VALUE;
		int total = 0;
		while (true) {
			final QueryResult res = twitter.search(query);

			// Stopping criterion
			if (res.getTweets() == null || res.getTweets().size() == 0)
				break;

			// Parse the tweets
			int processed = 0;
			for (final Status status : res.getTweets()) {
				SeimpTrainingRow row = null;
				// Process only if the tweet is in english.
				if (status.getLang().equals("en")) {
					// get the media entities from the status
					MediaEntity[] media = status.getMediaEntities();
					for (MediaEntity m : media) {
						if (m.getMediaURL().contains("jpg")) {
							if (row == null) {
								row = new SeimpTrainingRow();

								// Set the tweet parameters.
								row.set_id(status.getId());
								row.setText(status.getText());
								row.setCreatedAt(status.getCreatedAt());
								row.setUserId(status.getUser().getId());
								row.setFavoriteCount(status.getFavoriteCount());
								row.setRetweetCount(status.getRetweetCount());
							}
							row.addImage(m.getMediaURL());
						}
					}
					if (row != null) {
						// check if the row exist already in mongo.
						final BasicDBObject whereClause = new BasicDBObject();
						whereClause.put("_id", status.getId());
						if (dbc.find(whereClause).size() == 0) {
							// save in mongo.
							insertJSON(dbc, row);
						}
						++processed;
						total++;
					}
				}

				// Set the max id.
				if (status.getId() < lastID)
					lastID = status.getId();
			}

			writer.println("Saved " + processed + " from "
					+ res.getTweets().size() + " tweets.");

			// Handle twitter rate-limit 450 request per 15 min.
			Thread.sleep(3000);

			query.setMaxId(lastID - 1);
		}
		writer.println("Completed for -" + keyword + " (" + total + ")");

	}

	public static void insertJSON(final DBCollection dbc, final Object content) {
		// Convert the content to DB Object and insert to specified
		// collection/db.

		// Convert to JSON, followed by DBObject
		final String jsonContent = Utilities.convertToJson(content);
		final DBObject dbo = (DBObject) JSON.parse(jsonContent);

		// Insert into Mongo.
		dbc.insert(dbo);
	}

	public static void writeToDisk(final String dbIp,final int dbPort,final String dbName,
			final String collectionName, final String fileName)
			throws IOException {
		// Write the entire collection into a file.
		final PrintWriter writer = new PrintWriter(fileName);
		final MongoClient mongoClient = new MongoClient(new ServerAddress(dbIp,
				dbPort));
		final DBCollection dbc = mongoClient.getDB(dbName).getCollection(
				collectionName);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (cursor.hasNext()) {
			writer.println(cursor.next().toString());
		}
		writer.close();
	}

	public static void loadToMongo(final String dbIp, final int dbPort,
			final String dbName, final String collectionName,
			final String filePath) throws IOException {
		// Loads the content specified by 'filePath' to the supplied database.
		final MongoClient mongoClient = new MongoClient(new ServerAddress(dbIp,
				dbPort));
		final DBCollection dbc = mongoClient.getDB(dbName).getCollection(
				collectionName);
		final BufferedReader reader = new BufferedReader(new FileReader(
				filePath));
		String line = null;
		while ((line = reader.readLine()) != null) {
			final DBObject dbo = (DBObject) JSON.parse(line);
			dbc.insert(dbo);
		}
		reader.close();
	}

	public static void loadToMongo(final String srcDbIp, final String destDbIp,
			final int port, final String dbName, final String collectionName)
			throws UnknownHostException {
		// Loads the collection from source db to target db.
		final MongoClient srcMongoClient = new MongoClient(new ServerAddress(
				srcDbIp, port));
		final DBCollection srcDbc = srcMongoClient.getDB(dbName).getCollection(
				collectionName);
		final MongoClient destMongoClient = new MongoClient(new ServerAddress(
				destDbIp, port));
		final DBCollection destDbc = destMongoClient.getDB(dbName)
				.getCollection(collectionName);
		final DBCursor cursor = srcDbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (cursor.hasNext()) {
			final DBObject dbo = (DBObject) JSON
					.parse(cursor.next().toString());
			destDbc.insert(dbo);
		}
	}
}
