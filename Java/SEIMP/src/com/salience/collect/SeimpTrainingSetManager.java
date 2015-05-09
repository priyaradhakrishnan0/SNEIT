package com.salience.collect;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import twitter4j.MediaEntity;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.Query.ResultType;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.salience.collect.kb.GCDsearch;
import com.salience.commons.AppGlobals;
import com.salience.util.TwitterManager;
import com.salience.util.Utilities;
import com.salience.util.mongo.MongoDbManager;
import com.salience.util.mongo.MongoUtil;

public class SeimpTrainingSetManager {

	public static void main(final String[] argv) throws Exception {
		long start = System.currentTimeMillis();
		//computeNE("seimp",AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME,AppGlobals.NER.ALAN_RITTER,AppGlobals.NER.ARK_TWEET,AppGlobals.NER.STANFORD_CRF);
		//MongoUtil.writeToDisk("10.2.4.21", 27017, "seimp", AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME, "inter_g.txt");
		//MongoUtil.loadToMongo("localhost","10.2.4.21", 27017, "seimp", AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);
		//fillMergedNeList("seimp",AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);
		//addConceptsToTweet("seimp",AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);	
		createDatasetFromTweetid("tweet_ids","3000_W_KB");
		System.out.println("Time taken - "
				+ ((System.currentTimeMillis() - start) / 1000) + " s.");
	}
	
	public static void createDatasetFromTweetid(final String src,final String dest) throws IOException{
		final PrintWriter writer=new PrintWriter(dest);
		final BufferedReader br=new BufferedReader(new FileReader(src));
		String line=null;
		final DBCollection dbc=MongoDbManager.getDB(AppGlobals.MONGO_DB_NAME).getCollection(AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME);
		while((line=br.readLine())!=null){
			final BasicDBObject whereClause = new BasicDBObject();
			whereClause.put("_id", Long.parseLong(line));
			final DBCursor cursor=dbc.find(whereClause);
			while(cursor.hasNext()){
				writer.println(cursor.next().toString());
			}
		}
		br.close();
		writer.close();		
	}

