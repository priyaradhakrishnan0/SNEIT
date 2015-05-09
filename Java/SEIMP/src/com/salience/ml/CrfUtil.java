package com.salience.ml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.salience.collect.Annotation;
import com.salience.collect.SeimpTrainingRow;
import com.salience.commons.AppGlobals;
import com.salience.util.Utilities;

public class CrfUtil {
	
	private static boolean isValidRecord(final SeimpTrainingRow row) {
        /*
         * Returns true if the record is valid one to process.
         */
        if(row.getAnnotationList()==null || row.getAnnotationList().size()==0) return false;
        final Annotation ann=row.getAnnotationList().iterator().next();
        return ann.getComments().trim().length()==0 && (ann.getKbList()!=null && ann.getKbList().size()!=0);
    }

	public static void createCrfDataset(final String srcFile,
			final String destFile) throws UnknownHostException, IOException,
			ClassNotFoundException, InterruptedException {
		/*
		 * Utility to create dataset for Sequence learning using CRF.
		 */
		final long start = System.currentTimeMillis();
		final String FS = "$$$";
		final PrintWriter writer = new PrintWriter(destFile);
		final BufferedReader reader=new BufferedReader(new FileReader(srcFile));
		int a = 0,b=0;
		String line=null;
		int count=0;
		MongoClient mc=new MongoClient(new ServerAddress("localhost",AppGlobals.MONGO_DB_PORT));
		DBCollection dbc=mc.getDB(AppGlobals.MONGO_DB_NAME).getCollection("ritterResponse");
				
		while ((line=reader.readLine())!=null) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(line,
							"com.salience.collect.SeimpTrainingRow");
			if (isValidRecord(row)) {
				final Annotation ann = row.getAnnotationList().iterator()
						.next();

				/*
				 * final String ritterResponse = Utilities.makeGetCall(
				 * "http://10.2.4.249:5050/extract?tweet=" + URLEncoder.encode(
				 * row.getText().replaceAll("\n", " "), "UTF-8"), null); // Save
				 * the response to mongodb RitterResponse resp = new
				 * RitterResponse(); resp.set_id(row.get_id());
				 * resp.setTags(ritterResponse); MongoDbManager.insertJSON(dbc2,
				 * resp);
				 */

				final String ritterResponse = getRitterResponseFromMongo(dbc,
						row.get_id()).trim();

				// Process the ritter response.
				final StringBuffer buffer = new StringBuffer();
				for (final String unit : ritterResponse.split(" ")) {
					if (unit.startsWith("http") || countChar(unit, '/') != 3)
						continue;
					final String[] components = unit.split("/");
					final String word = components[0];
					final String entity = components[1];
					final String pos = components[2];
					final String chunk = components[3];
					final String isSNE = isSNE(ann.getSneList(), word);
					final String isBeginSNE = isSNE.equals("O-SNE") ? isSNE
							: isBeginSNE(ann.getSneList(), word);

					buffer.append(word + FS + entity + FS + pos + FS + chunk
							+ FS + isSNE + FS);
					
					/*if(chunk.startsWith("B-NP") || chunk.startsWith("I-NP")) {
						b++;
						if(isBeginSNE.equals("I-SNE"))
							a++;
					}*/
					
				}

				writer.println(buffer.toString());
				//Thread.sleep(1000);
				System.out.println(count+++"\t"+buffer.toString());
			}

		}
		writer.close();
		//System.out.println(a+"/"+b+"="+(100*((float)a/(float)b)));
		//System.out.println((System.currentTimeMillis() - start) + " ms");
	}	
	
	private static boolean isFullCaps(final String str){
		if(str==null || str.length()==0) return false;
		for(int i=0;i<str.length();i++)
			if(isAlphaNumeric(str.charAt(i)) && isLower(str.charAt(i)))
					return false;
		return true;
	}
	
	private static boolean isFullLower(final String str){
		if(str==null || str.length()==0) return false;
		for(int i=0;i<str.length();i++)
			if(isAlphaNumeric(str.charAt(i)) && isUpper(str.charAt(i)))
					return false;
		return true;
	}
	
	private static boolean isUpper(final char ch) {return ('A'<=ch && ch<='Z'); }
	private static boolean isLower(final char ch) {return ('a'<=ch && ch<='z'); }

	private static String getRitterResponseFromMongo(final DBCollection dbc,
			final long id) throws ClassNotFoundException {
		/*
		 * Gets the cached ritter response from mongo.
		 */
		final BasicDBObject whereClause = new BasicDBObject();
		whereClause.put("_id", id);
		final DBCursor cursor = dbc.find(whereClause);
		while (cursor.hasNext()) {
			final RitterResponse row = (RitterResponse) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.ml.RitterResponse");
			return row.getTags();
		}
		System.out.println("Not found. " + id);
		return null;
	}

	public static String isSNE(final List<String> sneList, final String token) {
		/*
		 * Utility that returns 'SNE' if token is substring of any string in the
		 * snelist, otherwise 'O'
		 */
		if (sneList == null || sneList.size() == 0 || indexOfAlphaNum(token) == -1 || AppGlobals.STOP_WORD_LIST.indexOf(token.trim())!=-1)
			return "O-SNE";
		for (String sne : sneList) {
			sne = sne.substring(0, sne.indexOf("(") - 1).trim();
			if (sne.contains(token)
					|| (indexOfAlphaNum(token) != -1
							&& token.substring(indexOfAlphaNum(token)).trim()
									.length() != 0 && sne.contains(token
							.substring(indexOfAlphaNum(token))))
					|| (sne.contains(refineStr(token))) || (token.length()>4 && token.endsWith("’s") && sne.startsWith(token.substring(0, token.length()-4))))
				return "I-SNE";
		}
		return "O-SNE";
	}

	public static String isBeginSNE(final List<String> sneList,
			final String token) {
		/*
		 * Utility that returns 'B-SNE' if token is starting substring of any
		 * string in the snelist, otherwise 'O' Outside
		 */
		if (sneList == null || sneList.size() == 0)
			return "I-SNE";
		for (String sne : sneList) {
			sne = sne.substring(0, sne.indexOf("(") - 1).trim();
			if (sne.startsWith(token)
					|| (indexOfAlphaNum(token) != -1
							&& token.substring(indexOfAlphaNum(token)).trim()
									.length() != 0 && sne.startsWith(token
							.substring(indexOfAlphaNum(token))))
					|| (sne.startsWith(refineStr(token))) || (token.length()>4 && token.endsWith("’s") && sne.startsWith(token.substring(0, token.length()-4))) )
				return "B-SNE";
		}
		return "I-SNE";
	}

	public static int indexOfAlphaNum(final String str) {
		/*
		 * Returns the starting index of a alpha-numeric in the str.
		 */
		if (str == null || str.trim().length() == 0)
			return -1;
		for (int i = 0; i < str.length(); i++)
			if (isAlphaNumeric(str.charAt(i)))
				return i;
		return -1;
	}

	public static int countChar(final String str, final char ch) {
		/*
		 * Returns the count of character 'ch' in str.
		 */
		if (str == null || str.trim().length() == 0)
			return -1;
		int count = 0;
		for (int i = 0; i < str.length(); i++)
			if (str.charAt(i) == ch)
				++count;
		return count;
	}

	public static String refineStr(final String str) {
		/*
		 * Removes non alphanumeric characters.
		 */
		if (str == null || str.trim().length() == 0)
			return null;
		final StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < str.length(); i++)
			if (isAlphaNumeric(str.charAt(i)))
				buffer.append(str.charAt(i));
		return buffer.toString();
	}

	public static boolean isAlphaNumeric(final char ch) {
		/*
		 * Returns true if the character 'ch' is alphanumeric
		 */
		return (('0' <= ch && ch <= '9') || ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z'));
	}

	public static void main(final String[] argv) throws Exception {

		createCrfDataset("master_10938.txt","CRF_master_10938_IO.txt");
		
		/*
		 * final MongoClient client = new MongoClient(new ServerAddress(
		 * "localhost", 27017)); final DBCollection dbc =
		 * client.getDB("seimp").getCollection( "ritterResponse"); final
		 * DBCursor cursor = dbc.find(); int count = 0; while (cursor.hasNext())
		 * { final RitterResponse row = (RitterResponse) Utilities
		 * .convertToPOJO(cursor.next().toString(),
		 * "com.salience.ml.RitterResponse");
		 * if(row.getTags().trim().contains("\n")){ System.out.println(count++);
		 * } } System.out.println(count);
		 */

		/*
		 * final MongoClient client = new MongoClient(new ServerAddress(
		 * "localhost", 27017)); final DBCollection dbc =
		 * client.getDB("seimp").getCollection(
		 * AppGlobals.LARGE_SEIMP_TRAINING_SET_COLLECTION_NAME); final DBCursor
		 * cursor = dbc.find(); int count = 0; while (cursor.hasNext()) { final
		 * SeimpTrainingRow row = (SeimpTrainingRow) Utilities
		 * .convertToPOJO(cursor.next().toString(),
		 * "com.salience.collect.SeimpTrainingRow"); if (row.getAnnotationList()
		 * != null && row.getAnnotationList().iterator().next() != null &&
		 * row.getAnnotationList().iterator().next().getSneList() != null) {
		 * System.out.println(row.getAnnotationList().iterator().next()
		 * .getSneList()); count +=
		 * row.getAnnotationList().iterator().next().getSneList() .size(); } }
		 * System.out.println(count);
		 */

		/*
		 * final String ritterResponse = Utilities .makeGetCall(
		 * "http://10.2.4.249:5050/extract?tweet=" + URLEncoder .encode(
		 * "Lovely media interaction with two fast bowling greats #NZvWI #CWC15 http://t.co/sZhGMIQmyl"
		 * , "UTF-8"), null); System.out.println(ritterResponse);
		 */

		/*
		 * BufferedReader reader=new BufferedReader(new
		 * FileReader("CricketWorldCup2015Dataset.txt")); PrintWriter writer=new
		 * PrintWriter("new_dataset.txt"); String line=null,content=""; int
		 * count=0; while((line=reader.readLine())!=null){
		 * if(line.startsWith("...")){ writer.println(content); content=""; }
		 * else { content+=line.trim()+"\t"; } } writer.close(); reader.close();
		 */

	}

}
