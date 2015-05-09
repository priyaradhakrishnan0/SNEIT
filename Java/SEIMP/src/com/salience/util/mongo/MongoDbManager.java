package com.salience.util.mongo;

import java.net.UnknownHostException;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;
import com.salience.commons.AppGlobals;
import com.salience.util.Utilities;

public class MongoDbManager {
	
	private static MongoClient mongoClient=null;
	static{
		try {
			mongoClient=new MongoClient(new ServerAddress(AppGlobals.MONGO_DB_SERVER_IP,AppGlobals.MONGO_DB_PORT));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
	
	public static DBCollection getCollection(final String dbName,final String collection){
		//Gets the specified collection.
		return mongoClient.getDB(dbName).getCollection(collection);
	}
	
	public static DB getDB(final String dbName){
		//Get the specified database.
		return mongoClient.getDB(dbName);
	}
	
	public static void insertJSON(final String dbName,final String collection,final Object content){
		//Convert the content to DB Object and insert to specified collection/db.
		
		//Convert to JSON, followed by DBObject
		final String jsonContent=Utilities.convertToJson(content);
		final DBObject dbo=(DBObject)JSON.parse(jsonContent);
		
		//Insert into Mongo.
		final DBCollection dbc=getCollection(dbName,collection);
		dbc.insert(dbo);
	}
	
	public static void insertJSON(final DBCollection dbc,final Object content){
		//Convert the content to DB Object and insert to specified collection/db.
		
		//Convert to JSON, followed by DBObject
		final String jsonContent=Utilities.convertToJson(content);
		final DBObject dbo=(DBObject)JSON.parse(jsonContent);
		
		//Insert into Mongo.
		dbc.insert(dbo);
	}

}
