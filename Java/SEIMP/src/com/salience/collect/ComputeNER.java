package com.salience.collect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.salience.commons.AppGlobals;
import com.salience.util.Utilities;
import com.salience.util.mongo.MongoDbManager;

public class ComputeNER {
	
	public final static void main(final String[] argv) throws Exception {
		final DBCollection dbc = MongoDbManager.getCollection("seimp","completeDataset");
		final DBCursor cursor = dbc.find();
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		final List<AppGlobals.NER> nerModules=Arrays.asList(AppGlobals.NER.ALAN_RITTER,AppGlobals.NER.ALAN_RITTER,AppGlobals.NER.STANFORD_CRF);
		int count=0;
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			
			// Save individual ner output separately.
			final BasicDBList bdbList = new BasicDBList();
			final HashMap<String,List<String>> neMap=new HashMap<String,List<String>>();
			final List<String> mergedList=new ArrayList<String>();
			for (int index = 0; index < nerModules.size(); index++) {
				final BasicDBObject bdbo = new BasicDBObject();
				bdbo.put("name", nerModules.get(index).name());
				neMap.put(nerModules.get(index).name(),Utilities.doNER(nerModules.get(index), row.getText()));
				bdbo.put("neList",neMap.get(nerModules.get(index).name()));
				bdbList.add(bdbo);
				
				for(final String ne:neMap.get(nerModules.get(index).name()))
					if(mergedList.indexOf(ne)==-1)
						mergedList.add(ne);
				
			}
			final BasicDBObject setNERBdbo = new BasicDBObject();
			setNERBdbo.put("$set",
					new BasicDBObject().append("nerList", bdbList));
			dbc.update(new BasicDBObject().append("_id", row.get_id()),
					setNERBdbo);

			// Save the merged NER output too.
			final BasicDBObject setNEBdbo = new BasicDBObject();
			setNEBdbo.put(
					"$set",
					new BasicDBObject().append("mergedNeList",mergedList));
			dbc.update(new BasicDBObject().append("_id", row.get_id()),
					setNEBdbo);
			
			Thread.sleep(50);
		}
		System.out.println(count);
	}

}
