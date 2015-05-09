package salience.kb;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import utility.Logger;
import Variables.Variables;

public class WikipediaSearch {

		public static void weightedsearch(String t, String ib, String d) throws IOException, ParseException {
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Variables.wikiLuceneIndex)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
//			QueryParser parser = new QueryParser(Version.LUCENE_45, "lyrics", analyzer);
			Map<String, Float> boosts = new HashMap<String, Float>();
			boosts.put("title", (float) 1.5);
			boosts.put("infobox", (float) 1.3);
			boosts.put("desc", (float) 1);
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_45, new String[] {t, ib, d}, analyzer, boosts);
			Query query = parser.parse(d);
			TopDocs results = searcher.search(query, 100);
			ScoreDoc[] hits = results.scoreDocs;
			int numTotalHits = results.totalHits;
			if (numTotalHits > 0)
				hits = searcher.search(query, numTotalHits).scoreDocs;
			System.out.println(numTotalHits + " records Found ");

			for (int i = 0; i < numTotalHits && i <= 10; i++) {
				Document doc = searcher.doc(hits[i].doc);
				String id = doc.get("id");
				String title = doc.get("title");
				System.out.println(id + " : " + title);
			}

		}//weightedsearch

		public static ArrayList<String> search(String t, String ib, String d) {
			ArrayList<String> titles = new ArrayList<String>();
			try{
			IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Variables.wikiLuceneIndex)));
			IndexSearcher searcher = new IndexSearcher(reader);
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
			QueryParser parser = new QueryParser(Version.LUCENE_45, "title", analyzer);

			Query query;
				//query = parser.parse(t);
				d = d.replaceAll("[\"():/,\t^?:*&%$#@!=+';-\\]\\[]"," ");//d stores tweet, which need cleaning of symbols
				query = MultiFieldQueryParser.parse(Version.LUCENE_45, new String[] { t, ib, d }, new String[] { "title",	"infobox", "desc" }, analyzer);
			
				TopDocs results = searcher.search(query, 100);
				ScoreDoc[] hits = results.scoreDocs;
				int numTotalHits = results.totalHits;
				if (numTotalHits > 0)
					hits = searcher.search(query, numTotalHits).scoreDocs;
		
				for (int i = 0; i < numTotalHits && i <= 10; i++) {
					Document doc = searcher.doc(hits[i].doc);
					//String id = doc.get("id");
					String title = doc.get("title").trim().replace("\n", "");
					titles.add(title);		
					//System.out.println(title);
				}

			} catch (ParseException p) {
				// TODO Auto-generated catch block
				p.printStackTrace();
			}catch (IOException io) {
				// TODO Auto-generated catch block
				io.printStackTrace();
			}

			return titles;
		}//search

		public static HashMap<String, Double> search(String ne, String tweet) {
			HashMap<String, Double> titleMap = new HashMap<String, Double>();			
			try{
				IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(Variables.wikiLuceneIndex)));
				IndexSearcher searcher = new IndexSearcher(reader);
				Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
				QueryParser parser = new QueryParser(Version.LUCENE_45, "title", analyzer);				
				tweet = tweet.replaceAll("[\"():/,\t^?:*&%$#@!=+';-\\]\\[]"," ");//d stores tweet, which need cleaning of symbols
				Query query = MultiFieldQueryParser.parse(Version.LUCENE_45, new String[] { ne, tweet }, new String[] { "title",	"desc" }, analyzer);
				TopDocs results = searcher.search(query, 100);
				ScoreDoc[] hits = results.scoreDocs;
				int numTotalHits = results.totalHits;
				if (numTotalHits > 0)
					hits = searcher.search(query, numTotalHits).scoreDocs;
		
				for (int i = 0; i < numTotalHits && i <= 10; i++) {
					Document doc = searcher.doc(hits[i].doc);
					//String id = doc.get("id");
					String title = doc.get("title").trim().replace("\n", ""); 
					double score = hits[i].score; //System.out.println("Title = "+title+" Score = "+score);
					titleMap.put(title, score);	
				}

			} catch (ParseException p) {
				// TODO Auto-generated catch block
				p.printStackTrace();
			}catch (IOException io) {
				// TODO Auto-generated catch block
				io.printStackTrace();
			}

			return titleMap;
		}//search
		
		public static void main(String[] args) throws IOException, ParseException {
			//weightedsearch("sachin", "cricket", "India");
			//System.out.println(search("sachin", "movies", "India"));
			System.out.println(search("sachin", " Today we saw the nation bid adeu to Sachin an all time wonder of cricket"));
			//System.out.println(search("Carrel","surgeon", "france"));
			//System.out.println(entityLinker("sachin", "cricket"));
		}//main

		public static String entityLinker(String entity, String context) {
				 ArrayList<String> titles = search(entity, entity+" "+context, entity+" "+context);
				if(titles.size()>0){
					return "http://en.wikipedia.org/wiki/"+titles.get(0).replaceAll(" ", "_");
				} else {
					return null;
				}
		}//entityLinker

	
}//class