	public static void addConceptsToTweet(final String dbName,
			final String collection) throws ClassNotFoundException {
		/*
		 * Add the Google Wiki Disambiguated page concepts for every tweet and
		 * ne.
		 */
		final int MAX = 10;
		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collection);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		int count = 0;
		while (cursor.hasNext()) {		
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			if (row.getMergedNeList() != null && row.getConceptList()==null) {
				// Generate the concepts
				final BasicDBList bdbList = new BasicDBList();
				for (String ne : row.getMergedNeList()) {
					ne = ne.substring(0, ne.lastIndexOf("(") - 1).trim();
					final HashMap<String, Double> conceptMap = GCDsearch
							.searching(ne, row.getText());
					final Concept concept = new Concept();
					concept.setNe(ne);
					if (conceptMap == null)
						concept.setKbEntries(null);
					else {
						final LinkedHashMap<String, Double> sortedMap = (LinkedHashMap<String, Double>) Utilities
								.sortMapByValue(conceptMap);
						final List<String> resList = new ArrayList<String>();
						int i = 0;
						for (final Entry<String, Double> entry : sortedMap
								.entrySet()) {
							if (i < MAX && i < sortedMap.size())
								resList.add(entry.getKey());
							else
								break;
							i++;
						}
						concept.setKbEntries(resList);
					}

					final BasicDBObject bdbo = new BasicDBObject();
					bdbo.put("ne", concept.getNe());
					bdbo.put("kbEntries", concept.getKbEntries());
					bdbList.add(bdbo);
				}

				final BasicDBObject setConceptBdbo = new BasicDBObject();
				setConceptBdbo.put("$set",
						new BasicDBObject().append("conceptList", bdbList));
				dbc.update(new BasicDBObject().append("_id", row.get_id()),
						setConceptBdbo);
			}
			System.out.println(count++);
		}

	}

	public static void fillMergedNeList(final String dbName,
			final String collection) throws IOException, ClassNotFoundException {
		// For every tweet in the collection, collect the NE's from different
		// ner output and union them.
		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collection);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");

			// Set the annotation to null
			row.setAnnotationList(null);

			// Union the NE's
			final HashMap<String, List<String>> neMap = new HashMap<String, List<String>>();
			for (final NERList ner : row.getNerList()) {
				for (final String ne : ner.getNeList()) {
					if (neMap.get(ne) == null)
						neMap.put(ne, new ArrayList<String>());
					neMap.get(ne).add(ner.getName());
				}
			}
			final List<String> resNeList = new ArrayList<String>();
			for (final Entry<String, List<String>> entry : neMap.entrySet()) {
				final StringBuilder builder = new StringBuilder(entry.getKey());
				builder.append(" (");
				for (final String ner : entry.getValue())
					builder.append(ner + ",");
				builder.setLength(builder.length() - 1);
				builder.append(")");
				resNeList.add(builder.toString());
			}
			row.setMergedNeList(resNeList);

			// Save the merged NER output.
			final BasicDBObject setNEBdbo = new BasicDBObject();
			setNEBdbo.put(
					"$set",
					new BasicDBObject().append("mergedNeList",
							row.getMergedNeList()).append("annotationList",
							row.getAnnotationList()));
			dbc.update(new BasicDBObject().append("_id", row.get_id()),
					setNEBdbo);
		}

	}

	public static void computeNE(final String dbName, final String collection,
			final AppGlobals.NER... nerModules) throws IOException,
			ClassNotFoundException, InterruptedException {
		// For every tweet in the collection, compute and save the NE's based on
		// tweet text.

		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collection);
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");

			// Save individual ner output separately.
			final BasicDBList bdbList = new BasicDBList();
			for (int index = 0; index < nerModules.length; index++) {
				final BasicDBObject bdbo = new BasicDBObject();
				bdbo.put("name", nerModules[index].name());
				bdbo.put("neList",
						Utilities.doNER(nerModules[index], row.getText()));
				bdbList.add(bdbo);
			}
			final BasicDBObject setNERBdbo = new BasicDBObject();
			setNERBdbo.put("$set",
					new BasicDBObject().append("nerList", bdbList));
			dbc.update(new BasicDBObject().append("_id", row.get_id()),
					setNERBdbo);
			Thread.sleep(500);

			// Save the merged NER output too.
			final BasicDBObject setNEBdbo = new BasicDBObject();
			setNEBdbo.put(
					"$set",
					new BasicDBObject().append("mergedNeList",
							Utilities.mergeNER(row.getText(), nerModules)));
			dbc.update(new BasicDBObject().append("_id", row.get_id()),
					setNEBdbo);
			Thread.sleep(500);
		}
	}

	public static List getUniqueUsers(final String dbName,
			final String collection) {
		// Find the unique users from the supplied collection.
		return MongoDbManager.getCollection(dbName, collection).distinct(
				"userId");
	}

	public static void createSmallDataSet(final List userList)
			throws NumberFormatException, TwitterException,
			InterruptedException, FileNotFoundException {
		// Creates the training dataset based on recent tweets from supplied
		// users.
		final Twitter twitter = TwitterManager.getTwitterInstance();

		for (final Object user : userList) {
			final long userId = Long.parseLong(user.toString());

			// Get the recent tweets for the user.
			int countValidTweets = 0;
			List<Status> tweets = null;

			int iteration = 1;
			Paging paging = new Paging(iteration, 100);
			do {
				try {
					tweets = twitter.getUserTimeline(userId, paging);
				} catch (Exception e) {
					if (AppGlobals.IS_DEBUG)
						System.out.println("User " + userId
								+ " is not processed due to error "
								+ e.getMessage());
				}

				for (final Status status : tweets) {
					if (countValidTweets < AppGlobals.GET_TWEETS_FROM_USER_TIME_LINE_COUNT) {
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
										row.setFavoriteCount(status
												.getFavoriteCount());
										row.setRetweetCount(status
												.getRetweetCount());
									}
									row.addImage(m.getMediaURL());
								}
							}
							if (row != null) {
								// check if the row exist already in mongo.
								final BasicDBObject whereClause = new BasicDBObject();
								whereClause.put("_id", status.getId());
								if (MongoDbManager
										.getCollection(
												AppGlobals.MONGO_DB_NAME,
												AppGlobals.SMALL_SEIMP_TRAINING_SET_COLLECTION_NAME)
										.find(whereClause).size() == 0) {
									// save in mongo.
									MongoDbManager
											.insertJSON(
													AppGlobals.MONGO_DB_NAME,
													AppGlobals.SMALL_SEIMP_TRAINING_SET_COLLECTION_NAME,
													row);
									++countValidTweets;
								}
							}
						}
					}
				}

				paging.setPage(++iteration);
				if (iteration > AppGlobals.MAX_PAGE_PER_USER_CHECK) {
					if (AppGlobals.IS_DEBUG)
						System.out.println("Got only " + countValidTweets
								+ " tweets for user " + userId);
					break;
				}

				// Sleep the main thread to account for GET
				// statuses/user_timeline 300 req/15min 1req/3seconds
				Thread.sleep(3000);

			} while (countValidTweets < AppGlobals.GET_TWEETS_FROM_USER_TIME_LINE_COUNT);
			if (AppGlobals.IS_DEBUG)
				System.out.println("Got full "
						+ AppGlobals.GET_TWEETS_FROM_USER_TIME_LINE_COUNT
						+ " tweets for user " + userId);
		}

	}

	public static void createBigCollection(final String keyword)
			throws TwitterException, InterruptedException {
		// Creates a tweet collection obtained from supplied keyword.
		final Twitter twitter = TwitterManager.getTwitterInstance();

		// Compose the query.
		final Query query = new Query(keyword + " -filter:retweets");
		query.setLang("en");
		query.setResultType(ResultType.mixed);
		query.setCount(100);
		// query.since("2014-02-20");
		// query.until("2014-03-19");

		long lastID = Long.MAX_VALUE;
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
						if (MongoDbManager
								.getCollection(
										AppGlobals.MONGO_DB_NAME,
										AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME)
								.find(whereClause).size() == 0) {
							// save in mongo.
							MongoDbManager
									.insertJSON(
											AppGlobals.MONGO_DB_NAME,
											AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME,
											row);
						}
						++processed;
					}
				}

				// Set the max id.
				if (status.getId() < lastID)
					lastID = status.getId();
			}

			if (AppGlobals.IS_DEBUG)
				System.out.println("Saved " + processed + " from "
						+ res.getTweets().size() + " tweets.");

			// Handle twitter rate-limit 450 request per 15 min.
			Thread.sleep(3000);

			query.setMaxId(lastID - 1);
		}
		System.out.println("Completed for -" + keyword);

	}

	public static void createInterAnnotationDataset(final String keyword,
			int size) throws TwitterException, InterruptedException {
		// Creates a tweet collection of size defined by 'size' and obtained
		// from supplied keyword.
		final Twitter twitter = TwitterManager.getTwitterInstance();

		// Compose the query.
		final Query query = new Query(keyword + " -filter:retweets");
		query.setLang("en");
		query.setResultType(ResultType.mixed);
		query.setCount(100);

		long lastID = Long.MAX_VALUE;
		final HashMap<Long, SeimpTrainingRow> rowMap = new HashMap<Long, SeimpTrainingRow>();
		while (true) {
			final QueryResult res = twitter.search(query);
			int processed = 0;

			// Stopping criterion
			if (res.getTweets() == null || res.getTweets().size() == 0)
				break;

			// Parse the tweets
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
						rowMap.put(row.get_id(), row);
						++processed;
					}
				}

				// Set the max id.
				if (status.getId() < lastID)
					lastID = status.getId();
			}

			if (AppGlobals.IS_DEBUG)
				System.out.println("Obtained " + processed + " from "
						+ res.getTweets().size() + " tweets.");

			// Handle twitter rate-limit 450 request per 15 min.
			Thread.sleep(3000);

			query.setMaxId(lastID - 1);
		}
		if (AppGlobals.IS_DEBUG)
			System.out.println("Obtained " + rowMap.size()
					+ " tweets in total.");

		// Randomly pick 'size' tweets from the map
		final List<Long> keys = new ArrayList(rowMap.keySet());
		Collections.shuffle(keys);
		int count = 0;
		for (int rowIndex = 0; (rowIndex < keys.size() && rowIndex < size); rowIndex++) {
			MongoDbManager.insertJSON(AppGlobals.MONGO_DB_NAME,
					AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME,
					rowMap.get(keys.get(rowIndex)));
			++count;
		}

		if (AppGlobals.IS_DEBUG)
			System.out.println("Saved " + count + " random tweets in total.");

		System.out.println("Completed for -" + keyword);
	}
	
	public static void createInterAnnotationDatasetFromTweetIds() throws Exception{
		final BufferedReader reader=new BufferedReader(new FileReader("inter-annotation-tweet-ids.txt"));
		String line=null;
		/*final Twitter twitter = TwitterManager.getTwitterInstance();
		while((line=reader.readLine())!=null){
			String id=line.trim();
			Status status = null;
			try{
				status=twitter.showStatus(Long.parseLong(id));
			}catch(Exception e){
				e.printStackTrace();
				System.out.println(id);
				continue;
			}
			SeimpTrainingRow row = new SeimpTrainingRow();
			row.set_id(status.getId());
			row.setText(status.getText());
			row.setCreatedAt(status.getCreatedAt());
			row.setUserId(status.getUser().getId());
			row.setFavoriteCount(status.getFavoriteCount());
			row.setRetweetCount(status.getRetweetCount());
			for (MediaEntity m : status.getMediaEntities()) {
				if (m.getMediaURL().contains("jpg")) {
					row.addImage(m.getMediaURL());
				}
			}
			MongoDbManager.insertJSON(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME,row);
		}*/
		/*
		while((line=reader.readLine())!=null){
			final BasicDBObject whereClause = new BasicDBObject();
			whereClause.put("_id", Long.parseLong(line));
			if (MongoDbManager
					.getCollection(
							AppGlobals.MONGO_DB_NAME,
							AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME)
					.find(whereClause).size() == 0) {
				System.out.println(line);
			}
		}*/
		final BasicDBObject whereClause = new BasicDBObject();
		whereClause.put("_id", 580327670346571776L);
		final MongoClient mongoClient=new MongoClient(new ServerAddress("10.2.4.249",AppGlobals.MONGO_DB_PORT));
		final DBCursor cursor=mongoClient.getDB(AppGlobals.MONGO_DB_NAME).getCollection(AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME)
				.find(whereClause);
		while(cursor.hasNext()){
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			SeimpTrainingRow row1 = new SeimpTrainingRow();
			row1.set_id(row.get_id());
			row1.setText(row.getText());
			row1.setCreatedAt(row.getCreatedAt());
			row1.setUserId(row.getUserId());
			row1.setFavoriteCount(row.getFavoriteCount());
			row1.setRetweetCount(row.getRetweetCount());
			row1.setImageList(row.getImageList());
			MongoDbManager.insertJSON(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME,row1);
			System.out.println("Success");
		}
		
		reader.close();
	}

}