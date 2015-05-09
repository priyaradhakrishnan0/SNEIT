package com.salience.collect.kb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.yaml.snakeyaml.util.UriEncoder;

import com.hp.hpl.jena.query.ParameterizedSparqlString;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.salience.commons.AppGlobals;
import com.salience.util.Utilities;
import com.salience.util.mongo.MongoDbManager;

public class KnowledgeBase {

	/*public static void searchInWiki(final String keyword) throws IOException,
			ParseException {
		
		 * Searches the keyword in wikipedia and returns the titles of the
		 * relevant documents. (requires lucene-5.0.0 core,analyzers-common,backward-codecs,querparser)
		 
		// Setup the reader
		final Directory directory = FSDirectory.open(FileSystems.getDefault()
				.getPath(AppGlobals.LUCENE_WIKI_INDEX));
		final DirectoryReader reader = DirectoryReader.open(directory);
		final IndexSearcher searcher = new IndexSearcher(reader);
		final Analyzer analyzer = new StandardAnalyzer();

		// Setup the query parser
		final Map<String, Float> boostMap = new HashMap<String, Float>();
		boostMap.put("title", 50F);
		boostMap.put("infobox", 20F);
		boostMap.put("desc", 100F);
		final QueryParser parser = new MultiFieldQueryParser(new String[] {
				"title", "infobox", "desc" }, analyzer, null);
		final Query query = parser.parse(keyword);

		// Iterate through the results
		final ScoreDoc[] hits = searcher.search(query, null, 1000).scoreDocs;
		for (int i = 0; (i < hits.length && i < AppGlobals.LUCENE_HITS_TO_PROCESS); i++) {
			final Document hitDoc = searcher.doc(hits[i].doc);
			System.out.println(hitDoc.get("id") + "-" + hitDoc.get("title"));
		}

		reader.close();
		directory.close();
	}*/

	public static void searchInDbPedia(final String keyword)
			throws IOException, ParseException {
		/*
		 * Searches the keyword in dbpedia and returns the titles of the
		 * relevant documents.
		 */
		final ParameterizedSparqlString qs = new ParameterizedSparqlString(
				Utilities.readFromStream(new BufferedReader(new FileReader(
						"sparqlQuery"))));//
		final Literal literal = ResourceFactory
				.createLangLiteral(keyword, "en");
		// qs.setParam( "label", literal );
		final QueryExecution exec = QueryExecutionFactory.sparqlService(
				"http://dbpedia.org/sparql", qs.asQuery());
		final ResultSet results = ResultSetFactory.copyResults(exec
				.execSelect());
		ResultSetFormatter.out(results);
	}

	public static List<String> searchInDbPediaRest(final String keyword)
			throws IOException {
		/*
		 * Searches the keyword using the dbpedia sparql rest endpoint.
		 */
		// Hit the dbpedia server.
		final String query = Utilities.readFromStream(
				AppGlobals.DBPEDIA_SPARQL_QUERY_FILE)
				.replaceAll("\\$", keyword);
		final String content = Utilities.makeGetCall(
				AppGlobals.DBPEDIA_SPARQL_ENDPOINT + UriEncoder.encode(query),
				AppGlobals.HTTP_PROXY);
		System.out.println(AppGlobals.DBPEDIA_SPARQL_ENDPOINT + UriEncoder.encode(query));
		
		// Process the output.
		if (content == null)
			return null;
		final org.jsoup.nodes.Document document = Jsoup.parse(content);
		final List<String> resList = new ArrayList<String>();
		int count = 0;
		for (final Element tr : document.select("tr")) {
			if (count != 0) {
				Element elmt = tr.select("td").iterator().next();
				System.out.println(elmt != null ? elmt.text() : "");
			}
			++count;
		}

		return resList;
	}

	public static void loadGWCToMongo() throws IOException {
		/*
		 * Loads the Google wiki concepts file to mongo db (for future queries).
		 */
		long start = System.currentTimeMillis();
		final BufferedReader reader = new BufferedReader(new FileReader(
				new File(AppGlobals.GOOGLE_WIKI_CONCEPTS_FILE)));
		String line = null;
		while ((line = reader.readLine()) != null) {
			final GWCRow row = new GWCRow();
			String remStr = new String(line);
			row.setWord(remStr.substring(0, remStr.indexOf('\t')).trim());
			remStr = remStr.substring(remStr.indexOf('\t') + 1);
			row.setCprob(Double.valueOf(remStr.substring(0, remStr.indexOf(' '))));
			remStr = remStr.substring(remStr.indexOf(' ') + 1);
			row.setConcept(remStr.substring(0, remStr.indexOf(' ')));
			row.setMisc(remStr.substring(remStr.indexOf(' ') + 1));
			// MongoDbManager.insertJSON(AppGlobals.MONGO_DB_NAME,AppGlobals.GOOGLE_WIKI_CONCEPTS_COLLECTION_NAME,
			// row);
			System.out.println(line);
		}
		reader.close();
		System.out.println((System.currentTimeMillis() - start) / 1000 + " s");
	}

	public static List<String> searchInGWC(final String keyword, final int n)
			throws ClassNotFoundException {
		/*
		 * Searches the google wiki concepts for the given keyword and returns
		 * top 'n' concepts with high cprob.
		 */
		// Issue the command.
		final DB db = MongoDbManager.getDB(AppGlobals.MONGO_DB_NAME);
		final DBObject dbo = new BasicDBObject();
		dbo.put("text", AppGlobals.GOOGLE_WIKI_CONCEPTS_COLLECTION_NAME);
		dbo.put("search", keyword);
		final CommandResult res = db.command(dbo);
		final GWCResult row = (GWCResult) Utilities.convertToPOJO(
				res.toString(), "com.salience.collect.kb.GWCResult");

		// Process the results.
		if (row.getResults() == null || row.getResults().size() == 0)
			return null;
		final List<Concept> conceptList = new ArrayList<Concept>();
		for (final GWCResultRow entry : row.getResults())
			conceptList.add(new Concept(entry.getObj().getWord(),entry.getObj().getConcept(),entry.getObj().getCprob()));
		Collections.sort(conceptList);
		final List<String> resList=new ArrayList<String>();
		for(int index=0;(index<n && index<conceptList.size() );index++)
			resList.add(conceptList.get(index).concept);
		return resList;
	}

	public static void main(final String[] argv) throws Exception {
		/*String keyword="IIIT";
		
		for(final String ans:searchInGWC(keyword, 12))
			System.out.println(ans);*/
		
		searchInDbPediaRest("India");
	}

}

class Concept implements Comparable<Concept> {
	String word;
	String concept;
	double cprob;

	public Concept() {
	}

	public Concept(String word, String concept, double cprob) {
		this.word = word;
		this.concept = concept;
		this.cprob = cprob;
	}

	@Override
	public int compareTo(Concept o) {
		if(this.cprob<o.cprob) return 1;
		if(this.cprob>o.cprob) return -1;
		return 0;
	}

}
