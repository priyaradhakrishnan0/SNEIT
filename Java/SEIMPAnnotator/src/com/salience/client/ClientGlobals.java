package com.salience.client;

import java.util.ArrayList;
import java.util.List;

public class ClientGlobals {
	public static String MONGO_DB_NAME="seimp";
	public static List<String> COLLECTION_LIST=new ArrayList<String>();
	public static List<String> ANNOTATOR_LIST=new ArrayList<String>();
	public static int MAX_KB_ENTRIES=5;
	static{
		COLLECTION_LIST.add("smallseimptrainingset");
		COLLECTION_LIST.add("largeseimptrainingset");
		COLLECTION_LIST.add("interannotationset");
		COLLECTION_LIST.add("a1");
		COLLECTION_LIST.add("a2");

		ANNOTATOR_LIST.add("ganesh");
		ANNOTATOR_LIST.add("priya");
		ANNOTATOR_LIST.add("hari");
		ANNOTATOR_LIST.add("ayushi");
	}
	
}