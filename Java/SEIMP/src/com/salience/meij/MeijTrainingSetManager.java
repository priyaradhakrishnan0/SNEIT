package com.salience.meij;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.salience.commons.AppGlobals;
import com.salience.util.TwitterManager;
import com.salience.util.Utilities;
import com.salience.util.mongo.MongoDbManager;

public class MeijTrainingSetManager {

	public static void createTrainingSet() throws IOException, TwitterException {
		final BufferedReader reader = new BufferedReader(new FileReader(
				AppGlobals.MEIJ_WSDM_2012_ANNOTATIONS));
		final Twitter twitter = TwitterManager.getTwitterInstance();

		// Parse the meij training set line by line.
		final MeijTrainingSet tSet = new MeijTrainingSet();
		String line = reader.readLine();
		String prevTweetId = null;
		while (line != null) {
			final String[] content = line.split("\t");
			final String curTweetId = content[0].trim();
			final String wikiId = content[1].trim();
			final String entityName = content[2].trim(); // Nothing but
															// wikipedia title.

			// Retrieve the tweet text for the curTweetId
			if (!entityName.equals("-")) {
				if (prevTweetId != null && prevTweetId.equals(curTweetId)) {
					// Repeating tweet
					final MeijTrainingRow row = tSet.getData().get(
							tSet.getData().size() - 1);

					// Add the new entity
					row.getEntityList().add(new Entity(entityName, wikiId));
				} else {
					// New tweet
					try {
						// Get the tweet
						final Status status = twitter.showStatus(Long
								.parseLong(content[0]));

						// Create a new row
						final MeijTrainingRow row = new MeijTrainingRow();

						// Set the tweet parameters.
						row.set_id(content[0]);
						row.setText(status.getText());
						row.setCreatedAt(status.getCreatedAt());
						row.setUserId(status.getUser().getId());
						row.setLang(status.getLang());
						row.setFavoriteCount(status.getFavoriteCount());
						row.setRetweetCount(status.getRetweetCount());

						final List<Entity> entityList = new ArrayList<Entity>();
						entityList.add(new Entity(entityName, wikiId));
						row.setEntityList(entityList);

						// Add to the training set.
						tSet.addNewRow(row);

						// Make the main thread sleep, to account for twitter
						// rate limitation. (180 request in 15min=> 1 request in
						// 5 s)
						Thread.sleep(5000);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			prevTweetId = curTweetId;
			line = reader.readLine();
		}
		reader.close();

		if (AppGlobals.IS_DEBUG)
			System.out.println("Found " + tSet.getData().size()
					+ " tweets to be valid from Meij Dataset.");

		// Save the new set into mongo.
		for (final MeijTrainingRow row : tSet.getData())
			MongoDbManager.insertJSON(AppGlobals.MONGO_DB_NAME,
					AppGlobals.MEIJ_TRAINING_SET_COLLECTION_NAME, row);
	}

	public static void evaluate() throws ClassNotFoundException, IOException {
		// Find & Return the performance metric for the MEIJ dataset.
		final BasicDBObject whereClause = new BasicDBObject();
		whereClause.put("lang", "en");
		final DBCursor cursor = MongoDbManager.getCollection(
				AppGlobals.MONGO_DB_NAME,
				AppGlobals.MEIJ_TRAINING_SET_COLLECTION_NAME).find(whereClause);
		int tCount=0;
		while (cursor.hasNext()) {
			final MeijTrainingRow row = (MeijTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.meij.MeijTrainingRow");

			// Get the NE candidates list.
			final List<String> candidateList = Utilities.mergeNER(row.getText(),AppGlobals.NER.ALAN_RITTER,AppGlobals.NER.ARK_TWEET,AppGlobals.NER.STANFORD_CRF);
			if (candidateList == null || candidateList.size() == 0)
				continue;

			int neCount = 0;
			for (final Entity entity : row.getEntityList()) {
				// Check if it's a NE
				if (Utilities.contains(candidateList, entity.getName()))
					++neCount;
			}
			
			if(neCount!=0) ++tCount;
		}
		
		System.out.println(tCount);
	}

	public final static void main(final String[] argv) throws Exception {
		// MeijTrainingSetManager.createTrainingSet();
		MeijTrainingSetManager.evaluate();
	}

}