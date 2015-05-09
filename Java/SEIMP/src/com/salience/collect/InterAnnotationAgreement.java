package com.salience.collect;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.salience.commons.AppGlobals;
import com.salience.util.Utilities;
import com.salience.util.mongo.MongoDbManager;

public class InterAnnotationAgreement {

	public static void percentageAgreement(final String dbName,
			final String collectionName) throws ClassNotFoundException {
		// Computes the percentage of cases on which annotators agree.
		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collectionName);
		final DBCursor cursor = dbc.find();
		double score = 0.0;
		int count = 0;
		while (cursor.hasNext()) {
			String text = cursor.next().toString();
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(text,
							"com.salience.collect.SeimpTrainingRow");
			//if(!row.getText().toLowerCase().contains("#nationalawards")) continue;
			if(!row.getText().toLowerCase().contains("#applewatch")) continue;
			//if(row.getText().toLowerCase().contains("#nationalawards") || row.getText().toLowerCase().contains("#applewatch")) continue;
			//if(!row.getText().contains("#AppleWatch")) continue;
			if (computeSNEScore(row)==true) {
				score++;
				//System.out.println(text);
			}
			count++;
		}
		//System.out.println(score);
		score /= count;
		//System.out.println(count);
		System.out.println("Percentage agreement - " + score);
	}
	
	public static void percentageAgreementWithKB(final String dbName,
			final String collectionName) throws ClassNotFoundException {
		// Computes the percentage of cases on which annotators agree.
		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collectionName);
		final DBCursor cursor = dbc.find();
		double score = 0.0;
		int count = 0;
		while (cursor.hasNext()) {
			String text = cursor.next().toString();
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(text,
							"com.salience.collect.SeimpTrainingRow");
			//if(!row.getText().toLowerCase().contains("#nationalawards")) continue;
			if(!row.getText().toLowerCase().contains("#applewatch")) continue;
			//if(row.getText().toLowerCase().contains("#nationalawards") || row.getText().toLowerCase().contains("#applewatch")) continue;
			if (computeSNEScore(row)==true && computeKBScore(row)==true) {
				score++;
				//System.out.println(text);
			}
			count++;
		}
		//System.out.println(score);
		score /= count;
		//System.out.println(count);
		System.out.println("Percentage agreement with KB - " + score);
	}

	private static boolean checkAllSame(
			final HashMap<String, List<String>> map) {
		if (map == null || map.size() < 2)
			return true;
		final Entry<String, List<String>> firstEntry = map.entrySet()
				.iterator().next();
		for (final Entry<String, List<String>> entry : map.entrySet()) {
			if (entry.getKey() != firstEntry.getKey()) {
				if (!isTwoListSame(firstEntry.getValue(), entry.getValue()))
					return false;
			}
		}
		return true;
	}

	private static boolean isTwoListSame(final List<String> list1,
			final List<String> list2) {
		if ((list1 == null && list2 == null)
				|| (list1!=null && list1.size() == 0 && list2!=null && list2.size() == 0))
			return true;
		if (list1 == null || list2 == null || list1.size() == 0
				|| list2.size() == 0)
			return false;
		for (final String str : list2)
			if (list1.indexOf(str) == -1)
				return false;
		for (final String str : list1)
			if (list2.indexOf(str) == -1)
				return false;
		return true;
	}
	
	private static boolean isTwoMapSame(final HashMap<String,String> map1,final HashMap<String,String> map2){
		if ((map1 == null && map2 == null)
				|| (map1!=null && map1.size() == 0 && map2!=null && map2.size() == 0))
			return true;
		if (map1 == null || map2 == null || map1.size() == 0
				|| map2.size() == 0)
			return false;
		for (final String key:map1.keySet())
			if(map2.get(key)==null || !map2.get(key).equals(map1.get(key)))
				return false;
		for (final String key:map2.keySet())
			if(map1.get(key)==null || !map1.get(key).equals(map2.get(key)))
				return false;
		return true;
	}

	private static boolean computeSNEScore(SeimpTrainingRow row) {
		// Returns true if all the annotators are in agreement.
		final List<Annotation> annotationList = row.getAnnotationList();
		if (annotationList == null || annotationList.size() == 1)
			return true;
		row.setAnnotationList(filterUsers(row.getAnnotationList()));
		final HashMap<String,List<String>> map=new HashMap<String,List<String>>();
		for(final Annotation ann:row.getAnnotationList()){
			map.put(ann.getAnnotator(), ann.getSneList());
		}		
		return checkAllSame(map);
	}
	
	private static List<Annotation> filterUsers(final List<Annotation> kbList){
		if(kbList==null || kbList.size()==0) return kbList;
		final List<String> whiteList=Arrays.asList("priya","ganesh");
		final List<Annotation> newList=new ArrayList<Annotation>();
		for(final Annotation ann:kbList){
			if(whiteList.indexOf(ann.getAnnotator())!=-1){
				newList.add(ann);
			}
		}
		//System.out.println("Untouched annotators = "+(kbList.size()-newList.size()));
		return newList;
	}

	private static boolean computeKBScore(SeimpTrainingRow row) {
		// Returns true if all the annotators are in agreement.
		final List<Annotation> annotationList = row.getAnnotationList();
		if (annotationList == null || annotationList.size() == 1)
			return true;
		row.setAnnotationList(filterUsers(row.getAnnotationList()));
		List<KBAnnotation> kbAnnotations = annotationList.iterator()
				.next().getKbList();
		final HashMap<String, String> entryMap1 = new HashMap<String, String>();
		if (kbAnnotations != null && kbAnnotations.size() > 0) {
			for (final KBAnnotation kbAnn : kbAnnotations)
				entryMap1.put(kbAnn.getNe(), kbAnn.getKbEntry());
		}
		for (int index = 1; index < annotationList.size(); index++) {
			final HashMap<String, String> entryMap2 = new HashMap<String, String>();
			kbAnnotations=annotationList.get(index).getKbList();
			if (kbAnnotations != null && kbAnnotations.size() > 0) {
				for (final KBAnnotation kbAnn : kbAnnotations)
					entryMap2.put(kbAnn.getNe(), kbAnn.getKbEntry());
			}
			if(!isTwoMapSame(entryMap1,entryMap2))
				return false;
		}
		return true;
	}

	public static void fleissKappa(final String dbName,
			final String collectionName, final int annotatorsCount)
			throws ClassNotFoundException {
		// Compute the fleiss score based on
		// https://www.youtube.com/watch?v=KLoeZstQz0E
		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collectionName);
		DBCursor cursor = dbc.find();

		// Get the categories.
		final List<String> categories = new ArrayList<String>();
		categories.add("null");
		int rowSize = 0;
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			//if(!row.getText().toLowerCase().contains("#nationalawards")) continue;
			//if(!row.getText().toLowerCase().contains("#applewatch")) continue;
			if(row.getText().toLowerCase().contains("#nationalawards") || row.getText().toLowerCase().contains("#applewatch")) continue;
			for (final String sne : row.getMergedNeList())
				if (categories.indexOf(sne) == -1)
					categories.add(sne);
			++rowSize;
		}
		System.out.println(rowSize);
		int colSize = categories.size();

		// Compute the rowsum, colsum.
		cursor = dbc.find();
		int globalRowVal[] = new int[rowSize];
		int globalColVal[] = new int[colSize];
		int curRowIndex = 0;
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			//if(!row.getText().toLowerCase().contains("#nationalawards")) continue;
			//if(!row.getText().toLowerCase().contains("#applewatch")) continue;
			if(row.getText().toLowerCase().contains("#nationalawards") || row.getText().toLowerCase().contains("#applewatch")) continue;
			row.setAnnotationList(filterUsers(row.getAnnotationList()));
			if (row.getAnnotationList() == null) {
				globalRowVal[curRowIndex] = 1;
				globalColVal[categories.indexOf("null")] += annotatorsCount;
			} else {
				int localColVal[] = new int[colSize];
				// Fill this tweet's category frequency table.
				for (int index = 0; index < row.getAnnotationList().size(); index++)
					if (row.getAnnotationList().get(index).getSneList() != null)
						for (final String sne : row.getAnnotationList()
								.get(index).getSneList()) {
							localColVal[categories.indexOf(sne)] += 1;
							globalColVal[categories.indexOf(sne)] += 1;
						}
				int rowVal = 0;
				for (int index = 0; index < colSize; index++)
					rowVal += localColVal[index] * localColVal[index];
				rowVal = (rowVal - annotatorsCount)
						/ (annotatorsCount * (annotatorsCount - 1));
				globalRowVal[curRowIndex] = rowVal;
			}
			++curRowIndex;
		}

		// Calculate p_bar
		double p_bar = 0;
		for (int index = 0; index < rowSize; index++)
			p_bar += globalRowVal[index];
		p_bar = p_bar / rowSize;

		// Calculate p_e
		double p_e = 0;
		for (int index = 0; index < colSize; index++)
			p_e += ((globalColVal[index] / (rowSize * annotatorsCount)) * (globalColVal[index] / (rowSize * annotatorsCount)));

		double fleissKappa = (p_bar - p_e) / (1 - p_e);
		System.out.println("Kappa score - " + (fleissKappa));
	}

	public static double cohenKappa(final String user1, final String user2,
			final String dbName, final String collectionName)
			throws ClassNotFoundException, IOException {
		/*
		 * Computes the Cohen Kappa score for two users
		 */
		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collectionName);
		DBCursor cursor = dbc.find();
		//BufferedReader reader = new BufferedReader(new FileReader("old_ann"));

		// Initialize the contigency table.
		int[][] contigency = new int[2][2];
		contigency[0][0] = contigency[0][1] = contigency[1][0] = contigency[1][1] = 0;
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			if (row.getAnnotationList() == null
					|| row.getAnnotationList().size() == 0
					|| row.getMergedNeList() == null
					|| row.getMergedNeList().size() == 0)
				continue;
			//if(!row.getText().toLowerCase().contains("#nationalawards")) continue;
			if(!row.getText().toLowerCase().contains("#applewatch")) continue;
			//if(row.getText().toLowerCase().contains("#nationalawards") || row.getText().toLowerCase().contains("#applewatch")) continue;
			final List<String> neMasterList = row.getMergedNeList();

			// Initialize the sub matrix.
			final int[][] subMatrix = new int[2][neMasterList.size()];
			for (int i = 0; i < 2; i++)
				for (int j = 0; j < neMasterList.size(); j++)
					subMatrix[i][j] = 0;

			for (final Annotation annotator : row.getAnnotationList()) {
				if (annotator.getSneList() == null)
					continue;
				if (annotator.getAnnotator().equalsIgnoreCase(user1)) {
					for (final String sne : annotator.getSneList()) {
						subMatrix[0][neMasterList.indexOf(sne)] = 1;
					}
				}
				if (annotator.getAnnotator().equalsIgnoreCase(user2)) {
					for (final String sne : annotator.getSneList()) {
						subMatrix[1][neMasterList.indexOf(sne)] = 1;
					}
				}
			}

			// Fill the contigency table.
			for (int j = 0; j < neMasterList.size(); j++)
				contigency[subMatrix[0][j]][subMatrix[1][j]]++;
		}
		//reader.close();

		// Compute the cohen kappa
		float total = contigency[0][0] + contigency[0][1] + contigency[1][0]
				+ contigency[1][1];
		float c1 = ((contigency[0][0] + contigency[1][0]) * (contigency[0][0] + contigency[0][1]))
				/ total;
		float c2 = ((contigency[1][0] + contigency[1][1]) * (contigency[0][1] + contigency[1][1]))
				/ total;
		float diagSum = contigency[0][0] + contigency[1][1];
		//System.out.println((diagSum - (c1 + c2)) / (total - (c1 + c2)));
		return (diagSum - (c1 + c2)) / (total - (c1 + c2));
	}
	
	public static double cohenKappaIncludingKB(final String user1, final String user2,
			final String dbName, final String collectionName)
			throws ClassNotFoundException, IOException {
		/*
		 * Computes the Cohen Kappa score for two users
		 */
		final DBCollection dbc = MongoDbManager.getCollection(dbName,
				collectionName);
		DBCursor cursor = dbc.find();
		//BufferedReader reader = new BufferedReader(new FileReader("old_ann"));

		// Initialize the contigency table.
		int[][] contigency = new int[2][2];
		contigency[0][0] = contigency[0][1] = contigency[1][0] = contigency[1][1] = 0;
		while (cursor.hasNext()) {
			final SeimpTrainingRow row = (SeimpTrainingRow) Utilities
					.convertToPOJO(cursor.next().toString(),
							"com.salience.collect.SeimpTrainingRow");
			if (row.getAnnotationList() == null
					|| row.getAnnotationList().size() == 0
					|| row.getMergedNeList() == null
					|| row.getMergedNeList().size() == 0)
				continue;
			//if(!row.getText().toLowerCase().contains("#nationalawards")) continue;
			if(!row.getText().toLowerCase().contains("#applewatch")) continue;
			//if(row.getText().toLowerCase().contains("#nationalawards") || row.getText().toLowerCase().contains("#applewatch")) continue;
			final List<String> neMasterList = row.getMergedNeList();
			final List<String> kbMasterList=new ArrayList<String>();
			for(final Annotation ann:row.getAnnotationList()) {
				if(ann.getAnnotator().equalsIgnoreCase(user1) || ann.getAnnotator().equalsIgnoreCase(user2) ){
					if(ann.getKbList()!=null) {
						for(final KBAnnotation kbAnn:ann.getKbList()) {
							final String key=kbAnn.getNe()+"$"+kbAnn.getKbEntry();
							if(kbMasterList.indexOf(key)==-1){
								kbMasterList.add(key);
							}
						}
					}
				}
			}

			// Initialize the sub matrix.
			final int[][] subMatrix = new int[2][neMasterList.size()+kbMasterList.size()];
			for (int i = 0; i < 2; i++)
				for (int j = 0; j < neMasterList.size(); j++)
					subMatrix[i][j] = 0;

			for (final Annotation annotator : row.getAnnotationList()) {
				if (annotator.getSneList() == null)
					continue;
				if (annotator.getAnnotator().equalsIgnoreCase(user1)) {
					for (final String sne : annotator.getSneList()) {
						subMatrix[0][neMasterList.indexOf(sne)] = 1;
					}
					if(annotator.getKbList()!=null) {
						for(final KBAnnotation kbAnn:annotator.getKbList()) {
							final String key=kbAnn.getNe()+"$"+kbAnn.getKbEntry();
							subMatrix[0][neMasterList.size()+kbMasterList.indexOf(key)] = 1;
						}
					}
				}
				if (annotator.getAnnotator().equalsIgnoreCase(user2)) {
					for (final String sne : annotator.getSneList()) {
						subMatrix[1][neMasterList.indexOf(sne)] = 1;
					}
					if(annotator.getKbList()!=null) {
						for(final KBAnnotation kbAnn:annotator.getKbList()) {
							final String key=kbAnn.getNe()+"$"+kbAnn.getKbEntry();
							subMatrix[1][neMasterList.size()+kbMasterList.indexOf(key)] = 1;
						}
					}
				}
			}

			// Fill the contigency table.
			for (int j = 0;j < (neMasterList.size()+kbMasterList.size()); j++)
				contigency[subMatrix[0][j]][subMatrix[1][j]]++;
		}
		//reader.close();

		// Compute the cohen kappa
		float total = contigency[0][0] + contigency[0][1] + contigency[1][0]
				+ contigency[1][1];
		float c1 = ((contigency[0][0] + contigency[1][0]) * (contigency[0][0] + contigency[0][1]))
				/ total;
		float c2 = ((contigency[1][0] + contigency[1][1]) * (contigency[0][1] + contigency[1][1]))
				/ total;
		float diagSum = contigency[0][0] + contigency[1][1];
		//System.out.println((diagSum - (c1 + c2)) / (total - (c1 + c2)));
		return (diagSum - (c1 + c2)) / (total - (c1 + c2));
	}

	public static void main(final String[] argv) throws Exception {		
		percentageAgreement(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);
		percentageAgreementWithKB(AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);
		final String[] ann=new String[]{"priya","ayushi","hari","ganesh"};
		double score=0.0;
		int times=0;
		for(int i=0;i<3;i++) {
			for(int j=i+1;j<4;j++) {
				score+=cohenKappa(ann[i],ann[j],AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);
				times++;
			}
		}
		System.out.println((score/times));
		
		score=0.0;
		times=0;
		for(int i=0;i<3;i++) {
			for(int j=i+1;j<4;j++) {
				score+=cohenKappaIncludingKB(ann[i],ann[j],AppGlobals.MONGO_DB_NAME,AppGlobals.INTER_ANNOTATION_SET_COLLECTION_NAME);
				times++;
			}
		}
		System.out.println((score/times));
	}

}
