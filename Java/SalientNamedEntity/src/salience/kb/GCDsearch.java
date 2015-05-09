package salience.kb;


	import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

	import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONObject;
import org.apache.lucene.index.DirectoryReader;

import salience.pageRank.Node;
import Variables.Variables;


	public class GCDsearch {

		static String path=null;
		static int required;
		public GCDsearch()
		{
			path=Variables.googleConcepts;
			required=10;
		}

		@SuppressWarnings("deprecation")
		public static void main(String [] args) throws IOException, ParseException 
		{
			GCDsearch sc=new GCDsearch();
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(path)));
			//System.out.println(sc.searching("concept:Sport"));
			System.out.println(sc.entriesSortedByValues(sc.search("sachin", "cricket")));
			//sc.searching("Leo^3 Friendship");
		}


//		<K,V extends Comparable<? super V>>
//		SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
//			SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
//					new Comparator<Map.Entry<K,V>>() {
//						@Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
//							if(e2.getValue().compareTo(e1.getValue())==0 || e2.getValue().compareTo(e1.getValue())>0)
//								return 1;
//							else
//								return -1;
//						}
//					}
//					);
//			sortedEntries.addAll(map.entrySet());
//			return sortedEntries;
//		}
		
		public HashMap<String, Double> entriesSortedByValues(Map<String,Double> map) {

			List<Entry<String,Double>> sortedEntries = new ArrayList<Entry<String,Double>>(map.entrySet());
			
			Collections.sort(sortedEntries, new Comparator<Entry<String,Double>>() {
			            @Override
			            public int compare(Entry<String,Double> e1, Entry<String,Double> e2) {
			                return e2.getValue().compareTo(e1.getValue());
			            }
			        }
			);
			
			HashMap<String, Double> result = new LinkedHashMap<String, Double>();
		    for (Iterator<Entry<String, Double>> it = sortedEntries.iterator(); it.hasNext();) {
		        Map.Entry<String, Double> entry = it.next();
		        result.put(entry.getKey(), entry.getValue());
		    }

		    return result;

		}

		public HashMap<String,Double> search(String ne, String q)
		{ 
			
			ne = ne.replaceAll("#", " ");
			q=q.replaceAll("[\"():/,\t^?:*&%$#@!=+';-\\]\\[]"," ");
			for(String i:ne.split(" ")){
				if(i.trim().length() > 1)
					q = q + " " +i+"^3 ";
			}
			q = q.trim();
			try{
				IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(path)));
				//TFIDFSimilarity tfidf = new DefaultSimilarity();
				//searcher.setSimilarity(tfidf);
				IndexSearcher searcher = new IndexSearcher(reader);
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
				QueryParser parser = new QueryParser(Version.LUCENE_45, "text", analyzer);
	
				Query query;
				query = parser.parse(q);
				//System.out.println(query);
				HashMap<String,Double> hm = new HashMap<String,Double>();
				TopDocs results = searcher.search(query, 1000);
				//TopDocs results = searcher.search(fuzzy, 1000);
				ScoreDoc[] hits = results.scoreDocs;
				int numTotalHits = results.totalHits>10000?10000:results.totalHits;
				if(numTotalHits > 0)
					hits = searcher.search(query, numTotalHits).scoreDocs;
				System.out.println(numTotalHits + " total matching documents");
				int count=1;
				for(int i=0;i<numTotalHits;i++)
				{
					Document doc = searcher.doc(hits[i].doc);
					Double val = 0.0;


					if(hm.containsKey(doc.get("concept"))){
						//Double val = hm.get(doc.get("concept")) + 1.0/count+ computeScore(ne, doc.get("text"));
						val = hm.get(doc.get("concept")) + computeScore(ne, doc.get("text"));
						hm.put(doc.get("concept"), val);

					}
					else{
						val = computeScore(ne, doc.get("text"));
						hm.put(doc.get("concept"), val);
						//hm.put(doc.get("concept"), 1.0/count+computeScore(ne, doc.get("text")));
					}
					//System.out.println("Document "+(i+1)+" : "+doc.get("text")+" -> "+doc.get("concept")+" -> "+doc.get("probability")+" -> "+val);
					count++;
					if(count>required)
						break;
				}
				return hm;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return new HashMap<String,Double>();
		}

		public HashMap<String,Double> search(String ne, String q, int kbCutoff)
		{ 
			
			ne = ne.replaceAll("#", " ");
			q=q.replaceAll("[\"():/,\t^?:*&%$#@!=+';-\\]\\[]"," ");
			for(String i:ne.split(" ")){
				if(i.trim().length() > 1)
					q = q + " " +i+"^3 ";
			}
			q = q.trim();
			try{
				IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(path)));
				//TFIDFSimilarity tfidf = new DefaultSimilarity();
				//searcher.setSimilarity(tfidf);
				IndexSearcher searcher = new IndexSearcher(reader);
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
				QueryParser parser = new QueryParser(Version.LUCENE_45, "text", analyzer);
	
				Query query;
				query = parser.parse(q);
				//System.out.println(query);
				HashMap<String,Double> hm = new HashMap<String,Double>();
				TopDocs results = searcher.search(query, 1000);
				//TopDocs results = searcher.search(fuzzy, 1000);
				ScoreDoc[] hits = results.scoreDocs;
				int numTotalHits = results.totalHits>10000?10000:results.totalHits;
				if(numTotalHits > 0)
					hits = searcher.search(query, numTotalHits).scoreDocs;
				System.out.println(numTotalHits + " total matching documents");
				int count=1;
				for(int i=0;i<numTotalHits;i++)
				{
					Document doc = searcher.doc(hits[i].doc);
					Double val = 0.0;
					if(hm.containsKey(doc.get("concept"))){
						//Double val = hm.get(doc.get("concept")) + 1.0/count+ computeScore(ne, doc.get("text"));
						val = hm.get(doc.get("concept")) + computeScore(ne, doc.get("text"));
						hm.put(doc.get("concept"), val);
					} else{
						val = computeScore(ne, doc.get("text"));
						hm.put(doc.get("concept"), val);
						//hm.put(doc.get("concept"), 1.0/count+computeScore(ne, doc.get("text")));
					}
					//System.out.println("Document "+(i+1)+" : "+doc.get("text")+" -> "+doc.get("concept")+" -> "+doc.get("probability")+" -> "+val);
					count++;
					if(count>kbCutoff)
						break;
				}
				return hm;

			} catch (Exception e) {
				e.printStackTrace();
			}
			return new HashMap<String,Double>();
		}

		
		
		String getBest(HashMap<String,Double> hm){
			double max = -1;
			String mapped = "";
			for(String i:hm.keySet()){
				if(max < hm.get(i)){
					max = hm.get(i);
					mapped = i;
				}
			}
			return mapped;
		}

		HashMap<String,Double> rerank(HashMap<String,Double> hm ){
			return hm;
		}

		/*Jaccard similarity between ne and query string*/
		Double computeScore(String entity,String document){
			entity  = entity.toLowerCase();
			document = document.toLowerCase();
			entity = entity.replaceAll("_", " ");
			document = document.replaceAll("_", " ");
			String[] entityWords = entity.split(" ");
			String[] docWords = document.split(" ");
			HashSet<String> entityhs = new HashSet<>();
			HashSet<String> dochs = new HashSet<>();
			HashSet<String> union = new HashSet<>();
			for(String i:entityWords){
				entityhs.add(i);
				union.add(i);
			}
			for(String i:docWords){
				dochs.add(i);
				union.add(i);
			}
			entityhs.retainAll(dochs);
			Double score = entityhs.size()*1.0/union.size();
			//System.out.println(entity+" "+ document+" "+score);
			return score;
		}
		
		/*show fields of the GCD data*/
		public void showGCDFields() throws IOException{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(path)));
			Set<String> setFields = new TreeSet<String>();
			List<AtomicReaderContext> leaves = reader.leaves();
			for (AtomicReaderContext context : leaves) {
				AtomicReader atomicReader = context.reader();
				Fields fields = atomicReader.fields();
				for (String fieldName : fields) {
					setFields.add(fieldName);
				}
			}
			System.out.println(setFields);
		}
		

	}


